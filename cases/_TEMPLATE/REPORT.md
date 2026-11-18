# Case NNN — <Name>

- **Status:** <confirmed-malicious | suspected | benign | inconclusive>
- **Family / distribution:** <family or campaign name, distribution vector>
- **Primary impact:** <credential-theft | remote-code-execution | data-exfiltration | ...>
- **Analyst:** <name>
- **Date:** <YYYY-MM-DD>
- **Handling:** <static-only | dynamic in sandbox | ...>

---

## 1. Summary

<2–4 paragraphs: what it is, what it does, why it matters. Lead with the verdict.>

## 2. Sample identification

| Field | Value |
|---|---|
| File name | |
| Size | |
| SHA-256 | |
| Type | |
| Distribution site / URL | |

## 3. Structure

<Packages / components / entrypoints. Separate genuine functionality from malicious code.>

## 4. Execution flow

```
<ASCII flow diagram: entry -> behaviour -> C2 -> exfil>
```

## 5. Credential / data theft

<Exactly what is stolen, from where, by what mechanism. For account stealers: tokens, cookies,
password stores, browser data, session files, clipboard, etc.>

## 6. C2 infrastructure

<Domains, IPs, URLs, protocols, evasion (DoH, dead-drops, custom stacks, TLS bypass).>

## 7. Impact

## 8. Detection & response

### 8.1 Host indicators
### 8.2 Network indicators
### 8.3 Response steps

## 9. Indicators of compromise

<Point to iocs.csv; list the key items inline.>

## 10. Evidence & artefacts

| Path | Description |
|---|---|
| | |

## 11. Open questions / next steps
