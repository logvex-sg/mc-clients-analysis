/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11905
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 */
package w;

import com.water.module.c;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import w.G;
import w.I;
import w.l;

public class f
extends class_437 {
    private static final int W = 340;
    private static final int H = 380;
    private static final int PAD = 14;
    private static final int ROW_H = 42;
    private static final int ROWS = 4;
    private static final int MAX_CFG = 5;
    private static final float R = 16.0f;
    private static final float RR = 10.0f;
    private static final int BG = -15855080;
    private static final int SURFACE = -15459806;
    private static final int SURFACE2 = -15064528;
    private static final int BORDER = -14471362;
    private static final int ACCENT = -11886849;
    private static final int ACCENT2 = -14326805;
    private static final int TEXT = -1117441;
    private static final int TEXT2 = -7824982;
    private static final int TEXT3 = -12298906;
    private static final int GREEN = -14498466;
    private static final int GREEN_BG = -15914984;
    private static final int RED = -1096636;
    private static final int RED_BG = -13824498;
    private static final int AMBER = -680437;
    private static final int AMBER_BG = -13821184;
    private static final int SEL_BG = -14799547;
    private static final int SEL_BD = -12881964;
    private a tab = a.CONFIGS;
    private final List<String> configs = new ArrayList<String>();
    private int sel = 0;
    private int scroll = 0;
    private boolean newOpen = false;
    private boolean importOpen = false;
    private String newName = "";
    private String importCode = "";
    private String status = "";
    private int statusCol = -7824982;
    private long statusAt = 0L;
    private int px;
    private int py;

    private void t(class_332 class_3322, String string, int n2, int n3, int n4) {
        G.INSTANCE.a(class_3322, string, n2, n3, n4);
    }

    private void tc(class_332 class_3322, String string, int n2, int n3, int n4) {
        int n5 = G.INSTANCE.a(string);
        G.INSTANCE.a(class_3322, string, n2 - n5 / 2, n3, n4);
    }

    private int fw(String string) {
        return G.INSTANCE.a(string);
    }

    public f() {
        super((class_2561)class_2561.method_43470((String)"Water Configs"));
        w.a.a();
    }

    protected void method_25426() {
        this.px = (this.field_22789 - 340) / 2;
        this.py = (this.field_22790 - 380) / 2;
        this.refreshConfigs();
    }

    public void method_25394(class_332 class_3322, int n2, int n3, float f2) {
        G.D();
        I.a(class_3322, (float)this.px, (float)this.py, 340.0f, 380.0f, 16.0f, -15855080, false);
        I.a(class_3322, (float)this.px, (float)this.py, 340.0f, 380.0f, 16.0f, 1.0f, -14471362, false);
        this.renderHeader(class_3322, n2, n3);
        this.renderTabs(class_3322, n2, n3);
        if (this.tab == a.CONFIGS) {
            this.renderConfigs(class_3322, n2, n3);
        } else {
            this.renderShare(class_3322, n2, n3);
        }
        this.renderFooter(class_3322, n2, n3);
        if (!this.status.isEmpty() && System.currentTimeMillis() - this.statusAt < 3000L) {
            int n4 = this.fw(this.status) + 24;
            int n5 = this.px + 170 - n4 / 2;
            int n6 = this.py + 380 - 36;
            I.a(class_3322, (float)n5, (float)n6, (float)n4, 22.0f, 6.0f, -871756268, false);
            I.a(class_3322, (float)n5, (float)n6, (float)n4, 22.0f, 6.0f, 1.0f, this.statusCol & 0x55FFFFFF, false);
            this.tc(class_3322, this.status, this.px + 170, n6 + 7, this.statusCol);
        }
        if (this.newOpen) {
            this.renderNewPopup(class_3322, n2, n3);
        }
        if (this.importOpen) {
            this.renderImportPopup(class_3322, n2, n3);
        }
    }

    private void renderHeader(class_332 class_3322, int n2, int n3) {
        int n4 = this.py + 18;
        this.t(class_3322, "WATER CONFIGS", this.px + 14, n4, -1117441);
        String string = this.configs.size() + " / 5";
        int n5 = this.fw(string) + 16;
        I.a(class_3322, (float)(this.px + 340 - 14 - n5), (float)(n4 - 4), (float)n5, 20.0f, 5.0f, -15064528, false);
        this.tc(class_3322, string, this.px + 340 - 14 - n5 / 2, n4, -7824982);
        n2 = this.hov(n2, n3, this.px + 340 - 28, n4 - 4, 20, 20) ? 1 : 0;
        this.t(class_3322, "x", this.px + 340 - 22, n4, n2 != 0 ? -1117441 : -7824982);
        class_3322.method_25294(this.px + 14, this.py + 46, this.px + 340 - 14, this.py + 47, -14471362);
    }

    private void renderTabs(class_332 class_3322, int n2, int n3) {
        n2 = this.py + 56;
        String string = "MY CONFIGS";
        String string2 = "SHARE / IMPORT";
        boolean bl2 = this.tab == a.CONFIGS;
        int n4 = this.px + 14;
        int n5 = this.px + 14 + 130;
        I.a(class_3322, (float)(n4 - 6), (float)(n2 - 4), (float)(this.fw(string) + 12), 24.0f, 5.0f, bl2 ? -15064528 : 0, false);
        I.a(class_3322, (float)(n5 - 6), (float)(n2 - 4), (float)(this.fw(string2) + 12), 24.0f, 5.0f, !bl2 ? -15064528 : 0, false);
        this.t(class_3322, string, n4, n2 + 3, bl2 ? -1117441 : -7824982);
        this.t(class_3322, string2, n5, n2 + 3, !bl2 ? -1117441 : -7824982);
        n4 = bl2 ? n4 : n5;
        int n6 = bl2 ? this.fw(string) : this.fw(string2);
        I.a(class_3322, (float)n4, (float)(n2 + 22), (float)n6, 3.0f, 2.0f, -11886849, false);
        class_3322.method_25294(this.px + 14, n2 + 28, this.px + 340 - 14, n2 + 29, -14471362);
    }

    private void renderConfigs(class_332 class_3322, int n2, int n3) {
        int n4 = this.py + 100;
        if (this.configs.isEmpty()) {
            this.tc(class_3322, "No configs yet \u2014 create one below", this.px + 170, n4 + 168 / 2 - 6, -12298906);
            return;
        }
        for (int i2 = 0; i2 < 4; ++i2) {
            int n5;
            int n6 = i2 + this.scroll;
            if (n6 >= this.configs.size()) {
                if (n6 != this.configs.size() || this.configs.size() >= 5) continue;
                n5 = n4 + i2 * 42 + 4;
                I.a(class_3322, (float)(this.px + 14), (float)n5, 312.0f, 34.0f, 10.0f, 0x22FFFFFF, false);
                I.a(class_3322, (float)(this.px + 14), (float)n5, 312.0f, 34.0f, 10.0f, 1.0f, 0x22FFFFFF, false);
                this.tc(class_3322, "+ new config slot", this.px + 170, n5 + 17 - 5, -12298906);
                continue;
            }
            n5 = n4 + i2 * 42 + 4;
            int n7 = n6 == this.sel ? 1 : 0;
            int n8 = this.hov(n2, n3, this.px + 14, n5, 312, 34);
            n8 = n7 != 0 ? -14799547 : (n8 != 0 ? -15064528 : -15459806);
            int n9 = n7 != 0 ? -12881964 : -14471362;
            I.a(class_3322, (float)(this.px + 14), (float)n5, 312.0f, 34.0f, 10.0f, n8, false);
            I.a(class_3322, (float)(this.px + 14), (float)n5, 312.0f, 34.0f, 10.0f, 1.0f, n9, false);
            if (n7 != 0) {
                I.a(class_3322, (float)(this.px + 14), (float)(n5 + 8), 3.0f, 18.0f, 2.0f, -11886849, false);
            }
            this.t(class_3322, this.configs.get(n6), this.px + 14 + (n7 != 0 ? 14 : 10), n5 + 17 - 5, n7 != 0 ? -1117441 : -7824982);
            n6 = n5 + 17 - 10;
            n5 = this.px + 340 - 14 - 70;
            n7 = n5 - 80;
            n8 = this.hov(n2, n3, n7, n6, 70, 20) ? 1 : 0;
            I.a(class_3322, (float)n7, (float)n6, 70.0f, 20.0f, 5.0f, n8 != 0 ? -15914984 : -15459806, false);
            I.a(class_3322, (float)n7, (float)n6, 70.0f, 20.0f, 5.0f, 1.0f, n8 != 0 ? -14498466 : 0x33FFFFFF, false);
            this.tc(class_3322, "Reload", n7 + 35, n6 + 5, -14498466);
            n7 = this.hov(n2, n3, n5, n6, 70, 20) ? 1 : 0;
            I.a(class_3322, (float)n5, (float)n6, 70.0f, 20.0f, 5.0f, n7 != 0 ? -13824498 : -15459806, false);
            I.a(class_3322, (float)n5, (float)n6, 70.0f, 20.0f, 5.0f, 1.0f, n7 != 0 ? -1096636 : 0x33FFFFFF, false);
            this.tc(class_3322, "Delete", n5 + 35, n6 + 5, -1096636);
        }
    }

    private void renderShare(class_332 class_3322, int n2, int n3) {
        l l2 = l.a();
        int n4 = l2 != null && (l2.g || l2.h) ? 1 : 0;
        int n5 = this.px + 14;
        int n6 = this.py + 105;
        boolean bl2 = n4 == 0 && this.hov(n2, n3, n5, n6 += 8, 312, 36);
        I.a(class_3322, (float)n5, (float)n6, 312.0f, 36.0f, 10.0f, bl2 ? -14326805 : -15064528, false);
        I.a(class_3322, (float)n5, (float)n6, 312.0f, 36.0f, 10.0f, 1.0f, bl2 ? -11886849 : -14471362, false);
        String string = l2 != null && l2.g ? "Uploading..." : "Share Config";
        this.tc(class_3322, string, this.px + 170, n6 + 11, -1117441);
        n2 = n4 == 0 && this.hov(n2, n3, n5, n6 += 44, 312, 36) ? 1 : 0;
        I.a(class_3322, (float)n5, (float)n6, 312.0f, 36.0f, 10.0f, n2 != 0 ? -15064528 : -15459806, false);
        I.a(class_3322, (float)n5, (float)n6, 312.0f, 36.0f, 10.0f, 1.0f, n2 != 0 ? -11886849 : -14471362, false);
        String string2 = l2 != null && l2.h ? "Importing..." : "Import Config";
        this.tc(class_3322, string2, this.px + 170, n6 + 11, n2 != 0 ? -1117441 : -7824982);
        n6 += 48;
        if (l2 != null && !l2.c.isEmpty()) {
            String string3 = "Code: " + l2.c + "  (copied)";
            int n7 = this.fw(string3) + 24;
            n4 = this.px + 170 - n7 / 2;
            I.a(class_3322, (float)n4, (float)n6, (float)n7, 26.0f, 6.0f, -15064528, false);
            I.a(class_3322, (float)n4, (float)n6, (float)n7, 26.0f, 6.0f, 1.0f, -11886849, false);
            this.tc(class_3322, string3, this.px + 170, n6 + 8, -11886849);
        }
        if (l2 != null && !l2.d.isEmpty()) {
            this.tc(class_3322, l2.d, this.px + 170, this.py + 380 - 70, -1096636);
        }
    }

    private void renderFooter(class_332 class_3322, int n2, int n3) {
        int n4 = this.py + 380 - 54;
        class_3322.method_25294(this.px + 14, n4, this.px + 340 - 14, n4 + 1, -14471362);
        n4 += 14;
        if (this.tab == a.CONFIGS) {
            boolean bl2;
            int n5 = this.configs.size() >= 5 ? 1 : 0;
            boolean bl3 = bl2 = n5 == 0 && this.hov(n2, n3, this.px + 14, n4, 110, 18);
            this.t(class_3322, "+ New config", this.px + 14, n4 + 2, bl2 ? -11886849 : (n5 != 0 ? -12298906 : -7824982));
            n5 = this.px + 340 - 14 - 90;
            n2 = this.hov(n2, n3, n5, n4 - 4, 90, 26) ? 1 : 0;
            I.a(class_3322, (float)n5, (float)(n4 - 4), 90.0f, 26.0f, 6.0f, n2 != 0 ? -13821184 : -15459806, false);
            I.a(class_3322, (float)n5, (float)(n4 - 4), 90.0f, 26.0f, 6.0f, 1.0f, n2 != 0 ? -680437 : -14471362, false);
            this.tc(class_3322, "Reset all", n5 + 90 / 2, n4 + 2, -680437);
        }
    }

    private void renderNewPopup(class_332 class_3322, int n2, int n3) {
        int n4 = this.px + 170 - 340 / 2;
        int n5 = this.py + 190 - 130 / 2;
        I.a(class_3322, (float)n4, (float)n5, 340.0f, 130.0f, 12.0f, -15855080, false);
        I.a(class_3322, (float)n4, (float)n5, 340.0f, 130.0f, 12.0f, 1.0f, -14471362, false);
        this.tc(class_3322, "New Config Name", n4 + 340 / 2, n5 + 14, -1117441);
        I.a(class_3322, (float)(n4 + 16), (float)(n5 + 40), 308.0f, 30.0f, 6.0f, -15064528, false);
        I.a(class_3322, (float)(n4 + 16), (float)(n5 + 40), 308.0f, 30.0f, 6.0f, 1.0f, this.newName.isEmpty() ? -14471362 : -11886849, false);
        String string = this.newName.isEmpty() ? "Enter name..." : this.newName;
        this.t(class_3322, string, n4 + 26, n5 + 50, this.newName.isEmpty() ? -12298906 : -1117441);
        n4 = n4 + 340 / 2 - 55;
        n2 = this.hov(n2, n3, n4, n5 + 88, 110, 28) ? 1 : 0;
        I.a(class_3322, (float)n4, (float)(n5 + 88), 110.0f, 28.0f, 7.0f, n2 != 0 ? -14326805 : -15064528, false);
        I.a(class_3322, (float)n4, (float)(n5 + 88), 110.0f, 28.0f, 7.0f, 1.0f, n2 != 0 ? -11886849 : -14471362, false);
        this.tc(class_3322, "Create", n4 + 110 / 2, n5 + 97, -1117441);
    }

    private void renderImportPopup(class_332 class_3322, int n2, int n3) {
        int n4 = this.px + 170 - 380 / 2;
        int n5 = this.py + 190 - 130 / 2;
        I.a(class_3322, (float)n4, (float)n5, 380.0f, 130.0f, 12.0f, -15855080, false);
        I.a(class_3322, (float)n4, (float)n5, 380.0f, 130.0f, 12.0f, 1.0f, -14471362, false);
        this.tc(class_3322, "Enter Config Code", n4 + 380 / 2, n5 + 14, -1117441);
        I.a(class_3322, (float)(n4 + 16), (float)(n5 + 40), 348.0f, 30.0f, 6.0f, -15064528, false);
        I.a(class_3322, (float)(n4 + 16), (float)(n5 + 40), 348.0f, 30.0f, 6.0f, 1.0f, this.importCode.isEmpty() ? -14471362 : -11886849, false);
        String string = this.importCode.isEmpty() ? "Paste code here (Ctrl+V)..." : this.importCode;
        this.t(class_3322, string, n4 + 26, n5 + 50, this.importCode.isEmpty() ? -12298906 : -1117441);
        n4 = n4 + 380 / 2 - 55;
        n2 = this.hov(n2, n3, n4, n5 + 88, 110, 28) ? 1 : 0;
        I.a(class_3322, (float)n4, (float)(n5 + 88), 110.0f, 28.0f, 7.0f, n2 != 0 ? -14326805 : -15064528, false);
        I.a(class_3322, (float)n4, (float)(n5 + 88), 110.0f, 28.0f, 7.0f, 1.0f, n2 != 0 ? -11886849 : -14471362, false);
        this.tc(class_3322, "Import", n4 + 110 / 2, n5 + 97, -1117441);
    }

    public boolean method_25402(class_11909 class_119092, boolean bl2) {
        int n2 = (int)class_119092.comp_4798();
        int n3 = (int)class_119092.comp_4799();
        int n4 = this.px;
        int n5 = this.py;
        if (this.newOpen) {
            int n6 = n4 + 170 - 340 / 2;
            int n7 = n6 + 340 / 2 - 55;
            int n8 = n5 + 190 - 130 / 2;
            if (this.hov(n2, n3, n7, n8 + 88, 110, 28)) {
                this.doCreate();
                return true;
            }
            if (!this.hov(n2, n3, n6, n8, 340, 130)) {
                this.newOpen = false;
                return true;
            }
            return true;
        }
        if (this.importOpen) {
            int n9 = n4 + 170 - 380 / 2;
            int n10 = n9 + 380 / 2 - 55;
            int n11 = n5 + 190 - 130 / 2;
            if (this.hov(n2, n3, n10, n11 + 88, 110, 28)) {
                this.doImport();
                return true;
            }
            if (!this.hov(n2, n3, n9, n11, 380, 130)) {
                this.importOpen = false;
                return true;
            }
            return true;
        }
        if (this.hov(n2, n3, n4 + 340 - 28, n5 + 14, 20, 20)) {
            this.method_25419();
            return true;
        }
        if (this.hov(n2, n3, n4 + 14 - 6, n5 + 52, this.fw("MY CONFIGS") + 12, 28)) {
            this.tab = a.CONFIGS;
            return true;
        }
        if (this.hov(n2, n3, n4 + 14 + 124, n5 + 52, this.fw("SHARE / IMPORT") + 12, 28)) {
            this.tab = a.SHARE;
            return true;
        }
        if (this.tab == a.CONFIGS) {
            int n12;
            int n13;
            int n14;
            int n15 = n5 + 100;
            for (n14 = 0; n14 < 4 && (n13 = n14 + this.scroll) < this.configs.size(); ++n14) {
                int n16 = n4 + 340 - 14 - 70;
                int n17 = n16 - 80;
                int n18 = n15 + n14 * 42 + 4;
                n12 = n18 + 17 - 10;
                if (this.hov(n2, n3, n17, n12, 70, 20)) {
                    this.sel = n13;
                    this.doReload();
                    return true;
                }
                if (this.hov(n2, n3, n16, n12, 70, 20)) {
                    this.sel = n13;
                    this.doDelete();
                    return true;
                }
                if (!this.hov(n2, n3, n4 + 14, n18, 312, 34)) continue;
                this.sel = n13;
                return true;
            }
            n14 = n5 + 380 - 54 + 14;
            int n19 = n13 = this.configs.size() >= 5 ? 1 : 0;
            if (n13 == 0 && this.hov(n2, n3, n4 + 14, n14, 110, 18)) {
                this.newOpen = true;
                this.newName = "";
                return true;
            }
            if (n13 != 0 && this.hov(n2, n3, n4 + 14, n14, 110, 18)) {
                this.setStatus("Max 5 configs!", -1096636);
                return true;
            }
            n12 = n4 + 340 - 14 - 90;
            if (this.hov(n2, n3, n12, n14 - 4, 90, 26)) {
                this.doReset();
                return true;
            }
        } else {
            l l2 = l.a();
            boolean bl3 = l2 != null && (l2.g || l2.h);
            int n20 = n4 + 14;
            int n21 = n5 + 127;
            if (!bl3 && this.hov(n2, n3, n20, n21, 312, 36)) {
                this.doShare();
                return true;
            }
            if (!bl3 && this.hov(n2, n3, n20, n21 + 36 + 8, 312, 36)) {
                this.importOpen = true;
                this.importCode = "";
                return true;
            }
        }
        return super.method_25402(class_119092, bl2);
    }

    public boolean method_25401(double d2, double d3, double d4, double d5) {
        int n2 = Math.max(0, this.configs.size() - 4);
        this.scroll = (int)Math.max(0.0, Math.min((double)n2, (double)this.scroll - d5));
        return true;
    }

    public boolean method_25404(class_11908 object) {
        int n2 = object.method_74228();
        if (this.newOpen) {
            if (n2 == 256) {
                this.newOpen = false;
                return true;
            }
            if (n2 == 257) {
                this.doCreate();
                return true;
            }
            if (n2 == 259 && !this.newName.isEmpty()) {
                this.newName = this.newName.substring(0, this.newName.length() - 1);
                return true;
            }
            return true;
        }
        if (this.importOpen) {
            if (n2 == 256) {
                this.importOpen = false;
                return true;
            }
            if (n2 == 257) {
                this.doImport();
                return true;
            }
            if (n2 == 259 && !this.importCode.isEmpty()) {
                this.importCode = this.importCode.substring(0, this.importCode.length() - 1);
                return true;
            }
            if (object.method_74243()) {
                object = class_310.method_1551().field_1774.method_1460();
                if (object != null) {
                    this.importCode = ((String)object).trim();
                }
                return true;
            }
            return true;
        }
        if (n2 == 256) {
            this.method_25419();
            return true;
        }
        return super.method_25404((class_11908)object);
    }

    public boolean method_25400(class_11905 class_119052) {
        String string = class_119052.method_74226();
        if (this.newOpen && this.newName.length() < 24) {
            this.newName = this.newName + string;
            return true;
        }
        if (this.importOpen && this.importCode.length() < 64) {
            this.importCode = this.importCode + string;
            return true;
        }
        return super.method_25400(class_119052);
    }

    private void doReload() {
        c.INSTANCE.j();
        this.setStatus("Config reloaded!", -14498466);
    }

    private void doDelete() {
        if (this.configs.isEmpty()) {
            return;
        }
        try {
            Object object = class_310.method_1551();
            if (object != null && ((File)(object = new File(((class_310)object).field_1697, "water_config_" + this.configs.get(this.sel) + ".txt"))).exists()) {
                ((File)object).delete();
            }
        }
        catch (Exception exception) {}
        this.configs.remove(this.sel);
        if (this.sel >= this.configs.size()) {
            this.sel = Math.max(0, this.configs.size() - 1);
        }
        this.setStatus("Config deleted.", -1096636);
    }

    private void doReset() {
        c.INSTANCE.getModules().forEach(b2 -> {
            if (b2.isEnabled()) {
                b2.toggle();
            }
        });
        this.setStatus("All modules disabled!", -680437);
    }

    private void doShare() {
        l l2 = l.a();
        if (l2 == null) {
            return;
        }
        this.setStatus("Uploading...", -680437);
        l2.a(() -> {
            if (l2.d.isEmpty()) {
                this.setStatus("Code copied: " + l2.c, -14498466);
            } else {
                this.setStatus(l2.d, -1096636);
            }
        });
    }

    private void doImport() {
        if (this.importCode.trim().isEmpty()) {
            this.setStatus("Paste a code first!", -1096636);
            return;
        }
        l l2 = l.a();
        if (l2 == null) {
            return;
        }
        this.setStatus("Loading...", -680437);
        l2.a(this.importCode.trim(), () -> {
            this.importOpen = false;
            if (l2.d.isEmpty()) {
                this.setStatus("Config imported!", -14498466);
            } else {
                this.setStatus(l2.d, -1096636);
            }
        });
    }

    private void doCreate() {
        if (this.configs.size() >= 5) {
            this.setStatus("Max 5 configs!", -1096636);
            return;
        }
        if (this.newName.trim().isEmpty()) {
            this.setStatus("Enter a name first!", -1096636);
            return;
        }
        String string = this.newName.trim();
        try {
            Object object = class_310.method_1551();
            if (object != null && !((File)(object = new File(((class_310)object).field_1697, "water_config_" + string + ".txt"))).exists()) {
                ((File)object).createNewFile();
            }
        }
        catch (Exception exception) {}
        if (!this.configs.contains(string)) {
            this.configs.add(string);
        }
        this.newOpen = false;
        this.sel = this.configs.indexOf(string);
        this.setStatus("\"" + string + "\" created!", -14498466);
    }

    private void refreshConfigs() {
        this.configs.clear();
        try {
            File[] fileArray = class_310.method_1551();
            if (fileArray != null && (fileArray = fileArray.field_1697.listFiles(file -> file.isFile() && file.getName().startsWith("water_config_") && file.getName().endsWith(".txt"))) != null) {
                for (File file2 : fileArray) {
                    this.configs.add(file2.getName().replace("water_config_", "").replace(".txt", ""));
                }
            }
        }
        catch (Exception exception) {}
        this.sel = 0;
    }

    private void setStatus(String string, int n2) {
        this.status = string;
        this.statusCol = n2;
        this.statusAt = System.currentTimeMillis();
    }

    private boolean hov(int n2, int n3, int n4, int n5, int n6, int n7) {
        return n2 >= n4 && n2 <= n4 + n6 && n3 >= n5 && n3 <= n5 + n7;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25420(class_332 class_3322, int n2, int n3, float f2) {
    }

    private static enum a {
        CONFIGS,
        SHARE;

    }
}

