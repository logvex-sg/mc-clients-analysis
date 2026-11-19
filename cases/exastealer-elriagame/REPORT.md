# Case 025 — Exastealer v1.0 ("ElriaGame.exe"): Electron-wrapper Java stealer with UAC bypass

- **Verdict:** **confirmed malicious** — commercial-grade Java infostealer
- **Category:** Java infostealer (browser + wallet + Discord + Steam) delivered as a fake game
- **Handling:** OSINT — **source read, never executed.** No binary obtained.
- **Date:** 2026-11-19
- **Analyst:** OpenHands (open-source intelligence)
- **Primary source:** `fire/elriagame-exastealer-analysis` (https://github.com/fire/elriagame-exastealer-analysis)
- **Evidence:** `evidence/source-readme.md`, `evidence/IOCS.md`, `evidence/HASHES.txt`,
  `evidence/JAR_INTERNALS.md`

---

## 1. What it is

`ElriaGame.exe` presents itself as a game launcher. It is a **delivery wrapper for Exastealer v1.0**,
a Java infostealer that self-labels its family in the JAR manifest.

The engineering is unusually high quality for a Minecraft-adjacent stealer: a four-layer obfuscated
JavaScript dropper inside an Electron `app.asar`, an AES-encrypted payload archive, a **private JRE**
so it does not depend on the host's Java, JNA bindings into **DPAPI / NCrypt / NSS / SQLite**, and a
**COM elevation-moniker UAC bypass** in a native DLL. This is not a script kiddie kit.

## 2. Delivery chain (four stages before the payload runs)

```
ElriaGame.exe                       NSIS SFX, 146,758,741 bytes
└─ $PLUGINSDIR/
   ├─ nsis7z.dll, StdUtils.dll, System.dll     (stock NSIS plugins)
   └─ app-64.7z                               (electron-builder payload)
      └─ ElriaGame.exe (Electron shell, 235 MB), Chromium DLLs, resources.pak, locales/
         └─ resources/
            ├─ app.asar           62 KB — only {main: launcher1.js, dep: 7zip-bin}
            ├─ 7za.exe, 7za-ia32.exe          (bundled 7-Zip CLI)
            ├─ elevate.exe                    (UAC helper)
            └─ data.7z                        (7zAES, password 7zgw3s6kxCZi)
               ├─ emre.jar   42,593,204 bytes  ← Exastealer
               └─ jre.zip                       (private JRE)
```

The dropper is a single-line base64 blob plus a decoder that `eval`s its own output, **repeated four
times**:

```js
function decode(d, k, r) {
  var b = Buffer.from(d, 'base64');
  var kb = Buffer.from(k, 'utf8');
  for (var i = 0; i < b.length; i++)
    b[i] = (((b[i] - r - i) ^ kb[(i + r) % kb.length]) & 0xFF);
  return b.toString('utf8');
}
eval(decode(_blob, _key, _rot));
```

**Note the pattern:** `(byte - rot - index) ^ key[index % len]`. Subtract-by-index **and** XOR-by-index
together defeat naive base64/XOR decoders. The archive password `7zgw3s6kxCZi` is hard-coded in the
stage-4 script — the same "encrypted inner archive" evasion seen in case 027.

## 3. Stealth engineering

| Behaviour | Implementation |
|---|---|
| Invisible window | `0×0`, `skipTaskbar: true` |
| Prevents quit | Electron `close` handler vetoed |
| Self-respawn | relaunches itself with `_HIDDEN_MARKER=1` |
| Restart budget | `_RESTART_COUNT` 0→1→2, then stops (avoids crash-loop detection) |
| Detached child | `javaw.exe` with `windowsHide: true`, `detached: true` |
| No host Java needed | ships `jre.zip` |
| Logs that persist | `%TEMP%\launcher_debug.log` appended and **never deleted** (analyst gold) |

## 4. Capability set — the most complete in this database

Native access via **JNA** (`peynir.dll`, `MyCrypt32`):

| Target | Technique |
|---|---|
| Chromium **master key** | `CryptUnprotectData` (**DPAPI**) then **NCrypt** unwrap |
| Chromium **cookies / logins** | direct **SQLite** reads of the profile DBs |
| **Firefox** credentials | **NSS** `PK11SDR_Decrypt` |
| **Steam** session | Steam config / token files |
| **Discord** token | local storage + leveldb, then **validated live** against the API |
| **Browser wallets** | **61** extension IDs enumerated |
| **Desktop wallets** | **48** wallet applications |
| **UAC bypass** | COM elevation moniker → `ICMLuaUtil::ShellExec` |

### The UAC bypass (in `peynir.dll`) — worth recording in detail

```
moniker  Elevation:Administrator!new:{3E5FC7F9-9A51-4367-9063-A120244FBEC7}   (CMSTPLUA)
IID      {6EDD6D74-C007-4E75-B76A-E5740995E24C}                                (ICMLuaUtil)
```

Both GUIDs are **XOR-assembled at runtime** from three 16-byte blobs at
`.rdata:0x180018b80`, `0x180018b90`, `0x180018bb0`, so a string scan finds nothing. The
`Elevation:Administrator!new:` prefix *is* cleartext at `.rdata:0x180018b40`.

It then rewrites the **PEB `CurrentDirectory`/`DllPath`** and the loader entry's `FullDllName` to
point at `C:\Windows\System32\`, calls `CoGetObject(<moniker>, &BIND_OPTS3, &IID_ICMLuaUtil, &ppv)`
and invokes **vtable slot `+0x48`** (`ShellExec`). This is the well-known "ICMLuaUtil" bypass, but
implemented cleanly, with the CLSID hidden.

**Forensic side-effect:** the spoofed PEB path is **left in place** after the elevated call returns —
so a medium-integrity process whose PEB `CurrentDirectory` reads `C:\Windows\System32\` while its
image lives in `%LOCALAPPDATA%` is a strong detection.

## 5. C2

```
http://52.249.219.108:3001                       plain HTTP, non-standard port
POST /api/validate-tokens                        key + Discord-token validation
POST /api/discord-injection/<KEY>                Discord renderer IPC/injection callback
POST /api/internal/log                           progress/error logging
GET  https://discord.com/api/v10/users/@me       validate stolen token (live)
GET  https://canary.discord.com/api/v9/users/@me validate canary token (live)
build key  PANEL-XSER-YZ76-YFMK                  (PLhWEEjyn.ENCRYPTED_KEY)
```

Bundled clients (`okhttp3`, Apache HttpClient 5, `java-websocket`) mean **WebSocket exfil** is likely
in addition to HTTP POST.

**This is a real panel with a build key** — i.e. a **licensed MaaS product**, not a one-off. The
`<KEY>` in the URL path identifies the customer's build, which is how the operator bills.

## 6. Turkish-language authorship

`emre` is a Turkish given name; `peynir` is Turkish for "cheese"; the analysis source is Turkish. The
same locale appears in **SilentNet** (case 024) and the DonutSMP campaign (cases 001–010). This is
consistent with a **Turkish-speaking MaaS cluster** supplying Minecraft-adjacent stealers.

## 7. Indicators

```
sha256    319acc0f884b20f8c36c03912996c98f7860abf99d39e7492775c0320ae9e00d   ElriaGame.exe (NSIS)
sha256    eade28260b381d60087d3f6d045990dd1465cb329ff06882b480a01edbddbabc   $PLUGINSDIR/app-64.7z
sha256    33f0ac160b6807c7e70015a148ac4ddfe89341c7a8b8454234afc3c3b2060712   emre.jar (Exastealer)
sha256    9ec1331b90ce86a584320e9922a4fd1ed5afc2dce884d3ea8189c57026b8c9f7   emre.jar!/peynir.dll
ip        52.249.219.108:3001
url       http://52.249.219.108:3001/api/validate-tokens
url       http://52.249.219.108:3001/api/discord-injection/<KEY>
url       http://52.249.219.108:3001/api/internal/log
string    PANEL-XSER-YZ76-YFMK
string    7zgw3s6kxCZi                                   (data.7z password)
file      %LOCALAPPDATA%\emre\emre.jar
file      %LOCALAPPDATA%\emre\jre.zip
file      %LOCALAPPDATA%\emre\data.7z
file      %LOCALAPPDATA%\emre\data_helper.exe
registry  HKCU\...\Run\emre
string    _HIDDEN_MARKER / _RESTART_COUNT
string    Elevation:Administrator!new:{3E5FC7F9-9A51-4367-9063-A120244FBEC7}
string    {6EDD6D74-C007-4E75-B76A-E5740995E24C}
```

## 8. Detection ideas (generalised — these catch more than this sample)

1. `HKCU\Run` value whose data ends in `emre.jar --startup`.
2. `javaw.exe` whose parent is an Electron app under `%LOCALAPPDATA%`.
3. Any outbound traffic to `52.249.219.108:3001`.
4. Elevated `ICMLuaUtil` COM instantiation whose parent is medium-integrity `javaw.exe`.
5. **PEB `CurrentDirectory` = `C:\Windows\System32\` while image path is under `%LOCALAPPDATA%`.**
6. **Pattern rule:** signed-looking installer + bundled `7za.exe` + password-protected `data.7z`
   + Electron shell = stealer delivery. This generalises well beyond ElriaGame.

## 9. MITRE ATT&CK

`T1204.002`, `T1036.005`, `T1027`, `T1140`, `T1547.001`, `T1555.003`, `T1555.005` (wallet),
`T1539`, `T1528`, `T1550.001`, `T1548.002` (UAC bypass), `T1071.001`, `T1041`, `T1056`.

## 10. Remediation

Fully compromised host. Invalidate Minecraft/Discord/Steam sessions, rotate every browser-saved
password, **move crypto funds out of any wallet that was on the machine** (48 desktop wallets and 61
browser extensions were targeted), check for elevated persistence, and reimage.

## 11. Caveats

- Third-party analysis; not independently re-verified. No binary handled.
- `peynir.dll`'s WebSocket URL was not recovered by the original analyst.
- UAC bypass details are quoted from the source; not re-verified at instruction level.
