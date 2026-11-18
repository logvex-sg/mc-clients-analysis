# malware-analysis-SOBERCLIENT

Malware analysis case files. Current focus: **account stealers** (Minecraft/Microsoft session
token theft, browser credential grabbers, and the droppers that deliver them).

## Layout

```
cases/     per-case reports, IOCs and evidence  (see cases/README.md for the index)
raw/       original artefacts as received (large/binary, one per case)
tools/     shared analysis scripts (deobfuscators, parsers)
```

## Cases

| ID | Case | Status | Report |
|----|------|--------|--------|
| 001 | SoberClient | confirmed-malicious | [cases/soberclient/REPORT.md](cases/soberclient/REPORT.md) |
| 002 | Gooba Client | suspected (high) | [cases/gooba-client/REPORT.md](cases/gooba-client/REPORT.md) |
| 003 | Frost Client (Mutyx) | suspected (high) | [cases/frost-client/REPORT.md](cases/frost-client/REPORT.md) |
| 004 | Corz Client | suspected (high) | [cases/corz-client/REPORT.md](cases/corz-client/REPORT.md) |
| 005 | dqrkis Client | suspected (high) | [cases/dqrkis-client/REPORT.md](cases/dqrkis-client/REPORT.md) |
| 006 | Fake Pay Mod (fakepay) | suspected (high) | [cases/fakepay-client/REPORT.md](cases/fakepay-client/REPORT.md) |
| 007 | Krypton Client (KrLoader) | suspected (high) | [cases/krypton-client/REPORT.md](cases/krypton-client/REPORT.md) |
| 008 | Radium Client | suspected (high) | [cases/radium-client/REPORT.md](cases/radium-client/REPORT.md) |
| 009 | Xenon Client | suspected (high) | [cases/xenon-client/REPORT.md](cases/xenon-client/REPORT.md) |
| 010 | Zyron Client | suspected (high) | [cases/zyron-client/REPORT.md](cases/zyron-client/REPORT.md) |

Cases 001–010 are one campaign ("DonutSMP Clients", `donutsmpclients.com` → `file.garden`); all nine
hosted JARs (cases 002–010) have been analysed. See [cases/CAMPAIGN.md](cases/CAMPAIGN.md) for the
consolidated report.

## Rules of engagement

- **No execution of samples on this machine.** Static analysis and read-only network checks only,
  unless a separate isolated sandbox is arranged.
- Hash every artefact (SHA-256) before transforming it.
- Keep reports verdict-first and anchored to evidence (decoded strings, call sites, responses).
