#!/usr/bin/env python3
"""Decode char-by-char hidden strings from a javap -c disassembly.

The obfuscator materialises string literals as
    aload <sb>
    <push A>            (ldc/ldc_w int, sipush, bipush, iconst)
    <push B>
    isub | ixor | iadd
    i2c
    invokevirtual StringBuilder.append:(C)
    pop
where the emitted char is A op B.  Grouping consecutive appends to the same StringBuilder
variable recovers the literal.

Usage: decode_disasm.py <disassembly.txt> [--grep REGEX] [--min N]
"""
import re, sys, argparse

PUSH = re.compile(r'^\s*\d+:\s*(?:ldc(?:_w)?\s+#\d+\s+//\s*int\s+(-?\d+)'
                  r'|sipush\s+(-?\d+)'
                  r'|bipush\s+(-?\d+)'
                  r'|iconst_(-?[0-5]))\s*$')
OP = re.compile(r'^\s*\d+:\s*(isub|ixor|iadd)\s*$')
I2C = re.compile(r'^\s*\d+:\s*i2c\s*$')
APPEND = re.compile(r'^\s*\d+:\s*invokevirtual\s+#\d+\s+//\s*Method java/lang/StringBuilder\.append:\(C\)')
ALOAD = re.compile(r'^\s*\d+:\s*aload(?:_(\d))?\s*$')
POP = re.compile(r'^\s*\d+:\s*pop\s*$')
CLS = re.compile(r'^(?:public |final |abstract )*class (\S+)')


def push_val(m):
    for g in m.groups():
        if g is not None:
            return int(g)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('path')
    ap.add_argument('--grep')
    ap.add_argument('--min', type=int, default=3)
    a = ap.parse_args()
    grep = re.compile(a.grep) if a.grep else None
    lines = open(a.path, encoding='utf-8', errors='replace').read().split('\n')
    curcls = '?'
    i = 0
    n = len(lines)
    # track last aload var seen before a push
    out = []
    while i < n:
        m = CLS.match(lines[i])
        if m:
            curcls = m.group(1)
        # try to match a char append starting at i with an aload
        j = i
        var = None
        mm = ALOAD.match(lines[j])
        if mm:
            var = mm.group(1) or '0'
            j += 1
        ma = PUSH.match(lines[j]) if j < n else None
        if ma:
            A = push_val(ma)
            mb = PUSH.match(lines[j + 1]) if j + 1 < n else None
            if mb:
                B = push_val(mb)
                mo = OP.match(lines[j + 2]) if j + 2 < n else None
                if mo and I2C.match(lines[j + 3]) and APPEND.match(lines[j + 4]):
                    op = mo.group(1)
                    if op == 'isub':
                        v = A - B
                    elif op == 'ixor':
                        v = A ^ B
                    else:
                        v = A + B
                    ch = chr(v & 0xFFFF)
                    out.append((var, ch, i, curcls))
                    i = j + 5
                    continue
        i += 1
    # group consecutive chars with same var into strings
    groups = []
    cur = []
    for var, ch, ln, cls in out:
        if cur and cur[-1][0] == var and ln - cur[-1][1] <= 12:
            cur.append((var, ln, ch, cls))
        else:
            if cur:
                groups.append(cur)
            cur = [(var, ln, ch, cls)]
    if cur:
        groups.append(cur)
    for g in groups:
        s = ''.join(c[2] for c in g)
        if len(s) >= a.min and all(32 <= ord(c) < 127 for c in s):
            if grep and not grep.search(s):
                continue
            print('%s\t%s' % (g[0][3], s))


if __name__ == '__main__':
    main()
