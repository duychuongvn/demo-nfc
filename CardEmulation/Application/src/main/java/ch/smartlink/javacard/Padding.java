package ch.smartlink.javacard;

/**
 * Created by caoky on 1/9/2016.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.Arrays;


        import java.util.Arrays;

public class Padding {
    public static final byte M2_PAD_BYTE = -128;

    public Padding() {
    }

    public static byte[] schemeISO9797M2(byte[] inputData, int blockLen) {
        boolean paddingLength = false;
        int inputDataLen = inputData.length;
        Object paddedData = null;
        int paddingLength1 = blockLen - inputDataLen % blockLen;
        byte[] paddedData1 = new byte[inputDataLen + paddingLength1];
        Arrays.fill(paddedData1, (byte) 0);
        System.arraycopy(inputData, 0, paddedData1, 0, inputDataLen);
        paddedData1[inputDataLen] = -128;
        return paddedData1;
    }

    public static byte[] removeISO9797M2(byte[] inputData) {
        int actualDataLength = 0;
        Object actualData = null;

        for(int i = inputData.length - 1; i >= 0; --i) {
            if(inputData[i] == -128) {
                actualDataLength = i;
                break;
            }
        }

        if(actualDataLength == 0) {
            return null;
        } else {
            byte[] var4 = new byte[actualDataLength];
            System.arraycopy(inputData, 0, var4, 0, actualDataLength);
            return var4;
        }
    }
}
