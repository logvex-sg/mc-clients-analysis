# Case 002 — Gooba Client (`Goobaclient-1.21.11.jar`)

- **Status:** suspected-malicious (high confidence) — stealer/loader framework
- **Family / distribution:** DonutSMP "free client" hub (`donutsmpclients.com`), delivered via
  `file.garden`; obfuscation framework shared with the confirmed-malicious **SoberClient** (case 001)
- **Primary impact:** credential-theft (Minecraft/Microsoft session token), remote code execution,
  data exfiltration
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** The sample was never executed. Decompiled with CFR 0.152.

---

## 1. Summary

`Goobaclient-1.21.11.jar` is presented on `donutsmpclients.com/gooba-client` as a free
DonutSMP "utility client" (reach, hitbox analytics, ESP, etc.). Behind a working cheat client it
ships a heavily obfuscated loader/stealer framework that is the **same generation of malware
framework as SoberClient (case 001)**.

The JAR is obfuscated by a commercial-grade Java protector: every literal string is stored in a
per-class `String[]` and decrypted on demand by an `invokedynamic` bootstrap handler; class names
are flattened to `x`, `m`, `a`, `b`, `d`, `h`; and a 619-entry key table
(`7db0858f-…-de70fe5fddf8.bin`) maps the **SHA-256 of each class file** to a per-class key, so the
strings only decrypt when the class bytes are unmodified (anti-tamper / anti-analysis).

The malicious capability set recovered statically is:

- a **custom HTTP/S client** (`b.class`) that opens a raw `Socket`, sets the SNI host name
  manually, wraps it with an **all-accepting `X509TrustManager`** (`c.class`, empty
  `checkServerTrusted` — TLS validation bypassed), handshakes, and writes the HTTP request with a
  `DataOutputStream` (manual request construction);
- **process execution** via `ProcessBuilder` with `Redirect.from/appendTo`, `directory(...)` and
  `start()` (`a`, `d`, `h` classes);
- **reflective class loading** (`Class.forName(...).getConstructor().newInstance()`) and
  `MethodHandles` lookup of private members;
- a **`setSession` accessor** on the Minecraft client that can replace the player's
  `Session` object (`m.dl` → `class_310.field_1726`), the primitive used to swap/harvest an
  account session;
- a bundled native `discord-rpc.dll` and a "kuznya-integrity-target" marker (`META-INF`) that
  points a tamper check at the `x/r5` class.

The literal C2 endpoint strings live inside the encrypted string table and were **not recovered
statically** (they only exist decrypted at runtime), so the exact exfiltration URLs are not listed
here. What *is* established is the presence of the full loader/stealer machinery plus the family
match to the confirmed-malicious SoberClient. Treat as unsafe.

> Note: because the string table is decrypt-on-demand and class-keyed, the analyst did **not**
> execute the sample to dump plaintext strings (running a stealer's `main` would trigger its
> behaviour). See §11 for the safe follow-up options.

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `Goobaclient-1.21.11.jar` |
| Size | 20,231,395 bytes |
| SHA-256 | `c1e6ce561d01941eff7e696c4674dade3b5470472a13303366d4f2cf371cce27` |
| Type | Java archive (Fabric mod, client environment) |
| Archive entries | 873 (619 class files) |
| Distribution site | `https://www.donutsmpclients.com/gooba-client` |
| Download URL | `https://file.garden/auoc8y0R41ISH5sk/Goobaclient-1.21.11.jar` |
| `fabric.mod.json` id / name | `white` / `Goobaa` (description literally `"test"`) |
| Entrypoints | `main: [x.rc, x.fp, x.lz, x.zu, a]`, `client: [x.t, x.m2, x.rc, x.fp, x.lz, x.zu]`, `preLaunch: [x.r5]` |

## 3. Structure

Real client code (mixins `m/*`, rendering `x/*`) is interleaved with the malicious framework in the
default package:

| Class | Role |
|---|---|
| `j` | Obfuscation core. Holds the 3,111-entry string table `a[]` and the `invokedynamic` bootstrap (`j.a(Lookup, name, MethodType)`), plus the decryptor `j.a(n10, n11)` = `a[n10 ^ 0x2C1EF2BF]` XOR `(n11>>>16)`. Also resolves member handles (`findVirtual`/`findStatic`/`findGetter`/`findStaticGetter`/setters). |
| `i` | Class-key state machine. FNV-1a hashes class names against an 8-entry table to derive a per-class key (`i.a`/`i.b`). |
| `k` | Anti-tamper. Reads `/7db0858f-…-de70fe5fddf8.bin`, SHA-256-hashes the calling class's own `.class` bytes, and looks the key up in the table. |
| `b` | Custom HTTP/S client (raw `Socket` + manual SNI + `SSLContext.init(null, TrustManager[], SecureRandom)` + `DataOutputStream`). |
| `c` | `X509TrustManager` with empty `checkClientTrusted`/`checkServerTrusted` — **accepts any certificate**. |
| `f`, `g` | HTTP request/response helpers used by `b`. |
| `a`, `d`, `h` | Contain `ProcessBuilder` execution paths and the entrypoint logic. |
| `m/*` | Mixins (client behaviour). `m.dl` exposes `setSession(class_320)`. |
| `x/*` | Obfuscated client features (rendering, HUD, modules). |

`META-INF/kuznya-integrity-target` base64-decodes to `x/r5.class\x07\x80(I)Ljava/lang/String;` —
an integrity target naming the `x/r5` **preLaunch** entrypoint class and one of its methods, i.e.
code that must run (and pass a check) before the game finishes launching.

## 4. Execution flow

```
Fabric preLaunch entrypoint  x.r5            (guarded by kuznya-integrity-target)
        |
        v
  k.<clinit>  reads /7db0858f-…-de70fe5fddf8.bin
        |     SHA-256(own class bytes) -> per-class key   (anti-tamper)
        v
  j.<clinit>  builds encrypted string table a[0..3110]
        |
        v
  per-class decryptor  j.a(n10^0x2C1EF2BF, n11) -> plaintext string
        |
        +--> b.class : raw Socket -> SNIHostName -> all-accepting X509TrustManager
        |              -> startHandshake -> DataOutputStream.writeShort/Byte/Bytes
        |              (manual HTTP request to a hardcoded host, port, path)
        |
        +--> a/d/h.class : new ProcessBuilder(...).directory(dir)
        |                  .redirectInput/Output(File).start()   (launch process)
        |
        +--> Class.forName(name).getConstructor().newInstance()   (load class)
        |
        +--> m.dl : Minecraft.setSession(class_320)               (swap/harvest session)
```

## 5. Credential / data theft

The following primitives are present and reachable, consistent with session-token theft:

- **Session object access.** `m.dl` is a mixin accessor `@Accessor("field_1726")`
  `void setSession(net.minecraft.class_320)` on `class_310` (the Minecraft client). `class_320` is
  the Minecraft `Session` (username, UUID, **access token**). A setter implies the code also reads
  or constructs sessions — the mechanism used by this family to exfiltrate or hijack an account.
- **Exfiltration channel.** `b.class`'s custom HTTPS stack (with TLS verification disabled) is the
  transport for whatever data the caller hands it.
- **Code/process execution.** `ProcessBuilder` in `a`/`d`/`h` can launch arbitrary programs;
  the observed pattern (a `File` redirect target + `directory(dir)` + `start()`) matches a
  dropper that writes a payload file and launches it.

Because the string table is only decryptable at runtime, the specific field names sent to the
server (e.g. token/session fields) were not recovered statically.

## 6. C2 infrastructure

| Item | Value |
|---|---|
| Distribution hub | `https://www.donutsmpclients.com` (also `gooba-client`, `frost-client`, …) |
| File host | `https://file.garden/auoc8y0R41ISH5sk/` (9 clients hosted under one folder id) |
| Sample URL | `https://file.garden/auoc8y0R41ISH5sk/Goobaclient-1.21.11.jar` |
| Discord | `https://discord.com/invite/donutsmpclients` (site bundle) |
| Client transport | custom raw-socket TLS with **all-accepting TrustManager** + manual SNI; host/port/path are encrypted string-table constants (not recovered) |

Other samples on the same `file.garden` folder id: `Corzclient`, `dqrkisclient`, `fakepayclient`,
`frostclient`, `kryptonclient`, `radiumclient`, `xenonclient`, `zyronclient` (all `-1.21.11.jar`).
These should be treated as the same campaign.

## 7. Impact

If run, the loader can establish an outbound HTTPS channel to attacker infrastructure, execute
arbitrary processes on the host, load arbitrary classes, and read/replace the Minecraft session —
i.e. **theft of the player's Microsoft/Minecraft account** and potential full remote code
execution on the desktop.

## 8. Detection & response

### 8.1 Host indicators
- A Fabric mod whose `fabric.mod.json` has `"id":"white"`, `"name":"Goobaa"`, `"description":"test"`,
  or entrypoints under packages `x` / `m` / default package.
- Resource `7db0858f-8ed1-4c4b-91d3-de70fe5fddf8.bin` (42,092 bytes, `SHA-256-of-class ‖ key`
  records) at the JAR root.
- `META-INF/kuznya-integrity-target` file.
- Bundled `win32-x86-64/discord-rpc.dll`.
- A Minecraft session that resets to the login screen / "invalid session" after playing.

### 8.2 Network indicators
- TLS connections to hosts whose certificate chain fails validation (the client accepts any cert).
- Outbound from the JVM to `file.garden` during install and to an unknown HTTPS host at runtime.

### 8.3 Response steps
1. Do not run the JAR. Quarantine it and the whole `donutsmpclients.com` download set.
2. If it was run: **invalidate the Microsoft/Minecraft session** (sign out of all devices,
   change the Microsoft account password), then scan the host for dropped executables.
3. Remove the mod from `.minecraft/mods/` and delete associated config/cache directories.

## 9. Indicators of compromise

See `iocs.csv`. Key items: SHA-256 `c1e6ce…ce27`; URLs `donutsmpclients.com/gooba-client` and
`file.garden/auoc8y0R41ISH5sk/Goobaclient-1.21.11.jar`; files `7db0858f-…-de70fe5fddf8.bin`,
`META-INF/kuznya-integrity-target`, `win32-x86-64/discord-rpc.dll`; classes `b`, `j`, `i`, `k`,
`m.dl`.

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `raw/Goobaclient-1.21.11.jar` | original sample as received |
| `evidence/fabric.mod.json` | mod metadata (id `white`, entrypoints) |
| `evidence/META-INF_MANIFEST.MF` | manifest |
| `evidence/mixin.json` | mixin config |
| `evidence/kuznya-integrity-target` | tamper-check marker (base64) |
| `evidence/CLASS_NAMES.txt` | 619 class names |
| `evidence/DECODED_STRING_TABLE.txt` | 3,107 recovered strings from the `j` table (descriptors/method refs) |
| `evidence/DECODED_INTERESTING.txt` | filtered security-relevant entries |
| `evidence/classes/` | decompiled framework classes (`j`,`i`,`k`,`b`,`a`,`d`,`h`,`m/dl`) |
| `evidence/classes/c.class`, `c.javap.txt` | the all-accepting `X509TrustManager` (decompiler failed on it; bytecode included) |

## 11. Open questions / next steps

- Recover the encrypted C2 host/path and exfiltration field names. Safe options:
  (a) instrument only `j.a`/`b` in an isolated, network-namespaced VM and log decrypted strings;
  (b) static-decompile the `invokedynamic` bootstrap and re-implement `j.a` in a harness.
- Confirm whether the `x/r5` preLaunch path (named by the integrity marker) performs the
  session read / process launch.
- Compare hashes of the other eight `file.garden` clients; run the same analysis and file them as
  cases 004+.
