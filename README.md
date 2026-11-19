# mc-clients-analysis

Malware analysis case files. Current focus: **Minecraft account stealers** (Minecraft/Microsoft
session-token theft, browser credential grabbers, server-plugin backdoors, and the droppers/loaders
that deliver them). **33 cases**: 14 analysed first-hand from samples or bytecode (Cluster A) and 19
written from public OSINT (Cluster B).

## Layout

```
cases/     per-case reports, IOCs and evidence  (see cases/README.md for the index)
raw/       original artefacts as received (large/binary, one per case)
tools/     shared analysis scripts (deobfuscators, parsers)
```

## Cases

| ID | Case | Status | Report |
|----|------|--------|--------|
| 001 | SoberClient | **confirmed-malicious — SilentNet build** | [cases/soberclient/REPORT.md](cases/soberclient/REPORT.md) |
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

## OSINT cases (public threat families, no sample held)

| ID | Case | Status | Report |
|----|------|--------|--------|
| 011 | Weedhack Stealer | confirmed-malicious (public) | [cases/weedhack/REPORT.md](cases/weedhack/REPORT.md) |
| 012 | fractureiser (CurseForge 2023) | confirmed-malicious (public) | [cases/fractureiser/REPORT.md](cases/fractureiser/REPORT.md) |
| 013 | Mercurial Grabber | confirmed-malicious (public) | [cases/mercurial-grabber/REPORT.md](cases/mercurial-grabber/REPORT.md) |
| 014 | Session-stealer mod PoCs / kits | proof-of-concept | [cases/mc-session-stealer-poc/REPORT.md](cases/mc-session-stealer-poc/REPORT.md) |
| 015 | DonutSMP client ecosystem | mixed (context) | [cases/donutsmp-ecosystem/REPORT.md](cases/donutsmp-ecosystem/REPORT.md) |
| 016 | SkyblockAddons EtherHiding trojan | confirmed-malicious (public, hash) | [cases/skyblockaddons-etherhiding/REPORT.md](cases/skyblockaddons-etherhiding/REPORT.md) |
| 017 | Updater malware (2022 plugin campaign) | confirmed-malicious (public) | [cases/updater-campaign-2022/REPORT.md](cases/updater-campaign-2022/REPORT.md) |
| 018 | mavi-chivas infostealer (fake mod) | confirmed-malicious (public, hash) | [cases/mavi-chivas-infostealer/REPORT.md](cases/mavi-chivas-infostealer/REPORT.md) |
| 019 | LoggerMod (login-password stealer) | confirmed-malicious (public, source) | [cases/logger-mod-loginstealer/REPORT.md](cases/logger-mod-loginstealer/REPORT.md) |
| 020 | Lunar Client / launcher token stealers | confirmed-malicious (public, source) | [cases/lunar-client-stealer/REPORT.md](cases/lunar-client-stealer/REPORT.md) |
| 021 | OAuth / Microsoft-token grabbers | confirmed-malicious (public, source) | [cases/oauth-token-grabbers/REPORT.md](cases/oauth-token-grabbers/REPORT.md) |
| 022 | Server backdoor tooling | dual-use tooling | [cases/mc-backdoor-plugins/REPORT.md](cases/mc-backdoor-plugins/REPORT.md) |
| 023 | Defensive ecosystem (scanners/hardening) | defensive tooling | [cases/anti-rat-defenses/REPORT.md](cases/anti-rat-defenses/REPORT.md) |
| 024 | SilentNet stealer (Zig loader → Fabric mod) | confirmed-malicious | [cases/silentnet-stealer/REPORT.md](cases/silentnet-stealer/REPORT.md) |
| 025 | Exastealer v1.0 (ElriaGame.exe) | confirmed-malicious (public, hashes) | [cases/exastealer-elriagame/REPORT.md](cases/exastealer-elriagame/REPORT.md) |
| 026 | Optimizer / Weedhack (Fabric mod) | confirmed-malicious | [cases/optimizer-weedhack/REPORT.md](cases/optimizer-weedhack/REPORT.md) |

Cases 011–026 were written from **public research** (no sample obtained or executed) and are
**not independently re-verified**. See [cases/OSINT.md](cases/OSINT.md) for how they relate to the
DonutSMP campaign.

## Key finding — one backend behind many "brands"

Cases **001**, **024** and **026** are the same operation. They share operator-chosen constants
(a Polygon contract, an `eth_call` selector, a fallback domain and CDN path), so the match is proof
of common authorship rather than resemblance:

- **SoberClient** (001) and **SilentNet** (024) are byte-identical on **five** independent constants
  (`0x9c0a5073…ff9a`, `0xce6d41de`, `thisisafalsepositive.st`, `/cdn/v2/9f4e7a2c1b8d.png`,
  `/shard/prefireMc` + `/shard/submitMinecraftLog`). They differ only in campaign UUID — i.e. one
  codebase resold under two names.
- **Optimizer** (026) uses the same pattern on a different chain (Solana devnet) with the same
  fake-Microsoft staging-directory trick.

Blockchain dead-drop C2 now appears on **three chains** in this set: Ethereum (016), Polygon
(001/024), Solana (026). The purpose is the same each time — rotate C2 without re-releasing the
malware, and without owning a domain that can be seized.

## The "it works, so it's not malware" fallacy

The most common defence of these clients is that the cheat features genuinely work. They often do.
SoberClient is the clearest example: 886 classes of real cheat code, and 8 classes (0.39 % of the
file) of SilentNet stealer. **No cheat class references the payload.** The stealer is wired in
through Fabric's `main` entrypoint and the JAR manifest, so it runs on every launch regardless of
which cheat features the user touches. A functional client is the delivery mechanism, not evidence
of good faith.

## Rules of engagement

- **No execution of samples on this machine.** Static analysis and read-only network checks only,
  unless a separate isolated sandbox is arranged.
- Hash every artefact (SHA-256) before transforming it.
- Keep reports verdict-first and anchored to evidence (decoded strings, call sites, responses).
