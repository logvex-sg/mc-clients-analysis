#!/usr/bin/env python3
"""Brute-force the 16-bit XOR key of obfuscated string literals in the DonutSMP framework.

Each hidden literal is a String constant in the class constant pool.  At runtime it is
recovered as `plain[i] = enc[i] ^ (key >>> 16)`, where `key >>> 16` is a 16-bit value.
Because the plaintexts are meaningful ASCII (URLs, commands, JSON, headers), the correct
key is the one that yields all-printable, text-like output.  We try all 65536 keys per
literal and keep the best-scoring decode.

Usage: brute_strings.py <jar> [--grep REGEX] [--min N]
"""
import argparse, re, sys, zipfile
import deobf

# Characters common in the hidden strings (URLs, JSON, commands, java identifiers)
GOOD = set('abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
           ' .,:;/\\_-{}[]()"\'+=%$#@!?*&<>|~`^')
WORDY = re.compile(r'[a-zA-Z]{3,}')


def score(s):
    if not s:
        return -1
    printable = sum(1 for c in s if 32 <= ord(c) < 127)
    if printable != len(s):
        return -1
    good = sum(1 for c in s if c in GOOD)
    words = len(WORDY.findall(s))
    return good * 2 + words * 6 - s.count('\x00')


def best_decodes(enc, topn=3):
    res = []
    for k in range(0x10000):
        s = ''.join(chr((ord(c) ^ k) & 0xFFFF) for c in enc)
        sc = score(s)
        if sc > 0:
            res.append((sc, k, s))
    res.sort(reverse=True)
    return res[:topn]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('jar')
    ap.add_argument('--grep')
    ap.add_argument('--min', type=int, default=4)
    a = ap.parse_args()
    grep = re.compile(a.grep) if a.grep else None
    z = zipfile.ZipFile(a.jar)
    found = {}
    for n in z.namelist():
        if not n.endswith('.class'):
            continue
        try:
            cf = deobf.ClassFile(z.read(n))
        except Exception:
            continue
        for idx, e in cf.cp.items():
            if e[0] != 'String':
                continue
            enc = deobf.utf8(cf.cp, e[1])
            if not enc or len(enc) < a.min:
                continue
            # only consider strings that are NOT already clean ascii (obfuscated ones)
            if all(32 <= ord(c) < 127 for c in enc):
                continue
            for sc, k, s in best_decodes(enc):
                if grep and not grep.search(s):
                    continue
                found.setdefault(s, (n, k))
                break
    for s, (n, k) in sorted(found.items()):
        print('%s\t0x%04x\t%s' % (n, k, s))


if __name__ == '__main__':
    main()
