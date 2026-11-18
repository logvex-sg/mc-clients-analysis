/*
 * Decompiled with CFR 0.152.
 */
package com.example.compat.a.a.g;

import com.example.compat.a.a.a.ConfigA;
import com.example.compat.a.c.c.ConfigX;
import com.example.compat.a.c.c.EventZ;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ItemQ {
    private static final ExecutorService a = Executors.newFixedThreadPool(2);

    public static void a(int n) {
        ItemQ.a(n, 35);
    }

    public static void a(int n, int n2) {
        a.submit(() -> ItemQ.lambda$mouseClick$0(n, n2));
    }

    public static void b(int n) {
        EventZ eventZ = (EventZ)((ConfigX)ConfigA.e).getMouse();
    }

    public static void c(int n) {
        EventZ eventZ = (EventZ)((ConfigX)ConfigA.e).getMouse();
    }

    private static void lambda$mouseClick$0(int n, int n2) {
        try {
            ItemQ.b(n);
            Thread.sleep(n2);
            ItemQ.c(n);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }
}

