/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.UserApiService
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  net.minecraft.class_310
 *  net.minecraft.class_312
 *  net.minecraft.class_320
 *  net.minecraft.class_5520
 *  net.minecraft.class_7574
 *  net.minecraft.class_7853
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package com.example.compat.a.c.c;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_310;
import net.minecraft.class_312;
import net.minecraft.class_320;
import net.minecraft.class_5520;
import net.minecraft.class_7574;
import net.minecraft.class_7853;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={class_310.class})
public interface ConfigX {
    @Accessor(value="field_1726")
    public class_320 getSession();

    @Accessor(value="field_1726")
    @Mutable
    public void mc$setSession(class_320 var1);

    @Accessor(value="field_26902")
    @Mutable
    public void mc$setUserApiService(UserApiService var1);

    @Accessor(value="field_26842")
    @Mutable
    public void mc$setSocialInteractionsManager(class_5520 var1);

    @Accessor(value="field_39068")
    @Mutable
    public void mc$setProfileKeys(class_7853 var1);

    @Accessor(value="field_39492")
    @Mutable
    public void mc$setAbuseReportContext(class_7574 var1);

    @Accessor(value="field_45899")
    @Mutable
    public void mc$setGameProfileFuture(CompletableFuture<ProfileResult> var1);

    @Accessor(value="field_1729")
    public class_312 getMouse();

    @Invoker(value="method_1536")
    public boolean iDA();

    @Invoker(value="method_1583")
    public void iDIU();
}

