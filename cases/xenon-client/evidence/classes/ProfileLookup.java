/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.class_10538
 *  net.minecraft.class_12079$class_12081
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_7920
 *  net.minecraft.class_8685
 */
package com.xenon.c.a.d;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xenon.d.j;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_10538;
import net.minecraft.class_12079;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7920;
import net.minecraft.class_8685;

public final class i
extends com.xenon.c.c {
    private static final Duration HTTP_TIMEOUT;
    private static final long INPUT_DEBOUNCE_MS = 600L;
    private static final String PRIMARY_LOOKUP_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String FALLBACK_LOOKUP_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/";
    private static final String PROFILE_LOOKUP_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    private static final HttpClient HTTP_CLIENT;
    private static volatile class_8685 overrideSkin;
    private static volatile class_12079.class_12081 overrideTextureAsset;
    private final j<String> playerName = new j<String>(i.qz(false, -389807054, '\u9bed', -1395340801), "");
    private final AtomicInteger requestGeneration = new AtomicInteger();
    private class_10538 skinDownloader;
    private String lastObservedName = "";
    private String lastRequestedName = "";
    private long lastNameEditAt;
    private static String[] pn;
    private static Object[] eu;
    transient String myMetaphor = "myMetaphor @ jnt.so";

    public i() {
        super(i.qz(false, -389807053, '\u3426', 617228872), com.xenon.c.b.MISC);
        this.addSetting(this.playerName);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.lastObservedName = i.normalizeName(this.playerName.getValue());
        this.lastRequestedName = "";
        this.lastNameEditAt = System.currentTimeMillis();
        if (this.lastObservedName.isEmpty()) {
            i.clearOverride();
            return;
        }
        this.requestSkin(this.lastObservedName);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.requestGeneration.incrementAndGet();
        this.lastRequestedName = "";
        i.clearOverride();
    }

    @Override
    public void onTick() {
        block3: {
            String string = i.normalizeName(this.playerName.getValue());
            if (!Objects.equals(string, this.lastObservedName)) {
                this.lastObservedName = string;
                this.lastNameEditAt = System.currentTimeMillis();
                return;
            }
            if (string.isEmpty()) {
                if (overrideSkin != null || overrideTextureAsset != null) {
                    this.lastRequestedName = "";
                    i.clearOverride();
                }
                return;
            }
            if (Objects.equals(string, this.lastRequestedName) || System.currentTimeMillis() - this.lastNameEditAt < 600L) break block3;
            this.requestSkin(string);
        }
    }

    public static class_8685 getOverrideSkin(UUID uUID) {
        if (overrideSkin == null || uUID == null) {
            return null;
        }
        UUID uUID2 = i.getLocalPlayerUuid();
        return uUID2 != null && uUID2.equals(uUID) ? overrideSkin : null;
    }

    private void requestSkin(String string) {
        this.lastRequestedName = string;
        int n = this.requestGeneration.incrementAndGet();
        ((CompletableFuture)CompletableFuture.supplyAsync(() -> this.lookupSkin(string), class_156.method_27958().method_64116(i.qz(false, -276376225, '\u6fbe', -1767008798))).thenCompose(b2 -> this.getSkinDownloader().method_65861(this.createTextureId((b)b2), this.getCacheFile(b2.uuid()), b2.textureUrl(), true).thenApply(class_120812 -> new a((b)b2, (class_12079.class_12081)class_120812)))).whenComplete((a2, throwable) -> mc.execute(() -> {
            if (n != this.requestGeneration.get() || !this.isEnabled()) {
                if (a2 != null) {
                    i.destroyTexture(a2.textureAsset());
                }
                return;
            }
            if (throwable != null) {
                String string2 = i.getRootMessage(throwable);
                String string3 = string;
                this.sendFeedback(i.qz(false, -443886047, '\u614f', -1438397018) + string3 + i.qz(false, -443886042, '\u6e14', 2043405778) + string2);
                return;
            }
            i.applyOverride(a2.textureAsset(), a2.lookup().skinType());
            String string4 = a2.lookup().playerName();
            this.sendFeedback(i.qz(false, -443886041, '\uec31', -1850453594) + string4 + ".");
        }));
    }

    private class_10538 getSkinDownloader() {
        block0: {
            if (this.skinDownloader != null) break block0;
            this.skinDownloader = new class_10538(mc.method_1487(), mc.method_1531(), arg_0 -> ((class_310)mc).execute(arg_0));
        }
        return this.skinDownloader;
    }

    private b lookupSkin(String string) {
        try {
            UUID uUID = this.lookupUuid(string);
            c c2 = this.lookupTexturePayload(uUID);
            return new b(string, uUID, c2.textureUrl(), c2.skinType());
        }
        catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(i.qz(false, -639329230, '\u7686', -2025102984), interruptedException);
        }
        catch (IOException iOException) {
            throw new IllegalStateException(iOException.getMessage(), iOException);
        }
    }

    private UUID lookupUuid(String string) {
        String string2 = this.encodeName(string);
        JsonObject jsonObject = this.requestJson(i.qz(false, 258421019, '\u93a2', -927095006) + string2);
        if (jsonObject == null) {
            String string3 = this.encodeName(string);
            jsonObject = this.requestJson(i.qz(false, 258421018, '\uc974', 632253189) + string3);
        }
        if (jsonObject == null || !jsonObject.has(i.qz(false, 258421017, '\udea0', 1382914718))) {
            throw new IOException(i.qz(false, 258421016, '\u5aef', 1391108804));
        }
        return i.parseUuid(jsonObject.get(i.qz(false, 258421017, '\u4b05', 1382914718)).getAsString());
    }

    private c lookupTexturePayload(UUID uUID) {
        String string = uUID.toString().replace("-", "");
        JsonObject jsonObject = this.requestJson(i.qz(false, 790404878, '\uefec', -2110765293) + string);
        if (jsonObject == null || !jsonObject.has(i.qz(false, 790404879, '\u3d12', -182534169))) {
            throw new IOException(i.qz(false, 790404876, '\u0969', 845072711));
        }
        JsonArray jsonArray = jsonObject.getAsJsonArray(i.qz(false, 790404879, '\ud31f', -182534169));
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject2;
            if (!jsonElement.isJsonObject()) continue;
            JsonObject jsonObject3 = jsonElement.getAsJsonObject();
            if (!i.qz(false, 790404877, '\u25d8', -1025229126).equalsIgnoreCase(i.getString(jsonObject3, i.qz(false, 790404874, '\uc44f', 978690822))) || !jsonObject3.has(i.qz(false, 790404875, '\u7237', -545583694))) continue;
            String string2 = new String(Base64.getDecoder().decode(jsonObject3.get(i.qz(false, 790404875, '\u0428', -545583694)).getAsString()), StandardCharsets.UTF_8);
            JsonObject jsonObject4 = JsonParser.parseString((String)string2).getAsJsonObject();
            JsonObject jsonObject5 = jsonObject4.getAsJsonObject(i.qz(false, 790404877, '\u20d5', -1025229126));
            JsonObject jsonObject6 = jsonObject2 = jsonObject5 != null ? jsonObject5.getAsJsonObject(i.qz(false, 790404872, '\u2786', -1271960378)) : null;
            if (jsonObject2 == null || !jsonObject2.has(i.qz(false, 790404873, '\u797f', -1609807064))) break;
            String string3 = null;
            JsonObject jsonObject7 = jsonObject2.getAsJsonObject(i.qz(false, 790404886, '\uda28', -1611226997));
            if (jsonObject7 != null && jsonObject7.has(i.qz(false, 790404887, '\u2821', 54100458))) {
                string3 = jsonObject7.get(i.qz(false, 790404887, '\ubdd7', 54100458)).getAsString();
            }
            class_7920 class_79202 = i.qz(false, 790404884, '\ud6d2', -387068957).equalsIgnoreCase(string3) ? class_7920.field_41122 : class_7920.field_41123;
            return new c(jsonObject2.get(i.qz(false, 790404873, '\u897e', -1609807064)).getAsString(), class_79202);
        }
        throw new IOException(i.qz(false, 790404885, '\u2ef7', -556784033));
    }

    private JsonObject requestJson(String string) {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(string)).timeout(HTTP_TIMEOUT).header(i.qz(false, -718241060, '\ua19d', 1182939606), i.qz(false, -718241059, '\u6828', 629183043)).header(i.qz(false, -718241058, '\ud2de', 150252976), i.qz(false, -718241057, '\u37c2', 213269522)).GET().build();
        HttpResponse<String> httpResponse = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int n = httpResponse.statusCode();
        if (n == 404 || n == 204) {
            return null;
        }
        if (n < 200 || n >= 300) {
            int n2 = n;
            throw new IOException(i.qz(false, -718241072, '\udbfe', -1797527151) + n2);
        }
        String string2 = httpResponse.body();
        if (string2 == null || string2.isBlank()) {
            return null;
        }
        return JsonParser.parseString((String)string2).getAsJsonObject();
    }

    private static synchronized void applyOverride(class_12079.class_12081 class_120812, class_7920 class_79202) {
        i.destroyTexture(overrideTextureAsset);
        overrideTextureAsset = class_120812;
        overrideSkin = class_8685.method_74884((class_12079.class_12081)class_120812, null, null, (class_7920)class_79202);
    }

    private static synchronized void clearOverride() {
        i.destroyTexture(overrideTextureAsset);
        overrideTextureAsset = null;
        overrideSkin = null;
    }

    private static void destroyTexture(class_12079.class_12081 class_120812) {
        if (class_120812 == null || mc == null) {
            return;
        }
        try {
            mc.method_1531().method_4615(class_120812.comp_3627());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (!class_120812.comp_3626().equals((Object)class_120812.comp_3627())) {
                mc.method_1531().method_4615(class_120812.comp_3626());
            }
        }
        catch (Throwable throwable) {}
    }

    private void sendFeedback(String string) {
        if (mc == null || i.mc.field_1705 == null) {
            return;
        }
        try {
            String string2 = string;
            i.mc.field_1705.method_1743().method_1812((class_2561)class_2561.method_43470((String)(i.qz(false, 267717567, '\u29ab', 694924211) + string2)));
        }
        catch (Throwable throwable) {}
    }

    private class_2960 createTextureId(b b2) {
        String string = b2.playerName().toLowerCase(Locale.ROOT).replaceAll(i.qz(false, -855036571, '\udf41', -602758124), "");
        if (string.isEmpty()) {
            string = i.qz(false, -855036572, '\ucff2', -2084318290);
        }
        String string2 = b2.uuid().toString().replace("-", "");
        String string3 = string;
        return class_2960.method_60655((String)i.qz(false, -855036573, '\u0626', 1590737986), (String)(i.qz(false, -855036574, '\u34f8', 1769668016) + string3 + "_" + string2));
    }

    private Path getCacheFile(UUID uUID) {
        String string = uUID.toString().replace("-", "");
        return i.mc.field_1697.toPath().resolve(i.qz(false, -1352918531, '\u5edf', 712404019)).resolve(i.qz(false, -1352918532, '\u96b7', 224585223)).resolve(string + i.qz(false, -1352918589, '\u1bea', 1358902714));
    }

    private String encodeName(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    private static UUID parseUuid(String string) {
        String string2 = string.replace("-", "");
        return UUID.fromString(string2.replaceFirst(i.qz(false, 1733587618, '\u53c0', -1117200824), i.qz(false, 1733587617, '\u3386', -22265677)));
    }

    private static String normalizeName(String string) {
        return string == null ? "" : string.trim();
    }

    private static String getString(JsonObject jsonObject, String string) {
        return jsonObject.has(string) ? jsonObject.get(string).getAsString() : "";
    }

    private static String getRootMessage(Throwable throwable) {
        Throwable throwable2 = throwable;
        while (throwable2.getCause() != null) {
            throwable2 = throwable2.getCause();
        }
        String string = throwable2.getMessage();
        return string == null || string.isBlank() ? throwable2.getClass().getSimpleName() : string;
    }

    private static UUID getLocalPlayerUuid() {
        if (mc == null) {
            return null;
        }
        if (i.mc.field_1724 != null) {
            return i.mc.field_1724.method_5667();
        }
        return mc.method_1548() != null ? mc.method_1548().method_44717() : null;
    }

    static {
        int n;
        String[] stringArray = new String[38];
        String string = "\u3ac3\u2a52\u2ab7\u2aa1\u2a3a\u2a20\u2add\u2a66\u2ab4\u2aa5\u2a3b\u2a1f\u3ac3\uc9e6\uc907\uc91e\uc99a\uc9b1\uc970\uc990\uc923\uc914\uc984\uc9bf\u3ada\u95d8\u951d\u950b\u9590\u95aa\u9577\u95cc\u951e\u950f\u9591\u95b5\u953c\u95cb\u9515\u9501\u9599\u95a8\u957b\u3adb\u6163\u6189\u6189\u6111\u6136\u61f6\u6143\u61ca\u619b\u6100\u6129\u61ee\u614f\u6192\u6181\u6118\u6133\u61f4\u6147\u3af8\ua703\ua7c2\ua7d6\ua74e\ua77a\ua7e5\ua742\ua79f\ua7c9\ua744\ua76e\ua7ff\ua70a\ua7d5\ua7c4\ua753\ua773\ua7ac\ua757\ua7c7\ua7db\ua745\ua734\ua7b8\ua700\ua7cb\ua7c8\ua755\ua73e\ua7b7\ua707\ua7c7\ua7e6\ua775\ua743\ua79c\ua73c\ua7bd\ua7eb\ua773\ua74b\ua796\ua722\ua7ee\ua7ed\ua776\ua757\ua7da\u3a88\u6b24\u6be5\u6bf1\u6b69\u6b5d\u6bc2\u6b65\u6bb8\u6bee\u6b63\u6b49\u6bd8\u6b2d\u6bf4\u6be7\u6b70\u6b59\u6b9e\u6b3f\u6be5\u6be7\u6b7c\u6b59\u6b98\u6b22\u6be0\u6bfe\u6b64\u6b45\u6bce\u6b31\u6be0\u6bca\u6b14\u6b65\u6bb7\u6b06\u6bd0\u6bc2\u6b4f\u6b63\u6bb2\u6b12\u6b94\u6bdb\u6b45\u6b6b\u6bb4\u6b15\u6bcd\u6bd0\u6b06\u6b72\u6ba7\u6b15\u6bcc\u6bca\u6b53\u6b3f\u6ba8\u6b11\u6bc0\u6bdc\u6b0a\u3aca\u9abe\u9a6e\u3ad8\ua2dd\ua23c\ua225\ua2a1\ua28a\ua24b\ua2ab\ua238\ua221\ua2a6\ua2c1\ua251\ua2ee\ua229\ua226\ua2b0\u3af3\u4732\u47f3\u47e7\u477f\u474b\u47d4\u4773\u47ae\u47ea\u4760\u4745\u4793\u473f\u47e4\u47f1\u4770\u4749\u4788\u473e\u47f0\u47f7\u4737\u4747\u4793\u4728\u47fe\u47e5\u4770\u470e\u4795\u472b\u47f4\u479e\u475e\u477b\u47bb\u470d\u47ca\u47d8\u4745\u473b\u47af\u4719\u47c3\u47d8\u4742\u4760\u47a5\u470c\u47c3\u478c\u474f\u477a\u47b1\u470a\u47d8\u47c5\u4750\u4729\u3ac2\ubfde\ubf01\ubf08\ubf8b\ubfa9\ubf68\ubfdc\ubf1c\ubf08\ubf82\u3ade\uc95d\uc9b8\uc9ae\uc935\uc94c\uc9ca\uc97a\uc9ba\uc9ab\uc938\uc90e\uc9d1\uc922\uc9b1\uc9a4\uc923\uc958\uc9c8\uc973\uc9b4\uc9bf\uc929\u3ac0\u3e87\u3e4b\u3e42\u3ed2\u3ee4\u3e35\u3e90\u3e5b\u3acc\ua321\ua3f3\ua3eb\ua37f\u3acd\u0d8d\u0d47\u0d5e\u0ddb\u0dfc\u3acc\u6cdc\u6c19\u6c0f\u6c94\u3acb\u4b14\u4bce\u4bc4\u3ac0\ua0af\ua07a\ua07f\ua0f6\ua0c4\ua017\ua0b0\ua078\u3acd\u81ce\u8111\u810e\u8193\u81ad\u3acc\ucbd9\ucb1b\ucb0a\ucb92\u3ad4\u2658\u26a4\u26ff\u2636\u2607\u26c3\u2672\u26a1\u26b0\u2669\u2609\u26c7\u2673\u26a9\u26f3\u263b\u2605\u26ce\u2670\u26ac\u26bb\u2630\u2646\u26d6\u2661\u26a6\u26a9\u263f\u3ace\u39de\u3921\u3935\u39af\u398d\u395f\u3ad8\u926b\u92a7\u92b3\u9233\u9201\u92dd\u926d\u92a5\u92a0\u923a\u9208\u929f\u926c\u92a8\u92a0\u923d\u3ac2\uadac\uad57\uad55\uadde\uadb6\uad0c\uad98\uad47\uad54\uadd2\u3ad9\u3c03\u3ce3\u3cfc\u3c61\u3c57\u3cc2\u3c0e\u3ceb\u3cf1\u3c6a\u3c74\u3c89\u3c36\u3ce4\u3cf9\u3c67\u3c5f\u3acd\ue990\ue951\ue945\ue9dd\ue99a\u3ac6\ub3a1\ub374\ub358\ub3c6\ub3f6\ub30d\ub394\ub340\ub357\ub3c2\ub3f3\ub332\ub3ab\ub30b\u3ac2\ua406\ua4de\ua4f5\ua425\ua445\ua4d9\ua476\ua4bf\ua4c1\ua45f\u3ace\ud397\ud356\ud34f\ud3cb\ud3e0\ud321\u3acd\ubc73\ubcb3\ubcac\ubc31\ubc07\u3ace\ufd8a\ufd4f\ufd59\ufdc2\ufde8\ufd62\u3ac3\u6c02\u6cc2\u6cdd\u6c40\u6c76\u6ce3\u6c1f\u6cc0\u6cda\u6c4d\u6c73\u3acd\ue63d\ue6f8\ue6ee\ue675\ue65f\u3acc\u35dd\u355e\u3554\u35c1\u3a81\ude29\ude80\udeb8\ude2f\ude3b\udef1\ude6e\udebd\udeab\ude2a\ude10\udec0\ude35\udead\udeed\ude70\ude2b\uded1\ude68\ude96\ude9a\ude2b\ude16\udece\ude6d\udeb9\udeab\ude78\ude06\ude84\ude37\ude9e\ude9a\ude0d\ude1d\uded7\ude4c\ude9f\ude85\ude04\ude32\udee2\ude1f\ude8b\udecf\ude52\ude15\udeef\ude4a\udeb4\udebc\ude0d\ude34\udeec\ude43\ude97\ude89\ude5a\ude20\udea2\ude15\udebc\ude84\ude13\ude7f\udeb5\ude2a\udef9\udee7\ude66\ude5c\udedc\ude60\u3ac6\u40de\u4016\u401e\u408b\u40aa\u4063\u40d8\u4012\u4014\u4081\u40a2\u406d\u40d2\u401e\u3ad1\ucda9\ucd53\ucd4f\ucdd6\ucde8\ucd3f\ucdc9\ucd40\ucd43\ucd90\ucde2\ucd25\ucd93\ucd52\ucd53\ucd96\ucdea\ucd24\ucd94\ucd4e\ucd10\ucdca\ucdf0\ucd3b\ucdd7\u3aca\ue5a1\ue566\u3ada\u51ae\u5142\u5156\u51d6\u51e4\u513e\u518d\u5114\u515f\u51db\u51ea\u513b\u51c3\u5158\u5158\u51d9\u51f4\u516f";
        int n2 = 0;
        int n3 = 0;
        do {
            n = string.charAt(n2++) ^ 0x3AC8;
            char[] cArray = string.substring(n2, n + n2).toCharArray();
            for (int i2 = 0; i2 < cArray.length; ++i2) {
                int n4 = i2;
                char c2 = cArray[n4];
                cArray[n4] = (char)(c2 ^ (switch (i2 % 6) {
                    case 0 -> 73;
                    case 1 -> 149;
                    case 2 -> 130;
                    case 3 -> 31;
                    case 4 -> 47;
                    default -> 248;
                }));
            }
            stringArray[n3++] = new String(cArray).intern();
        } while ((n2 += n) < string.length());
        pn = stringArray;
        eu = new String[stringArray.length];
        HTTP_TIMEOUT = Duration.ofSeconds(10L);
        HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    public static String qz(boolean bl, int n, char c2, int n2) {
        int n3;
        block1: {
            int n4;
            int n5 = n4 = Thread.currentThread().getStackTrace()[2].getMethodName().intern().hashCode();
            n3 = n ^ new Random(new Random((n5 % 2 == 0 ? n5 << 1 : n5 >> 1) ^ 0x1C).nextInt()).nextInt();
            if (eu[n3] != null) break block1;
            char[] cArray = pn[n3].toCharArray();
            for (int i2 = 0; i2 < cArray.length; ++i2) {
                int n6 = i2;
                cArray[n6] = (char)(cArray[n6] ^ (char)(i2 ^ n2));
            }
            i.eu[n3] = new String(cArray).intern();
        }
        return (String)eu[n3];
    }

    private static final class c
    extends Record {
        private final String textureUrl;
        private final class_7920 skinType;
        transient String myMetaphor = "myMetaphor @ jnt.so";

        private c(String string, class_7920 class_79202) {
            this.textureUrl = string;
            this.skinType = class_79202;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{c.class, "textureUrl;skinType", "textureUrl", "skinType"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{c.class, "textureUrl;skinType", "textureUrl", "skinType"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{c.class, "textureUrl;skinType", "textureUrl", "skinType"}, this, object);
        }

        public String textureUrl() {
            return this.textureUrl;
        }

        public class_7920 skinType() {
            return this.skinType;
        }
    }

    private static final class b
    extends Record {
        private final String playerName;
        private final UUID uuid;
        private final String textureUrl;
        private final class_7920 skinType;
        transient String myMetaphor = "myMetaphor @ jnt.so";

        private b(String string, UUID uUID, String string2, class_7920 class_79202) {
            this.playerName = string;
            this.uuid = uUID;
            this.textureUrl = string2;
            this.skinType = class_79202;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{b.class, "playerName;uuid;textureUrl;skinType", "playerName", "uuid", "textureUrl", "skinType"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{b.class, "playerName;uuid;textureUrl;skinType", "playerName", "uuid", "textureUrl", "skinType"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{b.class, "playerName;uuid;textureUrl;skinType", "playerName", "uuid", "textureUrl", "skinType"}, this, object);
        }

        public String playerName() {
            return this.playerName;
        }

        public UUID uuid() {
            return this.uuid;
        }

        public String textureUrl() {
            return this.textureUrl;
        }

        public class_7920 skinType() {
            return this.skinType;
        }
    }

    private static final class a
    extends Record {
        private final b lookup;
        private final class_12079.class_12081 textureAsset;
        transient String myMetaphor = "myMetaphor @ jnt.so";

        private a(b b2, class_12079.class_12081 class_120812) {
            this.lookup = b2;
            this.textureAsset = class_120812;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{a.class, "lookup;textureAsset", "lookup", "textureAsset"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{a.class, "lookup;textureAsset", "lookup", "textureAsset"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{a.class, "lookup;textureAsset", "lookup", "textureAsset"}, this, object);
        }

        public b lookup() {
            return this.lookup;
        }

        public class_12079.class_12081 textureAsset() {
            return this.textureAsset;
        }
    }
}

