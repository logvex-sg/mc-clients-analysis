# Case 016 — SkyblockAddons "EtherHiding" trojan (OSINT, with sample hash)

- **Verdict:** **confirmed malicious** (public static analysis; sample hash published)
- **Category:** trojanised Fabric mod — Minecraft session-token stealer with **Ethereum
  blockchain dead-drop C2 ("EtherHiding")** and in-memory stage-2 loader
- **Handling:** OSINT only. **No sample was downloaded or executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary source:** `Kagias/skyblockaddons-trojan-analysis` —
  https://github.com/Kagias/skyblockaddons-trojan-analysis (full technical writeup + YARA + IOCs
  reproduced verbatim in `evidence/`)
- **Family:** **EtherHiding / Weedhack–Majanito** — the same family as case 011.

---

## 1. Verdict

`SkyblockAddons-26.2.jar` (SHA-256 `85ccb86c…f7b5efa`) is a **trojanised Fabric mod**. It is a
**member of the same family as Weedhack (case 011)** — the `Weedhack/Majanito` stealer — but uses an
**Ethereum smart-contract dead-drop** to resolve its C2. This is the **modern evolution of the
Cluster-A / Weedhack TTP set**, and it is a **near-exact architectural twin** of our SoberClient
(case 001): Fabric mod → reflective credential stealer → encrypted/remote stage-2.

**Documented from a published static analysis.** We did not obtain or run the sample. Raw artefacts
(YARA rule, IOC list, deobfuscation script) are preserved under `evidence/`.

## 2. Sample metadata (from the source)

| Field | Value |
|---|---|
| File name | `SkyblockAddons-26.2.jar` |
| Size | `1,592,758` bytes |
| SHA-256 | `85ccb86cf78c138c55eda930a3d577f7d5891f65f51dfa785acc3d1a1f7b5efa` |
| SHA-1 | `d3fa13c4326cc2a618ec1112a2cfaef675210f17` |
| MD5 | `b3353010af83c489a246c3f7b0ad30c0` |
| Type | JAR / Fabric mod (63 entries) |
| Build time | `2026-10-08 23:17` |
| Real mod id | `toggle-sprint-display` ("Toggle Sprint Display" 2.0.2 by Aeltumn) |
| Lure | filename impersonates the unrelated popular "SkyBlockAddons" mod |

**Two red flags in `fabric.mod.json`:**

1. The **filename** (`SkyblockAddons-26.2.jar`) does not match the real mod name
   (`Toggle Sprint Display`) — a reputation-borrowing rename.
2. The `mixins` array contains a **second, randomly-named mixin config** `ece3700569f8.mixins.json`
   alongside the legitimate one — this is the injected bootstrap.

## 3. Execution chain

```
Fabric loader
 └─ Mece3700569f8  @Mixin(net.minecraft.client.Minecraft)
      (HEAD inject into Minecraft.run — runs at game start)
       └─ reflective launch of randomly-named loader (q56p0dnpq.qw1mnsk.dq5m8eecjo.cxo7lzvo)
            ├─ jI0  — reads the live Minecraft session (user.getAccessToken()/getProfileId()/getUser)
            │         and starts a thread to download + load stage-2
            ├─ ljl  — Ethereum dead-drop resolver (eth_call, selector 0x1f1bd692, 31 RPC endpoints)
            ├─ j1$  — hand-rolled HTTPS/DoH client (1.1.1.1, 8.8.8.8; /dns-query?name=…&type=A)
            └─ lo/o — in-memory JAR → custom ClassLoader → invoke com.example.Main.initialize(String)
```

- **Bootstrap** is a single malicious Fabric **Mixin** that injects at the head of
  `net.minecraft.client.Minecraft.run` and then reflectively calls the loader — i.e. the payload
  only runs once the game is live (evades "just decompile it" review of the main class).
- **Credential theft** is a direct read of the Minecraft session:
  `user.getAccessToken()`, `getProfileId()`, `getUser()` → JSON (`accessToken`, `mcInfo`).
- **Stage-2** (`com.example.Main`) is downloaded from
  `https://systemnotifier.st/api/static/loading` and loaded **entirely in memory** (no file on disk).

## 4. C2 resolution — Ethereum dead-drop ("EtherHiding")

This is the key technique. Instead of a hardcoded domain, the mod calls a **public Ethereum RPC**
and reads the C2 hostname **stored in a smart contract's `text()`**:

- Selector (byte-verified constant `o_$Oi1b`): **`0x1f1bd692`** = `keccak256("text()")[:4]`.
- Contract (Ethereum mainnet): **`0x9044f5762e43b23ba91d124b51a045f1b51da652`**.
- Creator: `0x34d7fb0cdD43F39DdbDbe85cD6e0688B7596e665`.
- Creation tx: `0xda4cf534054d992967430e9da3352b2adcfe9f2a7f58085f5c05b211671e16e0`.
- The `eth_call` returns `systemnotifier.st`, which is then resolved over **DNS-over-HTTPS**.

**Why it matters:** the C2 is **immutable and takedown-resistant** — the operator can rotate the
hostname by writing to the contract, and blocking the domain does nothing because the lookup is
on-chain. DoH + connect-to-literal-IP defeats local DNS sinkholes.

## 5. Indicators of compromise

See `iocs.csv` / `iocs.json` (verbatim from the source) and the YARA rule
`evidence/minecraft_etherhiding.yar`. Key items:

```
domain        systemnotifier.st
url           https://systemnotifier.st/api/static/loading
eth_contract  0x9044f5762e43b23ba91d124b51a045f1b51da652
eth_selector  0x1f1bd692                (keccak256("text()")[:4])
eth_creator   0x34d7fb0cdD43F39DdbDbe85cD6e0688B7596e665
ipv4          1.1.1.1 , 8.8.8.8         (hardcoded DoH resolvers)
sha256        85ccb86cf78c138c55eda930a3d577f7d5891f65f51dfa785acc3d1a1f7b5efa
file          ece3700569f8.mixins.json (injected mixin config)
class         mece3700569f8/Mece3700569f8.class (Mixin bootstrap)
class         q56p0dnpq/qw1mnsk/dq5m8eecjo/cxo7lzvo/jI0.class (stealer + stage-2 downloader)
class         q56p0dnpq/qw1mnsk/dq5m8eecjo/cxo7lzvo/ljl.class (ETH resolver)
class         q56p0dnpq/qw1mnsk/dq5m8eecjo/cxo7lzvo/j1$.class (HTTPS/DoH client)
file          y0whhmrf/t52qmy3n/jyvfg75jyq/edmst/vault.bin      (decoy 1.5 MB padding)
file          y0whhmrf/t52qmy3n/jyvfg75jyq/edmst/integrity.bin  (decoy manifest)
string        0x1f1bd692 , /dns-query?name= , &type=A , getAccessToken , com.example.Main
```

### YARA

The source ships `minecraft_etherhiding.yar` (two rules:
`Minecraft_Fabric_EtherHiding_Stealer_Loader`, `Minecraft_Fabric_Mixin_Class_Bootstrap`). It keys on
the ETH selector + DoH strings + the `getAccessToken`/`getProfileId`/`getUser` trio + the
randomly-named packages. Reproduced verbatim in `evidence/minecraft_etherhiding.yar`.

## 6. The decoy "protector" layer

The mod ships a fake **"protector"**: a `vault.bin` (1.5 MB of random padding) and an
`integrity.bin` manifest in a *second* randomly-named package. This mimics the "Vault / VM
protection" marketing that cheat clients advertise — a **social-engineering decoy** to make the JAR
look like a protected, legitimate product. It is not a real protection layer.

## 7. MITRE ATT&CK (from the source)

`T1204.002` (user execution), `T1036.005` (masquerading), `T1027`/`T1027.009` (obfuscated files),
`T1140` (decode), `T1620` (reflective load), `T1033` (system owner discovery), **`T1528` (steal
application access token)**, `T1552` (unsecured credentials), `T1005` (local data),
`T1071.001`/`T1071.004` (web/DoH C2), `T1573` (encrypted channel), **`T1102.001` (dead drop
resolver)**, `T1568` (dynamic resolution), `T1090` (proxy), `T1105` (ingress tool transfer),
`T1657` (financial theft — from the broader family).

## 8. Relationship to Cluster A / SoberClient

| Trait | EtherHiding (016) | SoberClient (001) | Cluster A (002–010) |
|---|---|---|---|
| Delivery | trojanised Fabric mod | trojanised Fabric mod | "free client" JAR |
| Bootstrap | injected Fabric **Mixin** into `Minecraft.run` | mod entrypoint | obfuscated loader class |
| Steals | `getAccessToken`/`getProfileId` | session token + crypto wallets | session token |
| Stage-2 | in-memory JAR from C2 | encrypted strings | encrypted strings |
| C2 resolution | **Ethereum dead-drop + DoH** | hardcoded ETH RPC endpoints | hardcoded/encrypted |
| Decoy "protector" | yes (`vault.bin`) | — | — |

**Conclusion:** case 016 is the **same family** as Weedhack (011) and is the closest public
example of the architecture we inferred in SoberClient (001). It proves the Cluster-A/session-stealer
threat is an **active, evolving campaign** using **blockchain C2** to survive takedowns.

## 9. Remediation

- Delete the JAR; scan mod folders (use the YARA rule or a scanner from case 023).
- **Invalidate the Minecraft session** and rotate the Microsoft password (the access token is stolen).
- Block the ETH RPC `eth_call` pattern at egress if feasible; note DoH bypasses DNS blocking.
- Treat the machine as compromised if stage-2 ran (in-memory RAT, unknown capability).

## 10. Caveats

- Single public analysis; not independently re-verified by us. The contract may now return a
  different hostname (that is the point of EtherHiding).
- `vault.bin`/`integrity.bin` are decoys, not the payload.
