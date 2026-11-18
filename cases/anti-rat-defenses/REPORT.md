# Case 023 — Defensive ecosystem & scanner recommendations (OSINT)

- **Verdict:** **defensive tooling** — not malware. This case catalogues the community scanners and
  hardening mods a defender should use against the families in cases 001–022.
- **Handling:** OSINT only. **No tool was installed or run** as part of this catalogue.
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)

---

## 1. Why this case exists

Every offensive case in this catalogue (stealers, backdoors, OAuth grabbers) has a matching
**defensive** project. Collecting them in one place makes the catalogue actionable: a reader who
finds a suspicious JAR can scan it, and a player who must run third-party mods can harden their
session token.

## 2. Scanners (static analysis of JARs)

| Tool | Platform | Notes |
|---|---|---|
| `OpticFusion1/MCAntiMalware` | Java | Detects **1000+ malicious plugins**; runs on a server or PC. Recommended companion for the 2022 campaign (case 017). |
| `berrybyte-net/broom` | Go | Walks a server dir for `.jar`s and flags the **2022 Updater** variant (case 017). |
| `0xresetti/JarMalwareScanner` | C#/.NET 8 | Windows GUI; **8 variants** incl. **"DonutSMP Stealer"** (see §4); bytecode parser + auto-clean. |
| `Attackwave/mcrataway` | Python | Static scanner for mods/resourcepacks/datapacks/shaders; pure-Python bytecode analysis. |
| `tinywifi/jarsec` | Claude Code skill | Static **and** dynamic (Docker sandbox) analysis of MC mods. |
| `Kagias/skyblockaddons-trojan-analysis` | YARA | Rule for the EtherHiding family (case 016); see `cases/skyblockaddons-etherhiding/evidence/`. |
| `JusticeClient/End-Decompiler` | — | "Rat Detector / File Analysis" for MC mods, DLL and PE/EXE. |

## 3. Runtime hardening mods (protect the token while playing)

| Mod | What it does |
|---|---|
| `cev-api/TokenProtector` | Sanitises the Minecraft access token so other in-JVM mods can't read it (getter/field/reflection/`Unsafe`/`VarHandle`/`FabricLoader` launch-args). |
| `lyn1dev/AntiRAT` | 7-tier behavioural defense for Fabric 26.x; blocks the four universal stealer vectors (getter, reflection/`Unsafe`, disk scrape, native). |
| `PL-VIP/NMR-No-More-RAT` | Client-side "security firewall" that shows what mods try to do before they do it (1.20.1–26.3). |
| `planetofangels/anti-ssid-stealer` | Prompts to confirm/cancel any access to the access token; encrypts it at rest. |

> These are **best-effort**: all explicitly warn that native code, JNI/JNA, extra Java agents, or
> `Unsafe` can bypass in-JVM controls. They reduce risk; they are not a sandbox.

## 4. The scanners independently confirm our campaign

Two third-party scanners name the **exact campaign** catalogued here:

- `0xresetti/JarMalwareScanner` ships a **"DonutSMP Stealer"** variant signature: *"MC session theft
  via reflection, `getMethod`/`invoke` chains"* — the same technique as cases 001–010.
- The same scanner lists **"Weedhack"** (case 011) and other stealers
  (Niggaware, Bambooware, Silentnet, Microstealer, SilentRaven, STRRAT).

This is **independent corroboration** that the DonutSMP "free client" campaign and its
session-theft technique are **recognised in the wild**, not a one-off.

## 5. Defender checklist (synthesised from cases 001–022)

**Before installing anything:**
- Source mods only from **Modrinth / CurseForge / the author's GitHub releases**; avoid `file.garden`,
  random mirrors, and Discord "free clients".
- Prefer builds with a **verifiable source repo** and a hash you can check.

**To inspect a suspicious JAR (static):**
- Unzip and read `fabric.mod.json`/`mods.toml` for **unexpected extra mixins** and a **name/id
  mismatch** (case 016).
- Look for: root-level `<uuid>.bin` tables, `META-INF/kuznya-integrity-target`, an **all-accepting
  `X509TrustManager`**, a **raw-socket TLS client**, a `Session`/`User` accessor,
  `Class.forName` + `MethodHandles`, and Discord-webhook / paste-site strings (cases 001–010, 014).
- Run a scanner from §2 and the YARA rule from case 016.

**If a suspicious client/mod ran (assume compromise):**
- **Invalidate the Minecraft session** (sign out everywhere / launcher account) and **change the
  Microsoft password** — the token is live until invalidated (cases 011–016, 020).
- **Remove unfamiliar connected apps** in the Microsoft account (revokes OAuth refresh tokens, case 021).
- **Change server login passwords** on cracked servers (case 019) and any password stored in the browser.
- Treat the machine as compromised if a RAT/stage-2 ran (011, 012, 016, 017): scan, and reimage if unsure.

**While playing:**
- Run a token-hardening mod (§3); never complete a Microsoft login for a "client" you didn't seek out.

## 6. Caveats

- Tool quality varies; **absence of a detection is not proof of safety**.
- None of these tools was installed or executed here; capabilities are from their own documentation.
- Scanner variant lists drift as malware evolves.
