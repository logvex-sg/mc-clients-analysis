//! XOR çözümü — Ghidra: plain[i] = cipher[i] ^ key[i & 0x0f]
//! Kanıt: decompile-entry.md (0x401237–0x401257 ve benzeri döngüler)

pub fn decrypt_mod16(cipher: []const u8, key16: *const [16]u8) []u8 {
    var out: [512]u8 = undefined;
    for (cipher, 0..) |c, i| {
        out[i] = c ^ key16[i & 0x0f];
    }
    return out[0..cipher.len];
}

pub fn decrypt_plain(cipher: []const u8, key: []const u8) []u8 {
    var out: [64]u8 = undefined;
    for (cipher, 0..) |c, i| {
        out[i] = c ^ key[i];
    }
    return out[0..cipher.len];
}

/// PE tablosundaki `L` tampon boyutu; gerçek metin genelde L-1 bayt (extract_config.py).
pub fn effective_len(table_len: usize) usize {
    return if (table_len > 0) table_len - 1 else 0;
}
