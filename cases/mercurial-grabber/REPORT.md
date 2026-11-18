# Case 013 — Mercurial Grabber (OSINT)

- **Verdict:** **confirmed malicious** (public; widely distributed infostealer; not independently re-verified here)
- **Category:** multi-target infostealer with a **Minecraft session stealer** module
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `DayfallGT/NightfallGT` ("Mercurial Grabber v1.03") — https://github.com/DayfallGT/NightfallGT
  - Mirror forks: `Tysoefy/Mercurial-Grabber1`, `Spofxyy/Mercurial.Grabber.v1.03`,
    `slashgrabps/Download-Mercurial-Builder`

---

## 1. Verdict

**Mercurial Grabber** is a publicly-circulated Windows infostealer ("grabber") distributed as a
builder + payload. It is notable for the same reason Weedhack and Cluster A matter: it explicitly
targets **Minecraft session tokens** in addition to browser and messaging credentials. It is a
**generic** grabber (not mod-delivered), but it is the tooling class a "free client" attacker may
drop once they have execution.

**This case is documented from public repositories.** We did not obtain or run a sample.

## 2. Reported change history (from the published README)

- v1.03: "Fixed Minecraft session stealer bug"; "Added exception handling in grabbing token
  directory"; "Exception handling in detecting environment".

The presence of a **Minecraft session stealer module** and a **token directory** routine is the
reason this family is catalogued here.

## 3. Typical capabilities (infostealer class)

| Target | What is stolen |
|---|---|
| Minecraft | **session token / access token**, UUID, username |
| Browsers | saved passwords, cookies, autofill, cards (Chrome/Edge/Brave/Firefox/Opera) |
| Discord | account token(s), including multi-client |
| Other apps | (varies by build) FTP/Steam/Telegram and more |

- Output is packaged (zip) and sent to a **Discord webhook** or Telegram bot — the same
  exfiltration pattern seen in the Cluster-A modules and the PoC in case 014.

## 4. Why it matters for Minecraft players

A stolen **Minecraft session token** is a **live account takeover**: it can be used without the
password and without 2FA, until it expires or is invalidated. Infostealers like Mercurial harvest
these tokens alongside browser cookies, so a single infection can compromise the game account,
email, and any account whose browser session is present.

## 5. Indicators / detection

Mercurial builds are frequently repackaged, so **static strings and hashes drift**. Prefer
behavioural detection:

- Discord-webhook or Telegram exfiltration from a freshly-run `.exe`/`.jar`;
- reads of browser `Login Data`, `Cookies`, `Local State` and Discord `leveldb` directories;
- reads of the Minecraft `launcher_accounts.json` / session token store;
- a "grabber output" zip written to `%TEMP%`.

> No specific hashes are published here because no sample was obtained.

## 6. Relationship to Cluster A

Same **asset** (Minecraft session token) and same **exfil channel** (Discord webhook / HTTP POST).
Difference: Mercurial is a **standalone grabber** used post-exploitation, whereas the Cluster-A
samples are **client trojans** that carry the theft in the mod itself. In an intrusion, a
Cluster-A "free client" can be the **initial access** and a Mercurial-style grabber the
**collection/exfil** stage.

## 7. Remediation

- Invalidate the Minecraft session (launcher account) and change the Microsoft password.
- Invalidate Discord tokens and change the password; enable 2FA.
- Change passwords for any accounts saved in the browser; clear cookies.
- Run a reputable AV/EDR scan and restore from backup if unsure.

## 8. Caveats

- Second-hand detail; not independently re-verified.
- The repository is one of several mirrors; capability may differ between builds.
