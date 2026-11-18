#!/usr/bin/env python3
"""Recover the hidden string literals from the `j` table of the DonutSMP obfuscation framework.

The `j` class holds a static String[] table of XOR-encrypted literals.  Each entry is
decrypted at runtime as `plain[i] = enc[i] ^ (key >>> 16)` where the 16-bit key is produced
by inlined opaque arithmetic at the call site.  The plaintexts are meaningful ASCII, so the
correct per-entry key is the one that yields an all-printable, text-like string.  We brute
force all 65536 keys per entry with numpy.

Usage: recover_strings.py <jar> [--jclass j.class] [--grep REGEX]
"""
import argparse, re, sys, zipfile
import numpy as np
import deobf

WORDY = re.compile(r'[a-zA-Z]{3,}')


def build_matrix(table):
    items = sorted(table.items())
    idxs = [i for i, _ in items]
    maxlen = max(len(v) for _, v in items)
    arr = np.zeros((len(items), maxlen), dtype=np.int32)
    for r, (_, v) in enumerate(items):
        a = [ord(c) for c in v]
        arr[r, :len(a)] = a
    lens = np.array([len(v) for _, v in items])
    return idxs, arr, lens


def recover(table):
    idxs, arr, lens = build_matrix(table)
    best = {}
    for k in range(0x10000):
        dec = arr ^ k
        ok = (dec >= 32) & (dec < 127)
        # zero padding stays printable -> treat as ok
        rowok = ok.all(axis=1)
        if rowok.any():
            for r in np.nonzero(rowok)[0]:
                L = int(lens[r])
                if L < 4:
                    continue
                s = ''.join(chr(c) for c in dec[r, :L])
                sc = len(WORDY.findall(s)) * 10 + sum(1 for c in s if c in ':/._-{}"')
                cur = best.get(int(idxs[r]))
                if cur is None or sc > cur[0]:
                    best[int(idxs[r])] = (sc, k, s)
    return best


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('jar')
    ap.add_argument('--jclass', default=None)
    ap.add_argument('--grep')
    a = ap.parse_args()
    grep = re.compile(a.grep) if a.grep else None
    z = zipfile.ZipFile(a.jar)
    jname = a.jclass
    if not jname:
        for n in z.namelist():
            if n == 'j.class' or n.endswith('/j.class'):
                jname = n
                break
    jc = deobf.ClassFile(z.read(jname))
    table = deobf.extract_table(jc)
    sys.stderr.write('table entries: %d (j=%s)\n' % (len(table), jname))
    best = recover(table)
    for i in sorted(best):
        _, k, s = best[i]
        if grep and not grep.search(s):
            continue
        print('%d\t0x%04x\t%s' % (i, k, s))


if __name__ == '__main__':
    main()
