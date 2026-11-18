# Case 007 — Krypton Client (`kryptonclient-1.21.11.jar`)

- **Status:** suspected-malicious (high confidence) — DonutSMP stealer/loader campaign
- **Family / distribution:** SoberClient / DonutSMP free-client campaign; delivered from `https://www.donutsmpclients.com` via `file.garden`
- **Primary impact:** credential-theft (Minecraft/Microsoft session token), remote code execution, data exfiltration
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** The sample was never executed. Decompiled with CFR 0.152.

---

## 1. Summary

`kryptonclient-1.21.11.jar` is offered on `https://www.donutsmpclients.com` as "Krypton Client" — *"Elite Basefinding Engine [100% FREE]"* (site version
v2.8.4). Behind the advertised cheat/utility it carries the **same obfuscation framework and
malicious loader machinery as the confirmed-malicious SoberClient (case 001)** and the rest of the
DonutSMP Clients set (cases 002–009).

The JAR is protected by a commercial-grade Java obfuscator: every literal string lives in a per-class
`String[]` decrypted on demand through an `invokedynamic` bootstrap; class names are flattened; and a
root-level `<uuid>.bin` table maps the SHA-256 of each class file to a per-class key, so strings only
decrypt when the class bytes are unmodified (anti-tamper / anti-analysis).

Capabilities recovered statically:

- an **all-accepting `X509TrustManager`** (`A.class`) — TLS certificate validation is disabled;
- a **custom raw-socket TLS HTTP client** (classes `b`/`f`/`h` in this build) that sets the SNI host
  manually and hand-writes the request;
- **process execution** via `ProcessBuilder`;
- **reflective class loading** (`Class.forName(...)` / `MethodHandles`);
- a **session accessor** surface over the Minecraft client: 3 classes reference class_320/field_1726 (ap, tn, dev/krypton/mixin/TextVisitFactoryMixin).

Ships a **gzip-compressed 17.8 MB Windows PE** (`dev/krypton/native/x86_64-windows`, unpacked in memory by `dev/krypton/native/Loader.decompress`). The PE exports `Java_dev_krypton_native_Loader_init` and calls `VirtualProtect` — the same **jvmtp** native protector used by Corz and Frost.



> The campaign's primary C2/exfiltration strings live inside the encrypted string table and were
> **not recovered statically** (they exist decrypted only at runtime). The analyst did **not** execute
> the sample to dump plaintext (running a stealer would trigger its behaviour). See §11.

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `kryptonclient-1.21.11.jar` |
| Size | 6,134,771 bytes |
| SHA-256 | `62129ddc224334f13245472ea4b206a683b3d27df4f8fd330a3bb2ddf42c6270` |
| Type | Java archive (Fabric mod, client) |
| Class files | 499 |
| `fabric.mod.json` id / name / version | `krloader` / `KrLoader` / `1.0` |
| Entrypoints | preLaunch/main/client: [dev.krypton.diddy.DiddyEntrypoint] |
| Distribution site | `https://www.donutsmpclients.com (id: krypton)` |
| Download URL | `https://file.garden/auoc8y0R41ISH5sk/kryptonclient-1.21.11.jar` |
| Embedded class-key table | `41b28690-e104-4b7e-882a-cb9c114eeb6b.bin` (SHA-256 `2d596a20402ee999918f41d38b477b5aad4a2da9b4be781bd3234e9cf86b5921`) |
| Native payload(s) | | `dev/krypton/native/x86_64-windows (2,536,385 B gzip -> 17,824,256 B Windows PE)` | |

## 3. Structure

Genuine client code is interleaved with the malicious framework in the default/obfuscated packages:

| Class | Role |
|---|---|
| `j` | Obfuscation core — holds the per-class `String[]` table and the `invokedynamic` bootstrap / decryptor. |
| `i` | Class-key state machine (hashes class names to derive a per-class key). |
| `k` | Anti-tamper — reads the `41b28690-e104-4b7e-882a-cb9c114eeb6b.bin` table and SHA-256-hashes the calling class. |
| `b` | Custom HTTP/S client (raw `Socket` + manual SNI + `SSLContext.init(null, TrustManager[], …)`). |
| `A` | `X509TrustManager` with empty `checkClientTrusted`/`checkServerTrusted` — accepts any certificate. |
| loader / entry | `ProcessBuilder` execution and `Class.forName(...)` loading. |

## 4. Execution flow

```
Fabric entrypoint (client/main)
        |
        v
  k.<clinit>  read /41b28690-e104-4b7e-882a-cb9c114eeb6b.bin  ->  SHA-256(own class bytes) -> per-class key   (anti-tamper)
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

- **Session accessor.** 3 classes reference class_320/field_1726 (ap, tn, dev/krypton/mixin/TextVisitFactoryMixin). `class_320` is the Minecraft `Session` (username, UUID,
  **access token**); a setter implies the code can replace or harvest the live session — the
  mechanism this family uses to hijack an account.
- **TLS bypass.** The all-accepting trust manager lets the custom HTTP client talk to any host
  without certificate checks, defeating network inspection / MITM detection.

## 6. C2 infrastructure

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

- SHA-256 `62129ddc224334f13245472ea4b206a683b3d27df4f8fd330a3bb2ddf42c6270` (`kryptonclient-1.21.11.jar`)
- Embedded key table `41b28690-e104-4b7e-882a-cb9c114eeb6b.bin` — SHA-256 `2d596a20402ee999918f41d38b477b5aad4a2da9b4be781bd3234e9cf86b5921`
- Distribution: `https://www.donutsmpclients.com`, `https://file.garden/auoc8y0R41ISH5sk/`, `https://discord.com/invite/donutsmpclients`

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `raw/kryptonclient-1.21.11.jar` | Original sample as received (not executed) |
| `evidence/fabric.mod.json` | Mod metadata / entrypoints |
| `evidence/META-INF_MANIFEST.MF` | JAR manifest |
| `evidence/classes/` | Decompiled evidence classes |
| `evidence/ARCHIVE_ENTRIES.txt` | Full archive listing |

## 11. Open questions / next steps

- Recover the encrypted C2 endpoints without executing the sample (safe options: emulate the
  decryptor with the recovered per-class key table, or run in a **detached, network-isolated**
  sandbox and capture DNS/HTTP).
- The mod id/name is literally "krloader"/"KrLoader" — a loader, not just a cheat. Entrypoint `DiddyEntrypoint` implements `PreLaunchEntrypoint` so it runs before the game initialises.
