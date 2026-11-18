/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 */
package w;

import com.water.module.Category;
import com.water.module.b;
import com.water.module.c;
import com.water.setting.d;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.class_310;
import net.minecraft.class_437;
import w.a;
import w.f;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public final class l
extends b {
    private static l a;
    public volatile String c = "";
    public volatile String d = "";
    public volatile boolean g = false;
    public volatile boolean h = false;

    public l() {
        super("Config Share", Category.e);
        w.a.a();
        a = this;
    }

    @Override
    public void d() {
        class_310 class_3102 = class_310.method_1551();
        if (class_3102 != null) {
            class_3102.execute(() -> {
                class_3102.method_1507((class_437)new f());
                this.a(false);
            });
        }
    }

    public static l a() {
        return a;
    }

    public void a(Runnable runnable) {
        this.g = true;
        this.c = "";
        this.d = "";
        new Thread(() -> {
            try {
                Object object = this.a();
                Object object2 = new ByteArrayOutputStream();
                try (Object object3 = new GZIPOutputStream((OutputStream)object2);){
                    ((FilterOutputStream)object3).write(((String)object).getBytes(StandardCharsets.UTF_8));
                }
                object3 = Base64.getUrlEncoder().withoutPadding().encodeToString(((ByteArrayOutputStream)object2).toByteArray());
                object = (HttpURLConnection)new URI("https://dpaste.com/api/v2/").toURL().openConnection();
                ((HttpURLConnection)object).setRequestMethod("POST");
                ((URLConnection)object).setDoOutput(true);
                ((URLConnection)object).setConnectTimeout(6000);
                ((URLConnection)object).setReadTimeout(6000);
                ((URLConnection)object).setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                object2 = "content=" + URLEncoder.encode((String)object3, StandardCharsets.UTF_8) + "&syntax=text&expiry_days=365";
                object3 = ((URLConnection)object).getOutputStream();
                try {
                    ((OutputStream)object3).write(((String)object2).getBytes(StandardCharsets.UTF_8));
                }
                finally {
                    if (object3 != null) {
                        ((OutputStream)object3).close();
                    }
                }
                object3 = new String(((URLConnection)object).getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                ((HttpURLConnection)object).disconnect();
                this.c = object = ((String)object3).replaceAll("https?://dpaste\\.com/", "").replace("/", "").trim();
                object2 = class_310.method_1551();
                if (object2 != null) {
                    object2.execute(() -> l.a((class_310)object2, (String)object));
                }
            }
            catch (Exception exception) {
                this.d = "Upload failed: " + exception.getMessage();
            }
            finally {
                this.g = false;
                class_310 class_3102 = class_310.method_1551();
                if (runnable != null && class_3102 != null) {
                    class_3102.execute(runnable);
                }
            }
        }, "water-config-export").start();
    }

    public void a(String string, Runnable runnable) {
        this.h = true;
        this.d = "";
        new Thread(() -> {
            try {
                string = ((String)string).trim().replaceAll("\\s+", "");
                if (((String)string).isEmpty()) {
                    this.d = "Enter a code first!";
                    return;
                }
                string = (HttpURLConnection)new URI("https://dpaste.com/" + (String)string + ".txt").toURL().openConnection();
                ((URLConnection)((Object)string)).setConnectTimeout(6000);
                ((URLConnection)((Object)string)).setReadTimeout(6000);
                Object object2 = new String(((URLConnection)((Object)string)).getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                ((HttpURLConnection)((Object)string)).disconnect();
                try {
                    string = Base64.getUrlDecoder().decode((String)object2);
                }
                catch (Exception exception) {
                    try {
                        string = Base64.getDecoder().decode((String)object2);
                    }
                    catch (Exception exception2) {
                        this.d = "Invalid code!";
                        this.h = false;
                        string = class_310.method_1551();
                        if (runnable != null && string != null) {
                            string.execute(runnable);
                        }
                        return;
                    }
                }
                try {
                    object2 = new GZIPInputStream(new ByteArrayInputStream((byte[])string));
                    try {
                        string = new String(((InputStream)object2).readAllBytes(), StandardCharsets.UTF_8);
                    }
                    finally {
                        ((GZIPInputStream)object2).close();
                    }
                }
                catch (Exception exception) {
                    this.d = "Invalid code!";
                    this.h = false;
                    string = class_310.method_1551();
                    if (runnable != null && string != null) {
                        string.execute(runnable);
                    }
                    return;
                }
                this.f(string);
                com.water.module.c.INSTANCE.i();
            }
            catch (Exception exception) {
                this.d = "Load error: " + exception.getMessage();
            }
            finally {
                this.h = false;
                class_310 class_3102 = class_310.method_1551();
                if (runnable != null && class_3102 != null) {
                    class_3102.execute(runnable);
                }
            }
        }, "water-config-import").start();
    }

    private String a() {
        StringBuilder stringBuilder = new StringBuilder();
        for (b b2 : com.water.module.c.INSTANCE.getModules()) {
            if (b2.getCategory() == Category.e) continue;
            stringBuilder.append(b2.getName()).append(":").append(b2.isEnabled() ? "1" : "0").append(":").append(b2.getBind()).append(":");
            for (Object object : b2.getSettings()) {
                stringBuilder.append((String)((object = this.b((d<?>)object)) != null ? object : "")).append(",");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    private void f(String stringArray) {
        block2: for (String string : stringArray.split("\n")) {
            Object object;
            String[] stringArray2 = string.split(":", 4);
            if (stringArray2.length < 4 || (object = com.water.module.c.INSTANCE.a(stringArray2[0])) == null) continue;
            int n2 = stringArray2[1].equals("1");
            if (n2 != ((b)object).isEnabled()) {
                ((b)object).toggle();
            }
            try {
                ((b)object).b(Integer.parseInt(stringArray2[2]));
            }
            catch (Exception exception) {}
            String[] stringArray3 = stringArray2[3].split(",", -1);
            n2 = 0;
            object = ((b)object).getSettings().iterator();
            while (object.hasNext()) {
                d d2 = (d)object.next();
                if (n2 >= stringArray3.length) continue block2;
                if (!stringArray3[n2].isEmpty()) {
                    this.b(d2, stringArray3[n2]);
                }
                ++n2;
            }
        }
    }

    private String b(d<?> object) {
        if ((object = ((d)object).getValue()) instanceof Boolean) {
            return (Boolean)object != false ? "1" : "0";
        }
        if (object instanceof Float) {
            return String.valueOf((Float)object);
        }
        if (object instanceof Double) {
            return String.valueOf((Double)object);
        }
        if (object instanceof Integer) {
            return String.valueOf((Integer)object);
        }
        if (object instanceof String) {
            return ((String)object).replace(",", "").replace("\n", "");
        }
        if (object instanceof Color) {
            object = (Color)object;
            return String.format("%02x%02x%02x%02x", ((Color)object).getRed(), ((Color)object).getGreen(), ((Color)object).getBlue(), ((Color)object).getAlpha());
        }
        return "";
    }

    private void b(d<?> d2, String string) {
        try {
            Object obj = d2.getValue();
            if (obj instanceof Boolean) {
                d2.setValue(string.equals("1"));
            } else if (obj instanceof Float) {
                d2.setValue(Float.valueOf(Float.parseFloat(string)));
            } else if (obj instanceof Double) {
                d2.setValue(Double.parseDouble(string));
            } else if (obj instanceof Integer) {
                d2.setValue(Integer.parseInt(string));
            } else if (obj instanceof String) {
                d2.setValue(string);
            } else if (obj instanceof Color && string.length() == 8) {
                d2.setValue(new Color(Integer.parseInt(string.substring(0, 2), 16), Integer.parseInt(string.substring(2, 4), 16), Integer.parseInt(string.substring(4, 6), 16), Integer.parseInt(string.substring(6, 8), 16)));
            }
        }
        catch (Exception exception) {}
    }

    private static /* synthetic */ void a(class_310 class_3102, String string) {
        class_3102.field_1774.method_1455(string);
    }
}

