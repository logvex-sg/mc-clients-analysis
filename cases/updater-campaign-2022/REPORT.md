# Case 017 — "Updater" malware / 29-09-2022 Bukkit campaign (OSINT)

- **Verdict:** **confirmed malicious** (public; PaperMC security announcement; multiple detectors)
- **Category:** server-plugin backdoor/dropper campaign ("the Updater malware")
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - PaperMC malware announcement (2022-09-29) — https://forums.papermc.io/threads/malware-announcement.529/
  - `OpticFusion1/MCAntiMalware` — https://github.com/OpticFusion1/MCAntiMalware
  - `berrybyte-net/broom` — https://github.com/berrybyte-net/broom
- **Evidence:** `evidence/broom-main.go` (the scanner's JAR-walk logic)

---

## 1. Verdict

On **29 September 2022** a **backdoored plugin** was found distributed through **legitimate
marketplaces and build channels** (the "Updater" malware). It infected **server-side plugins**
(Bukkit/Spigot/Paper) rather than client mods, but it is the **direct ancestor** of the modern
client-side stealer campaigns: same idea (trojanise a JAR the operator does not own), same goal
(remote code execution on the victim).

**Documented from public sources.** We did not obtain or run the sample.

## 2. What it did (reported)

- Injected a backdoor into otherwise-legitimate plugins ("Updater"-style class).
- The backdoor **downloads and executes a second stage** from a remote host (dropper behaviour).
- It was spread by **compromising accounts/build pipelines** on plugin marketplaces, so the
  malicious JARs appeared under trusted plugin names — the same **supply-chain** vector as
  fractureiser (case 012).

## 3. Detection (public tooling)

Two community scanners were written for it:

- **`berrybyte-net/broom`** — a Go scanner that **walks a server directory for `.jar` files** and
  flags the 2022 variant (see `evidence/broom-main.go`: it recurses and matches
  `filepath.Ext(path) == ".jar"`). It self-describes as catching "one or possibly more variants of
  the 'Updater' malware".
- **`OpticFusion1/MCAntiMalware`** — a broader Java anti-malware that "detects over 1000 malicious
  plugins", maintained as an ongoing companion to this campaign.

## 4. Why it is catalogued here

- It is the **origin story** for the whole "Minecraft JAR malware" ecosystem that later produced
  fractureiser (012), the client stealers (011, 013–016) and the Cluster-A campaign (001–010).
- It shows the pattern is **bidirectional**: client mods *and* server plugins are both viable
  vectors, and both get distributed through **trusted-looking channels**.

## 5. Indicators / detection

The 2022 variant is largely **handled by community scanners** rather than a short static IOC list:

- **Scan every `.jar`** in `plugins/` (and `mods/`) with `MCAntiMalware` and/or `broom`.
- Look for a class that **`Runtime.exec`/`URL.openStream`s a second stage** on enable.
- Check plugin **hashes** against the marketplace originals; a size/manifest mismatch is a red flag.
- Treat any plugin that reaches out to an unknown host on load as suspicious.

> The community guidance is to run `OpticFusion1/MCAntiMalware` for a thorough check, since variants
> differ. No single hash is authoritative here.

## 6. Relationship to Cluster A

| Trait | Updater (2022) | Cluster A (002–010) |
|---|---|---|
| Target | server plugins | client mods/clients |
| Delivery | compromised marketplace/build | free-client site + `file.garden` |
| Technique | backdoor class → stage-2 download | obfuscated loader → encrypted C2 |
| Goal | RCE on victim host | session-token theft + RCE |
| Detection | MCAntiMalware, broom | static bytecode/string analysis |

## 7. Remediation

- Remove the infected plugin JAR(s) and replace with a **freshly downloaded, hash-verified** copy.
- Because the backdoor downloads a second stage, assume **RCE**: rotate any credentials the server
  host can reach and review for persistence.
- Re-scan all plugins after cleanup (the campaign re-infected follow-ups).

## 8. Caveats

- Second-hand; the 2022 infrastructure is largely dead, but the technique persists.
- Scanner coverage varies; absence of a hit is not proof of cleanliness.
