# Campaign report — "DonutSMP Clients" free-client distribution (`donutsmpclients.com` → `file.garden`)

- **Status:** active malicious distribution campaign (Minecraft "free client" trojan)
- **Verdict:** the whole set is **one campaign, one obfuscation framework, one loader/stealer family**;
  every hosted JAR is unsafe to run
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** **static-only.** No sample was executed.

---

## 1. Overview

`https://www.donutsmpclients.com` is a React single-page site advertising nine free "DonutSMP"
Minecraft utility clients. Every download button resolves to a JAR hosted on the third-party file
host `file.garden` under the folder `https://file.garden/auoc8y0R41ISH5sk/`. All nine JARs were
retrieved, hashed, and statically analysed (CFR 0.152; no execution).

All nine are **Fabric mods for Minecraft 1.21.11** that share:

- the same Java string-obfuscation framework (per-class encrypted `String[]` + `invokedynamic`
  bootstrap, class-keyed anti-tamper table);
- an **all-accepting `X509TrustManager`** (TLS certificate validation disabled);
- a **custom raw-socket TLS HTTP client** that sets the SNI host manually;
- **`ProcessBuilder` process execution** and **`Class.forName(...)` reflective loading**;
- (most) a **Minecraft `Session` accessor** (`class_320`/`field_1726`) able to read/replace the
  live account session — the access-token-theft primitive;
- the same lineage as the **confirmed-malicious SoberClient** (case 001).

This is the classic "free cheat client" trojan pattern: a working-looking cheat ships alongside a
loader/stealer that is activated at runtime. The campaign's primary C2/exfiltration strings are
encrypted and were not recovered statically (no execution was performed).

## 2. Inventory

| Case | Site id / name | JAR | Size | SHA-256 |
|---|---|---|---|---|
| 002 | `gooba-client` Gooba Client | `Goobaclient-1.21.11.jar` | 20,231,395 | `c1e6ce561d01941eff7e696c4674dade3b5470472a13303366d4f2cf371cce27` |
| 003 | `frost-client` Frost Client | `frostclient-1.21.11.jar` | 6,017,716 | `975cbb120e1c5d2d479f273af931c5a1667833ad8324470c482a06abb2181b3a` |
| 004 | `corz-client` Corz Client | `Corzclient-1.21.11.jar` | 40,389,435 | `d307a508e6f942f42402ee417fa8e6dd367678878a7431f5eef6e8f804ab8889` |
| 005 | `dqrkis-client` Dqrkis Client | `dqrkisclient-1.21.11.jar` | 2,166,303 | `63acd66b84fe93afeb496d144de6bba05d19b7325be3862f693726f73a6bd712` |
| 006 | `fake-pay` Fake Pay Mod | `fakepayclient-1.21.11.jar` | 862,791 | `006b391bd4285f1b5ec752ca148b0ca4aa31a138a11879efd170eff3df06bcf9` |
| 007 | `krypton` Krypton Client | `kryptonclient-1.21.11.jar` | 6,134,771 | `62129ddc224334f13245472ea4b206a683b3d27df4f8fd330a3bb2ddf42c6270` |
| 008 | `radium` Radium Client | `radiumclient-1.21.11.jar` | 1,074,803 | `a8f92a92752cc3d8b643ca9452aa86b5ba8b254ab5c69666853742c0fcf3147e` |
| 009 | `xenon` Xenon Client | `xenonclient-1.21.11.jar` | 1,000,405 | `2eba92931cd69b0926a23e8795aef448b7541248c3f05db95fc6bfd7c75cec06` |
| 010 | `zyron-client` Zyron Client | `zyronclient-1.21.11.jar` | 19,258,886 | `7483230638024e0bd1ef0ead0fb864b77be7e908e19c7b43e2710576f332a08b` |

Plus the confirmed-malicious **case 001 SoberClient** (`soberclient.xyz`, same framework family).

## 3. Capability matrix (statically recovered)

| Jar | TLS bypass | Custom TLS client | `ProcessBuilder` | Session accessor | Native protector | Notes |
|---|---|---|---|---|---|---|
| Corz (004) | ✔ | ✔ | ✔ | ✔ | **jvmtp** (22 MB encrypted blob) | largest; native-hidden logic |
| dqrkis (005) | ✔ | ✔ | ✔ | **full** (read + replace session + `UserApiService`) | – | stock "template-mod" id |
| fakepay (006) | ✔ | ✔ | ✔ | – | – | licence/HWID backend (workers.dev) |
| Krypton (007) | ✔ | ✔ | ✔ | ✔ | **jvmtp** (17.8 MB PE) | mod id `krloader`/`KrLoader` |
| Radium (008) | ✔ | ✔ | ✔ (PowerShell) | ✔ | – | dpaste config exfil; Spotify HUD |
| Xenon (009) | ✔ | ✔ | ✔ | ✔ | – | – |
| Zyron (010) | ✔ | ✔ | ✔ | ✔ | – | Discord-webhook modules |
| Gooba (002) | ✔ | ✔ | ✔ | ✔ | – (bundled `discord-rpc.dll`) | `kuznya-integrity-target` |
| Frost (003) | ✔ | ✔ | ✔ | ✔ | **jvmtp** + `mutyx_client.dll` | `com.zenya` client |

## 4. Shared tradecraft

### 4.1 Obfuscation framework
- Per-class encrypted `String[]` decrypted by an `invokedynamic` bootstrap handler (class `j`).
- Class-key state machine (`i`) hashes class names; anti-tamper class (`k`) reads a root-level
  `<uuid>.bin` table that maps **SHA-256(class bytes) → per-class key**. Modify a class and its
  strings stop decrypting.
- Every JAR carries a unique `<uuid>.bin` table (hash listed per case). Gooba additionally ships
  `META-INF/kuznya-integrity-target` (base64 → `x/r5.class…`) naming a preLaunch entrypoint.

### 4.2 Native "jvmtp" protector (Corz, Krypton, Frost)
A JNI stub (PE, e.g. `Java_a_b99571f71427e3b19_a_bootstrap` / `Java_dev_krypton_native_Loader_init`)
that:
1. is `System.load`ed from a temp file extracted from the JAR;
2. receives a large **encrypted bytecode blob** plus a key;
3. derives an anti-tamper key from a **stack-trace token** (`file|line|line|line`);
4. `VirtualProtect`s and decrypts the blob in memory (no plaintext class files on disk).

This hides the bulk of client logic (Corz: 22 MB blob) and complicates static analysis.

### 4.3 Account-theft primitive
The Minecraft `Session` (`net.minecraft.class_320`) holds username, UUID and the **access token**.
Most builds expose an `@Accessor("field_1726")` on `class_310`; dqrkis additionally exposes
`@Mutable setSession` plus `setUserApiService` / `setProfileKeys` — i.e. the code can **replace the
live session** (account swap) as well as read it (exfiltration).

## 5. Cleartext infrastructure observed

- `www.donutsmpclients.com` — hub (React SPA); Discord `discord.com/invite/donutsmpclients`.
- `file.garden/auoc8y0R41ISH5sk/` — download host for all nine JARs.
- `payfake-licenses.noahmenkyna667.workers.dev` — fakepay licence/HWID backend (Cloudflare Workers);
  `POST /api/validate` with `{"key":…,"hwid":…}`.
- `dpaste.com/api/v2/` — radium "Config Share" config exfiltration (public paste service).
- `discord.com/api/webhooks/...` — zyron coordinate/webhook modules (user-supplied webhook).
- GitHub: `github.com/donutsmpcrackss/{yo,clients,clients1}` referenced by the site JS.

(The primary stealer C2 endpoints are encrypted in the string table; see §7.)

## 6. Impact

If run: arbitrary process execution, arbitrary class loading, TLS interception bypass, and read/replace
of the Minecraft account session (account takeover). Treat every JAR in this campaign as unsafe.

## 7. Detection & response

- **Hash-based:** block the nine SHA-256 values in §2 (plus case 001).
- **Host:** Fabric mods under `.minecraft/mods` carrying root-level `<uuid>.bin` tables or `native/*`
  payloads; JVM child processes (`powershell`, etc.).
- **Network:** `file.garden` download of `*-1.21.11.jar`; the cleartext domains in §5.
- **Response:** quarantine the JAR; rotate the Microsoft/Minecraft password and **invalidate active
  sessions** (sign out everywhere) — assume the access token is stolen; audit for sibling JARs.

## 8. Open questions / next steps

- Recover the encrypted C2 endpoints **without executing the sample** — e.g. emulate the class
  decryptor with the recovered per-class key table, or run in a detached, network-isolated sandbox
  and capture DNS/HTTP.
- Unpack the jvmtp native blobs (Corz/Krypton/Frost) in an isolated VM to enumerate the hidden
  client logic and any additional endpoints.
- Cluster the campaign operator (Discord, GitHub `donutsmpcrackss`, Cloudflare Workers account,
  file.garden folder) with the SoberClient (case 001) infrastructure.
