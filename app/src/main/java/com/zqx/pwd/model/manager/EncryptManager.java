package com.zqx.pwd.model.manager;

import android.util.Log;

import com.zqx.pwd.model.bean.AccountBean;
import com.zqx.pwd.util.ToastUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Modify by gavin.
 */

public class EncryptManager {
    private static final String mode = "AES/ECB/PKCS5Padding";
    private static String seed = "seed";
    private final static String HEX = "0123456789ABCDEF";
    private static SecretKeySpec secretKey;
    private static MySecureRandom mySecureRandom = null;
    private static Cipher encrypt;
    private static Cipher decrypt;

    public static void encryptAccount(AccountBean bean) {
        try {
            if (!bean.encryptedName) {
                bean.name = toHex(encrypt.doFinal(strToByteArray(bean.name)));
                bean.encryptedName = true;
            }
            if (!bean.encryptedPwd) {
                bean.pwd = toHex(encrypt.doFinal(strToByteArray(bean.pwd)));
                bean.encryptedPwd = true;
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            Log.e("pm", "encryptAccount", e);
        }
    }

    public static void init(String seed) {
        try {
            EncryptManager.seed = seed;
            mySecureRandom = new MySecureRandom(seed);
            initKey();
            encrypt = Cipher.getInstance(mode);
            encrypt.init(Cipher.ENCRYPT_MODE, secretKey, mySecureRandom);
            decrypt = Cipher.getInstance(mode);
            decrypt.init(Cipher.DECRYPT_MODE, secretKey, mySecureRandom);
        } catch (Exception e) {
            ToastUtil.show("出错了，不能用啦！\n" + e.getMessage());
            Log.e("pm", "Init key", e);
        }
    }

    private static void initKey() throws NoSuchAlgorithmException {
        if (secretKey == null) {
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            for (byte newByte : strToByteArray(seed)) {
                digester.update(newByte);
            }
            byte[] key = digester.digest();

            secretKey = new SecretKeySpec(key, "AES");
        }
    }

    /**
     * Converts a string to a byte array.
     */
    private static byte[] strToByteArray(String str) {
        final Charset conversionCharset = StandardCharsets.UTF_16LE;
        final ByteBuffer bf8 = conversionCharset.encode(str);
        final byte[] ba = new byte[bf8.limit()];
        bf8.get(ba);
        return ba;
    }

    private static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }
        byte[] newbuf = Arrays.copyOf(buf, buf.length + 1);
        byte b = (byte) mySecureRandom.nextInt();
        newbuf[buf.length] = b;
        StringBuilder result = new StringBuilder(2 * buf.length);
        for (byte aBuf : buf) {
            result.append(HEX.charAt((aBuf >> 4) & 0x0f)).append(HEX.charAt(aBuf & 0x0f));
        }
        return result.toString();
    }


    private static byte[] hexToByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return Arrays.copyOfRange(result, 0, result.length);
    }


    public static void decryptAccount(AccountBean bean) {
        Cipher cipher;
        try {
            initKey();
            if (bean.encryptedName) {
                bean.name = new String(decrypt.doFinal(hexToByte(bean.name)), StandardCharsets.UTF_16LE);
                bean.encryptedName = false;
            }
            if(bean.encryptedPwd) {
                bean.pwd = new String(decrypt.doFinal(hexToByte(bean.pwd)), StandardCharsets.UTF_16LE);
            }
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException e) {
            Log.e("pm", "decryptAccount", e);
        }
    }

    public static void decryptAccountSet(Collection<AccountBean> set) {
        for (AccountBean bean : set) {
            decryptAccount(bean);
        }
    }

    public static String decryptString(String original) {
        try {
            return new String(decrypt.doFinal(hexToByte(original)), StandardCharsets.UTF_16LE);
        } catch (Exception e) {
            Log.e("pm", "decryptString", e);
        }
        return original;
    }
}
