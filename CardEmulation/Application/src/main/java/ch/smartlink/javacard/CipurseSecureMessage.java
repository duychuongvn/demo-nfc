package ch.smartlink.javacard;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.CryptoParameters;
import org.osptalliance.cipurse.IAes;
import org.osptalliance.cipurse.ICryptoEngine;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.PaddingAlgo;
import org.osptalliance.cipurse.ProcessingAlgo;
import org.osptalliance.cipurse.commands.ByteArray;
import org.osptalliance.cipurse.crypto.CipurseCrypto;
import org.osptalliance.cipurse.securemessaging.ICipurseSMKey;
import org.osptalliance.javacard.cipurse.host.SecureMessaging;
import org.osptalliance.javacard.cipurse.host.SecureMessagingException;

import java.util.Arrays;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.security.Key;
import javacard.security.RandomData;

/**
 * Created by caoky on 1/9/2016.
 */
public class CipurseSecureMessage extends SecureMessaging implements ISO7816 {
    private static short RESPONSE_MAX_LENGTH = 255;
    private static short RESPONSE_SM_MAX_LENGTH = 231;
    private static short CHALLENGES_MAX_LENGTH = 255;

    private static CipurseSecureMessage cipurseSecureMessage;
    private final RandomData random;
    private HrsCipurseCrypto cipurseCrypto = null;
    private ILogger logger = null;
    private IAes Aes = null;

    private byte[] mutualAuthHeader = new byte[]{(byte) 0, (byte) -126, (byte) 0, (byte) 0};
    private final byte[] getChallengeCommand = new byte[]{(byte) 0, (byte) -124, (byte) 0, (byte) 0, (byte) 22};
    private byte[][] keySet = null;
    private ICipurseSMKey samSmKey;
    private static byte[] nullVector = new byte[16];

    private HrsKey hrsKey;
    private APDU apdu;
    private short lc;
    private short p1p2;
    private byte p2;
    private byte p1;
    private byte ins;
    private byte cla;
    private short _0 = 0;
    private byte[] rP;
    private byte[] RP;
    private byte[] mutualAuthCmd = null;
    private byte[] Ct;

    public static CipurseSecureMessage getInstance(IAes aes, ILogger logger) throws CipurseException {
        if (cipurseSecureMessage == null) {
            cipurseSecureMessage = new CipurseSecureMessage(aes, logger);
        }
        return cipurseSecureMessage;
    }

    public void init(APDU apdu) {
        this.apdu = apdu;
        byte[] buf = apdu.getBuffer();
        cla= buf[OFFSET_CLA];
        ins = buf[OFFSET_INS];
        p1 = buf[OFFSET_P1];
        p2 = buf[OFFSET_P2];
        p1p2 = Util.makeShort(p1, p2);
        lc = (short) (buf[OFFSET_LC] & 0xFF);
        mutualAuthCmd = null;
    }

    public CipurseSecureMessage(IAes Aes, ILogger logger) throws CipurseException {
        try {
            this.logger = logger;
            this.Aes = Aes;
            this.cipurseCrypto = new HrsCipurseCrypto(Aes, logger);
            this.random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        } catch (CipurseException ce) {
            throw ce;
        } catch (Exception var6) {
            throw new CipurseException(var6);
        }
    }

    public void setKeyValues(byte[][] keySet) {
        this.keySet = keySet;
    }

    public byte[] wrapCommand(byte[] plainCommand, byte SMI) throws CipurseException {
        return this.samSmKey != null ? this.samSmKey.getCipurseSM().wrapCommand(plainCommand, SMI) : this.cipurseCrypto.wrapCommand(plainCommand, SMI);
    }

    public byte[] unWrapCommand(byte[] smCommand, byte SMI) throws CipurseException {
        return this.samSmKey != null ? this.samSmKey.getCipurseSM().unWrapCommand(smCommand, SMI) : this.cipurseCrypto.unwrapCommand(smCommand, SMI);
    }

    public byte[] getKVV(byte[] forKey) throws CipurseException {
        if (forKey.length % 16 != 0) {
            throw new CipurseException("Not a valid key length");
        } else {
            byte[] cipherText = this.Aes.aesEncrypt(forKey, nullVector);
            byte[] kvv = new byte[3];
            System.arraycopy(cipherText, 0, kvv, 0, 3);
            return kvv;
        }
    }

    public byte[] encryptText(byte[] keyValue, byte[] textToBeEncrypted) throws CipurseException {
        return this.Aes.aesEncrypt(keyValue, textToBeEncrypted);
    }

    public byte[] encryptText(byte keyNum, byte[] textToBeEncrypted) throws CipurseException {
        if (this.keySet != null && this.keySet.length > keyNum) {
            return this.Aes.aesEncrypt(this.keySet[keyNum], textToBeEncrypted);
        } else {
            throw new CipurseException("Key value is not initialized");
        }
    }

    public byte[] decryptText(byte[] keyValue, byte[] textToBeDecrypted) throws CipurseException {
        return this.Aes.aesDecrypt(keyValue, textToBeDecrypted);
    }

    public byte[] decryptText(byte keyNum, byte[] textToBeDecrypted) throws CipurseException {
        if (this.keySet != null && this.keySet.length > keyNum) {
            return this.Aes.aesDecrypt(this.keySet[keyNum], textToBeDecrypted);
        } else {
            throw new CipurseException("Key value is not initialized");
        }
    }

    public byte[] encryptText(byte keyAlgorithm, byte[] keyValue, byte[] textToBeEncrypted, CryptoParameters params) throws CipurseException {
        if (this.Aes instanceof ICryptoEngine) {
            ICryptoEngine padAlgo1 = (ICryptoEngine) this.Aes;
            return padAlgo1.encrypt(keyAlgorithm, keyValue, textToBeEncrypted, params);
        } else {
            if (keyAlgorithm == 9 && keyValue != null && keyValue.length == 16) {
                if (params == null) {
                    this.Aes.aesEncrypt(keyValue, textToBeEncrypted);
                } else {
                    PaddingAlgo padAlgo = params.getPaddingAlgo();
                    ProcessingAlgo procAlgo = params.getProcessingAlgo();
                    if ((padAlgo == null || padAlgo == PaddingAlgo.NONE) && (procAlgo == null || procAlgo == ProcessingAlgo.ECB) && params.getIV() == null) {
                        this.Aes.aesEncrypt(keyValue, textToBeEncrypted);
                    }
                }
            }

            throw new CipurseException("Crypto Engine doesn\'t support this functionality");
        }
    }

    public byte[] decryptText(byte keyAlgorithm, byte[] keyValue, byte[] textToBeDecrypted, CryptoParameters params) throws CipurseException {
        if (this.Aes instanceof ICryptoEngine) {
            ICryptoEngine padAlgo1 = (ICryptoEngine) this.Aes;
            return padAlgo1.decrypt(keyAlgorithm, keyValue, textToBeDecrypted, params);
        } else {
            if (keyAlgorithm == 9 && keyValue != null && keyValue.length == 16) {
                if (params == null) {
                    this.Aes.aesDecrypt(keyValue, textToBeDecrypted);
                } else {
                    PaddingAlgo padAlgo = params.getPaddingAlgo();
                    ProcessingAlgo procAlgo = params.getProcessingAlgo();
                    if ((padAlgo == null || padAlgo == PaddingAlgo.NONE) && (procAlgo == null || procAlgo == ProcessingAlgo.ECB) && params.getIV() == null) {
                        this.Aes.aesDecrypt(keyValue, textToBeDecrypted);
                    }
                }
            }

            throw new CipurseException("Crypto Engine doesn\'t support this functionality");
        }
    }

    /**
     * Convenience method to build a GET CHALLENGE command in the APDU buffer
     *
     * @param buffer
     * @param offset
     * @return
     */
    @Override
    public short buildGetChallenge(byte[] buffer, short offset) {
        RP = this.cipurseCrypto.getRandom(16);
        rP = this.cipurseCrypto.getRandom(6);
        return lc;
    }
    /**
     * Method to build MUTUAL AUTENTICATE command based on the previously derived
     * session key k0 and the random data generated with finishGetChallenge().
     *
     * @param buffer
     * @param offset
     * @param length
     * @param key
     * @throws SecureMessagingException
     */
    @Override
    public void finishGetChallenge(byte[] buffer, short offset, short length, Key key) throws SecureMessagingException {
        if (length > CHALLENGES_MAX_LENGTH)
            ISOException.throwIt(SW_WRONG_DATA);
        if(key instanceof HrsKey) {
            this.hrsKey = (HrsKey) key;

            // send to terminal
//            byte[] RP = new byte[16];
//            byte[] rP = new byte[6];
//            System.arraycopy(response, 0, RP, 0, 16);
//            System.arraycopy(response, 16, rP, 0, 6);
//            byte[] RT = this.cipurseCrypto.getRandom(16);
//            byte[] rT = this.cipurseCrypto.getRandom(6);
//            byte[] Cp = ex.generateK0AndGetCp((byte[])keyValue, RP, rP, RT, rT);


            // receive multual
//            System.arraycopy(Cp, 0, mutualAuth, 0, 16);
//            System.arraycopy(RT, 0, mutualAuth, 16, 16);
//            System.arraycopy(rT, 0, mutualAuth, 32, 6);
//            byte[] mutualAuthCmd = new byte[this.mutualAuthHeader.length + mutualAuth.length + 2];
//            System.arraycopy(this.mutualAuthHeader, 0, mutualAuthCmd, 0, this.mutualAuthHeader.length);
//            System.arraycopy(mutualAuth, 0, mutualAuthCmd, 5, mutualAuth.length);

//
//            this.rP = rP1;
//            this.RP = RP1;
//            this.RT = RT1;
//            this.rT = rT1;

//            byte[] cP = new byte[16];
//            byte[] cp = new byte[6];
//            try {
//                Ct = this.cipurseCrypto.generateK0AndGetCp(hrsKey.getKeyValue(), cP, cp, RP, rP);
//                System.arraycopy(Ct, 0, buffer, 0, Ct.length);
//                System.arraycopy(rP, 0, buffer, Ct.length, rP.length);
//            }catch (CipurseException ex) {
//                throw new SecureMessagingException(SW_WRONG_DATA);
//            }

            System.arraycopy(RP, 0, buffer, 0, 16);
            System.arraycopy(rP, 0, buffer, 16, 6);
        }
    }

    @Override
    public short buildMutualAuthenticate(byte[] buffer, short offset, short keyID) {
        byte[] mutualAuth = new byte[38];
        System.arraycopy(Ct, 0, mutualAuth, 0, 16);
        System.arraycopy(RP, 0, mutualAuth, 16, 16);
        System.arraycopy(rP, 0, mutualAuth, 32, 6);

        mutualAuthCmd = new byte[this.mutualAuthHeader.length + mutualAuth.length + 2];
        System.arraycopy(this.mutualAuthHeader, 0, mutualAuthCmd, 0, this.mutualAuthHeader.length);
        System.arraycopy(mutualAuth, 0, mutualAuthCmd, 5, mutualAuth.length);
        mutualAuthCmd[4] = (byte)(mutualAuth.length & 255);
        mutualAuthCmd[mutualAuthCmd.length - 1] = 16;
        return 0;
    }

    @Override
    public void finishMutualAuthenticate(byte[] buffer, short offset, short length) throws SecureMessagingException {
        try {
            byte[] cP = new byte[16];
            byte[] RT = new byte[16];
            byte[] rT = new byte[6];
            System.arraycopy(buffer, 5, cP, 0, 16);
            System.arraycopy(buffer, 21, RT, 0, 16);
            System.arraycopy(buffer, 37, rT, 0, 6);

            byte[] cP1 = this.cipurseCrypto.generateK0AndGetCp(hrsKey.getKeyValue(),RP, rP, RT, rT);

            if (Arrays.equals(cP1, cP)) {
                byte[] Ct = this.cipurseCrypto.generateCT(RT);
                System.arraycopy(Ct, 0, buffer, 0,Ct.length);
            } else {

                this.logger.log("Terminal response verification failed");
                ISOException.throwIt(SW_WRONG_DATA);
                throw new SecureMessagingException(SW_WRONG_DATA);
            }
        }catch (CipurseException ex) {
            logger.log(1, MessageUtil.hexStringToByteArray(ex.getMessage()));
            ISOException.throwIt(SW_WRONG_DATA);
            throw new SecureMessagingException(SW_WRONG_DATA);
        }
    }

    /**
     * Unwrap card response and store output data in the specified buffer.
     * @param inBuffer
     * @param inOffset
     * @param inLength
     * @param outBuffer
     * @param outOffset
     * @return
     */
    @Override
    public short unwrap(byte[] inBuffer, short inOffset, short inLength, byte[] outBuffer, short outOffset) {
        return 0;
    }

    /**
     * Wraps a command with the specified security level.
     * @param smi
     * @param inBuffer
     * @param inOffset
     * @param inLength
     * @param outBuffer
     * @param outOffset
     * @return
     */
    @Override
    public short wrap(short smi, byte[] inBuffer, short inOffset, short inLength, byte[] outBuffer, short outOffset) {
        return 0;
    }

    /**
     * Resets the authentication state and clears all session keys.
     */
    @Override
    public void resetSecurity() {

    }
}