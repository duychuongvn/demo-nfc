package ch.smartlink.javacard;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.IAes;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.Utility;
import org.osptalliance.cipurse.crypto.*;

import java.util.Arrays;
import java.util.Random;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;

/**
 * Created by caoky on 1/9/2016.
 */
public class HrsCipurseCrypto implements ISO7816{

    private byte[] frameKeyi;
    private byte[] frameKeyiPlus1;
    private byte[] RP;
    private byte[] rP;
    private byte[] RT;
    private byte[] rT;
    private byte[] k0;
    private ILogger logger = null;
    private IAes Aes = null;

    public HrsCipurseCrypto(IAes Aes, ILogger logger) throws CipurseException {
        try {
            this.logger = logger;
            this.Aes = Aes;
        } catch (Exception var4) {
            throw new CipurseException(var4);
        }
    }

    public byte[] getSessionKey() {
        return this.frameKeyi;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.frameKeyi = sessionKey;
    }

    public byte[] wrapCommand(byte[] plainCommand, byte SMI) throws CipurseException {
        switch(SMI & -64) {
            case -128:
                return this.getENCedCommand(plainCommand, (byte)SMI);
            case 0:
                return this.getPlainSMCommand(plainCommand, (byte)SMI);
            case 64:
                return this.getMACedCommand(plainCommand, (byte)SMI);
            default:
                throw new CipurseException("Invalid SMI value");
        }
    }

    public byte[] unwrapCommand(byte[] smCommand, byte SMI) throws CipurseException {
        switch(SMI & 12) {
            case 0:
                return this.unwrapPlainSMCommand(smCommand);
            case 4:
                return this.unwrapMACedCommand(smCommand);
            case 8:
                return this.unwrapENCedCommand(smCommand);
            default:
                throw new CipurseException("Invalid SMI value");
        }
    }

    public byte[] generateCT(byte[] RT) throws CipurseException {
        return this.Aes.aesEncrypt(this.k0, this.RT);
    }
    public byte[] generateK0AndGetCp(byte[] kid, byte[] RP1, byte[] rP1, byte[] RT1, byte[] rT1) throws CipurseException {
        if(kid.length == 16 && RP1.length == 16 && RT1.length == 16 && rP1.length == 6 && rT1.length == 6) {
            this.rP = rP1;
            this.RP = RP1;
            this.RT = RT1;
            this.rT = rT1;
            byte[] temp1 = this.extFunction(kid, 6);
            byte[] kp = this.computeNLM(temp1, this.rP);
            temp1 = this.pad2(kp);
            byte[] temp2 = this.pad(this.rT);
            temp1 = this.xor(temp1, temp2);
            this.k0 = this.Aes.aesEncrypt(temp1, kid);
            this.k0 = this.xor(this.k0, kid);
            temp1 = this.xor(this.k0, this.RT);
            byte[] temp3 = this.Aes.aesEncrypt(this.RP, temp1);
            this.frameKeyi = this.xor(temp3, temp1);
            byte[] cP = this.Aes.aesEncrypt(this.k0, this.RP);
            return cP;
        } else {
            throw new CipurseException("Invalid parameters");
        }
    }

    public byte[] getRandom(int size) {
        byte[] random = new byte[size];

        for(int i = 0; i < size; ++i) {
            random[i] = (byte)((new Random()).nextInt() % 200 & 255);
        }

        return random;
    }

    public boolean verifyPICCResponse(byte[] cT) throws CipurseException {
        byte[] cTDash = this.Aes.aesEncrypt(this.k0, this.RT);
        return Arrays.equals(cT, cTDash);
    }

    private byte[] getPlainSMCommand(byte[] command, byte SMI) throws CipurseException {
        Utility.eCaseType orgCaseType = Utility.getCaseType(command);
        if(orgCaseType == Utility.eCaseType.CASE_4) {
            if((short)(command[4] & 255) > 253) {
                throw new CipurseException("Lc > allowed");
            }
        } else if(orgCaseType == Utility.eCaseType.CASE_3 && (short)(command[4] & 255) > 254) {
            throw new CipurseException("Lc > allowed");
        }

        byte[] smCommand = this.getOSPTModifiedCommand(command, SMI);
        byte[] dataPadded = Padding.schemeISO9797M2(smCommand, 16);
        this.generateMAC(dataPadded);
        return this.getLeDashCommand(smCommand, orgCaseType, SMI);
    }

    private byte[] getMACedCommand(byte[] command, byte SMI) throws CipurseException {
        Utility.eCaseType orgCaseType = Utility.getCaseType(command);
        if(orgCaseType == Utility.eCaseType.CASE_4) {
            if((short)(command[4] & 255) > 245) {
                throw new CipurseException("Lc > allowed");
            }
        } else if(orgCaseType == Utility.eCaseType.CASE_3 && (short)(command[4] & 255) > 246) {
            throw new CipurseException("Lc > allowed");
        }

        byte[] smCommand = this.getOSPTModifiedCommand(command, SMI);
        smCommand[4] = (byte)(smCommand[4] + 8);
        byte[] smMacData = new byte[smCommand.length];
        System.arraycopy(smCommand, 0, smMacData, 0, smMacData.length);
        byte[] dataPadded = Padding.schemeISO9797M2(smMacData, 16);
        byte[] mac = this.generateMAC(dataPadded);
        byte[] smMacCommand = new byte[smCommand.length + 8];
        System.arraycopy(smCommand, 0, smMacCommand, 0, smCommand.length);
        System.arraycopy(mac, 0, smMacCommand, smMacCommand.length - 8, 8);
        return this.getLeDashCommand(smMacCommand, orgCaseType, SMI);
    }

    private byte[] getENCedCommand(byte[] command, byte SMI) throws CipurseException {
        Utility.eCaseType orgCaseType = Utility.getCaseType(command);
        if((orgCaseType == Utility.eCaseType.CASE_4 || orgCaseType == Utility.eCaseType.CASE_3) && (short)(command[4] & 255) > 231) {
            throw new CipurseException("Lc > allowed");
        } else {
            byte[] orgLe = null;
            byte[] orgCommandData = null;
            if(orgCaseType == Utility.eCaseType.CASE_3 || orgCaseType == Utility.eCaseType.CASE_4) {
                orgCommandData = new byte[(short)(command[4] & 255)];
                System.arraycopy(command, 5, orgCommandData, 0, orgCommandData.length);
            }

            if(orgCaseType == Utility.eCaseType.CASE_2 || orgCaseType == Utility.eCaseType.CASE_4) {
                orgLe = new byte[]{command[command.length - 1]};
            }

            byte[] smCommand = this.getOSPTModifiedCommand(command, SMI);
            Object nCryptogramPadded = null;
            byte[] nCryptogramPadded1;
            if(orgCommandData != null) {
                nCryptogramPadded1 = new byte[4 + orgCommandData.length + 4];
            } else {
                nCryptogramPadded1 = new byte[8];
            }

            nCryptogramPadded1 = Padding.schemeISO9797M2(nCryptogramPadded1, 16);
            int nCryptogramPaddedLen = nCryptogramPadded1.length;
            byte[] dataForMIC = new byte[smCommand.length];
            System.arraycopy(smCommand, 0, dataForMIC, 0, dataForMIC.length);
            if(orgLe != null) {
                dataForMIC[4] = (byte)(1 + nCryptogramPaddedLen + 1);
            } else {
                dataForMIC[4] = (byte)(1 + nCryptogramPaddedLen);
            }

            byte[] mic = this.computeMIC(dataForMIC);
            this.logger.log(2, "MIC", mic);
            System.arraycopy(smCommand, 0, nCryptogramPadded1, 0, 4);
            int micOffset = 4;
            if(orgCommandData != null) {
                System.arraycopy(orgCommandData, 0, nCryptogramPadded1, 4, orgCommandData.length);
                micOffset += orgCommandData.length;
            }

            System.arraycopy(mic, 0, nCryptogramPadded1, micOffset, mic.length);
            byte[] nCryptogramCipher = this.generateCipher(nCryptogramPadded1, true);
            boolean finalCDataLen = false;
            Object finalCData = null;
            int finalCDataLen1;
            byte[] finalCData1;
            if(orgLe != null) {
                finalCDataLen1 = nCryptogramCipher.length + 2;
                finalCData1 = new byte[finalCDataLen1];
                finalCData1[0] = SMI;
                System.arraycopy(nCryptogramCipher, 0, finalCData1, 1, nCryptogramCipher.length);
                finalCData1[finalCData1.length - 1] = orgLe[0];
            } else {
                finalCDataLen1 = 1 + nCryptogramCipher.length;
                finalCData1 = new byte[finalCDataLen1];
                finalCData1[0] = SMI;
                System.arraycopy(nCryptogramCipher, 0, finalCData1, 1, nCryptogramCipher.length);
            }

            byte[] encryptedCmd = new byte[5 + finalCData1.length];
            System.arraycopy(smCommand, 0, encryptedCmd, 0, 4);
            System.arraycopy(finalCData1, 0, encryptedCmd, 5, finalCData1.length);
            encryptedCmd[4] = (byte)finalCData1.length;
            return this.getLeDashCommand(encryptedCmd, orgCaseType, SMI);
        }
    }

    private byte[] unwrapPlainSMCommand(byte[] smCommand) throws CipurseException {
        byte[] dataPadded = Padding.schemeISO9797M2(smCommand, 16);
        this.generateMAC(dataPadded);
        return smCommand;
    }

    private byte[] unwrapMACedCommand(byte[] smCommand) throws CipurseException {
//        if(smCommand.length < 10) {
//            throw new CipurseException("Response is less than min MACed response length(=8)");
//        } else {
//            byte[] dataForMAC = new byte[smCommand.length - 8];
//            System.arraycopy(smCommand, 0, dataForMAC, 0, dataForMAC.length - 2);
//            System.arraycopy(smCommand, smCommand.length - 2, dataForMAC, dataForMAC.length - 2, 2);
//            byte[] dataPadded = Padding.schemeISO9797M2(dataForMAC, 16);
//            byte[] hostMac = this.generateMAC(dataPadded);
//            byte[] cardMac = new byte[8];
//            System.arraycopy(smCommand, smCommand.length - 8 - 2, cardMac, 0, 8);
//            if(Arrays.equals(cardMac, hostMac)) {
//                return dataForMAC;
//            } else {
//                throw new CipurseException("Response MAC verification failed");
//            }
//        }
        byte[] dataForMAC = new byte[smCommand.length - 8];
        System.arraycopy(smCommand, 0, dataForMAC, 0, dataForMAC.length );
        byte[] dataPadded = Padding.schemeISO9797M2(dataForMAC, 16);
        byte[] cardMac = this.generateMAC(dataPadded);
        byte[] hostMac = new byte[8];
        System.arraycopy(smCommand, smCommand.length - 8, hostMac, 0, 8);

        String strHostMac = MessageUtil.byteArrayToHexString(hostMac);
        String strCardMac = MessageUtil.byteArrayToHexString(cardMac);
        System.out.println(strHostMac);
        System.out.println(strCardMac);
        if(Arrays.equals(cardMac, hostMac)) {
            return smCommand;
        }
        ISOException.throwIt(SW_CONDITIONS_NOT_SATISFIED);
        return null ;
    }

    private byte[] unwrapENCedCommand(byte[] smCommand) throws CipurseException {
        byte[] encryptedResp = new byte[smCommand.length - 2];
        System.arraycopy(smCommand, 0, encryptedResp, 0, encryptedResp.length);
        if(encryptedResp.length % 16 != 0) {
            throw new CipurseException("Response is not multiple of AES Blok");
        } else {
            byte[] clearResp = this.generateCipher(encryptedResp, false);
            this.logger.log(2, "Deciphered Data", clearResp);
            byte[] unpaddedClearResp = Padding.removeISO9797M2(clearResp);
            byte minENcedRespLen = 6;
            if(unpaddedClearResp != null && unpaddedClearResp.length != 0 && unpaddedClearResp.length >= minENcedRespLen) {
                if(unpaddedClearResp[unpaddedClearResp.length - 2] != smCommand[smCommand.length - 2] && unpaddedClearResp[unpaddedClearResp.length - 1] != smCommand[smCommand.length - 1]) {
                    throw new CipurseException("SW in ENCed response != actual SW");
                } else {
                    int actualRespDataLen = unpaddedClearResp.length - 6;
                    byte[] dataForMIC = new byte[actualRespDataLen + 2];
                    System.arraycopy(unpaddedClearResp, 0, dataForMIC, 0, actualRespDataLen);
                    System.arraycopy(smCommand, smCommand.length - 2, dataForMIC, actualRespDataLen, 2);
                    byte[] hostMIC = this.computeMIC(dataForMIC);
                    byte[] cardMIC = new byte[4];
                    System.arraycopy(unpaddedClearResp, unpaddedClearResp.length - 6, cardMIC, 0, 4);
                    if(Arrays.equals(cardMIC, hostMIC)) {
                        return dataForMIC;
                    } else {
                        throw new CipurseException("MIC verify failed");
                    }
                }
            } else {
                throw new CipurseException("Response is less than min ENCed response length(=6)");
            }
        }
    }

    private byte[] generateMAC(byte[] dataMAC) throws CipurseException {
        if(dataMAC.length % 16 != 0) {
            throw new CipurseException("MAC Data not block aligned");
        } else {
            byte[] blockDx = new byte[16];
            this.frameKeyiPlus1 = this.frameKeyi;

            byte[] actualMAC;
            for(int macBlock = 0; macBlock < dataMAC.length; macBlock += 16) {
                System.arraycopy(dataMAC, macBlock, blockDx, 0, 16);
                actualMAC = this.Aes.aesEncrypt(this.frameKeyiPlus1, blockDx);
                this.frameKeyiPlus1 = this.xor(actualMAC, blockDx);
            }

            this.logger.log(2, "Frame Key", this.frameKeyiPlus1);
            byte[] macBlock1 = this.xor(this.Aes.aesEncrypt(this.frameKeyi, this.frameKeyiPlus1), this.frameKeyiPlus1);
            System.arraycopy(this.frameKeyiPlus1, 0, this.frameKeyi, 0, 16);
            actualMAC = new byte[8];
            System.arraycopy(macBlock1, 0, actualMAC, 0, 8);
            return actualMAC;
        }
    }

    private byte[] generateCipher(byte[] dataENC, boolean isEncrypt) throws CipurseException {
        if(dataENC.length % 16 != 0) {
            throw new CipurseException("MAC Data not block aligned");
        } else {
            byte[] blockDx = new byte[16];
            byte[] ciphered = new byte[dataENC.length];
            this.frameKeyiPlus1 = this.frameKeyi;

            for(int i = 0; i < dataENC.length; i += 16) {
                byte[] hx = this.Aes.aesEncrypt(CipurseConstant.qConstant, this.frameKeyiPlus1);
                hx = this.xor(hx, this.frameKeyiPlus1);
                System.arraycopy(dataENC, i, blockDx, 0, 16);
                Object temp = null;
                byte[] temp1;
                if(isEncrypt) {
                    temp1 = this.Aes.aesEncrypt(hx, blockDx);
                } else {
                    temp1 = this.Aes.aesDecrypt(hx, blockDx);
                }

                System.arraycopy(temp1, 0, ciphered, i, 16);
                System.arraycopy(hx, 0, this.frameKeyiPlus1, 0, 16);
            }

            this.logger.log(2, "Frame Key", this.frameKeyiPlus1);
            System.arraycopy(this.frameKeyiPlus1, 0, this.frameKeyi, 0, 16);
            return ciphered;
        }
    }

    private byte[] computeMIC(byte[] dataForMIC) {
        byte MIC_LENGH = 4;
        byte[] mic = new byte[4];
        Object paddedDataForMIC = null;
        byte[] paddedDataForMIC1;
        if(dataForMIC.length % MIC_LENGH != 0) {
            int crc1 = MIC_LENGH - dataForMIC.length % MIC_LENGH;
            paddedDataForMIC1 = new byte[dataForMIC.length + crc1];
            System.arraycopy(dataForMIC, 0, paddedDataForMIC1, 0, dataForMIC.length);
        } else {
            paddedDataForMIC1 = new byte[dataForMIC.length];
            System.arraycopy(dataForMIC, 0, paddedDataForMIC1, 0, paddedDataForMIC1.length);
        }

        long crc11 = this.computeCRC(paddedDataForMIC1);

        for(int crc2 = 0; crc2 <= paddedDataForMIC1.length - 4; crc2 += 4) {
            byte temp1 = paddedDataForMIC1[crc2];
            byte temp2 = paddedDataForMIC1[crc2 + 1];
            paddedDataForMIC1[crc2] = paddedDataForMIC1[crc2 + 2];
            paddedDataForMIC1[crc2 + 1] = paddedDataForMIC1[crc2 + 3];
            paddedDataForMIC1[crc2 + 2] = temp1;
            paddedDataForMIC1[crc2 + 3] = temp2;
        }

        long crc21 = this.computeCRC(paddedDataForMIC1);
        mic[0] = (byte)((int)(crc21 >> 8 & 255L));
        mic[1] = (byte)((int)(crc21 & 255L));
        mic[2] = (byte)((int)(crc11 >> 8 & 255L));
        mic[3] = (byte)((int)(crc11 & 255L));
        return mic;
    }

    private long computeCRC(byte[] inputData) {
        long initialCRC = 25443L;
        long ch = 0L;

        for(int i = 0; i < inputData.length; ++i) {
            ch = (long)((short)(inputData[i] & 255));
            ch = (long)((short)((int)(ch ^ (long)((short)((int)(initialCRC & 255L))))));
            ch = (long)((short)((int)((ch ^ ch << 4) & 255L)));
            long first = initialCRC >> 8 & 65535L;
            long second = ch << 8 & 65535L;
            long third = ch << 3 & 65535L;
            long four = ch >> 4 & 65535L;
            initialCRC = (first ^ second ^ third ^ four) & 65535L;
        }

        return initialCRC;
    }

    private byte[] getLeDashCommand(byte[] smCommand, Utility.eCaseType orgCaseType, byte SMI) {
        Object smPlainCommand = null;
        byte[] smPlainCommand1;
        if(orgCaseType != Utility.eCaseType.CASE_1 && orgCaseType != Utility.eCaseType.CASE_3) {
            smPlainCommand1 = new byte[smCommand.length + 1];
            System.arraycopy(smCommand, 0, smPlainCommand1, 0, smCommand.length);
            smPlainCommand1[smPlainCommand1.length - 1] = 0;
        } else if((SMI & 12) != 0) {
            smPlainCommand1 = new byte[smCommand.length + 1];
            System.arraycopy(smCommand, 0, smPlainCommand1, 0, smCommand.length);
            smPlainCommand1[smPlainCommand1.length - 1] = 0;
        } else {
            smPlainCommand1 = smCommand;
        }

        return smPlainCommand1;
    }

    private byte[] xor(byte[] firstArray, byte[] secondArray) throws CipurseException {
        if(firstArray.length != secondArray.length) {
            throw new CipurseException("Invalid parameters");
        } else {
            byte[] resultArray = new byte[firstArray.length];

            for(int i = 0; i < firstArray.length; ++i) {
                resultArray[i] = (byte)(firstArray[i] ^ secondArray[i]);
            }

            return resultArray;
        }
    }

    private byte[] getOSPTModifiedCommand(byte[] command, byte SMI) throws CipurseException {
        Utility.eCaseType caseType = Utility.getCaseType(command);
        Object osptModifiedCmd = null;
        byte[] commandHeader = new byte[4];
        if(command.length >= 4) {
            System.arraycopy(command, 0, commandHeader, 0, 4);
        }

        byte[] osptModifiedCmd1;
        switch(caseType.ordinal()) {
            case 1:
                osptModifiedCmd1 = new byte[commandHeader.length + 2];
                System.arraycopy(commandHeader, 0, osptModifiedCmd1, 0, commandHeader.length);
                osptModifiedCmd1[4] = 1;
                osptModifiedCmd1[5] = SMI;
                break;
            case 2:
                osptModifiedCmd1 = new byte[commandHeader.length + 3];
                System.arraycopy(commandHeader, 0, osptModifiedCmd1, 0, commandHeader.length);
                osptModifiedCmd1[4] = 2;
                osptModifiedCmd1[5] = SMI;
                osptModifiedCmd1[6] = command[4];
                break;
            case 3:
                osptModifiedCmd1 = new byte[(short)(commandHeader.length + 2 + (short)(command[4] & 255))];
                System.arraycopy(commandHeader, 0, osptModifiedCmd1, 0, commandHeader.length);
                osptModifiedCmd1[4] = (byte)(command[4] + 1);
                osptModifiedCmd1[5] = SMI;
                System.arraycopy(command, 5, osptModifiedCmd1, 6, (short)(command[4] & 255));
                break;
            case 4:
                osptModifiedCmd1 = new byte[commandHeader.length + 3 + (short)(command[4] & 255)];
                System.arraycopy(commandHeader, 0, osptModifiedCmd1, 0, commandHeader.length);
                osptModifiedCmd1[4] = (byte)(command[4] + 2);
                osptModifiedCmd1[5] = SMI;
                System.arraycopy(command, 5, osptModifiedCmd1, 6, (short)(command[4] & 255));
                osptModifiedCmd1[osptModifiedCmd1.length - 1] = command[command.length - 1];
                break;
            default:
                throw new CipurseException("Command with invalid case");
        }

        osptModifiedCmd1[0] = (byte)(osptModifiedCmd1[0] | 4);
        return osptModifiedCmd1;
    }

    private byte[] computeNLM(byte[] x, byte[] y) {
        byte shiftBitsBy = 40;
        long x1 = 0L;
        long y1 = 0L;

        int i;
        for(i = 0; i < x.length; ++i) {
            x1 |= (long)(x[i] & 255) << shiftBitsBy - i * 8;
        }

        for(i = 0; i < y.length; ++i) {
            y1 |= (long)(y[i] & 255) << shiftBitsBy - i * 8;
        }

        long nlm = this.computeNLM(x1, y1);
        byte[] retNLM = new byte[6];

        for(i = 0; i < retNLM.length; ++i) {
            retNLM[i] = (byte)((int)(nlm >> shiftBitsBy - i * 8));
        }

        return retNLM;
    }

    private long computeNLM(long x, long y) {
        long nlm = 0L;

        for(int i = 0; i < 48; ++i) {
            nlm = this.shiftRight(nlm);
            if((nlm & 1L) == 1L) {
                nlm ^= 59032325644658L;
            }

            y = this.shiftRight(y);
            if((y & 1L) == 1L) {
                nlm ^= x;
            }
        }

        return nlm;
    }

    private long shiftRight(long ui48Bit) {
        ui48Bit <<= 1;
        if((ui48Bit & 281474976710656L) != 0L) {
            ui48Bit |= 1L;
            ui48Bit &= 281474976710655L;
        }

        return ui48Bit;
    }

    private byte[] pad(byte[] x) throws CipurseException {
        if(x.length > 16) {
            throw new CipurseException("Data to pad length > 128 bits");
        } else {
            byte[] y = new byte[16];
            System.arraycopy(x, 0, y, y.length - x.length, x.length);
            return y;
        }
    }

    private byte[] pad2(byte[] x) throws CipurseException {
        if(x.length * 2 > 16) {
            throw new CipurseException("Length of data to pad * 2 > 128 bits");
        } else {
            byte[] y = new byte[16];
            System.arraycopy(x, 0, y, 16 - x.length, x.length);
            System.arraycopy(x, 0, y, 16 - x.length * 2, x.length);
            return y;
        }
    }

    private byte[] extFunction(byte[] x, int N) throws CipurseException {
        if(x.length < N) {
            throw new CipurseException("Invalid parameters");
        } else {
            byte[] y = new byte[N];
            System.arraycopy(x, x.length - N, y, 0, N);
            return y;
        }
    }
}
