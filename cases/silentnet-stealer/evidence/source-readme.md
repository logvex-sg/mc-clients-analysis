# SilentNet Stealer — Uçtan Uca Teknik Analiz, Deobfuscation ve IOC Paketi

> **Uyarı / Disclaimer:** Bu depo yalnızca **siber güvenlik araştırması, tehdit istihbaratı (Threat Intelligence), tespit mühendisliği (SOC/SIEM/YARA) ve eğitim** amacıyla paylaşılmıştır. GitHub Hizmet Şartları gereği canlı zararlı ikili dosyalar (`.exe`, `.pyd`, `.jar`) depoya **dahil edilmemiştir**; yalnızca SHA-256 özetleri, statik analiz raporları, deobfuscator araçları ve deobfuscate edilmiş / yeniden yapılandırılmış kaynak kodları yer almaktadır.

---

## 1. Yönetici Özeti (Executive Summary)

**SilentNet**, çok aşamalı (multi-stage) dağıtım mimarisi kullanan ve kurban sistemlerdeki tarayıcı kimlik bilgilerini (Chrome, Edge, Opera GX), oturum çerezlerini, Discord tokenlarını, Steam/Riot/Roblox oturumlarını ve özellikle **Minecraft (Fabric / Lunar / Feather / Modrinth)** hesaplarını hedefleyen gelişmiş bir bilgi hırsızıdır (infostealer).

Bu araştırmada aynı kampanyaya (**Campaign UUID:** `fa357567-049b-426b-96ec-7d8488629333`) ait **üç farklı bileşen** uçtan uca analiz edilmiş ve kaynak seviyesinde geri kazanılmıştır:

| Aşama / Bileşen | Dosya Adı | Tür / Derleyici | SHA-256 | Kaynak Kurtarma Durumu |
|---|---|---|---|---|
| **Stage-0 Native Loader** | `Launcher.exe` | PE32+ x86-64 (**Zig 0.12.1**) | `79420c6623177c1d48fb87b6e32c0cd06f3360fce406037506872b9b1b535006` | XOR config çözücü (`tools/extract_config.py`) + Ghidra decompile + Zig pseudocode (`src/windows-loader-zig/`) |
| **Stage-1/2 Java & Fabric Portu** | `mod.jar` | Java Archive (Fabric Loom / Standalone) | `9ae06f129d04f63a639b48d6d293f5e89079614cbb22d042c203bb0843e81944` | **%100 Deobfuscate Edilmiş Java Kaynak Kodu** (`src/mod-jar/java/`, `javac` ile derlenebilir) |
| **Stage-2 Native Python Stealer** | `AppHost\app.pyd` | PE32+ DLL (**Nuitka** `PyInit_app`) | `548ffea3467f9a2984e247edc3dc3e5c5be9fad507cc3fc1c23b6b3826a12c7a` | Tam `main.py` + `requirements.txt` + davranışsal Python rekonstrüksiyonu (`src/windows-python/`) |

---

## 2. Saldırı Zinciri ve Mimari (Attack Chain)

```text
┌────────────────────────────────────────────────────────────────────────────┐
│ KANAL A: Native Zig Loader (Launcher.exe)                                  │
├────────────────────────────────────────────────────────────────────────────┤
│ 1. Anti-VM / Sandbox Kontrolleri:                                          │
│    - CPU: "Intel Core Processor (Broadwell)" kontrolü                      │
│    - Donanım: >= 6 çekirdek, RAM & C:\ boş disk eşiği, GetTickCount64      │
│ 2. XOR Yapılandırma Çözümü (plain[i] = cipher[i] ^ key[i & 0x0f])          │
│ 3. Blockchain C2 Çözümleme (EtherHiding):                                  │
│    - Polygon Mainnet Contract: 0x9c0a507300fd902787bb193d80fca5ce6e1bff9a  │
│    - eth_call Selector: 0xce6d41de  ──►  "thisisafalsepositive.st"         │
│ 4. Gömülü Python Kurulumu (%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex)│
│    - python-3.12.7-embed-amd64.zip + get-pip.py                            │
│    - curl -X PUT ile CDN'den requirements.txt ve AppHost\app.pyd indirme   │
│    - Header: x-cdn-origin-verify (secret / trusted-upstream)               │
└────────────────────────────────────┬───────────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ KANAL B: Java / Minecraft Fabric Sideload (mod.jar)                        │
├────────────────────────────────────────────────────────────────────────────┤
│ 1. Giriş: Standalone (uwkRopogVyp -> StandaloneMain) veya                  │
│    Minecraft Fabric Mod (ArKZkARk -> FabricModEntry, sahte id: "library")  │
│ 2. Sistem & MC Oturum Keşfi (ayjnnnDriver -> SystemFingerprint):           │
│    - Reflection ile Minecraft instance/session, username, UUID, accessToken│
│ 3. Ağ & C2 (zjqaaMltlo -> RawHttpClient & readIglpzeg -> StealerCore):     │
│    - Cloudflare DoH (1.1.1.1:443 / cloudflare-dns.com) ile DNS bypass      │
│    - TrustAllManager ile TLS sertifika doğrulama bypass'ı                  │
│    - Polygon eth_call (0xce6d41de) ile aktif C2 domain çözümleme           │
│    - POST /shard/prefireMc ──► prefireId alma                              │
│ 4. PNG Steganografi ile Taşınabilir Runtime İndirme:                       │
│    - GET https://<c2>/cdn/v2/9f4e7a2c1b8d.png                              │
│    - PNG içindeki özel "hIds" chunk'ını Inflater + ZipInputStream ile açma │
│ 5. Child Process & Telemetri Exfil:                                        │
│    - NtProfileIndex\python.exe AppHost\main.py -u <uuid> --env ...         │
│    - POST /shard/submitMinecraftLog (combined.log + runtime + MC log)      │
└────────────────────────────────────┬───────────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ STAGE-2: Nuitka Python Stealer (AppHost\main.py ──► app.pyd)               │
├────────────────────────────────────────────────────────────────────────────┤
│ - Chromium (Chrome, Edge, Opera GX): Local State (DPAPI) + Login Data /    │
│   Cookies (AES-GCM, SQLite)                                                │
│ - Oyun & Uygulama: Discord Tokens, Steam (vdf), Riot, Roblox, Minecraft    │
│ - Ekran görüntüsü (screenshot.jpg) + Sistem/IP özeti (INFO.txt)            │
│ - ZIP Arşivleme & C2 Paneline Exfiltration                                 │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Depo Yapısı (Repository Layout)

```text
.
├── README.md                                  # Ana teknik özet (bu belge)
├── .gitignore                                 # Zararlı ikili dosyaları (.exe/.jar/.pyd) engeller
├── reports/                                   # Detaylı Türkçe teknik analiz raporları
│   ├── 01-launcher-zig-rapor.md               # Stage-0 Zig Loader (Launcher.exe) analizi
│   ├── 02-stage2-python-nuitka-rapor.md       # Stage-2 NtProfileIndex & Nuitka app.pyd analizi
│   ├── 03-mod-jar-java-rapor.md               # Java/Fabric mod.jar mimari ve sınıf analizi
│   ├── 04-exfil-ve-c2-protokolu.md            # DoH, ham TLS soket, PNG steganografi ve HTTP istekleri
│   ├── 05-decompile-entry-FUN_00401011.md     # Ghidra/REA Launcher.exe giriş fonksiyonu decompile
│   └── 06-decompile-http-FUN_00413c78.md      # Ghidra/REA HTTP yanıt okuma fonksiyonu decompile
├── ioc/                                       # Tehdit İstihbaratı ve Tespit Kuralları
│   ├── ioc-blocklist.txt                      # Firewall / EDR / SIEM için satır satır IOC listesi
│   ├── decrypted-strings.json                 # Launcher.exe içinden XOR ile çözülen tüm sabitler
│   ├── eth-call-payload.json                  # Polygon C2 çözümleme JSON-RPC yükü
│   └── silentnet.yar                          # YARA tespit kuralları (Zig Loader, mod.jar, app.pyd)
├── src/                                       # Kurtarılan ve Deobfuscate Edilen Kaynak Kodlar
│   ├── mod-jar/                               # %100 Deobfuscate edilmiş, derlenebilir Java kaynakları
│   │   ├── java/net/silentnet/stealer/
│   │   │   ├── StandaloneMain.java            # (Orijinal: uwkRopogVyp) Çift tıklama / child JVM başlatıcı
│   │   │   ├── StealerCore.java               # (Orijinal: readIglpzeg) C2, PNG hIds açıcı, süreç yönetimi
│   │   │   ├── RawHttpClient.java             # (Orijinal: zjqaaMltlo) DoH (1.1.1.1), TrustAll TLS, raw HTTP
│   │   │   ├── SystemFingerprint.java         # (Orijinal: ayjnnnDriver) Reflection ile MC token/session avcısı
│   │   │   └── FabricModEntry.java            # (Orijinal: ArKZkARk) Minecraft Fabric ModInitializer girişi
│   │   ├── build.gradle.kts
│   │   ├── SOURCE_COMPLETENESS.md
│   │   └── README.md
│   ├── windows-loader-zig/                    # Launcher.exe davranışsal Zig rekonstrüksiyonu
│   │   ├── main.zig
│   │   ├── crypto.zig
│   │   ├── anti_analysis.zig
│   │   ├── http_client.zig
│   │   ├── embedded_config.zig
│   │   └── FUN_00413c78.c
│   └── windows-python/                        # Stage-2 Python stealer katmanı
│       ├── main.py                            # Orijinal 147 bayt giriş betiği
│       ├── requirements.txt                   # CDN #1'den yakalanan pip bağımlılık listesi
│       ├── app_recovered.py                   # Nuitka app.pyd davranışsal Python rekonstrüksiyonu
│       └── SOURCE_RECOVERY.md
└── tools/                                     # Analiz ve Deobfuscation Araçları
    ├── extract_config.py                      # Launcher.exe PE .rdata XOR yapılandırma çözücü
    ├── fast_deobf.py                          # JADX çıktısı için 1. aşama char[] ve sınıf deobfuscator
    ├── full_deobf_strings.py                  # 2. aşama tam StringBuilder & char-aritmetik çözücü
    └── clean_obfuscation.py                   # 3. aşama sahte kontrol akışı (opaque predicate) temizleyici
```

---

## 4. Öne Çıkan Teknik Bulgular

### 4.1 Polygon Blockchain Üzerinden C2 Çözümleme (EtherHiding)
Hem `Launcher.exe` (Zig) hem de `mod.jar` (`StealerCore.getDomain()`), sabit bir C2 adresine güvenmek yerine Polygon Mainnet üzerindeki `0x9c0a507300fd902787bb193d80fca5ce6e1bff9a` akıllı sözleşmesine `0xce6d41de` fonksiyon seçicisiyle `eth_call` isteği gönderir. Sözleşme yanıtındaki ABI-encoded string çözümlendiğinde aktif C2/CDN alan adı (`thisisafalsepositive.st`) elde edilir.

### 4.2 Java `mod.jar` String Obfuscation ve Tam Deobfuscation
Orijinal `mod.jar` içindeki tüm sabit dizeler, yüzlerce satırlık iç içe `StringBuilder` ve `(char) (a ^ b)` / `(char) (a + b)` / `(char) (a - b)` aritmetik ifadeleriyle gizlenmişti. Ayrıca sahte `if ((1621021143 & 1621021143) == 1621021143)` gibi *opaque predicate* blokları eklenmişti.
- `tools/` altındaki 3 aşamalı Python pipeline'ı (`fast_deobf.py` → `full_deobf_strings.py` → `clean_obfuscation.py`) tüm karakter aritmetiğini ve yapay `StringBuilder` zincirlerini **sıfır veri kaybıyla** düz metne indirgemiştir.
- Elde edilen `src/mod-jar/java/` kaynakları `javac` ile **hatasız derlenmektedir**.

### 4.3 PNG `hIds` Chunk Steganografisi
`StealerCore.crwmaiBi()` metodu, C2 sunucusundan `https://<c2>/cdn/v2/9f4e7a2c1b8d.png` dosyasını standart bir tarayıcı görsel isteği (`Sec-Fetch-Dest: image`) gibi indirir. `taTDj()` metodu PNG imzasını doğruladıktan sonra PNG chunk'larını tarar ve **`hIds`** (`0x68 0x49 0x64 0x73`) adlı özel chunk'ın içindeki deflate-sıkıştırmalı ZIP arşivini bellekte açarak `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex` dizinine taşınabilir Python çalışma zamanını ve `AppHost` dosyalarını çıkartır.

### 4.4 DoH (DNS-over-HTTPS) ve Ham Soket / Trust-All TLS
`RawHttpClient.java`, işletim sistemi DNS çözümleyicisini tamamen atlayarak `1.1.1.1:443` (`cloudflare-dns.com`) üzerinden ham DNS sorgusu (`application/dns-message`) gönderir ve sonucu 5 dakika boyunca bellekte önbelleğe alır. Tüm HTTPS bağlantıları, sunucu sertifikalarını doğrulamayan `TrustAllManager` üzerinden ham `SSLSocket` ile gerçekleştirilir.

---

## 5. Temel IOC Özeti (Indicators of Compromise)

### Dosya Hash'leri (SHA-256)
```text
79420c6623177c1d48fb87b6e32c0cd06f3360fce406037506872b9b1b535006  Launcher.exe (Zig 0.12.1 Loader)
9ae06f129d04f63a639b48d6d293f5e89079614cbb22d042c203bb0843e81944  mod.jar (Java/Fabric Stealer)
548ffea3467f9a2984e247edc3dc3e5c5be9fad507cc3fc1c23b6b3826a12c7a  app.pyd (Nuitka Python Stealer)
56aec3b560f70e6cb88de9ef1abd252a4c0c90ae3bb6ed269d89cd6116257334  main.py
604c41be9d7d5289578a88ad4473a1369d5b56449f917d27230e054fc829cfe7  requirements.txt
```

### Ağ ve C2 Göstergeleri
- **C2 / CDN Alan Adı:** `thisisafalsepositive.st`
- **Polygon Sözleşmesi:** `0x9c0a507300fd902787bb193d80fca5ce6e1bff9a` (Selector: `0xce6d41de`)
- **Kampanya UUID:** `fa357567-049b-426b-96ec-7d8488629333`
- **HTTP Yolları:**
  - `/cdn/5ef451455923` (`PUT` — `requirements.txt`)
  - `/cdn/e/42a522313d92` (`PUT` — Stage-2 yardımcı modül)
  - `/cdn/e/36f2c0035ef9` (`PUT` — `AppHost\app.pyd`)
  - `/cdn/v2/9f4e7a2c1b8d.png` (`GET` — PNG `hIds` steganografi paketi)
  - `/shard/prefireMc` (`POST` — Minecraft oturum ön-kayıt)
  - `/shard/submitMinecraftLog` (`POST` — Telemetri ve log sızdırma)
- **Özel HTTP Başlıkları:**
  - `x-cdn-origin-verify: dK9mT3nR7xQ2pL8wF4jH6yB1cN5gA0sZ12345678abc=` (veya `trusted-upstream`)
  - `X-Runtime-Env: jre-embedded`
  - `X-Edge-Cache-Revalidate: stale-if-error`

### Host / Dosya Sistemi Göstergeleri
- `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex\`
- `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex\AppHost\app.pyd`
- `%LOCALAPPDATA%\Microsoft\Windows\NtProfileIndex\_spawn.log` ve `_stealer.log`

---

## 6. MITRE ATT&CK Eşleştirmesi

| Taktik | Teknik ID | Teknik Adı | Gözlenen Davranış |
|---|---|---|---|
| Execution | `T1204.002` | Malicious File | Sahte `Launcher.exe` ve Minecraft `mod.jar` (`id: library`) |
| Execution | `T1059.006` | Python | Taşınabilir CPython 3.12.7 + `AppHost\main.py` |
| Defense Evasion | `T1027.013` | Encrypted/Encoded File | 16-bayt XOR blob'ları (`Launcher.exe`), `char` aritmetiği (`mod.jar`), PNG `hIds` chunk |
| Defense Evasion | `T1497.001` | System Checks | Broadwell CPU kontrolü, 6 çekirdek, RAM/Disk eşiği ve `GetTickCount64` |
| Command & Control | `T1102.002` | Bidirectional Communication | Polygon akıllı sözleşmesi (`0xce6d41de`) üzerinden C2 domain çözümleme |
| Command & Control | `T1071.004` | DNS | Cloudflare DoH (`1.1.1.1:443`) ile yerel DNS izlemesini atlatma |
| Credential Access | `T1555.003` | Credentials from Web Browsers | Chrome, Edge, Opera GX `Login Data` ve `Cookies` (DPAPI + AES-GCM) |
| Collection | `T1113` | Screen Capture | `pillow` üzerinden masaüstü ekran görüntüsü (`screenshot.jpg`) |
| Exfiltration | `T1041` | Exfiltration Over C2 Channel | Ham TLS soketi (`TrustAllManager`) ve ZIP/JSON POST ile veri sızdırma |
