# Case 024 — SilentNet: multi-stage Minecraft stealer (Zig loader → Fabric mod → Nuitka Python)

- **Verdict:** **confirmed malicious** (full source-level deobfuscation published; hashes published)
- **Category:** commercial-grade MaaS infostealer sold as a Minecraft "aimbot/cheat"
- **Handling:** OSINT — **source read, never executed.** No binary obtained.
- **Date:** 2026-11-19
- **Analyst:** OpenHands (open-source intelligence)
- **Primary source:** `Mekverberk/silentnet-stealer-analysis`
  (https://github.com/Mekverberk/silentnet-stealer-analysis) — Turkish-language full technical
  analysis with **100 % deobfuscated Java source**, Zig loader source, YARA and IOC package
- **Evidence:** `evidence/source-readme.md`, `evidence/silentnet.yar`, `evidence/ioc-blocklist.txt`,
  `evidence/embedded_config.zig`, `evidence/crypto.zig`, `evidence/StealerCore.java`

---

## 1. Verdict

SilentNet is a **three-stage infostealer** distributed as a Minecraft cheat ("aimbot tool"). It
targets **Minecraft accounts specifically** — Fabric, Lunar, Feather and Modrinth launchers — plus
browsers (Chrome/Edge/Opera GX), Discord tokens, Steam, Riot and Roblox sessions.

It is the most complete **"stealer-as-a-service for Minecraft"** documented to date, and it is the
direct reference implementation for the loader in **case 028 (vapev4_cracked.exe)**.

## 2. Sample set (one campaign, UUID `fa357567-049b-426b-96ec-7d8488629333`)

| Stage | File | Type | SHA-256 |
|---|---|---|---|
| 0 | `Launcher.exe` | PE32+ x86-64, **Zig 0.12.1** | `79420c6623177c1d48fb87b6e32c0cd06f3360fce406037506872b9b1b535006` |
| 1/2 | `mod.jar` | JAR, Fabric Loom / standalone | `9ae06f129d04f63a639b48d6d293f5e89079614cbb22d042c203bb0843e81944` |
| 2 | `AppHost\app.pyd` | PE32+ DLL, **Nuitka** (`PyInit_app`) | `548ffea3467f9a2984e247edc3dc3e5c5be9fad507cc3fc1c23b6b3826a12c7a` |
| — | (stage 2 alt) | — | `56aec3b560f70e6cb88de9ef1abd252a4c0c90ae3bb6ed269d89cd6116257334` |
| — | (stage 2 alt) | — | `8f9799be-c0c3-45e9-b7ab-df8d4d342043` |

## 3. Attack chain

```
Launcher.exe  (Zig, stage 0)
 ├─ anti-VM: CPU string == "Intel Core Processor (Broadwell)", >= 6 cores,
 │            RAM/disk thresholds, GetTickCount64 sandbox timing
 ├─ XOR-decrypts its config:  plain[i] = cipher[i] ^ key[i & 0x0f]      (16-byte repeating key)
 ├─ C2 resolution: eth_call to a POLYGON contract, then DoH to the returned host
 ├─ drops Python 3.12.7 embeddable + get-pip.py from python.org / bootstrap.pypa.io
 │            (abusing the *legitimate* Python CDN as a payload channel)
 └─ fetches stage 2 from /cdn/<hex> paths on the resolved CDN host
        ↓
mod.jar  (Java/Fabric, stage 1/2)
 └─ StealerCore: harvests Minecraft + browser + Discord + Steam + Roblox credentials
        ↓
app.pyd  (Nuitka-compiled Python, stage 2)
 └─ full Python stealer, reconstructed source-level by the analyst
```

## 4. C2 — Polygon blockchain dead-drop (the key technique)

From `evidence/embedded_config.zig` (deobfuscated constants):

```
POLYGON_CONTRACT   = 0x9c0a507300fd902787bb193d80fca5ce6e1bff9a
ETH_CALL_SELECTOR  = 0xce6d41de
ETH_CALL_JSON      = {"jsonrpc":"2.0","method":"eth_call",
                      "params":[{"to":"0x9c0a...ff9a","data":"0xce6d41de"},"latest"],"id":1}
CDN_HOST_FALLBACK  = thisisafalsepositive.st
```

Eight public Polygon RPC endpoints are rotated:

```
polygon-rpc.com · rpc-mainnet.matic.quiknode.pro · polygon-public.nodies.app · 1rpc.io/matic
polygon-bor-rpc.publicnode.com · api.zan.top/polygon-mainnet
polygon.rpc.subquery.network/public · endpoints.omniatech.io/v1/matic/mainnet/public
```

**This is EtherHiding again** (cases 016, 026): the C2 hostname lives in a smart contract, so it can
be rotated by the operator and cannot be taken down or DNS-sinkholed. The fallback host
`thisisafalsepositive.st` is a deliberate taunt aimed at analysts.

CDN payload paths (also in the config): `/cdn/5ef451455923`, `/cdn/e/42a522313d92`,
`/cdn/e/36f2c0035ef9`, `/cdn/v2/9f4e7a2c1b8d.png`, plus API shards `/shard/prefireMc`,
`/shard/submitMinecraftLog`.

**Origin authentication header** (shows this is a *managed service*, not a hobby kit):

```
x-cdn-origin-verify: dK9mT3nR7xQ2pL8wF4jH6yB1cN5gA0sZ12345678abc=
```

## 5. Loader config — persistence and stealth

| Constant | Value | Meaning |
|---|---|---|
| `REGISTRY_NT_PROFILE` | `Microsoft\Windows\NtProfileIndex` | **registry persistence** under a name that mimics a Windows key |
| `ENV_NO_BYTECODE` | `PYTHONDONTWRITEBYTECODE` | avoids leaving `.pyc` artefacts on disk |
| `PYTHON_EMBED_ZIP` | `python.org/ftp/python/3.12.7/python-3.12.7-embed-amd64.zip` | abuses a **trusted** CDN |
| `GET_PIP_URL` | `bootstrap.pypa.io/get-pip.py` | abuses a **trusted** CDN |
| `HTTP_USER_AGENT` | Chrome/120 UA | blends into normal traffic |

Using **python.org and bootstrap.pypa.io** as payload hosts is notable: those domains are
allow-listed in many egress policies, so the download never looks suspicious.

## 6. Obfuscated class names (anti-signature)

From the IOC list, the Fabric mod deliberately ships names that look like normal Fabric internals:

```
net.fabric.ArKZkARk        net.fabric.readIglpzeg
```

## 7. MITRE ATT&CK

`T1204.002` (user execution), `T1036.005` (masquerading as a cheat), `T1027` (obfuscation),
`T1497.001` (anti-VM), `T1102.001` (dead-drop resolver), `T1071.001` (web C2), `T1573` (encrypted
channel), `T1105` (ingress tool transfer), `T1547.001` (Run-key persistence), `T1528` (steal
application access token), `T1555.003` (browser credentials), `T1539` (steal session cookies),
`T1005` (local data).

## 8. Indicators

See `evidence/ioc-blocklist.txt` (verbatim) and `iocs.csv`. Highlights:

```
sha256    79420c6623177c1d48fb87b6e32c0cd06f3360fce406037506872b9b1b535006   Launcher.exe
sha256    9ae06f129d04f63a639b48d6d293f5e89079614cbb22d042c203bb0843e81944   mod.jar
sha256    548ffea3467f9a2984e247edc3dc3e5c5be9fad507cc3fc1c23b6b3826a12c7a   app.pyd
contract  0x9c0a507300fd902787bb193d80fca5ce6e1bff9a                     Polygon dead-drop
selector  0xce6d41de                                                      eth_call selector
domain    thisisafalsepositive.st                                         CDN host fallback
url       https://thisisafalsepositive.st/cdn/5ef451455923
url       https://thisisafalsepositive.st/cdn/e/42a522313d92
url       https://thisisafalsepositive.st/cdn/e/36f2c0035ef9
header    x-cdn-origin-verify
secret    dK9mT3nR7xQ2pL8wF4jH6yB1cN5gA0sZ12345678abc=                    origin auth
uuid      fa357567-049b-426b-96ec-7d8488629333                            campaign id
class     net.fabric.ArKZkARk / net.fabric.readIglpzeg                   obfuscated
registry  Microsoft\Windows\NtProfileIndex                                persistence
```

**YARA:** `evidence/silentnet.yar` (shipped by the analyst).

## 9. Relationship to this database

| Case | Relationship |
|---|---|
| **028 vapev4_cracked.exe** | **same Zig 0.12.1 stage-0 loader design** (our own analysis) |
| 016 SkyblockAddons EtherHiding | same blockchain dead-drop technique, **Ethereum** (SilentNet uses **Polygon**) |
| 026 Optimizer | same **Weedhack** operator (panel `weedhack.lol`), **Solana** dead-drop |
| 001–010 Cluster A | same target (Minecraft session token) and same "free client" lure |

**Three different blockchains — Ethereum, Polygon, Solana — used by three related families.** That is
the clearest evidence yet that this is an **organised, evolving ecosystem**, not isolated hobbyists.

## 10. Remediation

- Invalidate the Minecraft session; change the Microsoft password; remove unfamiliar connected apps.
- Delete `%LOCALAPPDATA%\emre`-style dirs and any Python-embed drop folders; check
  `HKCU\...\Run` and `Microsoft\Windows\NtProfileIndex` for persistence.
- Rotate **all** browser-saved passwords, Discord token, Steam/Riot/Roblox sessions.
- Treat the host as fully compromised (a Nuitka-compiled stealer ran with user privileges).

## 11. Caveats

- Third-party analysis; not independently re-verified by us. No binary handled.
- Turkish-language source; the original is preserved verbatim in `evidence/`.
- Contract contents can change at any time (that is the design).
