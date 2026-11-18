# Case 018 — `mavi-chivas.exe` infostealer disguised as a Minecraft mod (OSINT, with sample hash)

- **Verdict:** **confirmed malicious** (public sandbox analysis; sample hash published)
- **Category:** Windows infostealer disguised as a Minecraft mod, delivered via Discord → Dropbox
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary source:** `guisosi/mavi-chivas` —
  https://github.com/guisosi/mavi-chivas (full Portuguese technical report reproduced in
  `evidence/source-readme.md`; analysed on ANY.RUN, revised 2026-11-15)

---

## 1. Verdict

`mavi-chivas.exe` (SHA-256 `29269FA6…1946F4`) is a **Windows infostealer** presented to the victim
as a **"Minecraft mod"** and shared in a **Discord server**. It is not a `.jar` at all — it is a
native `.exe` — but it belongs in this catalogue because the **lure, the target and the victim
population are identical** to the client-stealer campaign (Cluster A / cases 011–016): a Minecraft
player is tricked into running a "mod" that steals their credentials.

**Documented from a published sandbox analysis.** We did not obtain or run the sample. The full
report is preserved in `evidence/source-readme.md`.

## 2. Sample metadata (from the source)

| Field | Value |
|---|---|
| File name | `mavi-chivas.exe` |
| SHA-256 | `29269FA6CF19C9114B15D13044B64903C33BC382007EC85FAD8A5277711946F4` |
| Vector | file presented as a Minecraft mod, shared on Discord |
| Delivery host | Dropbox (`hxxps://www.dropbox[.]com/scl/fi/zprhs69…/mavi-chivas.exe`) |
| Analysis | ANY.RUN (dynamic sandbox), Defender triage |
| Classification | Malicious — **Infostealer (credential stealer)**, confirmed |

## 3. Behaviour (from the source)

- The victim's **browser (`msedge.exe`) downloads the file from Dropbox** — a "trusted" host rarely
  on blocklists — and the victim runs it.
- Execution goes through `cmd.exe /d /s /c "…"`, spawning **PowerShell six times** with full
  silence flags:
  `Powershell -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -Encoded <base64>`.
- The encoded command carries **two layers of Base64** (UTF-16 → JSON with a `Script` field → the
  second layer).
- PowerShell invokes **`csc.exe` via `Add-Type`** to compile a small **P/Invoke helper** that hides
  the console window (`ShowWindow(..., 0)`) — so the victim sees nothing.
- Confirmed theft of **credential, session-cookie and browser-history data**, followed by a
  **connection to Discord** (C2/exfil).
- String artefacts suggest **Alternate Data Streams (ADS)** and `waitfor`/`write` usage.

> The author's 2026-11-15 revision corrected three earlier mistakes: `csc.exe` compiles only the
> window-hiding helper (not the payload), there is **no** second stage (the Dropbox file *is* the
> sample), and the Discord connection now has network evidence.

## 4. MITRE ATT&CK (from the source)

`T1059.001` (PowerShell), `T1027.010` (command obfuscation — double Base64), plus infostealer
collection/exfiltration behaviours. Detection rules (Sigma) were written for the
PowerShell→`csc.exe` compile and the encoded-bypass pattern (see the source repo).

## 5. Indicators of compromise

See `iocs.csv`. Key items:

```
sha256   29269FA6CF19C9114B15D13044B64903C33BC382007EC85FAD8A5277711946F4
url      hxxps://www.dropbox[.]com/scl/fi/zprhs69ehmegj90tnjdfn/mavi-chivas.exe   (delivery)
process  cmd.exe /d /s /c "Powershell -NoLogo -NonInteractive -NoProfile -ExecutionPolicy Bypass -Encoded <base64>
process  csc.exe /noconfig /fullpaths @"C:\Users\<user>\AppData\Local\Temp\<random>.cmdline
string   ShowWindow(..., 0)   (console hiding via Add-Type P/Invoke helper)
string   waitfor / write / ADS   (string artefacts)
```

## 6. Why it belongs in this catalogue

The Cluster-A campaign and this sample share the **entire social-engineering model**:

| Trait | mavi-chivas (018) | Cluster A / SoberClient |
|---|---|---|
| Lure | "Minecraft mod" in a Discord server | "free client" site/Discord |
| Host | Dropbox (trusted cloud) | `file.garden` (trusted-ish host) |
| Victim | Minecraft player | Minecraft player |
| Goal | browser/session credential theft | Minecraft session token theft |
| Obfuscation | double-Base64 PowerShell | per-class `String[]` + `invokedynamic` |

The lesson is the same: **the file type and host are irrelevant; the "it's a mod, trust me" premise
is the vulnerability.**

## 7. Remediation

- Delete the file; **rotate all credentials** (browser-saved passwords, cookies, any account used in
  that browser) and sign out all sessions.
- Because the stealer connected out to Discord, treat the host as compromised: run a full AV/EDR scan.
- Do not run executables shared as "mods" in Discord — real mods are `.jar` and come from a mod index.

## 8. Caveats

- Single public analysis; not independently re-verified by us.
- Portuguese-language source; the verbatim report is preserved in `evidence/`.
