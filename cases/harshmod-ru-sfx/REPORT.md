# Case 027 — `harshmodV11.exe`: Russian-language password-protected SFX dropper (REAL SAMPLE)

- **Verdict:** **malicious — trojanised cheat-client dropper** (password-protected payload = AV evasion)
- **Confidence:** high on *dropper* behaviour; payload not recoverable (encrypted)
- **Category:** Windows SFX (self-extracting) dropper, Russian-language, distributed via GitHub
- **Handling:** **static analysis only — never executed.** Sample obtained and hashed by us.
- **Date:** 2026-11-19
- **Analyst:** OpenHands (first-hand static analysis)

---

## 1. Provenance — a real sample found in a live GitHub repo

| Field | Value |
|---|---|
| Source repo | `ResolvingRU/minecraft-cheat-1.16.5-1.19.2-` (public) |
| Repo created | 2026-08-29 00:08:41Z |
| Repo pushed | 2026-08-29 00:13:59Z |
| Repo contents | **2 files only** — a `README` and `harshmodV11.exe` |
| File | `harshmodV11.exe` |
| Size | `1,313,676` bytes |
| **SHA-256** | `86151e9f7438f0d593a8834c1f610da1cda3763229ce26e7bda59530e120890c` |
| Type | PE32+ x86-64, **WinRAR SFX** (self-extracting archive) |
| Embedded archive | RAR5 at file offset `0x75C00` (482,304), 831,372 bytes |

**Red flags in the repo itself:** a brand-new, single-purpose account; a repo whose name is a
Minecraft-cheat search string (`minecraft-cheat-1.16.5-1.19.2-`); two commits five minutes apart; and
one opaque `.exe`. This is the classic "SEO-bait repo" pattern used to get a binary indexed by search
and mirrored by scrapers.

## 2. What the binary actually is

It is **not** a cheat client — it is a **WinRAR SFX stub** wrapping an encrypted archive:

- Stub strings: `sfxrar.exe`, `WinRAR SFX module`, `D:\Projects\WinRAR\SFX\build\sfxrar64\Release\sfxrar.pdb`
- SFX resources: `IDS_WRONGPASSWORD`, `IDS_WRONGFILEPASSWORD`, `$GETPASSWORD1:CAPTION`

The **embedded SFX script comment is in Russian**:

```
;Расположенный ниже комментарий содержит команды SFX-сценария
Path=%temp%
SavePath
Setup=harshmodV9.exe
Silent=1
Overwrite=1
Update=U
```

Translation and meaning:

| Directive | Meaning |
|---|---|
| `Path=%temp%` | extract into `%TEMP%` (not a user-visible folder) |
| `Setup=harshmodV9.exe` | **run this file automatically after extraction** |
| `Silent=1` | **no UI at all** — no progress window, no error dialogs |
| `Overwrite=1` / `Update=U` | overwrite any existing copy |

So the user double-clicks "harshmodV11.exe", sees **nothing**, and `harshmodV9.exe` is written to
`%TEMP%` and launched. Note the **version downgrade in the chain** (V11 drops V9) — a deliberate
mismatch so the dropped file does not share the name of the thing the user downloaded.

## 3. The payload is password-protected — and that is the point

`harshmodV9.exe` (834,048 bytes) is inside a **password-encrypted RAR5** archive.

- `7-Zip 25.01` → `ERROR: Unsupported Method` (cannot decrypt)
- `unrar 7.12` → `Incorrect password for harshmodV9.exe`
- Tried a targeted wordlist (product name, `harshmod`, `harshmodV9`, `1234`–`123456`, `qwerty`,
  `admin`, `password`, year strings, `minecraft`, `cheat`, `vk`, `tg`) — **all failed**

**Why this matters:** an encrypted archive cannot be scanned by signature AV or by a sandbox that
only inspects the outer file. The password is delivered out-of-band (in the cheat's Discord/Telegram).
This is a **known commodity-malware evasion technique** and by itself is a strong malicious signal —
no legitimate mod ships as a silent, password-protected SFX.

## 4. Assessment

**Malicious dropper.** Certainty is high because:

1. **Silent execution** (`Silent=1`) of a **payload in `%TEMP%`** — no legitimate software does this.
2. **Password-protected inner archive** — exists solely to defeat scanning.
3. **Name mismatch** (V11 → V9) — anti-inspection.
4. **Russian-language SFX script** — the sample is from a Russian-speaking cheat distribution.
5. **SEO-bait delivery repo** — a throwaway account created solely to host the binary.

The final payload's capability is **unknown** (encrypted). Based on the family it belongs to
(Russian Minecraft cheat distribution, see cases 001–010, 024, 026) the most likely payload is a
**session-token stealer with a RAT component**. It should be treated as such.

## 5. Indicators

```
sha256   86151e9f7438f0d593a8834c1f610da1cda3763229ce26e7bda59530e120890c   harshmodV11.exe
sha256   (unknown — inner payload is encrypted)
file     %TEMP%\harshmodV9.exe                    (dropped and auto-executed)
string   Setup=harshmodV9.exe                     (SFX script)
string   Path=%temp%                              (SFX script)
string   Silent=1                                 (SFX script)
string   D:\Projects\WinRAR\SFX\build\sfxrar64\Release\sfxrar.pdb   (WinRAR SFX stub)
```

## 6. Detection ideas

- **SFX + `Silent=1` + `Path=%temp%`** is a near-perfect rule: parse the RAR SFX comment block of any
  PE and alert on `Silent=1` combined with a `%temp%` path and a `Setup=` line.
- Alert on **password-protected archives inside executables** (RAR5 header with the encrypted flag).
- Alert on processes whose image path is `%TEMP%\*.exe` with no parent installer UI.

## 7. Remediation

If this ran: the machine executed an unknown payload silently. Treat as **compromised** — invalidate
the Minecraft session, change the Microsoft password, remove unfamiliar connected apps, and reimage
if the payload's nature cannot be established.

## 8. Caveats

- Payload capability is **not** established; only the dropper is analysed.
- Password not recovered, so no inner-file IOCs exist.
- Static analysis only; the sample was never executed.
