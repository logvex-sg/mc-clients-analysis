import re,sys
L=open(sys.argv[1],encoding='utf-8',errors='replace').read().split('\n')
lim=int(sys.argv[2]) if len(sys.argv)>2 else len(L)

class SB:
    __slots__=('buf',)
    def __init__(s): s.buf=[]
class CA:
    __slots__=('n','d')
    def __init__(s,n): s.n=n; s.d={}
def imm(op,arg):
    if op in ('bipush','sipush'):
        try: return ('int',int(arg.split()[0]))
        except: return ('int',0)
    if op in ('ldc','ldc_w'):
        m=re.search(r'int (-?\d+)',arg)
        if m: return ('int',int(m.group(1)))
        m=re.search(r'String (.+)',arg)
        return ('str',m.group(1)) if m else ('obj',None)
    if op=='iconst_m1': return ('int',-1)
    if op.startswith('iconst_'): return ('int',int(op.split('_')[1]))
    return (op,arg)

out=[];cls=None
for mi in range(len(L)):
    if not re.match(r'^  \S.*\(.*\);\s*$',L[mi]): continue
    name=L[mi].strip()
    # find end
    end=len(L)
    for j in range(mi+1,len(L)):
        if re.match(r'^  \S.*\(.*\);\s*$',L[j]) or re.match(r'^\}',L[j]): end=j;break
    end=min(end,lim)
    st=[];loc={}
    for line in L[mi:end]:
        m=re.match(r'\s*(\d+): (\S+)\s*(.*)',line)
        if not m: continue
        op,arg=m.group(2),m.group(3).strip()
        try:
            if op=='new' and 'StringBuilder' in arg: st.append(SB())
            elif op=='new' and 'String' in arg: st.append(('newstr',))
            elif op=='new': st.append(('new',arg))
            elif op=='newarray' and 'char' in arg:
                n=st.pop()[1] if st and st[-1][0]=='int' else 0
                st.append(CA(n))
            elif op=='anewarray': st.append(('arr',[]))
            elif op=='invokespecial' and 'StringBuilder."<init>"' in arg:
                if not (st and isinstance(st[-1],SB)): st.append(SB())
            elif op=='invokespecial' and 'String."<init>":([C)V' in arg:
                ca=st.pop() if st else None
                s=''.join(ca.d.get(i,'\x00') for i in range(ca.n)) if isinstance(ca,CA) else ''
                st.append(('str',s))
            elif op=='invokespecial':
                if st and st[-1][0]=='new': st.pop()
            elif op=='dup': st.append(st[-1] if st else ('obj',None))
            elif op in ('pop','pop2'):
                if st: st.pop()
            elif op in ('aload_0','aload_1','aload_2','aload_3'): st.append(loc.get(int(op[-1]),('obj',None)))
            elif op=='aload': st.append(loc.get(int(arg),('obj',None)))
            elif op.startswith('astore'):
                v=st.pop() if st else ('obj',None)
                k=op[-1]; loc[int(k) if k.isdigit() else int(arg)]=v
            elif op in ('iload','iload_0','iload_1','iload_2','iload_3','fload','lload','dload'): st.append(('int',0))
            elif op.startswith('istore') or op.startswith('lstore') or op.startswith('fstore') or op.startswith('dstore'):
                if st: st.pop()
            elif op in ('bipush','sipush','iconst_m1','iconst_0','iconst_1','iconst_2','iconst_3','iconst_4','iconst_5','ldc','ldc_w'):
                st.append(imm(op,arg))
            elif op in ('iadd','isub','ixor','imul','iand','ior'):
                b=st.pop() if st else ('int',0); a=st.pop() if st else ('int',0)
                if a[0]=='int' and b[0]=='int':
                    st.append(('int',{'iadd':a[1]+b[1],'isub':a[1]-b[1],'ixor':a[1]^b[1],'imul':a[1]*b[1],'iand':a[1]&b[1],'ior':a[1]|b[1]}[op]))
                else: st.append(('int',0))
            elif op=='i2c':
                a=st.pop() if st else ('int',0); st.append(('char',(a[1]&0xFFFF) if a[0]=='int' else 0))
            elif op=='castore':
                v=st.pop() if st else ('char',0); idx=st.pop() if st else ('int',0); arr=st.pop() if st else None
                if isinstance(arr,CA) and idx[0]=='int' and v[0]=='char': arr.d[idx[1]]=chr(v[1])
            elif op=='aastore':
                v=st.pop() if st else None; idx=st.pop() if st else ('int',0); arr=st.pop() if st else None
                if arr and arr[0]=='arr': arr[1].append(v)
            elif op=='invokevirtual' and 'append:(C)' in arg:
                v=st.pop() if st else ('char',0); t=st.pop() if st else None
                if isinstance(t,SB): t.buf.append(chr(v[1]) if v[0]=='char' else '\x00'); st.append(t)
                else: st.append(('obj',None))
            elif op=='invokevirtual' and 'append:(Ljava/lang/String;)' in arg:
                v=st.pop() if st else ('obj',''); t=st.pop() if st else None
                if isinstance(t,SB): t.buf.append(v[1] if v[0]=='str' and isinstance(v[1],str) else ''); st.append(t)
                else: st.append(('obj',None))
            elif op=='invokevirtual' and 'StringBuilder.toString' in arg:
                t=st.pop() if st else None
                s=''.join(t.buf) if isinstance(t,SB) else ''
                st.append(('str',s))
            elif op in ('invokevirtual','invokestatic','invokeinterface','invokespecial'):
                # record any string args passed
                args=[]
                if ':' in arg and '(' in arg:
                    pass
                if st: st.pop()
                st.append(('obj',None))
            elif op.startswith('if') or op in ('goto','return','areturn','athrow','nop','iinc','tableswitch','lookupswitch','ldc2_w','lmul','lxor','land','lor','lshl','lshr','ladd','lsub','l2i','lcmp','arraylength'):
                pass
        except Exception:
            st=[]
    # collect strings
    for v in st:
        if isinstance(v,tuple) and v[0]=='str' and isinstance(v[1],str) and len(v[1])>4: out.append((name,v[1]))
    for v in loc.values():
        if isinstance(v,tuple) and v[0]=='str' and isinstance(v[1],str) and len(v[1])>4: out.append((name,v[1]))
seen=set()
for n,s in out:
    if s in seen: continue
    seen.add(s); print(n+"\t"+s)
