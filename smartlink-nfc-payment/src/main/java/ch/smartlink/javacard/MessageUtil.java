package ch.smartlink.javacard;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.commands.ByteArray;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Random;

public class MessageUtil {
    private static final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String getDisplayBalance(BigDecimal balance) {
        return balance.setScale(2).toPlainString();
    }

    public static String formatBalanceToStore(BigDecimal balance) {
        String data = getDisplayBalance(balance);
        return leftZeroPadding(data, 13);
    }

    public static String leftZeroPadding(String value, int len) {
        String data = value;

        while (data.length() < len) {
            data = "0" + data;
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

    public static String randomNumeric(int length) {
        Random random = new Random();
        String data = "";
        while (data.length() < length) {
            data = data + random.nextInt(10);
        }
        return data;
    }

    public static String randomString(int length) {
        Random random = new Random();
        String data = "";
        while (data.length() < length) {
            data = data + hexArray[random.nextInt(15)];
        }
        return data;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean errorHandler(ByteArray receivedStatus, ByteArray expectedStatus) throws CipurseException {
        ByteArray recStatus = new ByteArray(receivedStatus.getBytes());
        ByteArray expStatus = new ByteArray(expectedStatus.getBytes());

        int recSize = recStatus.size();
        if (recSize > 2) {
            recStatus = recStatus.subArray(recSize - 2, recSize);
        }

        System.out.println("Received Status: " + recStatus);
        System.out.println("Expected Status: " + expStatus);

        boolean result = false;
        result = recStatus.equals(expStatus);

        if (result)
            System.out.println("Expected result MATCHED");
        else {
            System.out.println("************************************************************");
            System.out.println("-------------- Expected result does NOT MATCH --------------");
            System.out.println("-------------- Script Execution Terminated    --------------");
            System.out.println("************************************************************");
            throw new CipurseException(receivedStatus.getString());
        }
        return result;
    }
    public static void handleError(ByteArray response) throws CipurseException {
        errorHandler(response, new ByteArray("90 00"));
    }

}
