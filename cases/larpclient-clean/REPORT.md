# Case 029 — LarpClient 2.3.1: a **clean** cheat client (negative control, REAL SAMPLE)

- **Verdict:** **NOT malicious** — no stealer, no RAT, no exfiltration found
- **Confidence:** high (full-JAR static review of every outbound host and every credential path)
- **Category:** commercial Minecraft Fabric cheat client
- **Handling:** static analysis only — never executed
- **Date:** 2026-11-19
- **Analyst:** OpenHands (first-hand static analysis)

---

## 1. Why a clean case is in this database

Every other case here is malicious. A database that only ever says "malware" cannot be trusted —
it proves nothing about specificity. **This case is the control**: a real, widely-distributed cheat
client that touches the Minecraft session, AES, `ProcessBuilder` and HWID, and yet is **benign**.
It demonstrates exactly which signals separate a legitimate client from a stealer.

| Field | Value |
|---|---|
| Source | `Recte-Development/larpclient-deobfuscated` (public deobfuscated build) |
| File | `LarpClient-1.21.11-2.3.1-recovered.jar` |
| Size | `19,282,759` bytes |
| **SHA-256** | `8caea2f11daa6ae5ce0ac74f0135c9b17d050f3e23341b5d8799810e80f5b58e` |
| Mod id / version | `larp-client` 2.3.1, author "Tobias", `environment: client` |
| Entries | 2,164 |

## 2. It genuinely does touch sensitive APIs — this is why naive scanners false-positive

A keyword scan lights up:

| Signal | Where | Why it is benign |
|---|---|---|
| `accessToken` | `account/TokenLogin`, `g5` | User **pastes their own** token into the GUI; used only for `GET https://api.minecraftservices.com/minecraft/profile` |
| `AES` / `javax/crypto` | `account/AccountVault` | `AES/GCM/NoPadding` **encrypting the user's own saved accounts at rest** |
| OS keychain | `account/SystemAccountProtection` | macOS `kSecClassGenericPassword`, Linux **libsecret** (`secret_password_store_sync`) — stores the vault key in the **system keychain** |
| `ProcessBuilder` | `lf` | Launches **`playerctl`** to read Spotify media info for a HUD element |
| `hwid` | `h`, `f` | Local identifier, and `f` **redacts** secrets before sharing configs |

## 3. The decisive evidence — the whole JAR has 9 hosts, all legitimate

Full-JAR regex for every `http(s)/ws(s)` URL:

| Count | Host | Purpose |
|---|---|---|
| 6 | `larpclient.com` | the vendor's own site |
| 3 | `sessionserver.mojang.com` | `session/minecraft/join` — **normal** Minecraft login handshake |
| 2 | `api.mojang.com` | normal profile lookups |
| 2 | `api.minecraftservices.com` | normal profile lookup |
| 2 | `api.donutsmp.net` | public auction-price API for a HUD module |
| 2 | `cdn.discordapp.com` | **CDN** for images (not `discord.com/api/webhooks`) |
| 2 | `lrclib.net` | lyrics API for the music HUD |
| 1 | `textures.minecraft.net` | skin textures |
| — | `xml.org`, `apache.org` | XML parser feature URIs (Java boilerplate) |

**There is no `discord.com/api/webhooks`, no paste site, no raw IP, no unknown domain.** That single
fact is dispositive: a stealer must exfiltrate *somewhere*, and this JAR has nowhere to send data.

## 4. It actively protects secrets

Three behaviours are the **opposite** of a stealer:

1. **`com/larpclient/h.class`** — regex redaction before a config can be shared:
   `license[_ -]?key|(?:access|refresh|install|session)[_ -]?token|token|password|client[_ -]?secret|authorization`
   plus `-----BEGIN .*PRIVATE KEY-----` and `Bearer \S+`.
2. **`com/larpclient/f.class`** — scrubs `LARP-[A-F0-9]{4}-…` licence keys, 64-hex strings,
   `Bearer …`, and `mfa.[\w-]+` tokens.
3. **`AccountVault`** — AES-GCM with a key held in the **OS keychain**, and it *refuses* to operate
   when the keychain is locked ("System account storage is unavailable or locked").

A stealer does the reverse: it reads plaintext, gathers, and posts outward.

## 5. Cross-check against the campaign signature

The Cluster-A signature (cases 001–010) is: all-accepting `X509TrustManager` + raw-socket TLS client
+ encrypted `.bin` string tables + `Class.forName`/`MethodHandles` bootstrap. LarpClient has:

| Campaign artefact | Present? |
|---|---|
| All-accepting `X509TrustManager` / `checkServerTrusted` override | **No** (0 hits) |
| Custom raw-socket TLS HTTP client | **No** (uses standard `HttpURLConnection` / Netty) |
| Root-level `<uuid>.bin` encrypted string tables | **No** |
| `META-INF/kuznya-integrity-target` | **No** |
| Discord webhook / unknown C2 | **No** |

## 6. Caveats — read these before trusting it

- This is a **deobfuscated, community-rebuilt** JAR (`*-recovered.jar`). The **official** binary may
  differ. Verdict applies to *this artefact only*.
- Cheat clients are **against server rules** even when not malware. "Not malicious" ≠ "safe to use".
- The JAR bundles `MinecraftAuth-5.0.2`, `gson-1.9.2`, `httpclient-1.9.2` — normal libraries.
- Static only; never executed. Absence of a finding is not proof of absence for encrypted blobs,
  but no such blob exists here (no entropy-8 regions).

## 7. Value to the database

This case defines the **false-positive baseline**. A detector that flags LarpClient is broken.
Concretely, a sound detector must distinguish:

| Benign | Malicious |
|---|---|
| AES **encrypting** a local vault | AES **encrypting** exfiltrated data |
| Key in the **OS keychain** | Key hardcoded / derived from HWID |
| User **pastes** their own token | Mod **reads** `Session`/`User` reflectively |
| Hosts: vendor + Mojang + CDN | Host: webhook / unknown VPS / raw IP |
| Redacts secrets before sharing | Harvests secrets to send |
