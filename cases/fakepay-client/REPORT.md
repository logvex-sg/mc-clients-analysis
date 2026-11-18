# Case 006 — Fake Pay Mod (`fakepayclient-1.21.11.jar`)

- **Status:** suspected-malicious (high confidence) — DonutSMP stealer/loader campaign
- **Family / distribution:** SoberClient / DonutSMP free-client campaign; delivered from `https://www.donutsmpclients.com` via `file.garden`
- **Primary impact:** credential-theft (Minecraft/Microsoft session token), remote code execution, data exfiltration
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** The sample was never executed. Decompiled with CFR 0.152.

---

## 1. Summary

`fakepayclient-1.21.11.jar` is offered on `https://www.donutsmpclients.com` as "Fake Pay Mod" — *"Realistic UI Screen Payment Simulator [100% FREE]"* (site version
v1.0.4). Behind the advertised cheat/utility it carries the **same obfuscation framework and
malicious loader machinery as the confirmed-malicious SoberClient (case 001)** and the rest of the
DonutSMP Clients set (cases 002–009).

The JAR is protected by a commercial-grade Java obfuscator: every literal string lives in a per-class
`String[]` decrypted on demand through an `invokedynamic` bootstrap; class names are flattened; and a
root-level `<uuid>.bin` table maps the SHA-256 of each class file to a per-class key, so strings only
decrypt when the class bytes are unmodified (anti-tamper / anti-analysis).

Capabilities recovered statically:

- an **all-accepting `X509TrustManager`** (`d.class`) — TLS certificate validation is disabled;
- a **custom raw-socket TLS HTTP client** (classes `b`/`f`/`h` in this build) that sets the SNI host
  manually and hand-writes the request;
- **process execution** via `ProcessBuilder`;
- **reflective class loading** (`Class.forName(...)` / `MethodHandles`);
- a **session accessor** surface over the Minecraft client: none recovered (no class_320 accessor).

No native payload.

Cleartext endpoints recovered from this sample:

- `https://payfake-licenses.noahmenkyna667.workers.dev  (com/fakeclient/license/License.class)`

> The campaign's primary C2/exfiltration strings live inside the encrypted string table and were
> **not recovered statically** (they exist decrypted only at runtime). The analyst did **not** execute
> the sample to dump plaintext (running a stealer would trigger its behaviour). See §11.

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `fakepayclient-1.21.11.jar` |
| Size | 862,791 bytes |
| SHA-256 | `006b391bd4285f1b5ec752ca148b0ca4aa31a138a11879efd170eff3df06bcf9` |
| Type | Java archive (Fabric mod, client) |
| Class files | 75 |
| `fabric.mod.json` id / name / version | `fakeclient` / `MITTKEE ON TOP` / `1.0.0` |
| Entrypoints | client: [com.fakeclient.FakeClient]; main: [a] |
| Distribution site | `https://www.donutsmpclients.com (id: fake-pay)` |
| Download URL | `https://file.garden/auoc8y0R41ISH5sk/fakepayclient-1.21.11.jar` |
| Embedded class-key table | `1caabd1a-b736-4053-a683-3983204df2cd.bin` (SHA-256 `a687091f1a1817da720c92be4a606af8d8ed2cef46241ed85b520b4e86087ea2`) |
| Native payload(s) | | _(none)_ | |

## 3. Structure

Genuine client code is interleaved with the malicious framework in the default/obfuscated packages:

| Class | Role |
|---|---|
| `j` | Obfuscation core — holds the per-class `String[]` table and the `invokedynamic` bootstrap / decryptor. |
| `i` | Class-key state machine (hashes class names to derive a per-class key). |
| `k` | Anti-tamper — reads the `1caabd1a-b736-4053-a683-3983204df2cd.bin` table and SHA-256-hashes the calling class. |
| `b` | Custom HTTP/S client (raw `Socket` + manual SNI + `SSLContext.init(null, TrustManager[], …)`). |
| `d` | `X509TrustManager` with empty `checkClientTrusted`/`checkServerTrusted` — accepts any certificate. |
| loader / entry | `ProcessBuilder` execution and `Class.forName(...)` loading. |

## 4. Execution flow

```
Fabric entrypoint (client/main)
        |
        v
  k.<clinit>  read /1caabd1a-b736-4053-a683-3983204df2cd.bin  ->  SHA-256(own class bytes) -> per-class key   (anti-tamper)
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

- **Session accessor.** none recovered (no class_320 accessor). `class_320` is the Minecraft `Session` (username, UUID,
  **access token**); a setter implies the code can replace or harvest the live session — the
  mechanism this family uses to hijack an account.
- **TLS bypass.** The all-accepting trust manager lets the custom HTTP client talk to any host
  without certificate checks, defeating network inspection / MITM detection.

## 6. C2 infrastructure

Cleartext endpoints present in this sample:

- `https://payfake-licenses.noahmenkyna667.workers.dev  (com/fakeclient/license/License.class)`

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

- SHA-256 `006b391bd4285f1b5ec752ca148b0ca4aa31a138a11879efd170eff3df06bcf9` (`fakepayclient-1.21.11.jar`)
- Embedded key table `1caabd1a-b736-4053-a683-3983204df2cd.bin` — SHA-256 `a687091f1a1817da720c92be4a606af8d8ed2cef46241ed85b520b4e86087ea2`
- Distribution: `https://www.donutsmpclients.com`, `https://file.garden/auoc8y0R41ISH5sk/`, `https://discord.com/invite/donutsmpclients`

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `raw/fakepayclient-1.21.11.jar` | Original sample as received (not executed) |
| `evidence/fabric.mod.json` | Mod metadata / entrypoints |
| `evidence/META-INF_MANIFEST.MF` | JAR manifest |
| `evidence/classes/` | Decompiled evidence classes |
| `evidence/ARCHIVE_ENTRIES.txt` | Full archive listing |

## 11. Open questions / next steps

- Recover the encrypted C2 endpoints without executing the sample (safe options: emulate the
  decryptor with the recovered per-class key table, or run in a **detached, network-isolated**
  sandbox and capture DNS/HTTP).
- Presented as a "Fake Pay" UI toy. Carries the same obfuscation framework and an all-accepting TLS trust manager, and adds a **licence backend**: `LicenseClient.validate()` POSTs `{"key":..,"hwid":..}` to `https://payfake-licenses.noahmenkyna667.workers.dev/api/validate`. This is a cleartext **device-fingerprint (HWID) + licence-key exfiltration** endpoint operated by the campaign (Cloudflare Workers).
