# Case 015 — DonutSMP client ecosystem (OSINT map)

- **Verdict:** **mixed** — this is an *ecosystem map*, not a single malware verdict. It records the
  public "DonutSMP client" landscape so Cluster A's campaign can be placed in context.
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)

---

## 1. Why map the ecosystem

"DonutSMP" is a popular Minecraft anarchy/SMP server. A large cottage industry of **free and paid
clients/modules** targets it. That is exactly the environment Cluster A exploits: attackers publish
"free DonutSMP clients" and hide stealers in them. Mapping the legitimate/adjacent projects lets a
defender tell a **real** community mod from a **look-alike** trojan.

## 2. Cluster-A lures vs. real projects

The Cluster-A samples use the **same feature vocabulary** as legitimate DonutSMP mods. Observed
legitimate equivalents (public repos):

| Feature in Cluster-A samples | Legitimate project with the same feature |
|---|---|
| Fake Pay (intercept `/pay`, show fake message) | `Exordium Client` — `payment/FakePayManager.java` |
| Fake Scoreboard (client-side sidebar) | `Exordium Client` — `scoreboard/FakeScoreboardManager.java` |
| Fake Spawner visuals | `Exordium Client` — `render/FakeSpawnerRenderer.java` |
| Name Spoofer / NameProtect | `Exordium Client` — `util/NameSpoofUtil.java` |
| DonutSMP automation modules | `Glazed` (Meteor Client addon for DonutSMP) |

`Exordium Client` (public repo `pushykiller69-bot/exordium-client`) is instructive: it implements
**fake pay, fake scoreboard, fake spawners, name spoofer and panic mode** — almost the same feature
set as our `fakepay` (case 006) and `zyron` (case 010) samples. The difference is that a real
project ships **only the features**; the Cluster-A trojans ship the same features **plus** an
obfuscated loader/stealer. Attackers copy the feature list to make the trojan look familiar.

## 3. Public "DonutSMP client" projects observed (for context, not endorsements)

| Project | What it is | Relevance |
|---|---|---|
| `pushykiller69-bot/exordium-client` | Fabric 1.21.1 QoL mod: fake pay/scoreboard/spawner/name spoofer | Feature-set twin of fakepay/zyron |
| `realnnpg/glazed` | Meteor Client addon for DonutSMP | Legitimate-ish automation |
| `Embermagnet97825/Breez-Client-resp` | "Minecraft Client (115 Modules)" (repo name suggests a *response*/analysis) | Ecosystem churn |
| `NinjaBetweenaquarius9355/donutsmp-casino-rig-resp` | DonutSMP casino "rig" mod | Gameplay cheating (not malware per se) |
| `m6rrz/DonutSmp-gamble-Rig` | DonutSMP gambling features | Gameplay cheating |

> The `*-resp` naming on two repos suggests they are **community responses / mirrors** to
> disputed or removed clients — a recurring pattern when a DonutSMP client is accused of being
> malicious.

## 4. The lure pattern (what defenders should expect)

1. A slick **website** (e.g. Cluster A's `donutsmpclients.com`) or a **Discord** invite advertises
   a free client with a feature list copied from real mods.
2. The download points to a **third-party file host** (Cluster A: `file.garden`) rather than a
   reputable index (Modrinth/CurseForge/GitHub releases).
3. The JAR is **obfuscated** (Cluster A: per-class `String[]` + `invokedynamic`; class-keyed `.bin`),
   ships a **TLS-bypass** `X509TrustManager`, and reads the **Minecraft `Session`**.
4. It works as advertised *enough* to be shared, while exfiltrating the session token in the
   background.

## 5. Relationship to Cluster A

This map is the **context** for cases 001–010. It shows the campaign's feature set is
**interchangeable** with legitimate projects — which is precisely why static analysis (not the
feature list) must decide safety. The Cluster-A JARs were confirmed unsafe by their **code**
(obfuscated loader, TLS bypass, `Session` access, `ProcessBuilder`), not by their features.

## 6. Guidance

- Prefer clients/mods from **Modrinth, CurseForge, or the author's own GitHub releases** with a
  verifiable build, not a website that mirrors a JAR onto `file.garden`-style hosts.
- Be suspicious of **free clients advertised via Discord + a landing page** with no source repo.
- Use a token-hardening mod (see case 014, TokenProtector) if you must run third-party mods.
- Treat any client that ships an **all-accepting `X509TrustManager`** or a **custom raw-socket TLS
  client** as hostile until proven otherwise.

## 7. Caveats

- OSINT; project names/status change frequently.
- Listing a project here is **not** an endorsement or a clean verdict — it is context only.
