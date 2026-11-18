#!/usr/bin/env python3
"""Static deobfuscator for the DonutSMP / SoberClient Java obfuscation framework.

Framework
---------
* Class `j` holds a static `String[] a` table of XOR-encrypted literals, populated in its
  `a()` / `b()` initialiser methods.
* Every string constant is materialised as an `invokedynamic` call site whose bootstrap is
  `j.a(Lookup,String,MethodType)CallSite`.  The call-site NAME's first char is a kind tag
  (virtual/static/getter/setter/...); the last two bootstrap ARGUMENTS are two ints
  (index, key).
* Decryption (from `j.a(int,int)`):
      idx = n ^ 0x969742E0
      plaintext = a[idx] with every char XORed by (key >>> 16)

This tool parses the raw class file, extracts the encrypted table from `j`, walks every
`invokedynamic`, and prints the decrypted strings with their call sites.  It also dumps the
whole decrypted table.

Usage:
    python3 deobf.py <jar> [--table] [--class com/foo/Bar] [--grep REGEX]
"""
import io, re, struct, sys, zipfile, argparse


# ---------------------------------------------------------------- class file parser
class Reader:
    def __init__(self, data):
        self.d = data
        self.p = 0

    def u1(self):
        v = self.d[self.p]; self.p += 1; return v

    def u2(self):
        v = struct.unpack_from('>H', self.d, self.p)[0]; self.p += 2; return v

    def u4(self):
        v = struct.unpack_from('>I', self.d, self.p)[0]; self.p += 4; return v

    def bytes(self, n):
        v = self.d[self.p:self.p + n]; self.p += n; return v


def parse_cp(r):
    count = r.u2()
    cp = {}
    i = 1
    while i < count:
        tag = r.u1()
        if tag == 1:  # Utf8
            ln = r.u2(); cp[i] = ('Utf8', r.bytes(ln).decode('utf-8', 'replace'))
        elif tag == 3:
            cp[i] = ('Int', struct.unpack('>i', r.bytes(4))[0])
        elif tag == 4:
            cp[i] = ('Float', struct.unpack('>f', r.bytes(4))[0])
        elif tag == 5:
            cp[i] = ('Long', struct.unpack('>q', r.bytes(8))[0]); i += 1
        elif tag == 6:
            cp[i] = ('Double', struct.unpack('>d', r.bytes(8))[0]); i += 1
        elif tag == 7:
            cp[i] = ('Class', r.u2())
        elif tag == 8:
            cp[i] = ('String', r.u2())
        elif tag in (9, 10, 11):
            a = r.u2(); b = r.u2(); cp[i] = ({9: 'Field', 10: 'Method', 11: 'InterfaceMethod'}[tag], a, b)
        elif tag == 12:
            a = r.u2(); b = r.u2(); cp[i] = ('NameAndType', a, b)
        elif tag == 15:
            k = r.u1(); a = r.u2(); cp[i] = ('MethodHandle', k, a)
        elif tag == 16:
            cp[i] = ('MethodType', r.u2())
        elif tag in (17, 18):
            a = r.u2(); b = r.u2(); cp[i] = ('Dynamic', a, b) if tag == 17 else ('InvokeDynamic', a, b)
        elif tag == 19:
            cp[i] = ('Module', r.u2())
        elif tag == 20:
            cp[i] = ('Package', r.u2())
        else:
            raise ValueError('unknown cp tag %d at %d' % (tag, i))
        i += 1
    return cp


def utf8(cp, idx):
    e = cp.get(idx)
    return e[1] if e and e[0] == 'Utf8' else None


def resolve_string(cp, idx):
    e = cp.get(idx)
    if e and e[0] == 'String':
        return utf8(cp, e[1])
    return None


def parse_attrs(r, cp, ctx='class'):
    n = r.u2()
    out = {}
    for _ in range(n):
        name = utf8(cp, r.u2())
        ln = r.u4()
        out.setdefault(name, []).append(r.bytes(ln))
    return out


class ClassFile:
    def __init__(self, data):
        r = Reader(data)
        assert r.u4() == 0xCAFEBABE
        r.u2(); r.u2()
        self.cp = parse_cp(r)
        r.u2(); self.this = r.u2(); r.u2()
        nif = r.u2()
        for _ in range(nif):
            r.u2()
        fields = []
        for _ in range(r.u2()):
            r.u2(); r.u2(); r.u2(); parse_attrs(r, self.cp)
            fields.append('field')
        self.methods = []
        for _ in range(r.u2()):
            r.u2(); name = utf8(self.cp, r.u2()); desc = utf8(self.cp, r.u2())
            attrs = parse_attrs(r, self.cp)
            self.methods.append((name, desc, attrs))
        self.attrs = parse_attrs(r, self.cp)
        self.bootstrap = self._parse_bootstrap()

    def _parse_bootstrap(self):
        res = []
        for blob in self.attrs.get('BootstrapMethods', []):
            r = Reader(blob)
            for _ in range(r.u2()):
                mref = r.u2()
                args = [r.u2() for _ in range(r.u2())]
                res.append((mref, args))
        return res

    def method_code(self, name, desc=None):
        for mn, md, attrs in self.methods:
            if mn == name and (desc is None or md == desc):
                for blob in attrs.get('Code', []):
                    r = Reader(blob)
                    r.u2(); r.u2()          # max_stack, max_locals
                    clen = r.u4()
                    return r.bytes(clen)
        return None

    def all_code(self):
        for mn, md, attrs in self.methods:
            for blob in attrs.get('Code', []):
                r = Reader(blob)
                r.u2(); r.u2()
                clen = r.u4()
                yield mn, md, r.bytes(clen)


# ---------------------------------------------------------------- bytecode scan
def scan_ldc_strings(code, cp):
    """Return list of (offset, index) for ldc/ldc_w of a String constant, plus int pushes."""
    out = []
    i = 0
    n = len(code)
    while i < n:
        op = code[i]
        if op == 0x12:      # ldc
            idx = code[i + 1]; out.append((i, 'ldc', idx)); i += 2
        elif op == 0x13:    # ldc_w
            idx = struct.unpack_from('>H', code, i + 1)[0]; out.append((i, 'ldc_w', idx)); i += 3
        elif op == 0x11:    # sipush
            out.append((i, 'sipush', struct.unpack_from('>h', code, i + 1)[0])); i += 3
        elif op == 0x10:    # bipush
            out.append((i, 'bipush', struct.unpack_from('>b', code, i + 1)[0])); i += 2
        elif 0x03 <= op <= 0x08:  # iconst_0..5
            out.append((i, 'iconst', op - 0x03)); i += 1
        elif op == 0xBA:    # invokedynamic
            idx = struct.unpack_from('>H', code, i + 1)[0]; out.append((i, 'indy', idx)); i += 5
        elif op in (0xAA, 0xAB):  # tableswitch / lookupswitch (4-byte aligned)
            j = i + 1
            j += (4 - ((j) % 4)) % 4
            if op == 0xAA:
                lo = struct.unpack_from('>i', code, j + 4)[0]
                hi = struct.unpack_from('>i', code, j + 8)[0]
                i = j + 12 + 4 * (hi - lo + 1)
            else:
                np = struct.unpack_from('>i', code, j + 4)[0]
                i = j + 8 + 8 * np
        elif op == 0xC4:    # wide
            i += 4 if code[i + 1] != 0x84 else 6
        else:
            # operand lengths for common opcodes
            i += 1 + OP_LEN.get(op, 0)
    return out


OP_LEN = {}
for _o in (0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3A, 0xA9):
    OP_LEN[_o] = 1
for _o in (0x84,):
    OP_LEN[_o] = 2
for _o in (0x99, 0x9A, 0x9B, 0x9C, 0x9D, 0x9E, 0x9F, 0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5, 0xA6,
           0xA7, 0xA8, 0xB2, 0xB3, 0xB4, 0xB5, 0xB6, 0xB7, 0xB8, 0xBB, 0xBD, 0xC0, 0xC1,
           0xC6, 0xC7):
    OP_LEN[_o] = 2
for _o in (0xB9, 0xBA, 0xBC, 0xBE, 0xBF, 0xC2, 0xC3):
    OP_LEN[_o] = 4


# ---------------------------------------------------------------- framework decode
def extract_table(jclass):
    """Parse j.class initialiser methods a()/b() -> dict index -> encrypted string."""
    table = {}
    for mn, md, code in jclass.all_code():
        if mn not in ('a', 'b') or md != '()V':
            continue
        toks = scan_ldc_strings(code, jclass.cp)
        pending_int = None
        for off, kind, val in toks:
            if kind in ('iconst', 'bipush', 'sipush'):
                pending_int = val
            elif kind in ('ldc', 'ldc_w'):
                s = resolve_string(jclass.cp, val)
                if s is not None and pending_int is not None:
                    table[pending_int] = s
                    pending_int = None
    return table


def decode_table(table, idx, key):
    enc = table.get(idx)
    if enc is None:
        return None
    k = (key >> 16) & 0xFFFF
    return ''.join(chr((ord(c) ^ k) & 0xFFFF) for c in enc)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('jar')
    ap.add_argument('--class', dest='cls', action='append', default=[])
    ap.add_argument('--grep')
    ap.add_argument('--table', action='store_true')
    ap.add_argument('--jclass', default='j.class')
    args = ap.parse_args()

    z = zipfile.ZipFile(args.jar)
    names = z.namelist()
    jname = args.jclass if args.jclass in names else None
    if jname is None:
        for n in names:
            if n.endswith('j.class') and n.count('/') == 0:
                jname = n; break
    jclass = ClassFile(z.read(jname))
    table = extract_table(jclass)
    sys.stderr.write('table entries: %d\n' % len(table))

    if args.table:
        for i in sorted(table):
            for key in range(0, 0x10000):
                s = decode_table(table, i, key << 16)
                if s and s.isprintable() and len(s) > 2 and all(32 <= ord(c) < 127 for c in s):
                    pass
            sys.stdout.write('%d\t%r\n' % (i, table[i]))

    targets = args.cls or [n for n in names if n.endswith('.class')]
    grep = re.compile(args.grep) if args.grep else None
    for cn in targets:
        if not cn.endswith('.class'):
            continue
        try:
            cf = ClassFile(z.read(cn))
        except Exception as e:
            sys.stderr.write('skip %s: %s\n' % (cn, e)); continue
        # is the bootstrap our j.a ?
        for mn, md, code in cf.all_code():
            toks = scan_ldc_strings(code, cf.cp)
            for off, kind, val in toks:
                if kind != 'indy':
                    continue
                e = cf.cp.get(val)
                if not e or e[0] != 'InvokeDynamic':
                    continue
                bsm_idx, nat = e[1], e[2]
                if bsm_idx >= len(cf.bootstrap):
                    continue
                mref, bargs = cf.bootstrap[bsm_idx]
                mh = cf.cp.get(mref)
                # resolve bootstrap target name
                tgt = None
                if mh and mh[0] == 'MethodHandle':
                    ref = cf.cp.get(mh[2])
                    if ref and ref[0] in ('Method', 'InterfaceMethod'):
                        tgt = utf8(cf.cp, ref[2])
                if tgt not in ('a', 'b'):
                    continue
                ints = [cf.cp[a][1] for a in bargs if cf.cp.get(a, ('',))[0] == 'Int']
                if len(ints) < 2:
                    continue
                n, key = ints[-2], ints[-1]
                idx = (n ^ 0x969742E0) & 0xFFFFFFFF
                s = decode_table(table, idx, key)
                if s is None:
                    continue
                if grep and not grep.search(s):
                    continue
                sys.stdout.write('%s :: %s\n' % (cn[:-6].replace('/', '.'), s))


if __name__ == '__main__':
    main()
