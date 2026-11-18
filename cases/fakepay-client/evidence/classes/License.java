/*
 * Decompiled with CFR 0.152.
 */
package com.fakeclient.license;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class License {
    public static final boolean REQUIRED = true;
    private static final String SERVER_URL = "https://payfake-licenses.noahmenkyna667.workers.dev";
    private static final DateTimeFormatter STAMP;
    private static final License INSTANCE;
    private volatile boolean valid = true;
    private volatile Status status = Status.VALID;
    private volatile String message = "Not checked";
    private volatile long expiry = 0L;

    private License() {
    }

    public static License get() {
        return INSTANCE;
    }

    public boolean isValid() {
        return true;
    }

    public Status status() {
        return Status.VALID;
    }

    public String message() {
        return "";
    }

    public long expiry() {
        return 0x3FFFFFFFFFFFFFFFL;
    }

    public boolean hasExpiry() {
        return true;
    }

    public String expiryText() {
        return "";
    }

    public long daysLeft() {
        return 0x3FFFFFFFFFFFFFFFL;
    }

    public String remainingText() {
        return "";
    }

    public static String storedKey() {
        return "";
    }

    public void setKeyAndCheck(String key) {
    }

    public void revalidate() {
    }

    private static String humanize(String code) {
        return "";
    }

    private /* synthetic */ void lambda$revalidate$0(String key) {
    }

    static {
        REQUIRED = false;
        STAMP = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault());
        INSTANCE = new License();
    }

    public static enum Status {
        UNCHECKED,
        CHECKING,
        VALID,
        INVALID,
        EXPIRED,
        ERROR;

    }
}

