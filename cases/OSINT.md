# OSINT — how the DonutSMP campaign relates to known Minecraft threats

- **Date:** 2026-11-18
- **Analyst:** OpenHands
- **Scope:** public, third-party research and repositories only. **No OSINT sample was downloaded
  or executed.** Nothing here is independently re-verified; every claim is attributed to a source.

This note places the locally-analysed **Cluster A** (DonutSMP "free client" campaign, cases
001–010) next to **Cluster B** (documented public threat families, cases 011–015).

## 1. The shared asset: the Minecraft session token

Every family here exists to obtain the victim's **Minecraft/Microsoft access token**. A stolen
token is a **live account takeover** — usable without the password or 2FA until it is invalidated.
Case 014 shows the theft is a ~100-line primitive:

```java
Session s = Minecraft.getMinecraft().getSession();
// s.getToken(), s.getUsername(), s.getPlayerID()
```

## 2. Family comparison

| | Cluster A (DonutSMP) | Weedhack (011) | fractureiser (012) | Mercurial (013) |
|---|---|---|---|---|
| Delivery | free "client" JAR (landing page + `file.garden`) | trojanised mod JAR | infected mods/plugins (CurseForge/Bukkit) | standalone grabber (post-exploit) |
| Primary goal | session token | session token → RAT | browser+Discord+MSA theft | multi-target infostealer |
| Obfuscation | per-class `String[]` + `invokedynamic`, `.bin` key tables | JNIC + custom cipher | staged (0→3) | varies per build |
| TLS bypass | all-accepting `X509TrustManager` | (n/r) | (n/r) | (n/r) |
| C2 | encrypted strings; cleartext webhook/paste endpoints | `receiver.cy`, `weedhack.cy`, Ethereum RPC | `files-8ie.pages.dev` + IPs | configurable webhook/bot |
| RAT features | suspected | yes (screen/webcam/keylog) | yes (full RAT, worm) | no (collection only) |
| Status | active campaign | active | historical (2023) | active tooling |

## 3. What Cluster A has in common with all of them

1. **Free mod/client as the lure.** Cluster A copies the feature list of real DonutSMP mods
   (case 015) so the trojan looks familiar.
2. **Obfuscation to survive review.** Cluster A's per-class encrypted `String[]` + `invokedynamic`
   is the same idea as Weedhack's JNIC and fractureiser's staged packing.
3. **A custom/TLS-bypassing HTTP client.** Cluster A ships an all-accepting `X509TrustManager` plus
   a raw-socket TLS client; this is a stealer hallmark (intercept/redirect without CA errors).
4. **Discord-webhook / paste-site exfiltration.** Cluster A modules (zyron, radium) and the PoC
   (case 014) all POST to `discord.com/api/webhooks/...` or a paste service.
5. **Session access.** Cluster A's `class_320`/`field_1726` accessor is the same primitive as the
   PoC and the Weedhack/fractureiser stealers.

## 4. What is specific to Cluster A

- One shared **obfuscation framework** across nine unrelated-looking clients (per-class `String[]`,
  `invokedynamic` bootstrap, class-keyed `.bin` anti-tamper tables) — a single operator/kit.
- **jvmtp native protector** in three of nine (Corz, Krypton, Frost) — not seen in Cluster B.
- A coordinated **distribution site** (`donutsmpclients.com`) and Discord, not a mod index.

## 5. Defensive takeaways

- **Treat the session token as the crown jewel.** If a suspicious client ran, invalidate the
  session (sign out everywhere) and rotate the Microsoft password — clearing the file is not enough.
- **Feature lists prove nothing.** Real and malicious DonutSMP clients share features (case 015);
  decide on code, not marketing.
- **Flag the Cluster-A code signatures** even without hashes: root-level `<uuid>.bin` tables,
  `META-INF/kuznya-integrity-target`, an all-accepting `X509TrustManager`, a raw-socket TLS client,
  and a `Session` accessor.
- **Sources are public**; for current Weedhack/fractureiser/Mercurial signatures use the community
  detectors named in cases 011–013, since IOCs drift.

## 6. Sources (all public)

- Weedhack — https://github.com/0xresetti/Weedhack-Remover
- fractureiser — https://github.com/clrxbl/NekoClient, https://github.com/ilumn/fractureiser-detector
- Mercurial Grabber — https://github.com/DayfallGT/NightfallGT
- Session-stealer PoC — https://github.com/shurp3/MC-Session-Stealer
- TokenProtector (defence) — https://github.com/cev-api/Token-Protector
- DonutSMP ecosystem — https://github.com/pushykiller69-bot/exordium-client, https://github.com/realnnpg/glazed

## 7. Cluster-B expansion (cases 016–023)

A second OSINT pass (also via the GitHub API) added eight more cases and **the key modern data point**:

- **016 — SkyblockAddons "EtherHiding" trojan.** A **Weedhack/Majanito-family** Fabric mod
  (SHA-256 `85ccb86c…`) that resolves its C2 from an **Ethereum smart contract** (`0x9044f576…`,
  selector `0x1f1bd692`) and loads an in-memory stage-2 JAR. This is the **active evolution of the
  exact architecture we inferred in SoberClient (001)** and confirms the Cluster-A threat model.
- **017 — Updater malware (29-09-2022)** — the plugin-backdoor campaign that started the modern
  "Minecraft JAR malware" ecosystem (detectors: `MCAntiMalware`, `broom`).
- **018 — mavi-chivas.exe** — a Windows infostealer disguised as a mod, delivered Discord→Dropbox
  (same social-engineering model as Cluster A, different file type).
- **019 — LoggerMod** — a mod that steals **server login passwords** to a Discord webhook.
- **020 — Lunar Client / launcher token stealers** — PoCs that scrape `accounts.json` /
  `launcher_profiles.json` and **validate stolen tokens** against Mojang.
- **021 — OAuth / Microsoft-token grabbers** — phishing the full Microsoft→Xbox→XSTS→Minecraft
  chain to mint a **refresh token** (persistent takeover).
- **022 — Server backdoor tooling** (OpenBukloit, qqBackdoor, LeuxBackdoor) — the offensive supply.
- **023 — Defensive ecosystem** — scanners and token-hardening mods.

**Independent corroboration (important):** the third-party scanner `0xresetti/JarMalwareScanner`
ships variant signatures literally named **"DonutSMP Stealer"** (described as *MC session theft via
reflection `getMethod`/`invoke`*) and **"Weedhack"**. That is an outside confirmation that the
campaign catalogued here is recognised in the wild.

### Updated family table

| | Cluster A (DonutSMP) | EtherHiding (016) | fractureiser (012) | mavi-chivas (018) | OAuth (021) |
|---|---|---|---|---|---|
| Entry | free client JAR | trojanised mod JAR | infected mod/plugin | fake "mod" `.exe` | phishing MS login |
| Prize | session token | session token | browser/Discord/MSA | browser credentials | **refresh token** |
| C2 | encrypted strings | **Ethereum dead-drop + DoH** | `files-8ie.pages.dev` | Discord | collector site |
| Persistence | (loader) | in-memory stage-2 | systemd/.bat | none | refresh token |

The through-line is unchanged: **a Minecraft player is tricked into running or authorising something,
and a live account token is stolen.** Case 016 shows the technique is now **blockchain-hardened**,
which is why detection must be **behavioural** (session access + outbound C2), not domain-based.
