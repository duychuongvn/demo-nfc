package ch.smartlink.smartticketdemo.util;

import java.math.BigDecimal;

public class MessageUtil {

    public static String getDisplayBalance(BigDecimal balance) {
        return balance.setScale(2).toPlainString();
    }
    public static String formatBalanceToStore(BigDecimal balance) {
        String data = getDisplayBalance(balance);
        return leftZeroPadding(data, 14);
    }
    public static String leftZeroPadding(String value, int len) {
        String data = value;
        while(data.length() < len) {
            data = "0"+data;
        }
        return data;
    }

    public static String leftSpacePadding(String value, int len) {
        String data = value;
        while (data.length() < len) {
            data = " " + data;
        }
        return data;
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public static String byteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
