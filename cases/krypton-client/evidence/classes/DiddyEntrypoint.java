/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.api.ModInitializer
 *  net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
 *  org.lwjgl.glfw.GLFW
 */
package dev.krypton.diddy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.lwjgl.glfw.GLFW;

public final class DiddyEntrypoint
implements PreLaunchEntrypoint,
ModInitializer,
ClientModInitializer {
    private static boolean lastRightShift;
    private static Object clientInstance;
    private static Object kryptonClient;
    private static long lastOpenFailureLog;
    private static int nkiRenderCalls;
    private static int nkiQppCalls;
    private static int forcedNkiQppCalls;
    private static int vpQppCalls;
    private static int forcedVpQppCalls;
    private static int hudEnableChecks;
    private static int autosaveTicks;
    private static boolean clickGuiPrimed;
    private static Object configAppliedToManager;
    private static boolean applyingConfig;
    private static boolean savingConfig;
    private static final Set<Object> initializedModules;
    private static final IdentityHashMap<Object, Object> keybindOwners;

    public void onPreLaunch() {
        DiddyEntrypoint.ensureKryptonClientSkeleton();
    }

    public void onInitialize() {
        DiddyEntrypoint.ensureKryptonClientSkeleton();
    }

    public void onInitializeClient() {
        DiddyEntrypoint.ensureKryptonClientSkeleton();
    }

    public static synchronized void initializeClientAtMinecraftTail(Object object) {
        DiddyEntrypoint.bindClient(object);
    }

    public static void onClientTick(Object object) {
        DiddyEntrypoint.bindClient(object);
        DiddyEntrypoint.dispatchEvent("xr");
        if (DiddyEntrypoint.isInGame(object) && (++hudEnableChecks & 0x1F) == 1) {
            DiddyEntrypoint.primeClickGuiState();
            DiddyEntrypoint.enableHudModule();
            DiddyEntrypoint.initializeEnabledModules();
        }
        if (DiddyEntrypoint.isInGame(object)) {
            DiddyEntrypoint.dispatchEvent("mk");
        }
        DiddyEntrypoint.forwardMovementKeys(object);
        boolean bl = DiddyEntrypoint.isRightShiftDown(object);
        if (bl && !lastRightShift && !DiddyEntrypoint.shouldSuppressKeybinds()) {
            DiddyEntrypoint.openClickGui(object);
        }
        lastRightShift = bl;
        DiddyEntrypoint.autosaveAppliedConfig();
    }

    private static synchronized void bindClient(Object object) {
        if (object == null) {
            return;
        }
        clientInstance = object;
        try {
            ClassLoader classLoader = DiddyEntrypoint.class.getClassLoader();
            Class<?> clazz = Class.forName("fa", true, classLoader);
            DiddyEntrypoint.ensureKryptonClientSkeleton();
            DiddyEntrypoint.setStaticField(clazz, "lr", object);
            DiddyEntrypoint.setStaticField(clazz, "euq", kryptonClient);
            DiddyEntrypoint.ensureClientField(clazz, "qn", Class.forName("xw", true, classLoader));
            DiddyEntrypoint.ensureRealClientField(clazz, "bs", Class.forName("nc", true, classLoader));
            DiddyEntrypoint.setInstanceField(kryptonClient, "config", clazz.getField("qn").get(kryptonClient));
            DiddyEntrypoint.ensureClientField(clazz, "av", Class.forName("mdl", true, classLoader));
            DiddyEntrypoint.ensureRenderMatrixStack(classLoader);
            DiddyEntrypoint.initializeRenderer(classLoader);
            DiddyEntrypoint.initializeFontRenderer(classLoader);
            DiddyEntrypoint.applyConfigOnce(clazz);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new RuntimeException("Failed to bind Krypton client state", reflectiveOperationException);
        }
    }

    public static void ensureRenderMatrixStack() {
        DiddyEntrypoint.ensureRenderMatrixStack(DiddyEntrypoint.class.getClassLoader());
    }

    private static void ensureRenderMatrixStack(ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName("fa", true, classLoader);
            Field field = clazz.getField("wwv");
            if (field.get(null) == null) {
                Class<?> clazz2 = Class.forName("net.minecraft.class_4587", false, classLoader);
                field.set(null, clazz2.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]));
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static synchronized void ensureKryptonClientSkeleton() {
        try {
            ClassLoader classLoader = DiddyEntrypoint.class.getClassLoader();
            Class<?> clazz = Class.forName("fa", true, classLoader);
            if (kryptonClient == null) {
                kryptonClient = DiddyEntrypoint.allocateWithoutConstructor(clazz);
            }
            DiddyEntrypoint.setStaticField(clazz, "euq", kryptonClient);
            DiddyEntrypoint.ensureAllocatedClientField(clazz, "qn", Class.forName("xw", true, classLoader));
            DiddyEntrypoint.setInstanceField(kryptonClient, "config", clazz.getField("qn").get(kryptonClient));
            DiddyEntrypoint.ensureMinimalModuleManager(clazz, Class.forName("nc", true, classLoader));
            DiddyEntrypoint.ensureAllocatedClientField(clazz, "av", Class.forName("mdl", true, classLoader));
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static Object allocateWithoutConstructor(Class<?> clazz) throws ReflectiveOperationException {
        Constructor constructor = Object.class.getDeclaredConstructor(new Class[0]);
        constructor.setAccessible(true);
        Class<?> clazz2 = Class.forName("sun.reflect.ReflectionFactory");
        Object object = clazz2.getMethod("getReflectionFactory", new Class[0]).invoke(null, new Object[0]);
        Method method = clazz2.getMethod("newConstructorForSerialization", Class.class, Constructor.class);
        Constructor constructor2 = (Constructor)method.invoke(object, clazz, constructor);
        constructor2.setAccessible(true);
        return constructor2.newInstance(new Object[0]);
    }

    private static void ensureAllocatedClientField(Class<?> clazz, String string, Class<?> clazz2) throws ReflectiveOperationException {
        Field field = clazz.getField(string);
        Object object = field.get(kryptonClient);
        if (object == null || !clazz2.isInstance(object)) {
            field.set(kryptonClient, DiddyEntrypoint.allocateWithoutConstructor(clazz2));
        }
    }

    private static void ensureMinimalModuleManager(Class<?> clazz, Class<?> clazz2) throws ReflectiveOperationException {
        Field field = clazz.getField("bs");
        Object object = field.get(kryptonClient);
        if (object != null && clazz2.isInstance(object)) {
            return;
        }
        Object object2 = DiddyEntrypoint.allocateWithoutConstructor(clazz2);
        ArrayList arrayList = new ArrayList();
        DiddyEntrypoint.setInstanceField(object2, "modules", arrayList);
        DiddyEntrypoint.setInstanceField(object2, "bt", arrayList);
        field.set(kryptonClient, object2);
    }

    private static boolean isRightShiftDown(Object object) {
        try {
            Method method = object.getClass().getMethod("method_22683", new Class[0]);
            Object object2 = method.invoke(object, new Object[0]);
            Method method2 = object2.getClass().getMethod("method_4490", new Class[0]);
            long l2 = (Long)method2.invoke(object2, new Object[0]);
            return GLFW.glfwGetKey((long)l2, (int)344) == 1;
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return false;
        }
    }

    public static void openClickGui(Object object) {
        block7: {
            try {
                if (object == null) {
                    object = clientInstance;
                }
                if (object == null) {
                    return;
                }
                if (!DiddyEntrypoint.isInGame(object)) {
                    return;
                }
                if (DiddyEntrypoint.shouldSuppressKeybinds()) {
                    return;
                }
                DiddyEntrypoint.bindClient(object);
                Object object2 = DiddyEntrypoint.getCurrentScreen(object);
                if (DiddyEntrypoint.isClickGuiScreen(object2)) {
                    return;
                }
                Object object3 = DiddyEntrypoint.clickGuiScreen();
                DiddyEntrypoint.setClientField("os", DiddyEntrypoint.shouldRenderBehindClickGui(object2) ? object2 : null);
                DiddyEntrypoint.setClientField("ql", null);
                DiddyEntrypoint.setClientField("eag", object3);
                DiddyEntrypoint.setScreen(object, object3);
                DiddyEntrypoint.setClientField("eag", object3);
            }
            catch (LinkageError | ReflectiveOperationException throwable) {
                long l2 = System.currentTimeMillis();
                if (l2 - lastOpenFailureLog <= 5000L) break block7;
                lastOpenFailureLog = l2;
                throwable.printStackTrace();
            }
        }
    }

    private static Object clickGuiScreen() throws ReflectiveOperationException {
        return DiddyEntrypoint.createClickGuiScreen();
    }

    private static void primeClickGuiState() {
        if (clickGuiPrimed) {
            return;
        }
        try {
            DiddyEntrypoint.createClickGuiScreen();
            clickGuiPrimed = true;
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    public static Object createClickGuiScreen() throws ReflectiveOperationException {
        Object object;
        Object object2;
        Class<?> clazz = Class.forName("vp", true, DiddyEntrypoint.class.getClassLoader());
        Field field = Class.forName("fa", true, DiddyEntrypoint.class.getClassLoader()).getField("eag");
        if (kryptonClient == null) {
            Class<?> clazz2 = Class.forName("fa", true, DiddyEntrypoint.class.getClassLoader());
            object2 = clazz2.getDeclaredConstructor(new Class[0]);
            ((Constructor)object2).setAccessible(true);
            kryptonClient = ((Constructor)object2).newInstance(new Object[0]);
            clazz2.getField("euq").set(null, kryptonClient);
        }
        if (clazz.isInstance(object = field.get(kryptonClient))) {
            return object;
        }
        object2 = DiddyEntrypoint.constructClickGuiWithoutBeacon(clazz);
        field.set(kryptonClient, object2);
        return object2;
    }

    private static Object constructClickGuiWithoutBeacon(Class<?> clazz) throws ReflectiveOperationException {
        ClassLoader classLoader = DiddyEntrypoint.class.getClassLoader();
        Object object = DiddyEntrypoint.allocateScreenWithBaseConstructor(clazz, DiddyEntrypoint.emptyText(classLoader));
        ArrayList arrayList = new ArrayList();
        DiddyEntrypoint.setInstanceField(object, "cvs", arrayList);
        DiddyEntrypoint.setInstanceField(object, "ie", null);
        DiddyEntrypoint.setInstanceField(object, "ru", -1.0);
        DiddyEntrypoint.setInstanceField(object, "bh", null);
        Class<?> clazz2 = Class.forName("yb", true, classLoader);
        Object[] objectArray = (Object[])clazz2.getMethod("values", new Class[0]).invoke(null, new Object[0]);
        Class<?> clazz3 = Class.forName("xx", true, classLoader);
        Constructor<?> constructor = clazz3.getDeclaredConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, clazz2, clazz);
        constructor.setAccessible(true);
        int n2 = 50;
        for (Object object2 : objectArray) {
            Object obj = constructor.newInstance(n2, 50, 230, 30, object2, object);
            arrayList.add(obj);
            n2 += 250;
        }
        Class<?> clazz4 = Class.forName("io", true, classLoader);
        Constructor constructor2 = clazz4.getDeclaredConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, clazz);
        constructor2.setAccessible(true);
        Object t2 = constructor2.newInstance(n2, 50, 230, object);
        DiddyEntrypoint.setInstanceField(object, "qjq", t2);
        return object;
    }

    private static void normalizeCategoryNames(ClassLoader classLoader) {
        try {
            Object[] objectArray;
            Class<?> clazz = Class.forName("yb", true, classLoader);
            for (Object object : objectArray = (Object[])clazz.getMethod("values", new Class[0]).invoke(null, new Object[0])) {
                DiddyEntrypoint.setStringFieldIfPresent(object, "name", "Pedro");
                DiddyEntrypoint.setStringFieldIfPresent(object, "pzn", "redstone");
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static void setStringFieldIfPresent(Object object, String string, String string2) {
        if (object == null) {
            return;
        }
        try {
            Field field = DiddyEntrypoint.declaredField(object.getClass(), string);
            field.setAccessible(true);
            field.set(object, string2);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static Object allocateScreenWithBaseConstructor(Class<?> clazz, Object object) throws ReflectiveOperationException {
        ClassLoader classLoader = DiddyEntrypoint.class.getClassLoader();
        Class<?> clazz2 = Class.forName("net.minecraft.class_2561", false, classLoader);
        Class<?> clazz3 = Class.forName("net.minecraft.class_437", false, classLoader);
        Constructor<?> constructor = clazz3.getDeclaredConstructor(clazz2);
        constructor.setAccessible(true);
        Class<?> clazz4 = Class.forName("sun.reflect.ReflectionFactory");
        Object object2 = clazz4.getMethod("getReflectionFactory", new Class[0]).invoke(null, new Object[0]);
        Method method = clazz4.getMethod("newConstructorForSerialization", Class.class, Constructor.class);
        Constructor constructor2 = (Constructor)method.invoke(object2, clazz, constructor);
        constructor2.setAccessible(true);
        return constructor2.newInstance(object);
    }

    private static Object emptyText(ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName("net.minecraft.class_2561", false, classLoader);
        for (Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !clazz.isAssignableFrom(method.getReturnType()) || method.getParameterTypes().length != 1 || method.getParameterTypes()[0] != String.class) continue;
            method.setAccessible(true);
            return method.invoke(null, "");
        }
        throw new NoSuchMethodException("empty text factory");
    }

    private static boolean shouldRenderBehindClickGui(Object object) {
        return object != null && !"vp".equals(object.getClass().getName()) && !"nki".equals(object.getClass().getName());
    }

    private static boolean isClickGuiScreen(Object object) {
        if (object == null) {
            return false;
        }
        String string = object.getClass().getName();
        return "vp".equals(string);
    }

    private static void setScreen(Object object, Object object2) throws ReflectiveOperationException {
        Class<?> clazz = Class.forName("net.minecraft.class_437", false, DiddyEntrypoint.class.getClassLoader());
        Method method = object.getClass().getMethod("method_1507", clazz);
        method.invoke(object, object2);
    }

    private static Object getCurrentScreen(Object object) {
        try {
            Field field = object.getClass().getDeclaredField("field_1755");
            field.setAccessible(true);
            return field.get(object);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return null;
        }
    }

    public static boolean isInGame(Object object) {
        if (object == null) {
            return false;
        }
        try {
            Field field = object.getClass().getDeclaredField("field_1687");
            Field field2 = object.getClass().getDeclaredField("field_1724");
            field.setAccessible(true);
            field2.setAccessible(true);
            return field.get(object) != null && field2.get(object) != null;
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return false;
        }
    }

    private static void enableHudModule() {
        try {
            Class<?> clazz = Class.forName("usf", true, DiddyEntrypoint.class.getClassLoader());
            Method method = kryptonClient.getClass().getMethod("modules", new Class[0]);
            Object object = method.invoke(kryptonClient, new Object[0]);
            if (!(object instanceof Iterable)) {
                return;
            }
            Iterable iterable = (Iterable)object;
            for (Object t2 : iterable) {
                String string;
                if (t2 == null || !clazz.isInstance(t2) || !"hud".equalsIgnoreCase(string = DiddyEntrypoint.moduleName(t2))) continue;
                Method method2 = Class.forName("nc", true, DiddyEntrypoint.class.getClassLoader()).getMethod("setModuleEnabled", clazz, Boolean.TYPE);
                method2.invoke(null, t2, true);
                DiddyEntrypoint.initializeModule(t2);
                return;
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static void initializeEnabledModules() {
        try {
            Class<?> clazz = Class.forName("usf", true, DiddyEntrypoint.class.getClassLoader());
            Method method = kryptonClient.getClass().getMethod("modules", new Class[0]);
            Object object = method.invoke(kryptonClient, new Object[0]);
            if (!(object instanceof Iterable)) {
                return;
            }
            Iterable iterable = (Iterable)object;
            Method method2 = Class.forName("nc", true, DiddyEntrypoint.class.getClassLoader()).getMethod("moduleEnabled", clazz);
            for (Object t2 : iterable) {
                if (t2 == null || !clazz.isInstance(t2) || !Boolean.TRUE.equals(method2.invoke(null, t2))) continue;
                DiddyEntrypoint.initializeModule(t2);
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static void initializeModule(Object object) {
        if (object == null || !initializedModules.add(object)) {
            return;
        }
        if (DiddyEntrypoint.shouldSkipModuleLifecycle(object)) {
            return;
        }
        try {
            object.getClass().getMethod("mg", new Class[0]).invoke(object, new Object[0]);
        }
        catch (Throwable throwable) {
            DiddyEntrypoint.fallbackModuleStart(object);
        }
    }

    public static void fallbackModuleLifecycle(Object object, boolean bl) {
        if (bl) {
            DiddyEntrypoint.fallbackModuleStart(object);
        } else {
            DiddyEntrypoint.fallbackModuleStop(object);
        }
    }

    private static void fallbackModuleStart(Object object) {
        if (object == null) {
            return;
        }
        DiddyEntrypoint.ensureModuleExecutors(object);
    }

    private static void fallbackModuleStop(Object object) {
        if (object == null) {
            return;
        }
        for (Field field : DiddyEntrypoint.allFields(object.getClass())) {
            if (!ExecutorService.class.isAssignableFrom(field.getType())) continue;
            try {
                ExecutorService executorService;
                field.setAccessible(true);
                Object object2 = field.get(object);
                if (!(object2 instanceof ExecutorService) || (executorService = (ExecutorService)object2).isShutdown()) continue;
                executorService.shutdownNow();
            }
            catch (Throwable throwable) {}
        }
    }

    private static void ensureModuleExecutors(Object object) {
        for (Field field : DiddyEntrypoint.allFields(object.getClass())) {
            if (!ExecutorService.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Object object2 = field.get(object);
                if (object2 instanceof ExecutorService && !((ExecutorService)object2).isShutdown()) continue;
                String string = object.getClass().getName();
                field.set(object, Executors.newSingleThreadExecutor(runnable -> DiddyEntrypoint.createModuleExecutorThread(string, field, runnable)));
            }
            catch (Throwable throwable) {}
        }
    }

    private static List<Field> allFields(Class<?> clazz) {
        ArrayList<Field> arrayList = new ArrayList<Field>();
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            Collections.addAll(arrayList, clazz2.getDeclaredFields());
        }
        return arrayList;
    }

    public static void forwardMovementKeys() {
        DiddyEntrypoint.forwardMovementKeys(clientInstance);
    }

    private static void forwardMovementKeys(Object object) {
        if (object == null || !DiddyEntrypoint.isClickGuiScreen(DiddyEntrypoint.getCurrentScreen(object))) {
            return;
        }
        try {
            Field field = DiddyEntrypoint.declaredField(object.getClass(), "field_1690");
            field.setAccessible(true);
            Object object2 = field.get(object);
            Object object3 = object.getClass().getMethod("method_22683", new Class[0]).invoke(object, new Object[0]);
            long l2 = (Long)object3.getClass().getMethod("method_4490", new Class[0]).invoke(object3, new Object[0]);
            DiddyEntrypoint.setKeyPressed(object2, "field_1894", l2, 87, 265);
            DiddyEntrypoint.setKeyPressed(object2, "field_1913", l2, 65, 263);
            DiddyEntrypoint.setKeyPressed(object2, "field_1881", l2, 83, 264);
            DiddyEntrypoint.setKeyPressed(object2, "field_1849", l2, 68, 262);
            DiddyEntrypoint.setKeyPressed(object2, "field_1903", l2, 32);
            DiddyEntrypoint.setKeyPressed(object2, "field_1832", l2, 340);
            DiddyEntrypoint.setKeyPressed(object2, "field_1867", l2, 341);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static void setKeyPressed(Object object, String string, long l2, int ... nArray) throws ReflectiveOperationException {
        Field field = DiddyEntrypoint.declaredField(object.getClass(), string);
        field.setAccessible(true);
        Object object2 = field.get(object);
        boolean bl = false;
        for (int n2 : nArray) {
            bl |= GLFW.glfwGetKey((long)l2, (int)n2) == 1;
        }
        object2.getClass().getMethod("method_23481", Boolean.TYPE).invoke(object2, bl);
    }

    public static boolean shouldForceVpRender() {
        return false;
    }

    public static boolean shouldSuppressKeybinds() {
        Object object = DiddyEntrypoint.getCurrentScreen(clientInstance);
        return object != null && !DiddyEntrypoint.isClickGuiScreen(object);
    }

    public static void handleKeyPress(long l2, int n2, Object object) {
        int n3;
        if (n2 != 1 || object == null || DiddyEntrypoint.shouldSuppressKeybinds()) {
            return;
        }
        try {
            Object object2 = object.getClass().getMethod("comp_4795", new Class[0]).invoke(object, new Object[0]);
            if (!(object2 instanceof Number)) {
                return;
            }
            Number number = (Number)object2;
            n3 = number.intValue();
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return;
        }
        if (n3 == -1) {
            return;
        }
        if (DiddyEntrypoint.toggleModuleKeybind(n3)) {
            return;
        }
        if (n3 == 344) {
            DiddyEntrypoint.openClickGui(clientInstance);
        }
    }

    private static boolean toggleModuleKeybind(int n2) {
        try {
            Class<?> clazz = Class.forName("fa", true, DiddyEntrypoint.class.getClassLoader());
            Object object = clazz.getField("euq").get(null);
            if (object == null) {
                return false;
            }
            Object object2 = clazz.getField("bs").get(object);
            if (object2 == null) {
                return false;
            }
            Object object3 = object2.getClass().getMethod("handleKeybind", Integer.TYPE).invoke(object2, n2);
            return Boolean.TRUE.equals(object3);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return false;
        }
    }

    public static boolean shouldSkipModuleLifecycle(Object object) {
        String string = DiddyEntrypoint.moduleName(object);
        return "Krypton+".equalsIgnoreCase(string) || object != null && "izs".equals(object.getClass().getName());
    }

    public static void saveConfig() {
        if (savingConfig || applyingConfig || configAppliedToManager == null) {
            return;
        }
        savingConfig = true;
        try {
            Object object = DiddyEntrypoint.getConfigObject();
            if (object != null) {
                object.getClass().getMethod("saveCurrent", new Class[0]).invoke(object, new Object[0]);
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
        }
        finally {
            savingConfig = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void registerKeybindSetting(Object object, Object object2) {
        if (object == null || object2 == null) {
            return;
        }
        IdentityHashMap<Object, Object> identityHashMap = keybindOwners;
        synchronized (identityHashMap) {
            keybindOwners.put(object, object2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void syncKeybindOwner(Object object) {
        block12: {
            Object object2;
            Object object3 = keybindOwners;
            synchronized (object3) {
                object2 = keybindOwners.get(object);
            }
            if (object2 == null) {
                return;
            }
            try {
                try {
                    object3 = object.getClass().getMethod("fh", new Class[0]).invoke(object, new Object[0]);
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    object3 = object.getClass().getMethod("wo", new Class[0]).invoke(object, new Object[0]);
                }
                if (!(object3 instanceof Number)) break block12;
                int n2 = ((Number)object3).intValue();
                try {
                    object2.getClass().getMethod("mw", Integer.TYPE).invoke(object2, n2);
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    // empty catch block
                }
                try {
                    Field field = DiddyEntrypoint.declaredField(object2.getClass(), "jm");
                    field.setAccessible(true);
                    field.setInt(object2, n2);
                }
                catch (ReflectiveOperationException reflectiveOperationException) {}
            }
            catch (LinkageError | ReflectiveOperationException throwable) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void applyConfigOnce(Class<?> clazz) {
        try {
            Object object = clazz.getField("bs").get(kryptonClient);
            if (object != null && object == configAppliedToManager) {
                return;
            }
            if (object == null) {
                return;
            }
            Object object2 = object.getClass().getMethod("kx", new Class[0]).invoke(object, new Object[0]);
            if (!(object2 instanceof List)) {
                return;
            }
            List list = (List)object2;
            if (list.isEmpty()) {
                return;
            }
            Object object3 = DiddyEntrypoint.getConfigObject();
            if (object3 == null) {
                return;
            }
            applyingConfig = true;
            try {
                object3.getClass().getMethod("applyConfig", new Class[0]).invoke(object3, new Object[0]);
                configAppliedToManager = object;
                autosaveTicks = 0;
            }
            finally {
                applyingConfig = false;
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            applyingConfig = false;
        }
    }

    private static void autosaveAppliedConfig() {
        if (configAppliedToManager == null) {
            return;
        }
        if (++autosaveTicks >= 20) {
            autosaveTicks = 0;
            DiddyEntrypoint.saveConfig();
        }
    }

    private static Object getConfigObject() throws ReflectiveOperationException {
        if (kryptonClient == null) {
            return null;
        }
        Class<?> clazz = Class.forName("fa", true, DiddyEntrypoint.class.getClassLoader());
        Object object = clazz.getField("qn").get(kryptonClient);
        if (object == null) {
            try {
                object = clazz.getDeclaredMethod("ru", Long.TYPE).invoke(kryptonClient, 0L);
            }
            catch (ReflectiveOperationException reflectiveOperationException) {
                // empty catch block
            }
        }
        return object;
    }

    private static String moduleName(Object object) {
        try {
            Method method = Class.forName("nc", true, DiddyEntrypoint.class.getClassLoader()).getMethod("moduleName", Class.forName("usf", true, DiddyEntrypoint.class.getClassLoader()));
            Object object2 = method.invoke(null, object);
            return object2 == null ? "" : object2.toString();
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return "";
        }
    }

    private static void dispatchEvent(String string) {
        try {
            ClassLoader classLoader = DiddyEntrypoint.class.getClassLoader();
            Object obj = Class.forName(string, true, classLoader).getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            Method method = Class.forName("mdl", true, classLoader).getMethod("ae", Class.forName("bll", true, classLoader));
            method.invoke(null, obj);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static void setStaticField(Class<?> clazz, String string, Object object) throws ReflectiveOperationException {
        Field field = clazz.getField(string);
        field.set(null, object);
    }

    private static void setClientField(String string, Object object) throws ReflectiveOperationException {
        Field field = Class.forName("fa", true, DiddyEntrypoint.class.getClassLoader()).getField(string);
        field.set(kryptonClient, object);
    }

    private static void setInstanceField(Object object, String string, Object object2) throws ReflectiveOperationException {
        Field field = DiddyEntrypoint.declaredField(object.getClass(), string);
        field.setAccessible(true);
        field.set(object, object2);
    }

    private static Field declaredField(Class<?> clazz, String string) throws NoSuchFieldException {
        for (Class<?> clazz2 = clazz; clazz2 != null; clazz2 = clazz2.getSuperclass()) {
            try {
                return clazz2.getDeclaredField(string);
            }
            catch (NoSuchFieldException noSuchFieldException) {
                continue;
            }
        }
        throw new NoSuchFieldException(string);
    }

    private static void ensureClientField(Class<?> clazz, String string, Class<?> clazz2) throws ReflectiveOperationException {
        Field field = clazz.getField(string);
        Object object = field.get(kryptonClient);
        if (object == null || !clazz2.isInstance(object) || DiddyEntrypoint.isUnconstructedConfig(object, clazz2)) {
            Constructor<?> constructor = clazz2.getDeclaredConstructor(new Class[0]);
            constructor.setAccessible(true);
            field.set(kryptonClient, constructor.newInstance(new Object[0]));
        }
    }

    private static boolean isUnconstructedConfig(Object object, Class<?> clazz) {
        if (!"xw".equals(clazz.getName())) {
            return false;
        }
        try {
            Field field = DiddyEntrypoint.declaredField(clazz, "leg");
            field.setAccessible(true);
            return field.get(object) == null;
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return true;
        }
    }

    private static void ensureRealClientField(Class<?> clazz, String string, Class<?> clazz2) throws ReflectiveOperationException {
        Object object;
        Field field = clazz.getField(string);
        Object object2 = field.get(kryptonClient);
        if (clazz2.isInstance(object2)) {
            try {
                object = clazz2.getMethod("kx", new Class[0]).invoke(object2, new Object[0]);
                if (object instanceof List && !((List)object).isEmpty()) {
                    return;
                }
            }
            catch (ReflectiveOperationException reflectiveOperationException) {
                return;
            }
        }
        object = clazz2.getDeclaredConstructor(new Class[0]);
        ((Constructor)object).setAccessible(true);
        field.set(kryptonClient, ((Constructor)object).newInstance(new Object[0]));
    }

    private static void initializeRenderer(ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName("xa", true, classLoader);
            Field field = clazz.getField("so");
            if (field.get(null) == null) {
                Method method = clazz.getMethod("smv", new Class[0]);
                method.invoke(null, new Object[0]);
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    private static void initializeFontRenderer(ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName("nt", true, classLoader);
            clazz.getField("oei").get(null);
            Object object = Class.forName("cu", true, classLoader).getMethod("nkq", new Class[0]).invoke(null, new Object[0]);
            if (object != null) {
                return;
            }
            Class<?> clazz2 = Class.forName("cb", true, classLoader);
            Method method = clazz2.getMethod("ke", new Class[0]);
            method.invoke(null, new Object[0]);
            object = Class.forName("cu", true, classLoader).getMethod("nkq", new Class[0]).invoke(null, new Object[0]);
            if (object == null) {
                DiddyEntrypoint.disableCustomFont(classLoader);
            }
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            DiddyEntrypoint.disableCustomFont(classLoader);
        }
    }

    private static void disableCustomFont(ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName("izs", true, classLoader);
            Field field = clazz.getField("mct");
            Object object = field.get(null);
            if (object != null) {
                Method method = object.getClass().getMethod("obi", Boolean.TYPE);
                method.invoke(object, false);
            }
            Class.forName("nt", true, classLoader).getField("oei").get(null);
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            // empty catch block
        }
    }

    public static Object getClientInstance() {
        return clientInstance;
    }

    public static void onNkiRenderHead(Object object) {
        DiddyEntrypoint.normalizeNkiScreen(object);
    }

    public static void onNkiQppHead(Object object) {
        DiddyEntrypoint.normalizeNkiScreen(object);
    }

    public static void onForcedNkiQpp(Object object) {
        DiddyEntrypoint.normalizeNkiScreen(object);
    }

    public static void onForcedNkiQppFailure(Throwable throwable) {
        long l2 = System.currentTimeMillis();
        if (l2 - lastOpenFailureLog > 5000L) {
            lastOpenFailureLog = l2;
            throwable.printStackTrace();
        }
    }

    public static void onVpQppHead(Object object) {
        ++vpQppCalls;
        DiddyEntrypoint.forwardMovementKeys();
    }

    public static void onForcedVpQpp(Object object) {
        ++forcedVpQppCalls;
    }

    public static void onForcedVpQppFailure(Throwable throwable) {
        long l2 = System.currentTimeMillis();
        if (l2 - lastOpenFailureLog > 5000L) {
            lastOpenFailureLog = l2;
            throwable.printStackTrace();
        }
    }

    private static void normalizeNkiScreen(Object object) {
        if (object == null || !"nki".equals(object.getClass().getName())) {
            return;
        }
        try {
            Field field = object.getClass().getField("oy");
            if (field.getInt(object) == 0) {
                field.setInt(object, 255);
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            // empty catch block
        }
    }

    private static String describeNki(Object object) {
        if (object == null) {
            return "null";
        }
        try {
            return "ht=" + String.valueOf(object.getClass().getField("ht").get(object)) + " ikh=" + String.valueOf(object.getClass().getField("ikh").get(object)) + " pdl=" + String.valueOf(object.getClass().getField("pdl").get(object)) + " xnj=" + String.valueOf(object.getClass().getField("xnj").get(object)) + " wpn=" + String.valueOf(object.getClass().getField("wpn").get(object)) + " oy=" + String.valueOf(object.getClass().getField("oy").get(object));
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return object.getClass().getName();
        }
    }

    private static Thread createModuleExecutorThread(String string, Field field, Runnable runnable) {
        Thread thread = new Thread(runnable, "krypton-" + string + "-" + field.getName());
        thread.setDaemon(true);
        return thread;
    }

    static {
        initializedModules = Collections.newSetFromMap(new IdentityHashMap());
        keybindOwners = new IdentityHashMap();
    }
}

