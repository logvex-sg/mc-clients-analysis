/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2583
 *  net.minecraft.class_5348
 *  net.minecraft.class_5481
 */
package zyron.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2583;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import zyron.framework.Category;
import zyron.framework.Module;
import zyron.framework.Setting;
import zyron.framework.StringMapSetting;
import zyron.framework.StringSetting;

@Environment(value=EnvType.CLIENT)
public final class ModuleNameProtect
extends Module {
    private static final int maxMatchLength = 32;
    private static ModuleNameProtect instance;
    private final Setting<String> protectedName;
    private final StringMapSetting protectFriends;

    public ModuleNameProtect(Category var1) {
        super(var1, "name-protect", "Replaces selected Minecraft names only while rendering on your screen.");
        this.protectedName = this.settings.getDefaultGroup().add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("your-protected-name")).description("Name shown instead of your real Minecraft account name on this screen only.")).placeholder("Protected name").defaultValue("Protected Player")).build());
        this.protectFriends = this.addStringMapSetting(((StringMapSetting.Builder)((StringMapSetting.Builder)new StringMapSetting.Builder().name("protect-friends")).description("Opens a menu for protecting multiple friends with local fake names.")).build());
        instance = this;
    }

    private StringMapSetting addStringMapSetting(StringMapSetting var1) {
        this.settings.getDefaultGroup().add(var1);
        return var1;
    }

    public static String replaceRenderedString(String var0) {
        ModuleNameProtect var1 = instance;
        return var1 == null ? var0 : var1.replaceInString(var0);
    }

    public static class_5348 replaceRenderedVisitable(class_5348 var0) {
        ModuleNameProtect var1 = instance;
        if (var1 != null && var0 != null && var1.shouldReplace()) {
            ArrayList var2 = new ArrayList();
            var0.method_27658((var2x, var3) -> {
                var2.add(class_5348.method_29431((String)var1.replaceInString(var3), (class_2583)var2x));
                return Optional.empty();
            }, class_2583.field_24360);
            return var2.isEmpty() ? var0 : class_5348.method_29432(var2);
        }
        return var0;
    }

    public static class_5481 replaceRenderedOrdered(class_5481 var0) {
        ModuleNameProtect var1 = instance;
        return var1 != null && var0 != null && var1.shouldReplace() ? var1.replaceInOrderedText(var0) : var0;
    }

    private String replaceInString(String var1) {
        if (var1 != null && !var1.isEmpty() && this.shouldReplace()) {
            List<NameReplacement> var2 = this.collectReplacements();
            if (var2.isEmpty()) {
                return var1;
            }
            StringBuilder var3 = new StringBuilder(var1.length());
            boolean var4 = false;
            int var5 = 0;
            while (var5 < var1.length()) {
                NameReplacement var6 = ModuleNameProtect.findReplacementAt(var1, var5, var2);
                if (var6 != null) {
                    var3.append(var6.replacement);
                    var5 += var6.source.length();
                    var4 = true;
                    continue;
                }
                int var7 = var1.codePointAt(var5);
                var3.appendCodePoint(var7);
                var5 += Character.charCount(var7);
            }
            return var4 ? var3.toString() : var1;
        }
        return var1;
    }

    private class_5481 replaceInOrderedText(class_5481 var1) {
        ArrayList var2 = new ArrayList();
        var1.accept((var1x, var2x, var3x) -> {
            var2.add(new StyledCodePoint(var3x, var2x));
            return true;
        });
        if (var2.isEmpty()) {
            return var1;
        }
        StringBuilder var3 = new StringBuilder(var2.size());
        ArrayList<Integer> var4 = new ArrayList<Integer>(var2.size() + 1);
        for (StyledCodePoint var6 : var2) {
            var4.add(var3.length());
            var3.appendCodePoint(var6.codePoint);
        }
        var4.add(var3.length());
        String var14 = var3.toString();
        List<NameReplacement> var15 = this.collectReplacements();
        if (var15.isEmpty()) {
            return var1;
        }
        ArrayList<StyledCodePoint> var7 = new ArrayList<StyledCodePoint>();
        boolean var8 = false;
        int var9 = 0;
        while (var9 < var2.size()) {
            int var10 = (Integer)var4.get(var9);
            NameReplacement var11 = ModuleNameProtect.findReplacementAt(var14, var10, var15);
            if (var11 == null) {
                var7.add((StyledCodePoint)var2.get(var9++));
                continue;
            }
            class_2583 var12 = ((StyledCodePoint)var2.get((int)var9)).style;
            var11.replacement.codePoints().forEach(var2x -> var7.add(new StyledCodePoint(var2x, var12)));
            int var13 = var10 + var11.source.length();
            while (++var9 < var2.size() && (Integer)var4.get(var9) < var13) {
            }
            var8 = true;
        }
        return !var8 ? var1 : var1x -> {
            int var2x = 0;
            for (StyledCodePoint var4x : var7) {
                if (!var1x.accept(var2x, var4x.style, var4x.codePoint)) {
                    return false;
                }
                var2x += Character.charCount(var4x.codePoint);
            }
            return true;
        };
    }

    private boolean shouldReplace() {
        return this.isActive() && this.mc.method_1548() != null;
    }

    private List<NameReplacement> collectReplacements() {
        ArrayList<NameReplacement> var1 = new ArrayList<NameReplacement>(2);
        String var2 = ModuleNameProtect.stripFormatting(this.mc.method_1548().method_1676());
        String var3 = ModuleNameProtect.stripFormatting(this.protectedName.get());
        ModuleNameProtect.addReplacement(var1, var2, var3);
        ((Map)this.protectFriends.get()).forEach((var1x, var2x) -> ModuleNameProtect.addReplacement(var1, ModuleNameProtect.stripFormatting(var1x), ModuleNameProtect.stripFormatting(var2x)));
        var1.sort(Comparator.comparingInt(var0 -> var0.source.length()).reversed());
        return var1;
    }

    private static void addReplacement(List<NameReplacement> var0, String var1, String var2) {
        if (!(var1.isEmpty() || var2.isEmpty() || var1.equalsIgnoreCase(var2))) {
            var0.add(new NameReplacement(var1, var2));
        }
    }

    private static NameReplacement findReplacementAt(String var0, int var1, List<NameReplacement> var2) {
        for (NameReplacement var4 : var2) {
            int var5 = var1 + var4.source.length();
            if (var5 > var0.length() || !var0.regionMatches(true, var1, var4.source, 0, var4.source.length()) || var1 > 0 && ModuleNameProtect.isWordCharacter(var0.codePointBefore(var1)) || var5 < var0.length() && ModuleNameProtect.isWordCharacter(var0.codePointAt(var5))) continue;
            return var4;
        }
        return null;
    }

    private static boolean isWordCharacter(int var0) {
        return var0 == 95 || var0 >= 48 && var0 <= 57 || var0 >= 65 && var0 <= 90 || var0 >= 97 && var0 <= 122;
    }

    private static String stripFormatting(String var0) {
        int var4;
        if (var0 == null) {
            return "";
        }
        StringBuilder var1 = new StringBuilder();
        boolean var2 = false;
        for (int var3 = 0; var3 < var0.length() && var1.length() < 32; var3 += Character.charCount(var4)) {
            var4 = var0.codePointAt(var3);
            if (var2) {
                var2 = false;
                continue;
            }
            if (var4 != 167) continue;
            var2 = true;
        }
        return var1.toString().strip();
    }

    @Environment(value=EnvType.CLIENT)
    private static final class NameReplacement {
        private final String source;
        private final String replacement;

        private NameReplacement(String source, String replacement) {
            this.source = source;
            this.replacement = replacement;
        }

        public String source() {
            return this.source;
        }

        public String replacement() {
            return this.replacement;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NameReplacement)) {
                return false;
            }
            NameReplacement other = (NameReplacement)o;
            return Objects.equals(this.source, other.source) && Objects.equals(this.replacement, other.replacement);
        }

        public int hashCode() {
            return Objects.hash(this.source, this.replacement);
        }

        public String toString() {
            return "NameReplacement[source=" + this.source + ", replacement=" + this.replacement + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class StyledCodePoint {
        private final int codePoint;
        private final class_2583 style;

        private StyledCodePoint(int codePoint, class_2583 style) {
            this.codePoint = codePoint;
            this.style = style;
        }

        public int codePoint() {
            return this.codePoint;
        }

        public class_2583 style() {
            return this.style;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof StyledCodePoint)) {
                return false;
            }
            StyledCodePoint other = (StyledCodePoint)o;
            return Objects.equals(this.codePoint, other.codePoint) && Objects.equals(this.style, other.style);
        }

        public int hashCode() {
            return Objects.hash(this.codePoint, this.style);
        }

        public String toString() {
            return "StyledCodePoint[codePoint=" + this.codePoint + ", style=" + String.valueOf(this.style) + "]";
        }
    }
}

