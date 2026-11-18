# Cases — account-stealer investigations

This folder holds one directory per investigation. The current focus is **account stealers**
(Minecraft/Microsoft session-token theft, browser credential theft, cookie/token grabbers, and
the droppers/loaders that deliver them).

## Layout

```
cases/
  README.md            <- this index
  _TEMPLATE/           <- copy this to start a new case
    REPORT.md
    case.json
    iocs.csv
  <case-slug>/         <- one directory per case
    REPORT.md          <- the full write-up
    case.json          <- machine-readable summary (status, hashes, C2)
    iocs.csv           <- indicators
    evidence/          <- extracted artefacts, disassembly, configs
    <sample>.zip       <- original artefact as received (if small enough to keep)
raw/                   <- original artefacts as received (large/binary, one per case)
tools/                 <- shared analysis scripts (deobfuscators, parsers)
```

## Starting a new case

1. Create the case folder: `cp -r cases/_TEMPLATE cases/<new-slug>`.
2. Put the original artefact in `raw/` (and/or `cases/<slug>/`).
3. Extract evidence into `cases/<slug>/evidence/`.
4. Fill in `case.json`, `iocs.csv` and `REPORT.md`.
5. Add a row to the index below.

## Index

### Cluster A — DonutSMP "free client" campaign (samples held locally)

| ID | Case | Status | Primary impact | Date | Report |
|----|------|--------|----------------|------|--------|
| 001 | SoberClient | confirmed-malicious | credential-theft, RCE, exfiltration | 2026-11-18 | [cases/soberclient/REPORT.md](soberclient/REPORT.md) |
| 002 | Gooba Client | suspected (high) | credential-theft, RCE, exfiltration | 2026-11-18 | [cases/gooba-client/REPORT.md](gooba-client/REPORT.md) |
| 003 | Frost Client (Mutyx) | suspected (high) | RCE, staged payload delivery, host recon | 2026-11-18 | [cases/frost-client/REPORT.md](frost-client/REPORT.md) |
| 004 | Corz Client | suspected (high) | credential-theft, RCE, exfiltration, native protector | 2026-11-18 | [cases/corz-client/REPORT.md](corz-client/REPORT.md) |
| 005 | dqrkis Client | suspected (high) | credential-theft, account-swap, RCE | 2026-11-18 | [cases/dqrkis-client/REPORT.md](dqrkis-client/REPORT.md) |
| 006 | Fake Pay Mod (fakepay) | suspected (high) | credential-theft, HWID/licence exfiltration, RCE | 2026-11-18 | [cases/fakepay-client/REPORT.md](fakepay-client/REPORT.md) |
| 007 | Krypton Client (KrLoader) | suspected (high) | RCE, native protector, session access | 2026-11-18 | [cases/krypton-client/REPORT.md](krypton-client/REPORT.md) |
| 008 | Radium Client | suspected (high) | credential-theft, config exfiltration, PowerShell | 2026-11-18 | [cases/radium-client/REPORT.md](radium-client/REPORT.md) |
| 009 | Xenon Client | suspected (high) | credential-theft, RCE, exfiltration | 2026-11-18 | [cases/xenon-client/REPORT.md](xenon-client/REPORT.md) |
| 010 | Zyron Client | suspected (high) | credential-theft, webhook exfiltration, RCE | 2026-11-18 | [cases/zyron-client/REPORT.md](zyron-client/REPORT.md) |

### Cluster B — OSINT threat families (no sample held; documented from public research)

| ID | Case | Status | Primary impact | Date | Report |
|----|------|--------|----------------|------|--------|
| 011 | Weedhack Stealer | confirmed-malicious (public) | session/credential theft, RAT, RCE | 2026-11-18 | [cases/weedhack/REPORT.md](weedhack/REPORT.md) |
| 012 | fractureiser (CurseForge 2023) | confirmed-malicious (public) | RCE, staged payload, credential theft | 2026-11-18 | [cases/fractureiser/REPORT.md](fractureiser/REPORT.md) |
| 013 | Mercurial Grabber | confirmed-malicious (public) | session/browser credential theft | 2026-11-18 | [cases/mercurial-grabber/REPORT.md](mercurial-grabber/REPORT.md) |
| 014 | Session-stealer mod PoCs / kits | tooling / proof-of-concept | session-token theft | 2026-11-18 | [cases/mc-session-stealer-poc/REPORT.md](mc-session-stealer-poc/REPORT.md) |
| 015 | DonutSMP client ecosystem (OSINT) | mixed (research) | n/a — ecosystem map | 2026-11-18 | [cases/donutsmp-ecosystem/REPORT.md](donutsmp-ecosystem/REPORT.md) |

> **Cluster B caveat:** these cases were written from **public, third-party research and
> repositories** (OSINT), not from samples we analysed. No Cluster-B sample was downloaded or
> executed. Verdicts are attributed to their original sources and are **not independently
> re-verified** by this project.

## Campaign notes

Cases 001–010 are one distribution campaign ("DonutSMP Clients", `donutsmpclients.com`) and one
Java string-obfuscation framework (per-class encrypted `String[]` + `invokedynamic` bootstrap,
class-keyed `.bin` tables). Common infrastructure:

- Hub: `https://www.donutsmpclients.com` (React SPA) — Discord `https://discord.com/invite/donutsmpclients`
- File host folder: `https://file.garden/auoc8y0R41ISH5sk/` — hosts nine `-1.21.11.jar` clients,
  all analysed (cases 002–010):

  | Site id | Site name | JAR | Case |
  |---|---|---|---|
  | `gooba-client` | Gooba Client | `Goobaclient-1.21.11.jar` | 002 |
  | `frost-client` | Frost Client | `frostclient-1.21.11.jar` | 003 |
  | `corz-client` | Corz Client | `Corzclient-1.21.11.jar` | 004 |
  | `dqrkis-client` | Dqrkis Client | `dqrkisclient-1.21.11.jar` | 005 |
  | `fake-pay` | Fake Pay Mod | `fakepayclient-1.21.11.jar` | 006 |
  | `krypton` | Krypton Client | `kryptonclient-1.21.11.jar` | 007 |
  | `radium` | Radium Client | `radiumclient-1.21.11.jar` | 008 |
  | `xenon` | Xenon Client | `xenonclient-1.21.11.jar` | 009 |
  | `zyron-client` | Zyron Client | `zyronclient-1.21.11.jar` | 010 |

- Shared obfuscation artefacts: root-level `<uuid>.bin` class-key tables; `META-INF/kuznya-integrity-target`
  (Gooba); classes `j` (string table + bootstrap) and `i`/`k` (key state machine / anti-tamper).
- **All nine** ship an all-accepting `X509TrustManager` (TLS bypass) and a custom raw-socket TLS HTTP
  client; most expose a Minecraft `Session` accessor (`class_320`/`field_1726`).
- **Three** bundle the **jvmtp** JVM native protector (a JNI stub that `System.load`s an encrypted
  bytecode blob): Corz (004), Krypton (007), Frost (003).
- See `CAMPAIGN.md` for the consolidated cross-case report and `OSINT.md` for how Cluster B
  relates to Cluster A.

## Conventions

- **Never execute samples on this machine.** Static analysis and read-only network checks only,
  unless a separate isolated sandbox is explicitly arranged.
- Record the **SHA-256** of every artefact as received, before any transformation.
- Keep the report verdict-first and evidence-anchored: every behavioural claim should trace to a
  decoded string, an API call site, or an observed network response.
- Cluster-B (OSINT) cases must state their source and must not claim independent verification.
- Update the project memory (`.openhands/memory/`) with durable findings and reuse `tools/`
  scripts across cases of the same family.
