/*
 * Decompiled with CFR 0.152.
 */
package com.mutyx.a7.s3;

import 608bbebf84695e52.b9a3bdf6a8e9e1b13.49342937c9ef478b;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

class a5c2d02a9be$3
implements X509TrustManager {
    @Override
    public native void checkClientTrusted(X509Certificate[] var1, String var2) throws CertificateException;

    @Override
    public native void checkServerTrusted(X509Certificate[] var1, String var2) throws CertificateException;

    @Override
    public native X509Certificate[] getAcceptedIssuers();

    static {
        49342937c9ef478b.init(a5c2d02a9be$3.class, 7);
        a5c2d02a9be$3.$clinit$();
    }

    private static native String a(int var0, int var1);

    private static native /* synthetic */ void $clinit$();
}

