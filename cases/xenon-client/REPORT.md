# Case 009 — Xenon Client (`xenonclient-1.21.11.jar`)

- **Status:** suspected-malicious (high confidence) — DonutSMP stealer/loader campaign
- **Family / distribution:** SoberClient / DonutSMP free-client campaign; delivered from `https://www.donutsmpclients.com` via `file.garden`
- **Primary impact:** credential-theft (Minecraft/Microsoft session token), remote code execution, data exfiltration
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** The sample was never executed. Decompiled with CFR 0.152.

---

## 1. Summary

`xenonclient-1.21.11.jar` is offered on `https://www.donutsmpclients.com` as "Xenon Client" — *"Advanced PvP Visuals [100% FREE]"* (site version
v1.9.5). Behind the advertised cheat/utility it carries the **same obfuscation framework and
malicious loader machinery as the confirmed-malicious SoberClient (case 001)** and the rest of the
DonutSMP Clients set (cases 002–009).

The JAR is protected by a commercial-grade Java obfuscator: every literal string lives in a per-class
`String[]` decrypted on demand through an `invokedynamic` bootstrap; class names are flattened; and a
root-level `<uuid>.bin` table maps the SHA-256 of each class file to a per-class key, so strings only
decrypt when the class bytes are unmodified (anti-tamper / anti-analysis).

Capabilities recovered statically:

- an **all-accepting `X509TrustManager`** (`c.class`) — TLS certificate validation is disabled;
- a **custom raw-socket TLS HTTP client** (classes `b`/`f`/`h` in this build) that sets the SNI host
  manually and hand-writes the request;
- **process execution** via `ProcessBuilder`;
- **reflective class loading** (`Class.forName(...)` / `MethodHandles`);
- a **session accessor** surface over the Minecraft client: 4 classes reference class_320/field_1726 (com/xenon/e/c, ChatHudMixin, EntityMixin, c/a/d/i).

No native payload.

Cleartext endpoints recovered from this sample:

- `https://api.minecraftservices.com/minecraft/profile/lookup/name/  (com/xenon/c/a/d/i)`

> The campaign's primary C2/exfiltration strings live inside the encrypted string table and were
> **not recovered statically** (they exist decrypted only at runtime). The analyst did **not** execute
> the sample to dump plaintext (running a stealer would trigger its behaviour). See §11.

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `xenonclient-1.21.11.jar` |
| Size | 1,000,405 bytes |
| SHA-256 | `2eba92931cd69b0926a23e8795aef448b7541248c3f05db95fc6bfd7c75cec06` |
| Type | Java archive (Fabric mod, client) |
| Class files | 178 |
| `fabric.mod.json` id / name / version | `xenon` / `Xenon Client` / `1.0.0` |
| Entrypoints | client: [com.xenon.XenonClient]; main: [h] |
| Distribution site | `https://www.donutsmpclients.com (id: xenon)` |
| Download URL | `https://file.garden/auoc8y0R41ISH5sk/xenonclient-1.21.11.jar` |
| Embedded class-key table | `fec38599-7dd1-4441-8914-3250527d7ce7.bin` (SHA-256 `c0eeb710448995f7d47619f90e4bdb5e3f60a20d9f64043a33bfb3432388c91d`) |
| Native payload(s) | | _(none)_ | |

## 3. Structure

Genuine client code is interleaved with the malicious framework in the default/obfuscated packages:

| Class | Role |
|---|---|
| `j` | Obfuscation core — holds the per-class `String[]` table and the `invokedynamic` bootstrap / decryptor. |
| `i` | Class-key state machine (hashes class names to derive a per-class key). |
| `k` | Anti-tamper — reads the `fec38599-7dd1-4441-8914-3250527d7ce7.bin` table and SHA-256-hashes the calling class. |
| `b` | Custom HTTP/S client (raw `Socket` + manual SNI + `SSLContext.init(null, TrustManager[], …)`). |
| `c` | `X509TrustManager` with empty `checkClientTrusted`/`checkServerTrusted` — accepts any certificate. |
| loader / entry | `ProcessBuilder` execution and `Class.forName(...)` loading. |

## 4. Execution flow

```
Fabric entrypoint (client/main)
        |
        v
  k.<clinit>  read /fec38599-7dd1-4441-8914-3250527d7ce7.bin  ->  SHA-256(own class bytes) -> per-class key   (anti-tamper)
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

- **Session accessor.** 4 classes reference class_320/field_1726 (com/xenon/e/c, ChatHudMixin, EntityMixin, c/a/d/i). `class_320` is the Minecraft `Session` (username, UUID,
  **access token**); a setter implies the code can replace or harvest the live session — the
  mechanism this family uses to hijack an account.
- **TLS bypass.** The all-accepting trust manager lets the custom HTTP client talk to any host
  without certificate checks, defeating network inspection / MITM detection.

## 6. C2 infrastructure

Cleartext endpoints present in this sample:

- `https://api.minecraftservices.com/minecraft/profile/lookup/name/  (com/xenon/c/a/d/i)`

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

- SHA-256 `2eba92931cd69b0926a23e8795aef448b7541248c3f05db95fc6bfd7c75cec06` (`xenonclient-1.21.11.jar`)
- Embedded key table `fec38599-7dd1-4441-8914-3250527d7ce7.bin` — SHA-256 `c0eeb710448995f7d47619f90e4bdb5e3f60a20d9f64043a33bfb3432388c91d`
- Distribution: `https://www.donutsmpclients.com`, `https://file.garden/auoc8y0R41ISH5sk/`, `https://discord.com/invite/donutsmpclients`

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `raw/xenonclient-1.21.11.jar` | Original sample as received (not executed) |
| `evidence/fabric.mod.json` | Mod metadata / entrypoints |
| `evidence/META-INF_MANIFEST.MF` | JAR manifest |
| `evidence/classes/` | Decompiled evidence classes |
| `evidence/ARCHIVE_ENTRIES.txt` | Full archive listing |

## 11. Open questions / next steps

- Recover the encrypted C2 endpoints without executing the sample (safe options: emulate the
  decryptor with the recovered per-class key table, or run in a **detached, network-isolated**
  sandbox and capture DNS/HTTP).
- Presents as "Xenon Client". Shares the campaign TLS stack and a session accessor (`com/xenon/e/c`).
