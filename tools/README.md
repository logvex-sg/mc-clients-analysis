# tools

Shared analysis scripts, reusable across cases.

| Script | Purpose |
|---|---|
| `decode_strings.py` | Deobfuscator for the SoberClient family. Rebuilds string constants assembled per character in bytecode (`ldc/sipush int, int -> isub/ixor/iadd -> i2c -> append(char)`). |

## decode_strings.py

Decodes a `javap` disassembly (`BYTECODE_DISASSEMBLY.txt`) and prints recovered strings in order,
grouped by method, with `### CLASS` / `-- <method>` markers.

```bash
python3 tools/decode_strings.py <start_line> <end_line>   # run from the dir holding the disassembly
```

Adjust the input filename at the top of the script for other artefacts.
