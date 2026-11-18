/*
 * Decompiled with CFR 0.152.
 */
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public class k {
    static Map a = new HashMap();
    private String a;
    private int a;

    static {
        InputStream inputStream = k.class.getResourceAsStream("/7db0858f-8ed1-4c4b-91d3-de70fe5fddf8.bin");
        byte[] byArray = inputStream.readAllBytes();
        int n10 = 0;
        do {
            byte[] byArray2 = Arrays.copyOfRange(byArray, n10, n10 + 64);
            byte[] byArray3 = Arrays.copyOfRange(byArray, n10 + 64, n10 + 68);
            String string = new String(byArray2).intern();
            a.put(string, new k(string, (byArray3[0] & 0xFF) << 24 | (byArray3[1] & 0xFF) << 16 | (byArray3[2] & 0xFF) << 8 | byArray3[3] & 0xFF));
        } while ((n10 += 68) < byArray.length);
    }

    k(String string, int n10) {
        this.a = string;
        this.a = n10;
    }

    public static String a(byte[] byArray) {
        byte[] byArray2 = MessageDigest.getInstance("SHA-256").digest(byArray);
        StringBuilder stringBuilder = new StringBuilder();
        int n10 = 0;
        do {
            stringBuilder.append(String.format("%02x", (byte)(byArray2[n10] & 0xFF)));
        } while (++n10 < byArray2.length);
        return stringBuilder.toString();
    }

    public static int a(Class clazz, int n10) {
        String string = clazz.getName().replace('.', '/').concat(".class");
        ClassLoader classLoader = clazz.getClassLoader();
        InputStream inputStream = classLoader != null ? classLoader.getResourceAsStream(string) : ClassLoader.getSystemResourceAsStream(string);
        if (inputStream == null) {
            return -1;
        }
        k k2 = (k)a.get(k.a(inputStream.readAllBytes()));
        return k2.a ^ (k2.a.hashCode() ^ n10);
    }
}

