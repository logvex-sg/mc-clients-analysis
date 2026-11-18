# Case 022 — Server backdoor tooling (OpenBukloit, qqBackdoor, LeuxBackdoor) — OSINT

- **Verdict:** **dual-use attack tooling** (published; used for server compromise) — not a single
  sample
- **Category:** injectors/modules that add a **backdoor** to Minecraft plugins or a
  **remote-access/token-stealing** backdoor to a client mod
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `VoxelHax/OpenBukloit` — https://github.com/VoxelHax/OpenBukloit
  - `qqTechnologies/qqBackdoor` — https://github.com/qqTechnologies/qqBackdoor
  - `accessmodifier364/LeuxBackdoor` — https://github.com/accessmodifier364/LeuxBackdoor
- **Evidence:** details reproduced from the projects' READMEs (below).

---

## 1. Verdict

This case documents the **offensive tooling** that supplies backdoors to the Minecraft ecosystem.
It is the "supply" side of the threats in cases 017 (plugin backdoor) and 011/016 (client stealers).
Not a verdict on one file, but a catalogue of the tools a defender must recognise.

## 2. OpenBukloit — plugin backdoor injector

From the project README: *"modern and powerful universal backdoor injector compatible with all
Bukkit/Spigot/Paper/etc plugins"* with a *"powerful camouflage engine, which makes [it] nearly
impossible to find"*.

- **Model:** it **patches an existing, legitimate plugin JAR** to add a backdoor class, then
  re-signs/repackages it — so the malicious plugin looks like the original.
- **Builtin backdoor:** executes **console commands** when a chat message begins with a secret key:
  > `hackthisserver op MyName` → runs `op MyName` as console.
- **Custom backdoors:** `--exploit <file.java|.class>`; the injected class must expose
  `public static void inject(JavaPlugin args)`, executed **after `onEnable`** — and *"you can do
  everything you want here, including downloading and running some arbitrary code from internet"*.
- **Camouflage engine** hides the injected class among the plugin's real classes.

This is the tool that turns a **clean plugin into a backdoored one** — the mechanism behind
case 017 and a recurring risk for any server that downloads plugins from unofficial mirrors.

## 3. qqBackdoor — client RAT backdoor

From the README (v6): a **client-side backdoor** for cheat clients (Phobos/others), developed by
`@Crystallinqq` & `@megyn-on-top`. It is a **tweaker/bootstrapper** that injects into the client and
talks to an attacker **socket server** the operator must write. Framed as "educational", it is the
classic **RAT-in-a-cheat-client** — exactly the risk model of the Cluster-A "free clients".

> A user running a cheat client with a backdoor like this is remotely controllable, and — because
> the client has the game's `Session` in memory — is a **session-token theft** target as well.

## 4. LeuxBackdoor — mod aimed at anarchy servers

`accessmodifier364/LeuxBackdoor` is a Minecraft mod aimed at anarchy servers, in the same
"client-side backdoor / utility" category. Included for completeness: it shows the category spans
"cheat feature" → "remote control" → "credential access".

## 5. Why it is catalogued here

- **Supply chain:** OpenBukloit is how a server plugin becomes a backdoor; qqBackdoor/LeuxBackdoor
  are how a client mod becomes one. Both feed the same victim pools as cases 017 and 001–016.
- **Camouflage:** the explicit "camouflage engine" is the offensive counterpart of the obfuscation
  we fight in Cluster A (per-class string encryption, injected mixins, decoy "protectors").
- **Dual-use framing:** all three are published as "educational/testing" — the same framing used by
  every stealer kit in this catalogue.

## 6. Detection guidance

- **Plugins:** a backdoor injected by OpenBukloit lives in a **normal-looking class** with an
  `inject(...)` method called from `onEnable`. Compare plugin JARs against the official release
  (hash + class list); scan with MCAntiMalware (case 017). Watch for console-command execution
  triggered by a chat prefix.
- **Client mods:** a mod that opens a **socket to an external host** and accepts commands is a
  backdoor. Combine with session-token checks (does it read `Session`/`User`?).
- **General:** treat any mod/plugin that both (a) reads the session/credentials or opens a remote
  socket **and** (b) ships obfuscation/camouflage as hostile until proven otherwise.

## 7. Relationship to Cluster A

| Trait | Backdoor tooling (022) | Cluster A (002–010) |
|---|---|---|
| What it is | injector / backdoor module | trojanised "free client" |
| Obfuscation | camouflage engine (plugin) / tweaker (client) | per-class string encryption + injected mixin |
| Access | console commands / remote socket | encrypted C2 + session theft |
| Overlap | **a backdoor is what makes "free client" dangerous** | — |

## 8. Caveats

- Tooling repos; not samples. No independent verification; details are from the projects' own READMEs.
- Availability/behaviour of these projects changes over time.
