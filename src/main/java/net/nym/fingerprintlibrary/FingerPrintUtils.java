/*
 * Copyright (c) 2017  Ni YueMing<niyueming@163.com>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */

package net.nym.fingerprintlibrary;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import static android.Manifest.permission.USE_FINGERPRINT;

/**
 * @author niyueming
 * @date 2017-03-02
 * @time 10:48
 */

public class FingerPrintUtils {

    private FingerPrintUtils() {
    }

    public static boolean check(Context context) {
        return (FingerPrintUtils.isKeyguardSecure(context)
                && FingerPrintUtils.hasFingerprintPermission(context)
                && FingerPrintUtils.hasEnrolledFingerprints(context)
        );
    }

    public static boolean isKeyguardSecure(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isKeyguardSecure = keyguardManager.isKeyguardSecure();
        if (!isKeyguardSecure) {
            System.out.println("设置界面中未开启密码锁屏功能");
        }
        return isKeyguardSecure;
    }

    public static FingerprintManager getFingerprintManager(Context context) {
        boolean isFlag = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isFlag = true;
        } else if (isHaveFingerprintManager()) {
            isFlag = true;
        }

        if (isFlag) {
            FingerprintManager fingerprintManager;
            fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            return fingerprintManager;
        }
        return null;
    }

    public static boolean hasEnrolledFingerprints(Context context) {
        boolean isFlag = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isFlag = true;
        } else if (isHaveFingerprintManager()) {
            isFlag = true;

        }

        if (isFlag) {
            FingerprintManager fingerprintManager;
            fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            if (ActivityCompat.checkSelfPermission(context, USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("没有权限：android.permission.USE_FINGERPRINT");
                return false;
            }
            boolean hasEnrolledFingerprints = fingerprintManager.hasEnrolledFingerprints();
            if (!hasEnrolledFingerprints) {
                System.out.println("您还没有录入指纹, 请在设置界面录入至少一个指纹");
            }
            return hasEnrolledFingerprints;
        }
        return false;

    }

    public static boolean hasFingerprintPermission(Context context) {
        if (ActivityCompat.checkSelfPermission(context, USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("没有权限：android.permission.USE_FINGERPRINT");
            return false;
        }
        return true;
    }

    public static boolean isHardwareDetected(Context context) {
        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager == null) {
            return false;
        }
        if (ActivityCompat.checkSelfPermission(context, USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("没有权限：android.permission.USE_FINGERPRINT");
            return false;
        }
        return fingerprintManager.isHardwareDetected();
    }

    public static boolean isHaveFingerprintManager() {
        try {
            Class.forName("android.hardware.fingerprint.FingerprintManager"); // 通过反射判断是否存在该类
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void authenticate(Context context,
                                    @Nullable FingerprintManager.CryptoObject crypto,
                                    @Nullable CancellationSignal cancel,
                                    @NonNull FingerprintManager.AuthenticationCallback callback,
                                    @Nullable Handler handler) {
        FingerprintManager fingerprintManager = getFingerprintManager(context);
        if (fingerprintManager == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fingerprintManager.authenticate(crypto,cancel,0,callback,handler);
    }

    public static void authenticate(Context context,
                                    @NonNull FingerprintManager.AuthenticationCallback callback) {
        authenticate(context,null,null,callback,null);
    }

    public static void authenticate(Context context,
                                    @Nullable FingerprintManager.CryptoObject crypto,
                                    @Nullable CancellationSignal cancel,
                                    @NonNull FingerprintManager.AuthenticationCallback callback) {
        authenticate(context,crypto,cancel,callback,null);
    }
}
