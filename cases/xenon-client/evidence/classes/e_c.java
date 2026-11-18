/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_5348
 *  net.minecraft.class_5481
 */
package com.xenon.e;

import com.xenon.c.a.d.g;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5348;
import net.minecraft.class_5481;

public final class c {
    transient String myMetaphor = "myMetaphor @ jnt.so";

    private c() {
    }

    public static String replace(String string) {
        if (string == null || !c.isActive()) {
            return string;
        }
        String string2 = c.getRealName();
        String string3 = c.getFakeName();
        if (string2 == null || string3 == null || string2.isBlank() || string2.equals(string3)) {
            return string;
        }
        return string.contains(string2) ? string.replace(string2, string3) : string;
    }

    public static class_5348 replace(class_5348 class_53482) {
        if (class_53482 == null || !c.isActive()) {
            return class_53482;
        }
        List<a> list = c.replaceChunks(c.collect(class_53482));
        if (list == null) {
            return class_53482;
        }
        if (list.isEmpty()) {
            return class_5348.field_25310;
        }
        ArrayList<class_5348> arrayList = new ArrayList<class_5348>(list.size());
        for (a a2 : list) {
            arrayList.add(class_5348.method_29431((String)a2.text(), (class_2583)a2.style()));
        }
        return class_5348.method_29432(arrayList);
    }

    public static class_5481 replace(class_5481 class_54812) {
        if (class_54812 == null || !c.isActive()) {
            return class_54812;
        }
        List<a> list = c.replaceChunks(c.collect(class_54812));
        if (list == null) {
            return class_54812;
        }
        if (list.isEmpty()) {
            return class_5481.method_34905();
        }
        ArrayList<class_5481> arrayList = new ArrayList<class_5481>(list.size());
        for (a a2 : list) {
            arrayList.add(class_5481.method_30747((String)a2.text(), (class_2583)a2.style()));
        }
        return class_5481.method_30749(arrayList);
    }

    private static boolean isActive() {
        return g.instance != null && g.instance.isEnabled() && c.getRealName() != null && !c.getRealName().isBlank();
    }

    private static String getRealName() {
        class_310 class_3102 = class_310.method_1551();
        if (class_3102 == null || class_3102.method_1548() == null) {
            return null;
        }
        return class_3102.method_1548().method_1676();
    }

    private static String getFakeName() {
        if (g.instance == null) {
            return null;
        }
        return g.instance.getFakeName();
    }

    private static List<a> collect(class_5348 class_53482) {
        ArrayList<a> arrayList = new ArrayList<a>();
        class_53482.method_27658((class_25832, string) -> {
            c.appendCodePoints(arrayList, string, class_25832);
            return Optional.empty();
        }, class_2583.field_24360);
        return arrayList;
    }

    private static List<a> collect(class_5481 class_54812) {
        ArrayList<a> arrayList = new ArrayList<a>();
        class_54812.accept((n, class_25832, n2) -> {
            arrayList.add(new a(new String(Character.toChars(n2)), class_25832));
            return true;
        });
        return arrayList;
    }

    private static void appendCodePoints(List<a> list, String string, class_2583 class_25832) {
        int n;
        if (string == null || string.isEmpty()) {
            return;
        }
        for (int i2 = 0; i2 < string.length(); i2 += Character.charCount(n)) {
            n = string.codePointAt(i2);
            list.add(new a(new String(Character.toChars(n)), class_25832));
        }
    }

    private static List<a> replaceChunks(List<a> list) {
        int n;
        String string = c.getRealName();
        String string2 = c.getFakeName();
        if (string == null || string2 == null || string.isBlank() || string.equals(string2)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList<Integer> arrayList = new ArrayList<Integer>(list.size());
        for (a object2 : list) {
            arrayList.add(stringBuilder.length());
            stringBuilder.append(object2.text());
        }
        String string3 = stringBuilder.toString();
        if (!string3.contains(string)) {
            return null;
        }
        ArrayList<a> arrayList2 = new ArrayList<a>();
        int n2 = 0;
        int n3 = 0;
        while ((n = string3.indexOf(string, n3)) >= 0) {
            while (n2 < list.size() && (Integer)arrayList.get(n2) < n) {
                arrayList2.add(list.get(n2++));
            }
            class_2583 class_25832 = n2 < list.size() ? list.get(n2).style() : class_2583.field_24360;
            arrayList2.add(new a(string2, class_25832));
            int n4 = n + string.length();
            while (n2 < list.size() && (Integer)arrayList.get(n2) < n4) {
                ++n2;
            }
            n3 = n4;
        }
        while (n2 < list.size()) {
            arrayList2.add(list.get(n2++));
        }
        return c.merge(arrayList2);
    }

    private static List<a> merge(List<a> list) {
        if (list.isEmpty()) {
            return list;
        }
        ArrayList<a> arrayList = new ArrayList<a>(list.size());
        a a2 = list.getFirst();
        for (int i2 = 1; i2 < list.size(); ++i2) {
            a a3 = list.get(i2);
            if (Objects.equals(a2.style(), a3.style())) {
                String string = a3.text();
                String string2 = a2.text();
                a2 = new a(string2 + string, a2.style());
                continue;
            }
            arrayList.add(a2);
            a2 = a3;
        }
        arrayList.add(a2);
        return arrayList;
    }

    private static final class a
    extends Record {
        private final String text;
        private final class_2583 style;
        transient String myMetaphor = "myMetaphor @ jnt.so";

        private a(String string, class_2583 class_25832) {
            class_25832 = class_25832 == null ? class_2583.field_24360 : class_25832;
            this.text = string;
            this.style = class_25832;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{a.class, "text;style", "text", "style"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{a.class, "text;style", "text", "style"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{a.class, "text;style", "text", "style"}, this, object);
        }

        public String text() {
            return this.text;
        }

        public class_2583 style() {
            return this.style;
        }
    }
}

