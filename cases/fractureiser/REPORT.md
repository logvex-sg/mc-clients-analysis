# Case 012 — fractureiser (CurseForge / Bukkit malware, June 2023) — OSINT

- **Verdict:** **confirmed malicious** (public attribution; not independently re-verified here)
- **Category:** staged RCE + credential-stealer worm distributed through Minecraft mods/plugins
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `clrxbl/NekoClient` — deobfuscated Stage-3 payload — https://github.com/clrxbl/NekoClient
  - `ilumn/fractureiser-detector` — https://github.com/ilumn/fractureiser-detector
  - Public "fractureiser" incident write-ups (June 2023)

---

## 1. Verdict

**fractureiser** is a real, mass-distributed malware campaign that infected **Minecraft mods and
plugins** on CurseForge and Bukkit in June 2023. It is included here because it is the canonical
example of the **"malicious mod that steals Minecraft/browser credentials"** threat class that
Cluster A belongs to.

**This case is documented from public research and the published deobfuscated Stage-3 repository.**
We did not obtain or run a sample.

## 2. Architecture (four stages)

| Stage | Role | Where it lives |
|---|---|---|
| Stage 0 | Infected mod/plugin JAR (trampoline) | the file the user downloads |
| Stage 1 | Dropper — writes/executes Stage 2 | a modified class inside the mod |
| Stage 2 | Downloader — fetches Stage 3, installs persistence | dropped to disk |
| Stage 3 | Full payload — "NekoClient" RAT | `%APPDATA%` / user config dir |

## 3. Stage-3 capabilities (observed directly in the public deobfuscated repo)

The Stage-3 payload in `clrxbl/NekoClient` is a feature-complete RAT. Package layout (evidence):

```
dev/neko/nekoclient/api/stealer/browser/...      browser cookie + credential theft
dev/neko/nekoclient/api/stealer/browser/impl/decrypt/chrome/ChromeDecryptor.java
dev/neko/nekoclient/api/stealer/browser/impl/decrypt/mozilla/MozillaDecryptor.java
dev/neko/nekoclient/api/stealer/discord/...      Discord account/token theft
dev/neko/nekoclient/api/stealer/msa/...          Microsoft / Minecraft (MSA) account theft
dev/neko/nekoclient/module/impl/CryptoClipperModule.java   crypto wallet clipper
dev/neko/nekoclient/api/windows/vmescape/VMEscape.java     VM/sandbox escape
dev/neko/nekoclient/api/ddos/...                 DDoS engine (HTTP/HTTPS/SSL/Minecraft)
dev/neko/nekoclient/packet/...                   C2 protocol (client/server packets)
dev/neko/nekoinjector/template/impl/FabricModTemplate.java
dev/neko/nekoinjector/template/impl/ForgeModTemplate.java
dev/neko/nekoinjector/template/impl/SpigotPluginTemplate.java
dev/neko/nekoinjector/template/impl/BungeecordPluginTemplate.java
dev/neko/nekoinjector/template/impl/MinecraftClientTemplate.java
```

Highlights:

- **Browser theft:** Chrome + Firefox cookie/credential databases, with per-browser decryptors
  (Chrome DPAPI/`Local State`; Mozilla NSS via a JNA `NSSLibrary`).
- **Discord theft:** account tokens (`DiscordStealer`).
- **Microsoft/Minecraft theft:** full MSA → Xbox Live → XSTS → Minecraft auth chain
  (`MicrosoftAuth`, `XSTSCredentials`, `XboxLiveCredentials`, `MinecraftCredentials`,
  `MinecraftProfile`) — i.e. **complete account takeover**.
- **Crypto clipper:** replaces copied wallet addresses (`CryptoClipperModule`).
- **Worm/injector:** re-infects other mods/plugins by template (Fabric/Forge/Spigot/Bungee),
  which is what made the campaign spread.
- **DDoS engine** and **VM escape** are secondary capabilities.

## 4. Indicators of compromise (reported)

### Network (publicly reported C2)

```
https://files-8ie.pages.dev/...     (Stage-2/Stage-3 delivery host)
85.217.144.130
111.90.150.186
107.189.3.101
```

### Stage-0 detection (per fractureiser-detector)

- Specific injected class names/hashes inside otherwise-legitimate mods.
- Presence of the Stage-0 loader class and its marker strings.
- Modified `META-INF/MANIFEST.MF` referencing the injected class.

### Host artefacts (reported)

- Stage-2 downloaded to the user's data directory and executed.
- Windows: a `.bat`-based second stage; Linux: a `systemd`-based persistence unit.

> Exact hashes vary by infected mod; use `fractureiser-detector` (Java/Python/Node) for current
> signatures rather than a static list.

## 5. Relationship to Cluster A (DonutSMP campaign)

| Trait | fractureiser (OSINT) | Cluster A (DonutSMP) |
|---|---|---|
| Delivery | infected mod/plugin JAR | free "client" JAR |
| Objective | browser + Discord + Minecraft MSA theft | Minecraft session theft |
| Payload | staged downloader → RAT | obfuscated loader + encrypted C2 |
| Steals MSA/Minecraft auth | yes (full chain) | yes (Session accessor) |
| Crypto clipper | yes | not observed |
| Worm behaviour | yes (mod re-infection) | not observed |

Same **threat class** (malicious Minecraft JAR stealing accounts), different family. It is the
reference case for **why "it's just a mod" is not a safety argument**.

## 6. Remediation (as published)

- Scan mod folders with `fractureiser-detector` (recursive Python mode) and remove hits.
- Update launchers that were patched to neutralise the loader.
- Because Stage-3 steals **browser + Discord + Microsoft/Minecraft** credentials, **rotate all of
  them**: log out/invalidate Minecraft session, change Microsoft password, invalidate Discord
  tokens, and change any passwords stored in browsers.
- Restore from backup / reimage if Stage 3 ran (it is a full RAT with persistence).

## 7. Caveats

- Second-hand detail; not independently re-verified.
- The Stage-3 repo is the attacker's own (accidentally published) code — treat it as authoritative
  for *capability* but not for the full list of victims/C2.
