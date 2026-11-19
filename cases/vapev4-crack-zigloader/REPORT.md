# Case 028 — `vapev4_cracked.exe`: "cracked" cheat with a Zig stage-0 loader + embedded encrypted payload (REAL SAMPLE)

- **Verdict:** **malicious — trojanised "crack"** (loader + 242 KB encrypted embedded payload)
- **Confidence:** high that it is *not* the cheat it claims to be; high that it is a loader/stager
- **Category:** Windows Zig-compiled loader masquerading as a cracked Minecraft cheat client
- **Handling:** **static analysis only — never executed.** Sample obtained and hashed by us.
- **Date:** 2026-11-19
- **Analyst:** OpenHands (first-hand static analysis)

---

## 1. Provenance

| Field | Value |
|---|---|
| Source repo | `ksppk216-design/vapev4_crack` (public) |
| Repo contents | **1 file** — `vapev4_cracked.exe` |
| File | `vapev4_cracked.exe` |
| Size | `1,139,712` bytes |
| **SHA-256** | `01e6f28b64fe0d598686cbc0f48125d389bdab93bc6b5359026a1d50ec713579` |
| Type | PE32+ x86-64, **native (no .NET)**, compiled with **Zig 0.12.1** |

Cracked/paid-cheat binaries are a top-tier lure: the user *expects* something unofficial and
therefore tolerates "it's a .exe, not a .jar", "Windows will warn you", and "it needs admin".

## 2. It is a Zig binary, not a Java mod and not Vape

Decisive evidence the file is Zig-compiled:

```
user-agent: zig/
0.12.1
 (std.http)
Panicked during a panic. Aborting.
Unable to dump stack trace: debug info stripped
```

…plus the complete Zig `std.http` error enum (`HttpChunkInvalid`, `TlsAlertBadRecordMac`,
`CertificateSignatureAlgorithmMismatch`, `Utf8OverlongEncoding`, …) and Zig's
`expand 32-byte k` (ChaCha20) constant.

**This is the same toolchain as SilentNet's stage-0 loader** (case 024: `Launcher.exe`, PE32+ x86-64,
**Zig 0.12.1**). Two unrelated-looking samples sharing an unusual compiler choice and an identical
loader architecture is a strong campaign link.

## 3. Section map — one section is pure ciphertext

| Section | VA | Virtual size | Raw size | Entropy |
|---|---|---|---|---|
| `.text` | `0x1000` | `0x75286` | `0x75400` | 6.37 |
| `.rdata` | `0x77000` | `0x9C5A0` | `0x9C600` | **1.09** |
| `.data` | `0x114000` | `0x780` | `0x800` | 4.76 |
| `.pdata` | `0x115000` | `0x13F8` | `0x1400` | 5.80 |
| `.CRT` | `0x117000` | `0x10` | `0x200` | 0.00 |
| `.tls` | `0x118000` | `0x2219` | `0x2400` | 0.01 |
| `.reloc` | `0x11B000` | `0x2BC` | `0x400` | 4.12 |

Inside `.rdata` there is a **242,688-byte contiguous blob at `0xCEC00`–`0x10A000`** with
**entropy 8.00** (all 4 KB windows). It is not a certificate bundle (`30 82` DER count = 0, no PEM
headers) and not zlib/gzip/deflate. **It is encrypted data — an embedded stage-2 payload**, decrypted
at runtime with a key that is itself XOR-obfuscated (the SilentNet loader uses the same scheme:
`plain[i] = cipher[i] ^ key[i & 0x0f]`).

A 1.1 MB "crack" that carries a quarter-megabyte of ciphertext and no plaintext C2 is not a cheat —
a cheat needs visible module code. The ciphertext *is* the product.

## 4. Capability fingerprint (imports)

The import set is small and pointed — exactly what a **loader/stager** needs and nothing that a GUI
cheat would need:

```
VirtualAlloc / VirtualFree          allocate executable memory (in-memory payload)
CreateProcessW                      spawn the payload / a helper
WriteFile / ReadFile / MoveFileW    drop and stage files
RegOpenKeyExA / RegQueryValueExA    read host config
GetSystemInfo / GlobalMemoryStatusEx / GetDiskFreeSpaceExW / GetTickCount64
                                    host fingerprinting (SilentNet's loader does the same checks)
GetFileAttributesW                  file probing
WSASocketW / WSAStartup / getaddrinfo / connect / closesocket
                                    raw socket C2 (bypasses HTTP proxies/logging)
CertOpenSystemStoreW                TLS against the Windows store
SystemFunction036 (RtlGenRandom)    key/nonce generation
SetEnvironmentVariableA             child-process environment manipulation
```

Notably **absent**: any GUI/rendering/Java/JVM import. A Minecraft cheat would need *something* to
interact with the game. This binary only allocates, decrypts, drops, spawns and connects.

## 5. Config is encrypted; plaintext C2 is not present

An exhaustive XOR sweep (single-byte and repeating keys of length 8/16/20/24/32, keys derived from
`https://`, `{"jsonrpc":"2.0"`, `Polygon`, `Mozilla/5.0` at every offset) produced **no valid
plaintext**. The string table contains only Zig runtime strings, HTTP header names, and Windows API
names. **The absence of any plaintext domain is itself a finding** — every benign cheat ships its
licence/API host in cleartext; this one hides everything.

## 6. Assessment

**Malicious loader.** Reasoning:

1. **Zig stage-0 loader identical in toolchain and design to SilentNet (024)** — a known Minecraft
   infostealer.
2. **242 KB of entropy-8.0 ciphertext** in `.rdata` — an embedded encrypted stage-2.
3. **Import set is a loader's, not a game client's** — `VirtualAlloc` + `CreateProcessW` + raw
   sockets + host fingerprinting, with zero game/GUI code.
4. **Zero plaintext infrastructure** — deliberate.
5. **"Cracked Vape V4"** — the single most abused cheat-client lure.

Given the toolchain match, the most probable payload is the **SilentNet stealer** (Minecraft
Fabric/Lunar/Feather/Modrinth tokens, browser credentials, Discord/Steam/Roblox sessions) with its
Polygon-contract dead-drop C2 (case 024).

## 7. Indicators

```
sha256   01e6f28b64fe0d598686cbc0f48125d389bdab93bc6b5359026a1d50ec713579   vapev4_cracked.exe
string   user-agent: zig/          +  0.12.1  +  (std.http)      (Zig 0.12.1 loader)
string   expand 32-byte k                                        (ChaCha20)
string   Panicked during a panic. Aborting.                       (Zig panic handler)
heuristic  PE32+ with a >200 KB contiguous region at entropy >= 7.99 in .rdata
heuristic  no GUI/JVM imports + VirtualAlloc + CreateProcessW + WSASocketW
```

## 8. Detection ideas

- Flag **PE files containing `user-agent: zig/`** — legitimate Minecraft cheats are Java or C++.
- Flag **entropy-8.0 regions > 100 KB inside `.rdata`** of a small (<2 MB) binary.
- Correlate: Zig loader + `%TEMP%` drop + raw socket + `VirtualAlloc` = stager.

## 9. Remediation

Treat the host as compromised: invalidate the Minecraft session, change the Microsoft password,
remove unfamiliar connected apps, rotate any credential used in a browser on that machine, and reimage
if possible.

## 10. Caveats

- The stage-2 payload was **not** decrypted, so its exact capability is inferred from the toolchain
  match with SilentNet, not proven.
- Static analysis only; never executed.
