/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_437
 *  net.minecraft.class_442
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.example.compat.a.c.c;

import com.example.compat.a.a.g.e.BindA;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import net.minecraft.class_442;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_442.class})
public abstract class CheckI
extends class_437 {
    private static final String a = "mc_core";
    @Unique
    private static final class_2960 b = class_2960.method_60655((String)"mc_core", (String)"textures/icon.png");
    @Unique
    private float c = 0.0f;
    @Unique
    private class_2960 d = null;
    @Unique
    private String e = "";

    protected CheckI(class_2561 class_25612) {
        super(class_25612);
    }

    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo callbackInfo) {
        this.updateHeadTexture();
    }

    @Unique
    private void updateHeadTexture() {
        if (this.field_22787 == null || this.field_22787.method_1548() == null) {
            return;
        }
        String string = this.field_22787.method_1548().method_1676();
        if (string == null || string.trim().isEmpty()) {
            return;
        }
        if (string.equals(this.e) && this.d != null) {
            return;
        }
        this.e = string;
        this.d = null;
        this.fetchHeadTexture(string);
    }

    @Unique
    private void fetchHeadTexture(String string) {
        CompletableFuture.runAsync(() -> this.lambda$fetchHeadTexture$2(string));
    }

    /*
     * Loose catch block
     */
    @Inject(method={"method_25394"}, at={@At(value="TAIL")})
    private void onRender(class_332 class_3322, int n, int n2, float f2, CallbackInfo callbackInfo) {
        class_2960 class_29602;
        boolean bl;
        int n3;
        block24: {
            block23: {
                float f3;
                boolean bl2;
                int n4;
                int n5;
                block22: {
                    block21: {
                        block26: {
                            block25: {
                                this.updateHeadTexture();
                                n3 = 24;
                                int n6 = 10;
                                n5 = class_3322.method_51421() - n3 - n6;
                                n4 = n6;
                                if (n < n5) break block21;
                                if (n > n5 + n3) break block21;
                                break block25;
                                catch (Exception exception) {
                                    throw CheckI.a(exception);
                                }
                            }
                            if (n2 < n4) break block21;
                            break block26;
                            catch (Exception exception) {
                                throw CheckI.a(exception);
                            }
                        }
                        try {
                            block27: {
                                if (n2 > n4 + n3) break block21;
                                break block27;
                                catch (Exception exception) {
                                    throw CheckI.a(exception);
                                }
                            }
                            bl2 = true;
                            break block22;
                        }
                        catch (Exception exception) {
                            throw CheckI.a(exception);
                        }
                    }
                    bl2 = false;
                }
                bl = bl2;
                try {
                    f3 = bl ? 1.0f : 0.0f;
                }
                catch (Exception exception) {
                    throw CheckI.a(exception);
                }
                float f4 = f3;
                this.c = class_3532.method_16439((float)(f2 * 0.2f), (float)this.c, (float)f4);
                class_3322.method_51448().pushMatrix();
                class_3322.method_51448().translate((float)((double)n5 + (double)n3 / 2.0), (float)((double)n4 + (double)n3 / 2.0));
                float f5 = 1.0f + this.c * 0.15f;
                class_3322.method_51448().scale(f5, f5);
                if (this.c > 0.0f) {
                    int n7 = (int)(this.c * 100.0f);
                    int n8 = n7 << 24 | 0xFF0000;
                    BindA.b(class_3322, 0, 0, n3, n8, 0xFF0000);
                }
                if (this.d == null) break block23;
                try {
                    block28: {
                        if (this.field_22787.method_1531().method_4619(this.d) == null) break block23;
                        break block28;
                        catch (Exception exception) {
                            throw CheckI.a(exception);
                        }
                    }
                    class_29602 = this.d;
                    break block24;
                }
                catch (Exception exception) {
                    throw CheckI.a(exception);
                }
            }
            class_29602 = b;
        }
        class_2960 class_29603 = class_29602;
        try {
            if (this.field_22787.method_1531().method_4619(class_29603) != null) {
                BindA.a(class_3322, class_29603, -n3 / 2, -n3 / 2, n3, n3);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            class_3322.method_51448().popMatrix();
            if (bl) {
                class_3322.method_51438(this.field_22793, (class_2561)class_2561.method_43470((String)"\u00a7cManage Accounts"), n, n2);
            }
        }
        catch (Exception exception) {
            throw CheckI.a(exception);
        }
    }

    @Inject(method={"method_25402"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(class_11909 class_119092, boolean bl, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
    }

    private void lambda$fetchHeadTexture$2(String string) {
        try {
            String string2 = "https://minotar.net/helm/" + string + "/64.png";
            HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string2).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            if (httpURLConnection.getResponseCode() != 200) {
                return;
            }
            InputStream inputStream = httpURLConnection.getInputStream();
            class_1011 class_10112 = class_1011.method_4309((InputStream)inputStream);
            inputStream.close();
            class_2960 class_29602 = class_2960.method_60655((String)a, (String)("account/head/" + string.toLowerCase()));
            this.field_22787.execute(() -> this.lambda$fetchHeadTexture$1(class_29602, class_10112));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void lambda$fetchHeadTexture$1(class_2960 class_29602, class_1011 class_10112) {
        try {
            this.field_22787.method_1531().method_4616(class_29602, (class_1044)new class_1043(CheckI::lambda$fetchHeadTexture$0, class_10112));
            this.d = class_29602;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static String lambda$fetchHeadTexture$0() {
        return "dynamic";
    }

    private static Exception a(Exception exception) {
        return exception;
    }
}

