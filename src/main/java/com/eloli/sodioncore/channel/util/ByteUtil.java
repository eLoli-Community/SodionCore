package com.eloli.sodioncore.channel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ByteUtil {
    public static byte[] merge(byte[] a, byte[] b) {
        byte[] c = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] sha256(byte[] a) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(a);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
