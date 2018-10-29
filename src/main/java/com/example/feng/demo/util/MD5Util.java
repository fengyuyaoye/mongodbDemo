package com.example.feng.demo.util;

import java.security.MessageDigest;

public class MD5Util {
    private static final String[] hexDigits =
        new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public MD5Util() {}

    public static String MD5Encode(String source) {
        return MD5Encode(source, "utf-8", false);
    }

    public static String MD5Encode(String source, String encoding, boolean uppercase) {
        String result = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(source.getBytes(encoding));
            result = byteArrayToHexString(messageDigest.digest());
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return uppercase ? result.toUpperCase() : result;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] arr$ = bytes;
        int len$ = bytes.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            byte tem = arr$[i$];
            stringBuilder.append(byteToHexString(tem));
        }

        return stringBuilder.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (b < 0) {
            n = 256 + b;
        }

        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
