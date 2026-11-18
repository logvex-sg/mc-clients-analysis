import re,sys
NUM = r'(?:ldc(?:_w)?\s+#\d+\s+//\s*int\s+(-?\d+)|sipush\s+(-?\d+)|bipush\s+(-?\d+))'
rn=re.compile(r'^\s*\d+:\s*'+NUM)
rs=re.compile(r'^\s*\d+:\s*ldc(?:_w)?\s+#\d+\s+//\s*String\s(.*)$')
ro=re.compile(r'^\s*\d+:\s*(isub|ixor|iadd)\s*$')
ri=re.compile(r'^\s*\d+:\s*i2c\s*$')
rc=re.compile(r'^(?:public |private |protected |static |final |abstract |native |synchronized )*[\w$.<>\[\], ]*\s+([\w$<>]+)\((.*)\);?\s*$')
rcl=re.compile(r'^[\w.$]+ class ([\w.$]+)')
def num(l):
    m=rn.match(l)
    if not m:return None
    for g in m.groups():
        if g is not None:return int(g)
# usage: decode_strings.py <disassembly.txt> [start_line] [end_line]
path = sys.argv[1] if len(sys.argv) > 1 else 'BYTECODE_DISASSEMBLY.txt'
lines = open(path, encoding='utf-8', errors='replace').read().split('\n')
start = int(sys.argv[2]) if len(sys.argv) > 2 else 0
end = int(sys.argv[3]) if len(sys.argv) > 3 else len(lines)
curcls=''; curmeth=''
i=start; chars=[]
def flush():
    global chars
    if chars:
        print('    STR:',''.join(chars)); chars=[]
while i<end:
    l=lines[i]
    m=rcl.match(l)
    if m: curcls=m.group(1); print('\n### CLASS',curcls)
    # method decl heuristic
    if re.match(r'^\s{2}(?:public |private |protected |static |final |abstract |native |synchronized |default |strictfp )*[\w$.\[\]<>]+ [\w$<>]+\(', l):
        flush(); print('  --',l.strip())
    n=num(l)
    if n is not None and i+3<end:
        n2=num(lines[i+1]); mop=ro.match(lines[i+2])
        if n2 is not None and mop and ri.match(lines[i+3]):
            a,b=n,n2; op=mop.group(1)
            v=a-b if op=='isub' else (a^b if op=='ixor' else a+b)
            chars.append(chr(v&0xFFFF)); i+=4; continue
    ms=rs.match(l)
    if ms: flush(); print('    STR:',ms.group(1))
    i+=1
flush()
