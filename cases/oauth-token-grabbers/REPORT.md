# Case 021 — OAuth / Microsoft-token grabbers (OSINT, source-read)

- **Verdict:** **confirmed malicious** (published source read directly)
- **Category:** "OAuth grabber" that walks the **full Microsoft → Xbox Live → XSTS → Minecraft**
  auth chain to mint a fresh Minecraft access token for the attacker
- **Handling:** OSINT only. **Source read, never executed.**
- **Date:** 2026-11-18
- **Analyst:** OpenHands (open-source intelligence)
- **Primary sources:**
  - `polincracy/Minecraft-oAuth-Grabber-MOG-` — https://github.com/polincracy/Minecraft-oAuth-Grabber-MOG-
  - `cev-api/MC-Token-Suite` — https://github.com/cev-api/MC-Token-Suite (token-exchange tooling)
- **Evidence:** `evidence/mog-index.js` (full token-exchange chain)

---

## 1. Verdict

An "OAuth grabber" does **not** steal the session token from the game; it tricks the victim into
completing a **Microsoft login** on an attacker-controlled Azure app, then exchanges the resulting
OAuth code for a **fresh Minecraft access token**. It is the **phishing-based sibling** of the
in-memory session stealers — and the published script is a complete, working implementation.

## 2. The full chain, verbatim (`evidence/mog-index.js`)

The attacker registers an Azure app ("Verification Bot"), hosts the repo, and points the redirect at
their server. When the victim lands on the callback with `?code=…`, the script does:

```
1. login.live.com/oauth20_token.srf              -> access_token, refresh_token
2. user.auth.xboxlive.com/user/authenticate      -> Xbox user token + uhs (user hash)
3. xsts.auth.xboxlive.com/xsts/authorize         -> XSTS token
4. api.minecraftservices.com/authentication/login_with_xbox -> Minecraft bearer token
5. api.minecraftservices.com/minecraft/profile   -> id (uuid) + name
   then POSTs {username, uuid} to the attacker endpoint (http://d-na.kr/oauth.php)
```

The exact code (excerpt):

```js
const url = 'https://login.live.com/oauth20_token.srf'   // step 1
...
const url = 'https://user.auth.xboxlive.com/user/authenticate'   // step 2
...
const url = 'https://xsts.auth.xboxlive.com/xsts/authorize'      // step 3
...
const url = 'https://api.minecraftservices.com/authentication/login_with_xbox'  // step 4
identityToken: "XBL3.0 x=" + userHash + ";" + xstsToken
...
const url = 'https://api.minecraftservices.com/minecraft/profile'  // step 5
```

**Note the config at the top of the file**: a hardcoded `client_id`, `redirect_uri`
(`https://d-na.herokuapp.com/`), a `secret_value`, and a `webhook_url`. This is a **published
attacker kit**: fork, set your Azure app + webhook, host on Heroku, done.

## 3. Why this is worse than a token scrape

- It yields a **refresh token** (step 1), so the attacker can mint **new** access tokens repeatedly
  — even after the current session is invalidated — until the user **revokes the app's consent**.
- The victim **completes a real Microsoft login** and often does not realise they authorised a
  third-party app, so there is no "suspicious file" to find.

## 4. Indicators of compromise

```
url    https://login.live.com/oauth20_token.srf                       (OAuth code -> token)
url    https://user.auth.xboxlive.com/user/authenticate               (Xbox user token)
url    https://xsts.auth.xboxlive.com/xsts/authorize                  (XSTS token)
url    https://api.minecraftservices.com/authentication/login_with_xbox
url    https://api.minecraftservices.com/minecraft/profile
host   d-na.herokuapp.com / d-na.kr                                   (attacker callback + collector)
string "Verification Bot"                                             (lure app name)
string XBL3.0 x=                                                      (Xbox identity token format)
```

> `cev-api/MC-Token-Suite` publishes the same token-exchange steps as five Python scripts (legit
> research utility) — useful to understand the chain, and to see how easily it is weaponised.

## 5. Relationship to Cluster A

Different **entry point** (phishing a Microsoft login vs. a malicious JAR), same **prize**
(Minecraft/Microsoft account). A mature campaign may use both: a "free client" for the in-game
session, and an OAuth lure for a **persistent refresh token**. Defenders should treat "log in with
Microsoft here" from any Minecraft-adjacent site as hostile until the app is verified.

## 6. Remediation

- Go to the Microsoft account's **"Apps and services" / privacy** page and **remove any unfamiliar
  connected app** — this revokes the refresh token (a password change alone does **not**).
- Change the Microsoft password and enable 2FA.
- Never complete a Microsoft login for a "client", "mod", or "verification bot" you did not seek out.

## 7. Caveats

- Published kit/tooling (some framed as educational); not independently re-verified by us.
- Source read only — no execution, and the attacker endpoints were not contacted.
