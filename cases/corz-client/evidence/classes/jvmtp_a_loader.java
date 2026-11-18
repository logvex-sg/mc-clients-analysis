/*
 * Decompiled with CFR 0.152.
 */
package a.b99571f71427e3b19;

import a.b99571f71427e3b19.C50195a7cf0;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class a {
    private static String mc2a9944fecca7ffa;
    private static byte[] mb5b403924f1f92cb;
    private static byte[] m62c1750435a98902;
    private static transient /* synthetic */ String qkJqoqrtGS = "uGF9d26ADJD1pwc7dKkMnpMFiBc3gyeJo7/AqXBycJU=";

    public static native void init(Class<?> var0, int var1);

    private static native void bootstrap(Class<?> var0, byte[] var1, byte[] var2, byte[] var3, String var4);

    private static void mfd5447eb2833d625(String platform) throws IOException {
        byte[] container = a.m2797329048170746("native/" + mc2a9944fecca7ffa + "/s1");
        byte[] variant = a.mc4570111f0c9a479(container, platform);
        if (variant == null) {
            throw new UnsatisfiedLinkError("invalid platform: " + platform);
        }
        File tempFile = File.createTempFile("jvmtp-s1-" + mc2a9944fecca7ffa + "-", a.m279336a8564800cd(platform));
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), variant, new OpenOption[0]);
        System.load(tempFile.getAbsolutePath());
    }

    private static byte[] mc4570111f0c9a479(byte[] c, String platform) {
        int p = 0;
        int count = (c[p] & 0xFF) << 8 | c[p + 1] & 0xFF;
        p += 2;
        for (int i = 0; i < count; ++i) {
            int tagLen = c[p++] & 0xFF;
            String tag = new String(c, p, tagLen, StandardCharsets.UTF_8);
            int offset = a.me5d84ad517ef41fe(c, p += tagLen);
            int len = a.me5d84ad517ef41fe(c, p += 4);
            p += 4;
            if (!tag.equals(platform)) continue;
            return Arrays.copyOfRange(c, offset, offset + len);
        }
        return null;
    }

    private static int me5d84ad517ef41fe(byte[] c, int o) {
        return (c[o] & 0xFF) << 24 | (c[o + 1] & 0xFF) << 16 | (c[o + 2] & 0xFF) << 8 | c[o + 3] & 0xFF;
    }

    private static byte[] m2797329048170746(String name) throws IOException {
        try (InputStream is = a.class.getResourceAsStream("/" + name);){
            if (is == null) {
                throw new IOException("resource not found: " + name);
            }
            byte[] byArray = is.readAllBytes();
            return byArray;
        }
    }

    private static byte[] m82512a2c3f67707b() {
        if (mb5b403924f1f92cb == null || m62c1750435a98902 == null) {
            return new byte[32];
        }
        byte[] baked = new byte[mb5b403924f1f92cb.length];
        for (int i = 0; i < baked.length; ++i) {
            baked[i] = (byte)(mb5b403924f1f92cb[i] ^ m62c1750435a98902[i % m62c1750435a98902.length]);
        }
        return a.m0e115e67801e1457(baked, a.m58df9ccb68c003f2());
    }

    private static byte[] m0e115e67801e1457(byte[] baked, byte[] tag) {
        byte[] in = new byte[baked.length + tag.length];
        System.arraycopy(baked, 0, in, 0, baked.length);
        System.arraycopy(tag, 0, in, baked.length, tag.length);
        return a.mbf37c74475433b2f(in);
    }

    private static byte[] m58df9ccb68c003f2() {
        return C50195a7cf0.md1d6a06057();
    }

    private static byte[] mbf37c74475433b2f(byte[] in) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(in);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static String m902bed67d4b6b206() {
        String archName;
        boolean isAarch64;
        String osName;
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        if (a.m752838b851d71044()) {
            return a.mb0d9d29e72f62533(arch) + "-android";
        }
        if (os.contains("darwin") || os.contains("mac")) {
            osName = "macos";
        } else if (os.contains("win")) {
            osName = "windows";
        } else if (os.contains("linux")) {
            osName = "linux";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
        boolean bl = isAarch64 = arch.contains("aarch64") || arch.contains("arm64");
        if (isAarch64) {
            archName = "aarch64";
        } else if (arch.contains("amd64") || arch.contains("x86_64") || arch.contains("x64")) {
            archName = "x86_64";
        } else {
            throw new UnsupportedOperationException("Unsupported architecture: " + arch);
        }
        return archName + "-" + osName;
    }

    private static boolean m752838b851d71044() {
        return System.getProperty("java.vm.name", "").equalsIgnoreCase("Dalvik") || System.getProperty("java.runtime.name", "").toLowerCase().contains("android");
    }

    private static String mb0d9d29e72f62533(String arch) {
        if (arch.contains("aarch64") || arch.contains("arm64")) {
            return "aarch64";
        }
        if (arch.contains("x86_64") || arch.contains("amd64")) {
            return "x86_64";
        }
        if (arch.contains("arm")) {
            return "armv7a";
        }
        if (arch.contains("x86") || arch.contains("i686") || arch.contains("i386")) {
            return "x86";
        }
        throw new UnsupportedOperationException("Unsupported Android architecture: " + arch);
    }

    private static String m279336a8564800cd(String platform) {
        if (platform.contains("windows")) {
            return ".dll";
        }
        if (platform.contains("macos")) {
            return ".dylib";
        }
        return ".so";
    }

    static {
        mb5b403924f1f92cb = new byte[]{-112, 94, 70, -83, -94, -67, 105, -128, 117, 28, 125, -55, 26, -60, 93, -96, 27, -59, -42, 10, -11, 92, -127, 55, -55, 37, 75, -24, -102, 70, -81, 90};
        m62c1750435a98902 = new byte[]{101, -110, -1, -94, 73, 113, -9, -70, 6, -54, 102, -114, -55, -23, -7, 68, -53, 41, 120, -68, -63, -72, -68, 112, 118, -53, -53, 48, 40, 64, -14, 92};
        mc2a9944fecca7ffa = "99571f71427e3b19";
        try {
            String platform = a.m902bed67d4b6b206();
            a.mfd5447eb2833d625(platform);
            byte[] blob = a.m2797329048170746("native/" + mc2a9944fecca7ffa + "/r");
            byte[] bk = a.m2797329048170746("native/" + mc2a9944fecca7ffa + "/bk");
            a.bootstrap(a.class, blob, bk, a.m82512a2c3f67707b(), "native/" + mc2a9944fecca7ffa + "/" + platform);
        }
        catch (IOException e) {
            throw new UnsatisfiedLinkError("jvmtp bootstrap failed: " + e.getMessage());
        }
    }
}
