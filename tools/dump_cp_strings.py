#!/usr/bin/env python3
"""Dump interesting cleartext constant-pool strings from every class in a jar.

Obfuscated string literals live in `j`'s XOR table, but plenty of strings remain in the
constant pool (URLs, registry paths, wallet paths, powershell, headers, JSON keys, tokens).
This extracts them so behaviour can be reviewed without running anything.

Usage: dump_cp_strings.py <jar> [--grep REGEX]
"""
import re, sys, zipfile
import deobf

INTERESTING = re.compile(
    r'https?://|wss?://|discord|webhook|token|passwd|password|secret|api[_-]?key|'
    r'wallet|metamask|exodus|\.json\b|AppData|Roaming|Local|Software\\|reg(istry)?\b|'
    r'powershell|cmd(\.exe)?|curl|wget|/bin/|HKEY|HKCU|HKLM|clipboard|base64|'
    r'Authorization|Bearer|session|accessToken|refresh|launcher_|\.minecraft|'
    r'%s|%USERNAME%|%APPDATA%|ProcessBuilder|socket|C2|telegram|\.onion|'
    r'@|\.xyz|\.ru|\.top|\.cc|\.su|\.pw|pastebin|hastebin|dpaste|ngrok|'
    r'LocalLow|Firefox|Chrome|Edge|Brave|Opera|Login Data|Cookies|'
    r'discord\\|discordptb|leveldb|Local Storage|keytar|dpapi|CryptUnprotect|'
    r'osascript|/Users/|Library/Application Support|bash -|sh -c|'
    r'java\.io\.tmpdir|System\.getenv|user\.name|user\.home|os\.name',
    re.I)

PLAIN = re.compile(r'^[\x20-\x7e]{4,400}$')


def main():
    jar = sys.argv[1]
    grep = re.compile(sys.argv[sys.argv.index('--grep') + 1]) if '--grep' in sys.argv else INTERESTING
    z = zipfile.ZipFile(jar)
    seen = {}
    for n in z.namelist():
        if not n.endswith('.class'):
            continue
        try:
            cf = deobf.ClassFile(z.read(n))
        except Exception:
            continue
        for i, e in cf.cp.items():
            if e[0] == 'Utf8':
                s = e[1]
                if PLAIN.match(s) and grep.search(s) and not s.startswith('(') and 'Lnet/' not in s:
                    seen.setdefault(s, set()).add(n)
    for s in sorted(seen):
        where = sorted(seen[s])
        loc = where[0] if len(where) == 1 else '%s (+%d)' % (where[0], len(where) - 1)
        print('%s\t%s' % (loc, s))


if __name__ == '__main__':
    main()
