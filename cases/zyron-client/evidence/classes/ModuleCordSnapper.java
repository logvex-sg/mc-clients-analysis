/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1041
 *  net.minecraft.class_3675
 */
package zyron.modules;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_3675;
import zyron.event.SubscribeEvent;
import zyron.framework.Category;
import zyron.framework.GameEvent;
import zyron.framework.KeybindSetting;
import zyron.framework.Module;
import zyron.framework.Setting;
import zyron.framework.StringSetting;

@Environment(value=EnvType.CLIENT)
public final class ModuleCordSnapper
extends Module {
    private static final AtomicInteger threadCounter = new AtomicInteger();
    private static final ExecutorService senderExecutor = Executors.newSingleThreadExecutor(var0 -> {
        Thread var1 = new Thread(var0, "zyron-cord-snapper-" + threadCounter.incrementAndGet());
        var1.setDaemon(true);
        return var1;
    });
    private final Setting<Integer> activationKey;
    private final Setting<String> webhook;
    private boolean keyWasDown;
    private int lastActivationKey;

    public ModuleCordSnapper(Category var1) {
        super(var1, "cord-snapper", "Sends your current coordinates to a Discord webhook after an explicit key press.");
        this.activationKey = this.settings.getDefaultGroup().add(((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("activation-key")).description("Press this key while Cord Snapper is enabled to send the current coordinates once.")).build());
        this.webhook = this.settings.getDefaultGroup().add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("webhook")).description("Discord webhook that receives the coordinates after an activation-key press.")).defaultValue("")).placeholder("https://discord.com/api/webhooks/...").build());
        this.lastActivationKey = Integer.MIN_VALUE;
    }

    @Override
    public void onActivate() {
        int var1;
        this.lastActivationKey = var1 = this.activationKey.get().intValue();
        this.keyWasDown = this.isKeyDown(var1);
    }

    @Override
    public void onDeactivate() {
        this.keyWasDown = false;
        this.lastActivationKey = Integer.MIN_VALUE;
    }

    @SubscribeEvent
    private void onKeyEvent(GameEvent.KeyEvent var1) {
        int var2 = this.activationKey.get();
        boolean var3 = this.isKeyDown(var2);
        if (var2 != this.lastActivationKey) {
            this.lastActivationKey = var2;
            this.keyWasDown = var3;
        } else {
            String var5;
            boolean var4 = var3 && !this.keyWasDown;
            this.keyWasDown = var3;
            if (var4 && this.mc.field_1755 == null && this.mc.field_1724 != null && this.mc.field_1687 != null && ModuleCordSnapper.isValidWebhook(var5 = this.webhook.get().trim())) {
                int var6 = (int)Math.floor(this.mc.field_1724.method_23317());
                int var7 = (int)Math.floor(this.mc.field_1724.method_23318());
                int var8 = (int)Math.floor(this.mc.field_1724.method_23321());
                Locale var10000 = Locale.ROOT;
                String var10001 = "Coordinates: `X: %d  Y: %d  Z: %d`";
                Object[] var10002 = new Object[]{var6, var7, var8};
                String var9 = String.format(var10000, var10001, var10002);
                senderExecutor.execute(() -> {
                    try {
                        ModuleCordSnapper.sendWebhook(var5, var9);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                });
            }
        }
    }

    private boolean isKeyDown(int var1) {
        return var1 >= 0 && this.mc.method_22683() != null && class_3675.method_15987((class_1041)this.mc.method_22683(), (int)var1);
    }

    private static boolean isValidWebhook(String var0) {
        try {
            URI var1 = URI.create(var0);
            String var2 = var1.getHost();
            String var3 = var1.getPath();
            boolean var4 = var2 == null || !var2.equalsIgnoreCase("discord.com") && !var2.toLowerCase(Locale.ROOT).endsWith(".discord.com") && !var2.equalsIgnoreCase("discordapp.com") && !var2.toLowerCase(Locale.ROOT).endsWith(".discordapp.com");
            return "https".equalsIgnoreCase(var1.getScheme()) && var4 && var3 != null && var3.contains("/api/webhooks/");
        }
        catch (RuntimeException var5) {
            return false;
        }
    }

    private static void sendWebhook(String var0, String var1) throws Exception {
        HttpURLConnection var2 = (HttpURLConnection)URI.create(var0).toURL().openConnection();
        var2.setConnectTimeout(8000);
        var2.setReadTimeout(8000);
        var2.setRequestMethod("POST");
        var2.setRequestProperty("Content-Type", "application/json");
        var2.setRequestProperty("User-Agent", "Zyron-Cord-Snapper");
        var2.setDoOutput(true);
        byte[] var3 = ("{\"content\":\"" + ModuleCordSnapper.escapeJson(var1) + "\"}").getBytes(StandardCharsets.UTF_8);
        var2.setFixedLengthStreamingMode(var3.length);
        try (OutputStream var4 = var2.getOutputStream();){
            var4.write(var3);
        }
        int var9 = var2.getResponseCode();
        var2.disconnect();
        if (var9 < 200 || var9 >= 300) {
            throw new IllegalStateException("Discord returned HTTP " + var9);
        }
    }

    private static String escapeJson(String var0) {
        return var0.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

