package jsc.org.lib.netreq.utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public final class Md5Utils {

    private final static String[] HEX_CODE = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    @NonNull
    public static String calcFileMd5(File file) {
        try {
            if (file.isFile() && file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                MessageDigest digest = MessageDigest.getInstance("MD5");
                byte[] buf = new byte[8192];
                int len;
                while ((len = fileInputStream.read(buf)) > 0) {
                    digest.update(buf, 0, len);
                }
                return toHexString(digest.digest());
            }
        } catch (Exception ignore) {

        }
        return "";
    }

    public static String toHexString(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(HEX_CODE[(b >> 4) & 0xF]);
            r.append(HEX_CODE[(b & 0xF)]);
        }
        return r.toString();
    }
}