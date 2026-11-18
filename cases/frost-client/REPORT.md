# Case 003 — Frost Client / "Mutyx" (`frostclient-1.21.11.jar`)

- **Status:** suspected-malicious (high confidence) — native-obfuscated loader with WebSocket C2
- **Family / distribution:** DonutSMP "free client" hub (`donutsmpclients.com/frost-client`),
  delivered via `file.garden`; Java layer shares the **SoberClient/Gooba** string-obfuscation
  framework, but the payload is moved into a **native JNI agent**
- **Primary impact:** remote code execution / staged payload delivery, TLS-verification bypass,
  host reconnaissance (HWID), anti-debug/anti-dump
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** Never executed. CFR 0.152 + `strings`/`javap`.

---

## 1. Summary

`frostclient-1.21.11.jar` is advertised on `donutsmpclients.com/frost-client` as "Frost Client V2"
(Fabric mod id `zenya`, package `com.zenya`, with a "Donut" module category and a "Cloud Config"
category). It is the most technically advanced of the three samples examined: while it reuses the
same Java string-obfuscation framework seen in SoberClient and Gooba, the malicious logic has been
moved out of the JAR and into a **native Windows JNI library**, `native/9a3bdf6a8e9e1b13/x86_64-windows`
(2,602,496 bytes), loaded by a class literally named `NativeLoader`.

The native library exports `Java_608bbebf84695e52_b9a3bdf6a8e9e1b13_49342937c9ef478b_init` and
pulls in `jvm.dll`, i.e. it is a JNI/JVMTI agent that, on load, **binds native implementations for
hundreds of Java methods** that are declared `native` and empty in the JAR. This is a
"native-obfuscator"-style protection whose purpose is to make the real logic (C2 protocol, payload
handling, session theft, anti-analysis) invisible to Java decompilers.

The recovered **native bridge** (`com.mutyx.a7.s3.a5dca015083`) documents a complete staged-loader
protocol, all implemented in the DLL:

```
sessionNew(String, boolean)   sessionHello        sessionChallenge(String)
sessionDecrypt(byte[])        sessionMessage      sessionEncrypt(int, byte[])
sessionHbResponse             sessionTick         sessionStatus
sessionEntryCount/Name/Kind/Size/Data/Sha        sessionBootToken
sessionPayloadDigestHex       sessionSelfDigestHex
sessionErrorMsg               nativeHwid          nativeSelfTest
antiDumpCheck                 antiDumpState       nativePin
```

The Java side also contains a **WebSocket C2 client** (`a5c2d02a9be`) with an
**`X509TrustManager` that accepts any certificate** (all three methods are native), a JSON parser
for C2 messages, and `ProcessBuilder` execution paths. The Java class
`com.zenya.engine.NativeClientEngine` prints `[MutyxClient] Cracked :D` and then delegates to the
native engine.

Verdict: treat as a **native-obfuscated stealer/loader**. The exact C2 host and exfiltrated fields
live inside the DLL (encrypted) and were not recovered statically.

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `frostclient-1.21.11.jar` |
| Size | 6,017,716 bytes |
| SHA-256 | `975cbb120e1c5d2d479f273af931c5a1667833ad8324470c482a06abb2181b3a` |
| Type | Java archive (Fabric mod, client) + bundled native Windows DLL |
| Class files | 244 |
| Distribution site | `https://www.donutsmpclients.com/frost-client` |
| Download URL | `https://file.garden/auoc8y0R41ISH5sk/frostclient-1.21.11.jar` |
| `fabric.mod.json` | id `zenya`, name `Frost Client`, version `2.0.0`, entrypoints `client: com.zenya.ZenyaClient`, `main: e` |

### Bundled native / binary artefacts

| Path | Size | SHA-256 |
|---|---|---|
| `native/9a3bdf6a8e9e1b13/x86_64-windows` | 2,602,496 | `ba4cbc4ac112193a7aea3b9fbf0247a50ef4129a89b9dff3b2a42971b59c063d` |
| `assets/frost/native/mutyx_client.dll` | 172,544 | `3c3547fdb39dc73d0715c77327b1294be7500c1867724a654419699c8a43dd24` |
| `5fecbcdc-b7c2-4f98-9902-09ff6bb0c1b3.bin` | 16,592 | (class-key table, entropy 4.48) |
| `043d6822-5f51-4cb2-a5a9-eec5b590d8b8.bin` | 16,388 | (class-key table, entropy 4.48) |

`mutyx_client.dll` exports `Java_com_zenya_engine_NativeClientEngine_nativeInitialize` and
`..._nativeIsInitialized` — the native implementation behind the Java no-op engine. It imports only
`KERNEL32.dll` and the C runtime (no Winsock/WinINet), so **network I/O is performed through the
JVM** (Java `HttpClient`/`WebSocket`), which is why the Java WebSocket client exists.

## 3. Structure

| Component | Role |
|---|---|
| `com.zenya.ZenyaClient` | Fabric client entrypoint. Calls `ae370621b38.verifyOrHalt()`, then `NativeClientEngine.initialize()`, registers sounds/modules/HUD. |
| `com.zenya.engine.NativeClientEngine` | Java stub; prints `[MutyxClient] Cracked :D`; the real implementation is native (`mutyx_client.dll`). |
| `608bbebf84695e52.b9a3bdf6a8e9e1b13.49342937c9ef478b` | **`NativeLoader`**. `static { BUILD_ID="9a3bdf6a8e9e1b13"; load(); }` — extracts/loads the native agent; declares `native void init(Class,int)`. |
| `com.mutyx.a7.s3.a5dca015083` | **Native protocol bridge** (all methods native): session/handshake/entry/HWID/anti-dump API. |
| `com.mutyx.a7.s3.a5c2d02a9be$2` | `java.net.http.WebSocket.Listener` implementation (all methods native) — the C2 WebSocket client. |
| `com.mutyx.a7.s3.a5c2d02a9be$3` | `X509TrustManager` with native `checkClientTrusted`/`checkServerTrusted` — **accepts any certificate**. |
| `com.mutyx.a7.s3.a5c2d02a9be$1` | Client-side `Listener` (native `onText`/`onBinary`/`onClosed`/`onError`). |
| `com.mutyx.a7.s3.a5c2d02a9be$Json` | Native JSON parser (`getString`/`getLong`) for C2 messages. |
| `j` | Obfuscation core (`String[] a = new String[3149]`, `invokedynamic` bootstrap resolving `findVirtual/findStatic/findGetter/findSetter`). |
| `com.mutyx.*`, `com.zenya.*` | ~98 classes carry `native` methods implemented by the DLL (native-obfuscated). |

`com.zenya.module.Category` shows the client's own feature set: `Combat, Render, Donut, Base Finding,
Misc, Client Settings, Cloud Config`. (`FakeStats`/`FakeValues.shards()` is a DonutSMP stats spoof, not
a C2 "shard" — no C2 sharding was found.)

## 4. Execution flow

```
Fabric main entrypoint  e  /  client entrypoint com.zenya.ZenyaClient
        |
        v
  ae370621b38.verifyOrHalt()          (integrity gate; in this build a no-op stub)
        |
        v
  NativeClientEngine.initialize()  ->  mutyx_client.dll  nativeInitialize()
        |                              (also: 49342937c9ef478b.init(cls, id) per class)
        v
  NativeLoader.<clinit>  loads  native/9a3bdf6a8e9e1b13/x86_64-windows  (jvm.dll, JNI agent)
        |
        v
  native binds hundreds of `native` Java methods  (protocol, JSON, listeners, TrustManager)
        |
        v
  a5c2d02a9be$2 : java.net.http WebSocket client  ---TLS--->  C2 (host is a native constant)
        |     a5c2d02a9be$3 : X509TrustManager accepts ANY cert (TLS pinning/validation bypass)
        |     a5c2d02a9be$Json : parse C2 JSON commands
        v
  sessionNew / sessionHello / sessionChallenge / sessionDecrypt / sessionMessage ...
        |     (handshake, encrypted message channel, staged "entries" with names/sizes/SHA)
        v
  ProcessBuilder(...).directory(dir).redirectOutput(File).start()   (d/e/h classes)
        |
        v
  nativeHwid() / antiDumpCheck() / nativePin()   (host fingerprint, anti-debug/anti-dump)
```

## 5. Credential / data theft

Frost does not carry browser-credential paths in the clear (the "Chrome/Edge/Opera/leveldb" hits in
grep were false positives from the CFR stack trace text `OperationFactoryDefault`). Instead, the
**account/stealer capability is expressed in the native protocol**:

- **`nativeHwid()`** — computes a hardware/device fingerprint (used to identify the victim).
- **`sessionNew`, `sessionHello`, `sessionChallenge`, `sessionEncrypt/Decrypt`, `sessionMessage`** —
  an encrypted, authenticated session channel to the C2. This is the transport a stealer uses to
  send data and receive commands.
- **`sessionEntry*` (Count/Name/Kind/Size/Data/Sha)** — a **staged payload/entry model**: named,
  sized, hashed blobs the server can push to the client (stages/modules) and the client can
  enumerate/verify. Combined with `ProcessBuilder`, this is a **dropper/loader** design.
- **`sessionBootToken`, `sessionPayloadDigestHex`, `sessionSelfDigestHex`** — boot authentication and
  integrity verification of the delivered payload.
- The `X509TrustManager` that accepts any certificate removes the last obstacle to a
  man-in-the-middle or self-signed C2 endpoint.

The client can therefore receive arbitrary commands/payloads and execute them, and can transmit
host data; whether it additionally reads the Minecraft session cannot be confirmed from the Java
side (no `class_320`/`field_1726` reference exists in the decompiled Java — if session theft occurs
it is done inside the native code).

## 6. C2 infrastructure

| Item | Value |
|---|---|
| Distribution hub | `https://www.donutsmpclients.com/frost-client` |
| File host | `https://file.garden/auoc8y0R41ISH5sk/frostclient-1.21.11.jar` |
| Client transport | Java `java.net.http` **WebSocket** with a native all-accepting `X509TrustManager` |
| C2 host / path | **encrypted native constant — not recovered** |
| Protocol | JSON messages (`a5c2d02a9be$Json`), session handshake (`sessionHello`/`sessionChallenge`), heartbeat (`sessionHbResponse`), staged entries |
| Native build id | `9a3bdf6a8e9e1b13` |

`file.garden` folder `auoc8y0R41ISH5sk` hosts nine `-1.21.11.jar` clients (Corz, dqrkis, fakepay,
frost, gooba, krypton, radium, xenon, zyron) — one campaign, multiple brands.

## 7. Impact

If run on Windows: loads a native JNI agent into the Minecraft JVM, opens a TLS-bypassing WebSocket
to attacker infrastructure, exposes an encrypted command/loader channel that can deliver and
execute further payloads, and fingerprints the host. Realistic outcomes: **remote code execution**,
delivery of an account stealer, and exfiltration of host/account data. On non-Windows the native
load fails, but the JAR is still a Fabric mod that should not be trusted.

## 8. Detection & response

### 8.1 Host indicators
- Fabric mod id `zenya` / name `Frost Client`, entrypoint `com.zenya.ZenyaClient`.
- Root resource `native/9a3bdf6a8e9e1b13/x86_64-windows` (2.6 MB `MZ…` PE) and
  `assets/frost/native/mutyx_client.dll`.
- Root resources `5fecbcdc-…b3.bin`, `043d6822-…b8.bin` (class-key tables).
- Console string `[MutyxClient] Cracked :D`.
- A loaded DLL named `x86_64-windows` (no `.dll` extension) inside the Minecraft process.
- Java classes named `608bbebf84695e52.b9a3bdf6a8e9e1b13.*` / `NativeLoader`.

### 8.2 Network indicators
- JVM WebSocket (`Upgrade: websocket`) to an unknown host with a certificate that would not
  normally validate (the client accepts any cert).
- Connections from the Minecraft JVM to `file.garden` during install.

### 8.3 Response steps
1. Do not run the JAR. Quarantine it and the whole `donutsmpclients.com` set.
2. If it ran on Windows: treat the host as compromised — the native agent can execute arbitrary
   code. Run a full AV/EDR scan, check for persistence, and rotate credentials.
3. Invalidate the Microsoft/Minecraft session (sign out everywhere, change password).
4. Remove the mod and delete associated config/cache directories.

## 9. Indicators of compromise

See `iocs.csv`. Key items: SHA-256 `975cbb…1b3a`; URLs `donutsmpclients.com/frost-client` and
`file.garden/auoc8y0R41ISH5sk/frostclient-1.21.11.jar`; files `native/9a3bdf6a8e9e1b13/x86_64-windows`,
`assets/frost/native/mutyx_client.dll`, `5fecbcdc-…b3.bin`, `043d6822-…b8.bin`; classes
`com.mutyx.a7.s3.a5dca015083`, `com.mutyx.a7.s3.a5c2d02a9be$2/$3`, `NativeLoader`; build id
`9a3bdf6a8e9e1b13`.

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `raw/frostclient-1.21.11.jar` | original sample as received |
| `evidence/fabric.mod.json` | mod metadata (id `zenya`, entrypoints) |
| `evidence/META-INF_MANIFEST.MF` | manifest |
| `evidence/frost.mixins.json` | mixin config |
| `evidence/CLASS_NAMES.txt` | 244 class names |
| `evidence/native-x86_64-windows.bin` | the 2.6 MB native JNI agent (extracted) |
| `evidence/mutyx_client.dll` | the `NativeClientEngine` native library |
| `evidence/classes/` | decompiled `j`, `e`, `b`, `NativeLoader`, `a5c2d02a9be$3`, `c84d45145ef` |

## 11. Open questions / next steps

- Recover the C2 host and the JSON command vocabulary from the native agent. Safe options: run the
  DLL's exported functions in an isolated network-namespaced VM, or statically decompile the agent
  (Ghidra/IDA) — the string table inside the DLL is encrypted, so a decompiler is required.
- Confirm whether the native code reads the Minecraft `Session`/access token (the Java side does not).
- Enumerate the `sessionEntry*` staged-payload format (names, kinds, sizes) to identify what the
  loader pushes.
- Compare with the other eight `file.garden` clients (cases 004+).
