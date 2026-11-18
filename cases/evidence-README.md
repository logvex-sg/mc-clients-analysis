# Raw data (evidence) index

Original artefacts, verbatim IOC lists and third-party writeups preserved for the **OSINT
(Cluster B)** cases 016–023. Nothing here was executed. Each file names its source repository.

## cases/skyblockaddons-etherhiding/evidence/

| File | What it is |
|---|---|
| `technical-analysis.md` | Full third-party static analysis of `SkyblockAddons-26.2.jar` (execution chain, EtherHiding C2, MITRE mapping, repro steps) |
| `minecraft_etherhiding.yar` | YARA rules (`Minecraft_Fabric_EtherHiding_Stealer_Loader`, `Minecraft_Fabric_Mixin_Class_Bootstrap`) |
| `iocs.csv` / `iocs.json` | Machine-readable IOCs from the source |
| `deob.py` | The source's string-deobfuscator script |
| `integrity.py` | The source's integrity-manifest verifier |

Source: https://github.com/Kagias/skyblockaddons-trojan-analysis

## cases/updater-campaign-2022/evidence/

| File | What it is |
|---|---|
| `broom-main.go` | The `broom` scanner's JAR-walk logic (2022 Updater variant) |

Source: https://github.com/berrybyte-net/broom

## cases/mavi-chivas-infostealer/evidence/

| File | What it is |
|---|---|
| `source-readme.md` | Full third-party (Portuguese) sandbox report for `mavi-chivas.exe` |

Source: https://github.com/guisosi/mavi-chivas

## cases/logger-mod-loginstealer/evidence/

| File | What it is |
|---|---|
| `ExampleMod.java` | Mod entrypoint (registers the chat hook) |
| `log.java` | The credential-theft hook (`/login` → password → Discord webhook) |
| `DiscordWebhook.java` | The exfiltration client |

Source: https://github.com/bush1root/LoggerMod

## cases/lunar-client-stealer/evidence/

| File | What it is |
|---|---|
| `Program.cs` | Lunar Client `accounts.json` stealer (C#) |
| `SkyblockExtras.java` | Forge mod that posts `getSession().getToken()` + IP |
| `server-app.js` | The validating backend (checks stolen tokens against Mojang) |
| `Client.java` | `launcher_profiles.json` scraper (socket exfil) |

Sources: https://github.com/tsukilo/LunarClientTokenStealer ,
https://github.com/Diliard/R.A.T ,
https://github.com/funnydude1668/minecraft-token-stealer

## cases/oauth-token-grabbers/evidence/

| File | What it is |
|---|---|
| `mog-index.js` | Full Microsoft→Xbox→XSTS→Minecraft token-exchange grabber |

Source: https://github.com/polincracy/Minecraft-oAuth-Grabber-MOG-

## cases/anti-rat-defenses/evidence/

| File | What it is |
|---|---|
| `jarmalwarescanner-readme.md` | Third-party scanner README, incl. its variant table naming **"DonutSMP Stealer"** and **"Weedhack"** |

Source: https://github.com/0xresetti/JarMalwareScanner

---

**Provenance note:** these are copies of public files (third-party reports, IOC lists, PoC source).
They are stored to keep the case evidence self-contained and are attributed above. No executable
sample is included, and none was run.
