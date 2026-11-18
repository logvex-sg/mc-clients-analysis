/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1011
 *  net.minecraft.class_1043
 *  net.minecraft.class_1044
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package w;

import com.water.module.Category;
import com.water.module.b;
import com.water.setting.d;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import w.I;
import w.a;
import w.e;
import w.i;
import w.n;
import w.p;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public final class o
extends b {
    private static o a;
    private final d<Float> ab = new d<Float>("Scale", Float.valueOf(1.0f), Float.valueOf(0.6f), Float.valueOf(2.0f));
    private volatile String f;
    private volatile String g;
    private volatile boolean i = false;
    private volatile boolean j = false;
    private volatile long c;
    private volatile long d = 0L;
    private volatile long e = 0L;
    private ScheduledExecutorService a;
    private ExecutorService a;
    private File a;
    private File b;
    private File c = 0L;
    private static final class_2960 a;
    private class_1043 a;
    private volatile long f = 0L;
    private volatile boolean k = false;
    private float a;
    private long g = 0L;
    private volatile boolean l = false;
    private final AtomicBoolean a;
    private final AtomicInteger a = new AtomicInteger(0);
    private volatile float b = 0.0f;
    private volatile String h = null;

    public o() {
        super("Spotify HUD", Category.e);
        w.a.a();
        this.a(this.ab);
        a = this;
    }

    public static boolean h() {
        return a != null && a.isEnabled();
    }

    public static float b() {
        return a == null ? 1.0f : o.a.ab.getValue().floatValue();
    }

    public static void a(float f2) {
        if (a == null) {
            return;
        }
        Float f3 = o.a.ab.getMin();
        float f4 = f3 instanceof Float ? f3.floatValue() : 0.6f;
        f3 = o.a.ab.getMax();
        float f5 = f3 instanceof Float ? f3.floatValue() : 2.0f;
        o.a.ab.setValue(Float.valueOf(Math.max(f4, Math.min(f5, f2))));
    }

    public static int a() {
        return Math.round(260.0f * o.b());
    }

    public static int b() {
        return Math.round(56.0f * o.b());
    }

    private static int a(int n2) {
        return Math.round((float)n2 * o.b());
    }

    private static float a(float f2) {
        return f2 * o.b();
    }

    private static int b(int n2) {
        class_310 class_3102 = class_310.method_1551();
        if (class_3102 == null) {
            return n2;
        }
        return Math.max(0, Math.min(n2, class_3102.method_22683().method_4486() - o.a()));
    }

    private static int c(int n2) {
        class_310 class_3102 = class_310.method_1551();
        if (class_3102 == null) {
            return n2;
        }
        return Math.max(0, Math.min(n2, class_3102.method_22683().method_4502() - o.b()));
    }

    private static int c() {
        return o.b(n.a(n.a.h)[0]);
    }

    private static int d() {
        return o.c(n.a(n.a.h)[1]);
    }

    @Override
    public void d() {
        this.l();
    }

    @Override
    public void f() {
        if (this.a == null || this.a.isShutdown()) {
            this.l();
        }
    }

    private void l() {
        this.a = 0.0f;
        this.g = 0L;
        this.l = false;
        this.a.set(0);
        this.m();
        this.c = new File(System.getenv("TEMP"), "water_spotify_art.png");
        Thread thread = new Thread(this::n, "water-spotify-initial");
        thread.setDaemon(true);
        thread.start();
        this.a = Executors.newSingleThreadScheduledExecutor(runnable -> {
            runnable = new Thread(runnable, "water-spotify");
            ((Thread)runnable).setDaemon(true);
            return runnable;
        });
        this.a = Executors.newSingleThreadExecutor(runnable -> {
            runnable = new Thread(runnable, "water-spotify-ctrl");
            ((Thread)runnable).setDaemon(true);
            return runnable;
        });
        this.a.scheduleAtFixedRate(() -> {
            int n2 = this.a.getAndIncrement();
            if (n2 >= 10) {
                return;
            }
            this.n();
            if (!this.f.isEmpty()) {
                this.a.set(Integer.MAX_VALUE);
            }
        }, 1000L, 800L, TimeUnit.MILLISECONDS);
        this.a.scheduleAtFixedRate(this::n, 2L, 2L, TimeUnit.SECONDS);
    }

    @Override
    public void e() {
        if (this.a != null) {
            this.a.shutdownNow();
        }
        if (this.a != null) {
            this.a.shutdownNow();
        }
        this.f = "";
        this.g = "";
        this.i = false;
        this.c = 0L;
        this.d = 0L;
        this.e = 0L;
        this.a = 0.0f;
        this.k = false;
        this.h = null;
        if (this.a != null) {
            try {
                class_310.method_1551().method_1531().method_4615(a);
            }
            catch (Exception exception) {}
            this.a = null;
        }
    }

    private void m() {
        try {
            this.a = File.createTempFile("water_smtc_poll_", ".ps1");
            this.a.deleteOnExit();
            try (FileWriter fileWriter = new FileWriter(this.a, StandardCharsets.UTF_8);){
                fileWriter.write("[void][System.Reflection.Assembly]::LoadFile('C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\System.Runtime.WindowsRuntime.dll')\r\n$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\r\n$g = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -like 'IAsyncOperation*' })[0]\r\nfunction Aw($op,$t){$m=$g.MakeGenericMethod($t);$task=$m.Invoke($null,@($op));$task.GetAwaiter().GetResult()}\r\n$asStreamForRead = [System.IO.WindowsRuntimeStreamExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsStreamForRead' -and $_.GetParameters().Count -eq 1 } | Select-Object -First 1\r\ntry {\r\n  $mgr = Aw([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\r\n  $s = $mgr.GetCurrentSession()\r\n  if ($s) {\r\n    $p = Aw($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])\r\n    $tl = $s.GetTimelineProperties()\r\n    $pb = $s.GetPlaybackInfo()\r\n    if ($p.Title) {\r\n      if ($p.Thumbnail -and $asStreamForRead) {\r\n        try {\r\n          $stream = Aw($p.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])\r\n          $netStream = $asStreamForRead.Invoke($null, @($stream))\r\n          $outPath = Join-Path $env:TEMP 'water_spotify_art.png'\r\n          $fs = [System.IO.File]::Create($outPath)\r\n          $netStream.CopyTo($fs)\r\n          $fs.Close()\r\n          $netStream.Close()\r\n        } catch {}\r\n      }\r\n      Write-Output ($p.Artist + '|||' + $p.Title + '|||' + [long]$tl.Position.TotalMilliseconds + '|||' + [long]$tl.EndTime.TotalMilliseconds + '|||' + ($pb.PlaybackStatus.ToString() -eq 'Playing'))\r\n    }\r\n  }\r\n} catch {}\r\n");
            }
            this.b = File.createTempFile("water_smtc_ctrl_", ".ps1");
            this.b.deleteOnExit();
            fileWriter = new FileWriter(this.b, StandardCharsets.UTF_8);
            try {
                fileWriter.write("param([string]$action)\r\n[void][System.Reflection.Assembly]::LoadFile('C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\System.Runtime.WindowsRuntime.dll')\r\n$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]\r\n$g = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -like 'IAsyncOperation*' })[0]\r\nfunction Aw($op,$t){$m=$g.MakeGenericMethod($t);$task=$m.Invoke($null,@($op));$task.GetAwaiter().GetResult()}\r\ntry {\r\n  $mgr = Aw([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])\r\n  $s = $mgr.GetCurrentSession()\r\n  if ($s) {\r\n    switch ($action) {\r\n      'next'    { Aw($s.TrySkipNextAsync()) ([bool]) | Out-Null }\r\n      'prev'    { Aw($s.TrySkipPreviousAsync()) ([bool]) | Out-Null }\r\n      'toggle'  { Aw($s.TryTogglePlayPauseAsync()) ([bool]) | Out-Null }\r\n      'repeat'  { try { $pb2 = $s.GetPlaybackInfo(); $cur = $pb2.AutoRepeatMode; $next = if($cur -eq [Windows.Media.MediaPlaybackAutoRepeatMode]::None){'Track'} elseif($cur -eq [Windows.Media.MediaPlaybackAutoRepeatMode]::Track){'List'} else{'None'}; Aw($s.TryChangeAutoRepeatModeAsync([Windows.Media.MediaPlaybackAutoRepeatMode]::$next)) ([bool]) | Out-Null } catch {} }\r\n    }\r\n  }\r\n} catch {}\r\n");
            }
            finally {
                ((Writer)fileWriter).close();
            }
        }
        catch (Exception exception) {
            this.a = null;
            this.b = null;
        }
    }

    private void n() {
        if (this.a == null || !this.a.exists()) {
            this.m();
        }
        if (this.a == null) {
            this.l = true;
            return;
        }
        if (!this.a.compareAndSet(false, true)) {
            return;
        }
        try {
            String[] stringArray;
            long l2 = System.currentTimeMillis();
            Object object = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", this.a.getAbsolutePath()).redirectErrorStream(true).start();
            String string = null;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(((Process)object).getInputStream(), StandardCharsets.UTF_8));){
                String string2;
                while ((string2 = bufferedReader.readLine()) != null) {
                    if (!(string2 = string2.trim()).contains("|||")) continue;
                    string = string2;
                    break;
                }
            }
            ((Process)object).waitFor(8L, TimeUnit.SECONDS);
            long l3 = System.currentTimeMillis();
            long l4 = (l2 + l3) / 2L;
            if (string != null && (stringArray = string.split("\\|\\|\\|", -1)).length >= 2 && !stringArray[1].trim().isEmpty()) {
                String string3 = stringArray[1].trim();
                object = stringArray[0].trim();
                long l5 = stringArray.length > 2 ? o.a(stringArray[2]) : 0L;
                long l6 = stringArray.length > 3 ? o.a(stringArray[3]) : 0L;
                boolean bl2 = stringArray.length > 4 && stringArray[4].trim().equalsIgnoreCase("True");
                boolean bl3 = !string3.equals(this.f);
                boolean bl4 = bl2 != this.i;
                long l7 = this.i ? this.c + Math.max(0L, l4 - this.d) : this.c;
                boolean bl5 = Math.abs(l5 - l7) > 4000L;
                this.f = string3;
                this.g = object;
                this.e = l6;
                this.i = bl2;
                if (bl3 || bl4 || bl5 || this.d == 0L) {
                    this.c = l5;
                    this.d = l4;
                }
                if (bl3) {
                    w.i.onSongChanged((String)object, string3);
                }
                return;
            }
        }
        catch (Exception exception) {
        }
        finally {
            this.l = true;
            this.a.set(false);
        }
    }

    private static long a(String string) {
        try {
            return Long.parseLong(string.trim());
        }
        catch (Exception exception) {
            return 0L;
        }
    }

    private static float a(String string) {
        if (string == null) {
            return 0.0f;
        }
        return (float)(string.hashCode() % 1000) / 1000.0f;
    }

    public static void g(String string) {
        if (a == null || o.a.a == null || o.a.b == null) {
            return;
        }
        long l2 = System.currentTimeMillis();
        if ("repeat".equals(string)) {
            boolean bl2 = o.a.j = !o.a.j;
        }
        if ("toggle".equals(string)) {
            if (o.a.i) {
                o.a.c += Math.max(0L, l2 - o.a.d);
                o.a.d = l2;
                o.a.i = false;
            } else {
                o.a.d = l2;
                o.a.i = true;
            }
        }
        o.a.a.submit(() -> {
            try {
                string = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass", "-File", o.a.b.getAbsolutePath(), "-action", string).redirectErrorStream(true).start();
                ((Process)((Object)string)).waitFor(5L, TimeUnit.SECONDS);
                if (o.a.a != null && !o.a.a.isShutdown()) {
                    o.a.a.submit(a::n);
                }
            }
            catch (Exception exception) {}
        });
    }

    private void o() {
        if (this.c == null || !this.c.exists() || this.c.length() < 200L) {
            return;
        }
        long l2 = this.c.lastModified();
        if (l2 == this.f && this.a != null) {
            return;
        }
        try {
            Object object = Files.readAllBytes(this.c.toPath());
            object = new ByteArrayInputStream((byte[])object);
            try {
                class_1011 class_10112 = class_1011.method_4309((InputStream)object);
                if (this.a != null) {
                    try {
                        class_310.method_1551().method_1531().method_4615(a);
                    }
                    catch (Exception exception) {}
                }
                this.a = new class_1043(() -> "spotify_art", class_10112);
                class_310.method_1551().method_1531().method_4616(a, (class_1044)this.a);
                this.f = l2;
                this.k = true;
            }
            finally {
                ((InputStream)object).close();
            }
        }
        catch (Exception exception) {}
    }

    public static void a(class_332 class_3322) {
        long l2;
        int n2;
        int n3;
        if (a == null || !a.isEnabled()) {
            return;
        }
        class_310 class_3102 = class_310.method_1551();
        if (class_3102 == null || class_3102.field_1724 == null) {
            return;
        }
        if (class_3102.field_1755 instanceof e) {
            return;
        }
        if (class_3102.method_53526().method_53536()) {
            return;
        }
        long l3 = System.nanoTime();
        float f2 = o.a.g == 0L ? 0.016f : Math.min(0.1f, (float)(l3 - o.a.g) / 1.0E9f);
        o.a.g = l3;
        o.a.a += (1.0f - o.a.a) * (1.0f - (float)Math.exp(-12.0f * f2));
        float f3 = o.a.a;
        if (f3 < 0.01f) {
            return;
        }
        a.o();
        if (!Objects.equals(o.a.h, o.a.f)) {
            o.a.b = o.a(o.a.f);
            o.a.h = o.a.f;
        }
        int n4 = o.a();
        int n5 = o.b();
        int n6 = o.a(52);
        int n7 = o.a(10);
        int n8 = o.c();
        int n9 = o.d();
        class_3102 = class_3102.field_1772;
        int n10 = p.e();
        float f4 = o.a(6.0f);
        I.a(class_3322, (float)n8, (float)n9, (float)n4, (float)n5, f4, o.col(526863, (int)(210.0f * f3)), false);
        I.a(class_3322, (float)n8, (float)n9, (float)n4, (float)n5, f4, o.col(0xFFFFFF, (int)(8.0f * f3)), false);
        I.a(class_3322, (float)n8, (float)n9, (float)n4, (float)n5, f4, 2.0f, o.col(n10 & 0xFFFFFF, (int)(220.0f * f3)), false);
        int n11 = n8 + n7;
        int n12 = n9 + (n5 - n6) / 2;
        if (o.a.k) {
            n3 = Math.max(0, Math.min(255, (int)(255.0f * f3))) << 24 | 0xFFFFFF;
            I.a(class_3322, (float)n11, (float)n12, (float)n6, a, n3, o.a(4.0f), false);
            I.a(class_3322, (float)n11, (float)n12, (float)n6, (float)n6, o.a(4.0f), 1.5f, o.col(n10 & 0xFFFFFF, (int)(100.0f * f3)), false);
        } else {
            I.a(class_3322, (float)n11, (float)n12, (float)n6, (float)n6, o.a(4.0f), o.col(0xFFFFFF, (int)(10.0f * f3)), false);
            I.a(class_3322, (float)n11, (float)n12, (float)n6, (float)n6, o.a(4.0f), 1.5f, o.col(n10 & 0xFFFFFF, (int)(80.0f * f3)), false);
            String string = "\u266b";
            n2 = class_3102.method_1727(string);
            class_3322.method_51433((class_327)class_3102, string, n11 + (n6 - n2) / 2, n12 + (n6 - 9) / 2, o.col(n10 & 0xFFFFFF, (int)(180.0f * f3)), false);
        }
        n3 = n11 + n6 + n7;
        n2 = n4 - (n3 - n8) - n7;
        long l4 = System.currentTimeMillis();
        if (o.a.i && o.a.d > 0L) {
            long l5 = Math.max(0L, l4 - o.a.d);
            l2 = o.a.c + l5;
            if (o.a.e > 0L) {
                l2 = Math.min(o.a.e, l2);
            }
        } else {
            l2 = o.a.c;
        }
        float f5 = o.a.e > 0L ? Math.min(1.0f, (float)l2 / (float)o.a.e) : 0.0f;
        int n13 = o.a(3);
        n4 = o.a(9) + o.a(3) + o.a(8) + o.a(4) + n13 + o.a(4);
        n4 = n9 + (n5 - n4) / 2 + o.a(5);
        String string = !o.a.l ? "" : (o.a.f.isEmpty() ? "No track playing" : o.a.f);
        class_3322.method_51433((class_327)class_3102, class_3102.method_27523(string, n2), n3, n4, o.col(0xFFFFFF, (int)(255.0f * f3)), false);
        class_3322.method_51433((class_327)class_3102, class_3102.method_27523(o.a.g, n2), n3, n4 += o.a(11), o.col(0xAAAAAA, (int)(200.0f * f3)), false);
        int n14 = n2;
        n6 = Math.max(0, Math.round((float)n14 * f5));
        I.a(class_3322, (float)n3, (float)(n4 += o.a(11)), (float)n14, (float)n13, (float)n13 / 2.0f, o.col(0xFFFFFF, (int)(30.0f * f3)), false);
        if (n6 > 0) {
            I.a(class_3322, (float)n3, (float)n4, (float)n6, (float)n13, (float)n13 / 2.0f, o.col(n10 & 0xFFFFFF, (int)(230.0f * f3)), false);
        }
        if (n6 > 0) {
            n7 = o.a(5);
            I.a(class_3322, (float)(n3 + n6 - n7 / 2), (float)(n4 - (n7 - n13) / 2), (float)n7, (float)n7, (float)n7 / 2.0f, o.col(0xFFFFFF, (int)(230.0f * f3)), false);
        }
        String string2 = o.a(l2);
        String string3 = o.a.e > 0L ? o.a(o.a.e) : "--:--";
        n4 = n4 + n13 + o.a(2);
        class_3322.method_51433((class_327)class_3102, string2, n3, n4, o.col(0x888888, (int)(160.0f * f3)), false);
        class_3322.method_51433((class_327)class_3102, string3, n3 + n14 - class_3102.method_1727(string3), n4, o.col(0x888888, (int)(160.0f * f3)), false);
    }

    private static String a(long l2) {
        long l3 = l2 / 1000L;
        return l3 / 60L + ":" + String.format("%02d", l3 % 60L);
    }

    private static int col(int n2, int n3) {
        return Math.max(0, Math.min(255, n3)) << 24 | n2 & 0xFFFFFF;
    }

    static {
        a = class_2960.method_60655((String)"water", (String)"spotify_art");
    }
}

