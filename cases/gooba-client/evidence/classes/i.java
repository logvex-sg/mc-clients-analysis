/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashMap;
import java.util.Map;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public class i {
    private static Map a = new HashMap();
    private static Map b = new HashMap();
    public static Class a;

    private static long a(String string) {
        long l10 = -3750763034362895579L;
        for (int i2 = 0; i2 < string.length(); ++i2) {
            l10 ^= (long)string.charAt(i2);
            l10 *= 1099511628211L;
        }
        return l10;
    }

    private static void a() {
        Map map = a;
        map.put(-5808556873153909620L, -587402869);
        map.put(-5808553574619024987L, 849239516);
        map.put(-5808554674130653198L, 405555646);
        map.put(-5808560171688794253L, 724338726);
        map.put(-5808561271200422464L, 937989761);
        map.put(-5808557972665537831L, 1806271838);
        map.put(-5808559072177166042L, 1350246186);
        map.put(-5808546977549255721L, 1823789749);
    }

    public static int a(Object object, int n10) {
        int n11 = (Integer)a.get(i.a(((Class)object).getName())) ^ n10;
        b.put(i.a(((Class)object).getName()), n11);
        return n11;
    }

    public static int b(Object object, int n10) {
        StackTraceElement[] stackTraceElementArray = new Throwable().getStackTrace();
        long l10 = stackTraceElementArray[2].getClassName().startsWith("java") ? i.a(stackTraceElementArray[7].getClassName()) : i.a(stackTraceElementArray[2].getClassName());
        int n11 = (Integer)a.get(i.a(((Class)object).getName())) ^ n10 ^ (Integer)b.get(l10);
        b.put(i.a(((Class)object).getName()), n11);
        return n11;
    }

    static {
        i.a();
        a = j.class;
    }
}

