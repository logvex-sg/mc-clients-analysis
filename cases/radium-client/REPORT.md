# Case 008 — Radium Client (`radiumclient-1.21.11.jar`)

- **Status:** suspected-malicious (high confidence) — DonutSMP stealer/loader campaign
- **Family / distribution:** SoberClient / DonutSMP free-client campaign; delivered from `https://www.donutsmpclients.com` via `file.garden`
- **Primary impact:** credential-theft (Minecraft/Microsoft session token), remote code execution, data exfiltration
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** The sample was never executed. Decompiled with CFR 0.152.

---

## 1. Summary

`radiumclient-1.21.11.jar` is offered on `https://www.donutsmpclients.com` as "Radium Client" — *"Elite PvP Suite [100% FREE]"* (site version
v3.1.2). Behind the advertised cheat/utility it carries the **same obfuscation framework and
malicious loader machinery as the confirmed-malicious SoberClient (case 001)** and the rest of the
DonutSMP Clients set (cases 002–009).

The JAR is protected by a commercial-grade Java obfuscator: every literal string lives in a per-class
`String[]` decrypted on demand through an `invokedynamic` bootstrap; class names are flattened; and a
root-level `<uuid>.bin` table maps the SHA-256 of each class file to a per-class key, so strings only
decrypt when the class bytes are unmodified (anti-tamper / anti-analysis).

Capabilities recovered statically:

- an **all-accepting `X509TrustManager`** (`g.class`) — TLS certificate validation is disabled;
- a **custom raw-socket TLS HTTP client** (classes `b`/`f`/`h` in this build) that sets the SNI host
  manually and hand-writes the request;
- **process execution** via `ProcessBuilder`;
- **reflective class loading** (`Class.forName(...)` / `MethodHandles`);
- a **session accessor** surface over the Minecraft client: 6 classes reference class_320/field_1726 (w/af, w/C, w/D, w/av + ChatHudMixin/EntityMixin).

No native payload, but ships the campaign TLS stack and a session accessor surface.

Cleartext endpoints recovered from this sample:

- `https://dpaste.com/api/v2/  (w/l "Config Share")`
- `https://api.minecraftservices.com/minecraft/profile/lookup/name/  (w/av)`
- `https://accounts.spotify.com / https://api.spotify.com / http://localhost:8888/callback  (w/A, w/B)`

> The campaign's primary C2/exfiltration strings live inside the encrypted string table and were
> **not recovered statically** (they exist decrypted only at runtime). The analyst did **not** execute
> the sample to dump plaintext (running a stealer would trigger its behaviour). See §11.

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `radiumclient-1.21.11.jar` |
| Size | 1,074,803 bytes |
| SHA-256 | `a8f92a92752cc3d8b643ca9452aa86b5ba8b254ab5c69666853742c0fcf3147e` |
| Type | Java archive (Fabric mod, client) |
| Class files | 172 |
| `fabric.mod.json` id / name / version | `water` / `BETTER CHAT` / `1.0.0` |
| Entrypoints | client: [com.water.WaterClient]; main: [d] |
| Distribution site | `https://www.donutsmpclients.com (id: radium)` |
| Download URL | `https://file.garden/auoc8y0R41ISH5sk/radiumclient-1.21.11.jar` |
| Embedded class-key table | `a0a84a3b-8398-4eb3-a665-fb9ec67a0689.bin` (SHA-256 `58b95f6a726a2020bf742a248de73bf71bc34423250d44ea4b5830f998475ea5`) |
| Native payload(s) | | _(none)_ | |

## 3. Structure

Genuine client code is interleaved with the malicious framework in the default/obfuscated packages:

| Class | Role |
|---|---|
| `j` | Obfuscation core — holds the per-class `String[]` table and the `invokedynamic` bootstrap / decryptor. |
| `i` | Class-key state machine (hashes class names to derive a per-class key). |
| `k` | Anti-tamper — reads the `a0a84a3b-8398-4eb3-a665-fb9ec67a0689.bin` table and SHA-256-hashes the calling class. |
| `b` | Custom HTTP/S client (raw `Socket` + manual SNI + `SSLContext.init(null, TrustManager[], …)`). |
| `g` | `X509TrustManager` with empty `checkClientTrusted`/`checkServerTrusted` — accepts any certificate. |
| loader / entry | `ProcessBuilder` execution and `Class.forName(...)` loading. |

## 4. Execution flow

```
Fabric entrypoint (client/main)
        |
        v
  k.<clinit>  read /a0a84a3b-8398-4eb3-a665-fb9ec67a0689.bin  ->  SHA-256(own class bytes) -> per-class key   (anti-tamper)
        |
        v
  j.<clinit>  build encrypted string table
        |
        v
  decrypt string  ->  raw Socket -> SNI -> all-accepting TrustManager -> hand-written HTTP
        |
        +--> ProcessBuilder(...).start()          (launch process)
        +--> Class.forName(...).newInstance()     (load class)
        +--> session accessor (class_320/field_1726) (read/replace Minecraft Session)
```

## 5. Credential / data theft

- **Session accessor.** 6 classes reference class_320/field_1726 (w/af, w/C, w/D, w/av + ChatHudMixin/EntityMixin). `class_320` is the Minecraft `Session` (username, UUID,
  **access token**); a setter implies the code can replace or harvest the live session — the
  mechanism this family uses to hijack an account.
- **TLS bypass.** The all-accepting trust manager lets the custom HTTP client talk to any host
  without certificate checks, defeating network inspection / MITM detection.

## 6. C2 infrastructure

Cleartext endpoints present in this sample:

- `https://dpaste.com/api/v2/  (w/l "Config Share")`
- `https://api.minecraftservices.com/minecraft/profile/lookup/name/  (w/av)`
- `https://accounts.spotify.com / https://api.spotify.com / http://localhost:8888/callback  (w/A, w/B)`

The campaign C2 resolver
lives in the encrypted string table (not recovered statically). Transport is a custom raw-socket TLS
client with certificate validation disabled.

## 7. Impact

If run, this client can execute arbitrary processes and load arbitrary classes on the player's
machine, and can read/replace the Minecraft session (account takeover). Treated as unsafe.

## 8. Detection & response

### 8.1 Host indicators
- A Fabric mod JAR matching the SHA-256 above under `.minecraft/mods/`.
- Root-level `<uuid>.bin` key tables and `native/*` payloads inside the mod JAR.
- Child processes spawned by the JVM (`powershell`, etc.).

### 8.2 Network indicators
- Connections to `file.garden` (download), `https://www.donutsmpclients.com`, and `discord.com` invite.
- See `iocs.csv`.

### 8.3 Response steps
1. Do **not** run the mod. Quarantine the JAR.
2. Rotate the Minecraft/Microsoft account password and **invalidate active sessions** (sign out of
   all devices) — assume the access token is compromised.
3. Remove the mod and audit `.minecraft/mods` for the other campaign JARs (cases 001–010).

## 9. Indicators of compromise

See `iocs.csv`. Key items:

- SHA-256 `a8f92a92752cc3d8b643ca9452aa86b5ba8b254ab5c69666853742c0fcf3147e` (`radiumclient-1.21.11.jar`)
- Embedded key table `a0a84a3b-8398-4eb3-a665-fb9ec67a0689.bin` — SHA-256 `58b95f6a726a2020bf742a248de73bf71bc34423250d44ea4b5830f998475ea5`
- Distribution: `https://www.donutsmpclients.com`, `https://file.garden/auoc8y0R41ISH5sk/`, `https://discord.com/invite/donutsmpclients`

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `raw/radiumclient-1.21.11.jar` | Original sample as received (not executed) |
| `evidence/fabric.mod.json` | Mod metadata / entrypoints |
| `evidence/META-INF_MANIFEST.MF` | JAR manifest |
| `evidence/classes/` | Decompiled evidence classes |
| `evidence/ARCHIVE_ENTRIES.txt` | Full archive listing |

## 11. Open questions / next steps

- Recover the encrypted C2 endpoints without executing the sample (safe options: emulate the
  decryptor with the recovered per-class key table, or run in a **detached, network-isolated**
  sandbox and capture DNS/HTTP).
- Presents as "BETTER CHAT". Contains a "Config Share" module (`w/l`) that gzips the client config, base64-url-encodes it and POSTs it to `https://dpaste.com/api/v2/` — a public paste service, i.e. off-box exfiltration of client state. A "Spotify HUD" module (`w/o`) shells out to **PowerShell** (`-ExecutionPolicy Bypass -File <temp.ps1>`) and bundles a bundled PowerShell helper.
