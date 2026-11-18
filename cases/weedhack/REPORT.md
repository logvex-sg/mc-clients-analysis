# Case 011 — Weedhack Stealer (OSINT)

- **Verdict:** **confirmed malicious** (public attribution; not independently re-verified here)
- **Category:** Minecraft-targeting session/credential stealer → staged RAT, delivered as a
  trojanised Fabric mod
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `0xresetti/Weedhack-Remover` — https://github.com/0xresetti/Weedhack-Remover
  - Original reporting on the "Weedhack" Minecraft stealer (multiple community write-ups, 2024–2025)

---

## 1. Verdict

Weedhack is a **real, actively-distributed malicious Minecraft mod** whose purpose is to steal the
victim's Minecraft/Microsoft account **session token** (full account takeover), then escalate to a
full remote-access trojan (RAT) with screen/webcam/keylogger capability. It is the closest known
analogue to the DonutSMP campaign in Cluster A: a "free client/mod" that hides a loader/stealer and
exfiltrates the live session.

**This case is documented from public research.** We did not obtain or run a sample. Treat every
technical claim as **attributed to the source above**, pending independent verification.

## 2. Distribution

- Delivered as a **trojanised Minecraft Fabric mod** (`.jar`) — same delivery vector as Cluster A.
- Publicly documented variants v1 → v4+; the v4+ generation moved to **Ethereum-RPC / blockchain
  C2** for config retrieval and in-memory payload execution.

## 3. Capabilities (as documented by the source)

| Capability | Detail |
|---|---|
| Session theft | Steals the Minecraft **session/access token** (account takeover) |
| Credential theft | Browser credential/token grabber behaviours reported |
| RAT features | `KeyLoggingHandler`, `WebcamShareHandler`, `ScreenShareHandler` |
| Screen/webcam | screen-share via WebSocket (WebP encoded); webcam stream ~25 FPS |
| Keylogging | keylogger data exfiltrated over Socket.IO |
| Persistence | scheduled task `JavaSecurityUpdater` (`schtasks /Create /SC ONLOGON /RL HIGHEST`); VBScript launcher in a hidden window; `HKCU\...\CurrentVersion\Run` |
| AV evasion | `Add-MpPreference -ExclusionPath 'C:\Users'` (PowerShell Defender exclusion) |
| Privilege escalation | `ShellExecute` with the `runas` verb (UAC bypass) |
| C2 fallback | Ethereum RPC calls retrieve an RSA-signed C2 config from the blockchain (`0xce6d41de`) |

## 4. Obfuscation

- Framework name reported as **JNIC** + custom string encryption.
- String algorithm described as **"interleave-xor-rotate-substitute"**, keys `[187, 67]`, decodable.
- Loader classes: `dev.jnic.BSOMwJ` (stage 2 module), `dev.jnic.fwcMeR` (stage 3 RAT),
  `dev.jnic.lXpXvp` (stage 1 v2). v4+ variants add **Greek-Unicode** entrypoint obfuscation and
  **ClassLoader abuse**.

## 5. Indicators of compromise (from the source)

### Network

```
receiver.cy
weedhack.cy
https://receiver.cy/files/jar/module
https://receiver.cy/files/jar/component
https://receiver.cy/api/component/lastModified
```

### Ethereum (blockchain C2)

```
function selector: 0xce6d41de
RSA public key (v2 stage 1):
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtmNzDf4737/iYWvscWg6vQg9dHa/yUchfQY9r5htNTLZ3ZDAbqrzN93I0ctZHa27oRnkpB7XpowI4NH8eIRmaMThggpTYRXzHzLvUjhyrFFPkIOo/HI1gZF5IV7/XmvYWqgEsSpxl0iesOUlaWO5A8QlTu0QLsZAzZtzZyLj/v1XbPT02rTvZkuRhE6nzpUR4GN3Jp4Bn8zQAWdFDe17PWZxOi19uUTMPzgFj9n3h7DprwBmE3fR7IMsbiFacAoSHfqkTpEwY7A8ArK1DQ1yJXPog/PQ4aTU9gU38WC20wtct796ImZiuRYdNWcSzHda5ZbvZdvpw6RHh0zQqGVhRQIDAQAB
```

### Filesystem

```
%TEMP%\lib*.tmp
%APPDATA%\Microsoft\SecurityUpdates
%APPDATA%\Microsoft\SecurityUpdates\SecurityInfo.json
%APPDATA%\Microsoft\SecurityUpdates\Updater.vbs
%APPDATA%\Microsoft\SecurityUpdates\component-*.jar
%APPDATA%\Microsoft\SecurityUpdates\security.lock
/dev/jnic/lib/a125e430-2459-4702-9797-49fce5f280ae.dat
/dev/jnic/lib/c4f763d6-e34c-42e9-bba1-b80cfa5a55df.dat
```

### Host artefacts

```
scheduled task : JavaSecurityUpdater
registry key   : HKCU\Software\Microsoft\Windows\CurrentVersion\Run
```

### Class / string indicators

```
me/mclauncher/LoaderClient        me/mclauncher/StagingHelper
me/mclauncher/RPCHelper           me/mclauncher/MEntrypoint
dev/majanito/Main                 dev/majanito/handlers
dev/majanito/security             initializeWeedhack
WeedhackFile                      WeedhackLog
dev/jnic/BSOMwJ                   dev/jnic/fwcMeR
dev/jnic/lXpXvp                   $jnicLoader
JavaSecurityUpdater               SecurityUpdates
KeyLoggingHandler                 WebcamShareHandler
ScreenShareHandler                0xce6d41de
Add-MpPreference -ExclusionPath   schtasks /Create
component-                        cfg.json
SecurityInfo.json                 Updater.vbs
```

## 6. Relationship to Cluster A (DonutSMP campaign)

Shared TTPs with the samples we analysed locally:

| Trait | Weedhack (OSINT) | Cluster A (DonutSMP) |
|---|---|---|
| Delivery | trojanised Fabric `.jar` | trojanised Fabric `.jar` |
| Objective | Minecraft session/access token | Minecraft session/access token |
| String obfuscation | JNIC + custom cipher | per-class `String[]` + `invokedynamic` |
| Class-loader abuse | yes (`$jnicLoader`, ClassLoader abuse) | yes (`Class.forName`, `MethodHandles`) |
| In-memory / staged payload | yes (stage 1 → 2 → 3) | suspected (encrypted C2 strings) |
| TLS bypass | (not reported) | all-accepting `X509TrustManager` |

This is a **different malware family** from the DonutSMP samples (different obfuscator, different
C2), but it targets the **same asset** (Minecraft session tokens) through the **same delivery
pattern** (free mod/client). It is included as a reference threat for defenders triaging
"free client" JARs.

## 7. Detection & remediation (as published)

The public remover tool (`0xresetti/Weedhack-Remover`) detects:

- running `javaw.exe` processes associated with the loader;
- infected JARs across 11 common MC launcher mod folders;
- new variants by constant-pool analysis (Greek-Unicode entrypoints, session-token-theft patterns,
  XOR obfuscation, ClassLoader abuse);
- old variants by filesystem indicators (`SecurityUpdates` folder, scheduled tasks, registry keys).

Remediation: kill the process, remove the artefacts, **invalidate the Minecraft launcher account**
to revoke the stolen session token, and — because the payload is a full RAT — **restore from backup
or reset the machine** (other implants may have been dropped).

## 8. Caveats

- All technical detail is **second-hand** (public tooling/docs); not independently re-verified.
- IOCs can drift as the malware evolves; v4+ changes infrastructure and obfuscation.
- No file hashes are published here because no sample was obtained.
