/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_2583
 */
package com.mutyx.q7.x9.r2;

import 608bbebf84695e52.b9a3bdf6a8e9e1b13.49342937c9ef478b;
import com.zenya.module.Category;
import com.zenya.module.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2583;

public final class c84d45145ef
extends Module {
    public c84d45145ef() {
        super("FakeStats", Category.e);
        this.$init$();
    }

    public static native c84d45145ef getInstance();

    private static native List<TextSegment> extractTextSegments(class_2561 var0);

    private static native String defaultIfEmpty(String var0, String var1);

    @Override
    public native void onEnable();

    @Override
    public native void onDisable();

    @Override
    public native void onTick();

    public native class_2561 fakeFooterText(class_2561 var1);

    private native class_2561 replaceFirstNumber(class_2561 var1, String var2, List<TextSegment> var3);

    private native void ensureSidebarCapture();

    private static native /* synthetic */ Optional lambda$extractTextSegments$0(ArrayList var0, class_2583 var1, String var2);

    private native /* synthetic */ void $init$();

    static {
        49342937c9ef478b.init(c84d45145ef.class, 36);
    }

    static final class FakeValues
    extends Record {
        native String toKey();

        @Override
        public final native String toString();

        @Override
        public final native int hashCode();

        @Override
        public final native boolean equals(Object var1);

        public native String money();

        public native String shards();

        public native String kills();

        public native String deaths();

        public native String playtime();

        private native /* synthetic */ void $init$(String var1, String var2, String var3, String var4, String var5);

        static {
            49342937c9ef478b.init(FakeValues.class, 63);
        }
    }

    static final class SidebarLine
    extends Record {
        @Override
        public final native String toString();

        @Override
        public final native int hashCode();

        @Override
        public final native boolean equals(Object var1);

        public native int score();

        public native class_2561 text();

        private native /* synthetic */ void $init$(int var1, class_2561 var2);

        static {
            49342937c9ef478b.init(SidebarLine.class, 114);
        }
    }

    static final class SidebarSnapshot
    extends Record {
        @Override
        public final native String toString();

        @Override
        public final native int hashCode();

        @Override
        public final native boolean equals(Object var1);

        public native class_2561 title();

        public native List<SidebarLine> lines();

        public native String signature();

        private native /* synthetic */ void $init$(class_2561 var1, List var2, String var3);

        static {
            49342937c9ef478b.init(SidebarSnapshot.class, 126);
        }
    }

    static final class TextSegment
    extends Record {
        @Override
        public final native String toString();

        @Override
        public final native int hashCode();

        @Override
        public final native boolean equals(Object var1);

        public native String value();

        public native class_2583 style();

        private native /* synthetic */ void $init$(String var1, class_2583 var2);

        static {
            49342937c9ef478b.init(TextSegment.class, 84);
        }
    }
}

