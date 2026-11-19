# Case 001 — SoberClient (Minecraft "utility client" with bundled account stealer)

- **Status:** Confirmed malicious
- **Family / distribution:** `SoberClient` — Trojanised Minecraft Fabric mod distributed from `soberclient.xyz`
- **Primary impact:** Minecraft/Microsoft account session-token theft, arbitrary second-stage execution
- **Analyst:** OpenHands (static analysis)
- **Date:** 2026-11-18
- **Handling:** No code was executed. All findings are from static disassembly and read-only network checks.

---

## 1. Summary

`soberclient.xyz` advertises an "advanced Minecraft client for DonutSMP and economy servers"
with base-finding and economy tools. The downloadable jar **does** contain that cheat client
(the `dev.sober.*` package is real and functional), but it also contains a second, hidden
payload in the `net.fabric.*` package that impersonates Fabric loader internals.

That hidden payload:

1. steals the logged-in Minecraft account's **username, UUID and session access token**
   (the token is a full account-takeover credential);
2. resolves a **rotating command-and-control domain from the Polygon blockchain** (so the
   operator can change C2 without re-releasing the jar);
3. downloads and runs a **portable Python "stealer"** stage from an attacker-controlled host,
   extracting it from a decoy PNG;
4. exfiltrates the stolen data and the victim's Minecraft logs to the C2.

The malware uses a custom raw-socket HTTP stack with a **TLS trust manager that accepts every
certificate**, and communicates over DNS-over-HTTPS — both standard anti-analysis / anti-network-
inspection measures.

---

## 2. Sample identification

| Field | Value |
|---|---|
| File name | `SoberClient.jar` (analysis copy: `SoberClient(1).jar`) |
| Size | 32,701,709 bytes |
| SHA-256 | `0dad93bc510c690860df1364ba291336e619c8c4ffca26a3c96e57b223f9e1ca` |
| Type | Java archive (Fabric mod), `application/java-archive` |
| `Main-Class` | `net.fabric.YnjcXePczS` |
| Fabric `main` entrypoint | `net.fabric.checkNltfjeqlm` |
| Fabric `client` entrypoint | `dev.sober.SoberClient` |
| Mod version | `1.0.0`, author "Violet", license GPL-3.0-only |
| Declared target | Minecraft `1.21.11`, Fabric loader `>=0.18.2`, Java `>=21` |
| Archive entries | 1,701 (894 class files, 766 assets) |
| Download URL | `https://mxbqrtnpmoaqoaqpvpvg.supabase.co/storage/v1/object/public/downloads/SoberClient.jar?download` |
| Distribution site | `https://soberclient.xyz/` (canonical), `https://www.soberclient.xyz/#download` |

The download URL is served from a **Supabase public storage bucket** (`mxbqrtnpmoaqoaqpvpvg`)
behind Cloudflare. The hosted file's size matched the analysed sample at time of analysis.

> Evidence in this case came from a static reverse-engineering package that contains a complete
> `javap` bytecode disassembly, the archive inventory, resource metadata and Fabric config.
> It is **not** a source decompilation (no decompiler was available in the RE environment), so
> some control-flow detail is inferred from bytecode. Every behavioural claim below is anchored
> to decoded string constants and concrete API call sites.

---

## 3. Structure — two clients in one jar

| Package | Purpose |
|---|---|
| `dev.sober.*` | The genuine-looking cheat client (modules, HUD, configs, mixins). Benign-ish "utility client" functionality. |
| `net.fabric.*` | **The malware.** Eight classes masquerading as Fabric loader internals so they blend into a mod's classpath. |

`net.fabric` classes:

| Class | Role |
|---|---|
| `net.fabric.checkNltfjeqlm` | Fabric `main` `ModInitializer`; `onInitialize()` spawns a worker thread that builds the victim profile and starts the chain. |
| `net.fabric.YnjcXePczS` | Jar `Main-Class`; standalone launcher, and re-launches itself as a **detached** process (`-restarted`). |
| `net.fabric.grsq_myprv_xx_bdxy` | Dropper / loader: `getDomain()`, `prefire()`, `execute()`, runtime download, stealer drop + spawn. |
| `net.fabric.sajxwzz` (+ nested `$lAxbb`, `$QWBciZSIa`, `$haamzu_oxzssw_jk`) | Custom HTTP client (raw sockets, chunked), DoH resolver, all-accepting `X509TrustManager`. |
| `net.fabric.lpt_twqemj` | Reflection-based theft of the Minecraft `Session` (username / UUID / access token). |

### 3.1 String obfuscation

All malicious string constants are assembled one character at a time in bytecode:

```
ldc/sipush int, int  ->  isub | ixor | iadd  ->  i2c  ->  StringBuilder.append(char)
```

Example: `51964 - 51847 = 117 = 'u'`. A decoder that emulates this pattern is provided in
`tools/decode_strings.py`; running it over the disassembly recovers the full string set used
throughout this report.

---

## 4. Execution flow

```
soberclient.xyz  ──►  Supabase bucket  ──►  SoberClient.jar
                                                  │
        (Fabric loads)                            │ (or run standalone via Main-Class)
        net.fabric.checkNltfjeqlm.onInitialize()   net.fabric.YnjcXePczS.main()
                          │                              │
                          └────────────┬─────────────────┘
                                       ▼
                    build victim profile (MC user / uuid / accessToken)
                                       ▼
        grsq.getDomain()  ── eth_call to Polygon RPC ──►  contract 0x9c0a…ff9a
                                       │  (fallback: thisisafalsepositive.st)
                                       ▼
        POST https://<c2>/shard/prefireMc        (beacon / profile check-in)
                                       ▼
        download portable Python runtime  ─►  %LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex
        write AppHost\main.py (extracted from a decoy PNG)
                                       ▼
        spawn detached: python.exe AppHost\main.py -u --env <..> --tag <..> --mcInfo <..> --prefireId <..>
                                       ▼
        POST https://<c2>/shard/submitMinecraftLog   (exfil: token, uuid, logs)
```

### 4.1 Entrypoints

- `fabric.mod.json` declares `"main": ["net.fabric.checkNltfjeqlm"]`. Fabric calls
  `onInitialize()`, which immediately does `new Thread(...).start()` — the malware runs off the
  render thread so it never stalls the game.
- The manifest `Main-Class` is `net.fabric.YnjcXePczS`, so the jar is *also* directly runnable
  (double-click / `java -jar`). Its `main` inspects arguments, and when launched with a
  `-restarted` flag it re-spawns the JVM detached via `ProcessBuilder` (with `redirectInput` from
  `NUL`, output appended to `_spawn.log`) — a persistence / detach trick so the payload outlives
  the launcher.

### 4.2 Victim profiling (`net.fabric.lpt_twqemj`)

The class reflectively walks the Minecraft client to read the current session. It carries **both**
the Mojang-mapped names and the intermediary (`method_…` / `field_…`) names, so it works regardless
of the obfuscation mapping:

- `net.minecraft.client.Minecraft.getInstance()` (`method_1551` / `field_1700`)
- `getSession()` → `Session` (`method_1548`), `getUser()`
- `getUsername` / `getName` (`method_1676` / `field_1982`)
- `getUuidOrNull` / `getProfileId` / `getUuid` (`method_44717` / `field_1985`)
- **`getAccessToken` (`method_1674` / `field_1983`)**

The access token is the high-value target: it is the bearer credential for the Microsoft/Mojang
account and is what "session stealers" resell.

---

## 5. C2 infrastructure

### 5.1 Blockchain dead-drop resolver

`grsq_myprv_xx_bdxy.getDomain()` performs a JSON-RPC `eth_call` against public **Polygon**
endpoints:

- `https://polygon.drpc.org`
- `https://polygon-bor-rpc.publicnode.com`
- `https://1rpc.io/matic`
- `https://api.zan.top/polygon-mainnet`
- `https://rpc-mainnet.matic.quiknode.pro`

Call body:

```json
{"jsonrpc":"2.0","method":"eth_call",
 "params":[{"to":"0x9c0a507300fd902787bb193d80fca5ce6e1bff9a","data":"…"},"latest"],"id":1}
```

The contract address is a hardcoded constant; the returned value yields the live C2 domain.
If the RPC path fails, the code falls back to a hardcoded domain **`thisisafalsepositive.st`**
(with payload path `/cdn/v2/9f4e7a2c1b8d.png`). This design lets the operator rotate C2 on-chain
and survive takedowns of the fallback domain.

### 5.2 Custom HTTP client (`net.fabric.sajxwzz`)

- Builds raw `HTTP/1.1` requests by hand over `java.net.Socket` (`GET`, `POST`, `PUT`), handling
  `Content-Length` and `transfer-encoding: chunked` manually.
- Resolves hostnames via **DNS-over-HTTPS** to `https://cloudflare-dns.com/dns-query`
  (and `1.1.1.1`) using `application/dns-message` — bypasses local DNS logging/filtering.
- Installs an **`X509TrustManager` whose `checkServerTrusted` accepts any certificate**
  (`net.fabric.sajxwzz$haamzu_oxzssw_jk`) — disables TLS validation entirely.
- Sets a browser-like `User-Agent` (`Mozilla/5.0 … Chrome/131 …`) and spoofed `Accept` /
  `Sec-Fetch-*` headers to look like normal image traffic.

### 5.3 Endpoints

| Endpoint | Method | Purpose |
|---|---|---|
| `https://<c2>/shard/prefireMc` | POST | Check-in / profile beacon |
| `https://<c2>/shard/submitMinecraftLog` | POST | Exfiltration of token, identity and logs |
| `/cdn/v2/9f4e7a2c1b8d.png` | GET | Fallback decoy/staging path on the fallback domain |

Exfil body (reconstructed from string constants):

```json
{"sessionId":"…","userId":"…","accessToken":"…","mcUsername":"…","mcUuid":"…",
 "env":"Fabric","content":"…","filename":"combined.log","executionEnvironment":"-Detached"}
```

and a second shape:

```json
{"mcInfo":"…","prefireId":"…","userId":"…","tag":"…","domain":"…","gameDir":"…",
 "mcUsername":"…","mcUuid":"…","env":"Fabric"}
```

Hardcoded operator identifiers: `userId = 4b33001c-a943-448a-bbd6-f01cf4d51040`, `tag = Default`.

---

## 6. Second stage — portable Python "stealer"

`grsq_myprv_xx_bdxy.execute()` / `xzh_zz_uuewk()` drive stage two:

1. Create working directory `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex` (disguised as a
   Windows folder).
2. Download a **portable Python runtime** from the C2 (`Download attempt …`, "Portable runtime
   ready", `python.exe`). A cached copy is reused if present.
3. Extract `AppHost\main.py` from a **decoy image** ("extracting from image… PNG extraction
   failed… Extracted N files… unpacking…"). The image is used as a smuggling container.
4. Spawn the stealer **detached**, with output to `_stealer.log`:

   ```
   python.exe AppHost\main.py -u --env <env> --tag <tag> --mcInfo <info> --prefireId <id>
   ```
5. Collect and upload the Minecraft log (`logs/latest.log` → combined log) and runtime info
   (`os.name`, `user.name`, `java.version`).

The stage-2 `main.py` itself was not recovered from this package (it is fetched at runtime).

---

## 7. Impact

- **Account takeover:** theft of the Minecraft/Microsoft **access token** allows the operator to
  authenticate as the victim (session hijacking) without the password.
- **Arbitrary code execution:** the loader runs attacker-supplied Python; stage two is unbounded.
- **Data exfiltration:** victim identity, game directory, environment and logs are uploaded.
- **Evasion:** blockchain C2 resolution, DoH, disabled TLS validation, browser-like traffic,
  thread offload, and detached re-spawn make detection and takedown harder.

---

## 8. Detection & response

### 8.1 Host indicators

- Directory: `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex\` (and `…\AppHost\main.py`)
- Files: `_stealer.log`, `_spawn.log`
- Processes: an unexpected `python.exe` running `AppHost\main.py` with `--prefireId` / `--mcInfo`
- A `javaw.exe` / `java.exe` process relaunched with a `-restarted` argument

### 8.2 Network indicators

- POSTs to `/shard/prefireMc` and `/shard/submitMinecraftLog`
- DNS-over-HTTPS requests to `cloudflare-dns.com/dns-query` from a game process
- `eth_call` JSON-RPC traffic to public Polygon endpoints from a desktop app
- TLS sessions with an invalid/unverifiable certificate (should not occur with a normal client)

### 8.3 Response

If the jar was ever launched:

1. Treat the Minecraft/Microsoft account as compromised — change the password and
   **revoke all active sessions ("sign out everywhere")**, then re-enable 2FA.
2. Inspect and remove `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex` and any stray
   `AppHost`/`python.exe` artifacts.
3. Assume the machine is untrusted if stage two ran; a clean reinstall is the safe option.

---

## 9. Indicators of compromise

See `iocs.csv` in this folder for the machine-readable list. Key items:

- Domains: `thisisafalsepositive.st`, `mxbqrtnpmoaqoaqpvpvg.supabase.co`
- URLs: `https://<c2>/shard/prefireMc`, `https://<c2>/shard/submitMinecraftLog`,
  `https://soberclient.xyz/`, `https://mxbqrtnpmoaqoaqpvpvg.supabase.co/storage/v1/object/public/downloads/SoberClient.jar?download`
- Polygon contract: `0x9c0a507300fd902787bb193d80fca5ce6e1bff9a`
- Hardcoded user id: `4b33001c-a943-448a-bbd6-f01cf4d51040`
- SHA-256: `0dad93bc510c690860df1364ba291336e619c8c4ffca26a3c96e57b223f9e1ca`

---

## 9.1 Family attribution — this is **SilentNet** (case 024)

Cross-referencing the decoded constants against [case 024](../silentnet-stealer/REPORT.md) gives a
**five-way exact match**. These are not similar values; they are byte-identical:

| Artefact | SoberClient (001) | SilentNet (024) | |
|---|---|---|---|
| Polygon contract | `0x9c0a507300fd902787bb193d80fca5ce6e1bff9a` | same | identical |
| `eth_call` selector | `0xce6d41de` | same | identical |
| Fallback host | `thisisafalsepositive.st` | same | identical |
| CDN path | `/cdn/v2/9f4e7a2c1b8d.png` | same | identical |
| API shards | `/shard/prefireMc`, `/shard/submitMinecraftLog` | same | identical |
| Persistence dir | `Microsoft\NtProfileIndex` | `Microsoft\Windows\NtProfileIndex` | same name |
| Staging | `jre-embedded`, `AppHost`, `main.py` | `jre-embedded`, `AppHost\app.pyd` | same layout |

A shared contract address, function selector, fallback domain and CDN path cannot arise by
coincidence: they are chosen by the operator. **SoberClient is a rebranded SilentNet build** with its
own campaign UUID (`4b33001c-a943-448a-bbd6-f01cf4d51040`) but the same backend. Any detection
written for SilentNet covers this sample, and vice versa.

This also resolves part of §11: the stage-2 stealer is the SilentNet portable-Python component, so
its capability set should be read from case 024 (browser credentials, cookies, wallets, Discord
tokens) rather than treated as unknown.

## 9.2 Unresolved artefact — `bridge.ps1`

`assets/sober/media/bridge.ps1` is an **11,691-byte PowerShell script shipped inside the mod's
assets**. No Java class references it (the string `bridge.ps1` appears nowhere in the 383,975-line
disassembly), and its content was not included in the RE package.

A PowerShell script in a cheat mod's asset folder has no legitimate purpose. It is either
(a) a stage the operator runs manually, (b) dead weight from a template, or (c) delivered to a
second stage that reconstructs the path. **Its behaviour is unknown and it is the single largest
open question in this case** — it should be extracted from the original jar before any closure.

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| `SoberClient_RE_analysis.zip` | Original static RE package as received |
| `evidence/BYTECODE_DISASSEMBLY.txt` | Full `javap` disassembly (383,975 lines) |
| `evidence/decoded-strings.tsv` | All payload strings recovered by abstract interpretation |
| `evidence/payload-inventory.tsv` | Sizes of the eight `net.fabric.*` payload classes |
| `evidence/ARCHIVE_INVENTORY.tsv` | Archive entry listing with sizes |
| `evidence/CLASS_NAMES.txt` | All 894 class names |
| `evidence/fabric.mod.json` | Mod metadata / entrypoints |
| `evidence/soberclient.mixins.json`, `evidence/soberclient.features.mixins.json` | Mixin configs |
| `evidence/META-INF_MANIFEST.MF` | Jar manifest (`Main-Class`) |
| `evidence/README.txt` | Notes from the RE package |
| `../../tools/decode_strings.py` | String deobfuscator for this family |

---

## 11. Open questions / next steps

- Recover and analyse the stage-2 `main.py` stealer (requires the live C2, which rotates).
- Resolve the Polygon contract's current C2 value and enumerate past values from chain history.
- Identify the actor(s) behind `soberclient.xyz` / the Discord (`discord.gg/soberclient`) and the
  Supabase project.
- Check for other mods distributed by the same actor using the same `net.fabric.*` loader.
