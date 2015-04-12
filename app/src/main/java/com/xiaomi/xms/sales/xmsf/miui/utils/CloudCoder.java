
package com.xiaomi.xms.sales.xmsf.miui.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CloudCoder {
    private static final String RC4_ALGORITHM_NAME = "RC4";

    /**
     * Helper class to instantiate an AES cipher with Xiaomi-specified format.
     *
     * @param aesKey AES key
     * @param opMode {@link Cipher#ENCRYPT_MODE} or {@link Cipher#DECRYPT_MODE}
     * @return the result cipher or null if failed.
     * @see Cipher
     */
    public static Cipher newAESCipher(String aesKey, int opMode) {
        byte[] keyRaw = Base64.decode(aesKey, Base64.NO_WRAP);
        Cipher cipher;
        SecretKeySpec keySpec = new SecretKeySpec(keyRaw, "AES");
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(
                    "0102030405060708".getBytes());
            cipher.init(opMode, keySpec, iv);
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Cipher newRC4Cipher(byte[] rc4Key, int opMode) {
        Cipher cipher;
        SecretKeySpec keySpec = new SecretKeySpec(rc4Key, RC4_ALGORITHM_NAME);
        try {
            cipher = Cipher.getInstance(RC4_ALGORITHM_NAME);
            cipher.init(opMode, keySpec);
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Compute SHA1 hash value for the string
     *
     * @param plain plain text. It will be encoded to BASE64 before hash
     * @return BASE64 encoded hash value
     */
    public static String hash4SHA1(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return Base64.encodeToString(md.digest(plain.getBytes("UTF-8")),
                    Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // this should never be reached
        throw new IllegalStateException("failed to SHA1");
    }

    /**
     * Generate signature for the request.
     *
     * @param method     http request method. GET or POST
     * @param requestUrl the full request url. e.g.: http://api.xiaomi.com/getUser?id=123321
     * @param params     request params. This should be a TreeMap because the
     *                   parameters are required to be in lexicographic order.
     * @param security   AES secret key. Must NOT be null.
     * @return hash value for the values provided
     */
    public static String generateSignature(String method, String requestUrl,
            Map<String, String> params, String security) {
        if (TextUtils.isEmpty(security)) {
            throw new InvalidParameterException("security is not nullable");
        }
        List<String> exps = new ArrayList<String>();
        if (method != null) {
            exps.add(method.toUpperCase());
        }
        if (requestUrl != null) {
            Uri uri = Uri.parse(requestUrl);
            exps.add(uri.getEncodedPath());
        }
        if (params != null && !params.isEmpty()) {
            final TreeMap<String, String> sortedParams
                    = new TreeMap<String, String>(params);
            Set<Map.Entry<String, String>> entries = sortedParams.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                exps.add(String.format("%s=%s", entry.getKey(),
                        entry.getValue()));
            }
        }
        exps.add(security);
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String s : exps) {
            if (!first) {
                sb.append('&');
            }
            sb.append(s);
            first = false;
        }
        return hash4SHA1(sb.toString());
    }
}