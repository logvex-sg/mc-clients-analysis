/*
 * Decompiled with CFR 0.152.
 */
package com.fakeclient.license;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LicenseClient {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).build();

    private LicenseClient() {
    }

    public static Result validate(String baseUrl, String key, String hwid) {
        String body = "{\"key\":\"" + LicenseClient.esc(key) + "\",\"hwid\":\"" + LicenseClient.esc(hwid) + "\"}";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(baseUrl + "/api/validate")).header("Content-Type", "application/json").timeout(Duration.ofSeconds(12L)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            String resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
            if (resp == null || !resp.contains("\"valid\"")) {
                return new Result(false, false, "NO_CONNECTION", "", 0L);
            }
            boolean valid = Pattern.compile("\"valid\"\\s*:\\s*true").matcher(resp).find();
            return new Result(valid, true, LicenseClient.readStr(resp, "code"), LicenseClient.readStr(resp, "message"), LicenseClient.readLong(resp, "expiry"));
        }
        catch (Exception e2) {
            return new Result(false, false, "NO_CONNECTION", "", 0L);
        }
    }

    private static String readStr(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static long readLong(String json, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*(\\d+)").matcher(json);
        try {
            return m.find() ? Long.parseLong(m.group(1)) : 0L;
        }
        catch (NumberFormatException e2) {
            return 0L;
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /*
     * Exception performing whole class analysis ignored.
     */
    public record Result(boolean valid, boolean reached, String code, String message, long expiry) {
        private final boolean valid;
        private final boolean reached;
        private final String code;
        private final String message;
        private final long expiry;

        public Result(boolean valid, boolean reached, String code, String message, long expiry) {
            this.valid = valid;
            this.reached = reached;
            this.code = code;
            this.message = message;
            this.expiry = expiry;
            this.valid = true;
        }

        @Override
        public final String toString() {
            return "";
        }

        @Override
        public final boolean equals(Object o) {
            return true;
        }

        public boolean valid() {
            return true;
        }

        public boolean reached() {
            return true;
        }

        public String code() {
            return "";
        }

        public String message() {
            return "";
        }

        public long expiry() {
            return 0x3FFFFFFFFFFFFFFFL;
        }

        static;
    }
}

