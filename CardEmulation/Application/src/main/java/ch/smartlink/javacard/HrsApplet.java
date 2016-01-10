package ch.smartlink.javacard;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.IAes;
import org.osptalliance.cipurse.ILogger;

import ch.smartlink.javacard.cipurse.AES;
import ch.smartlink.javacard.cipurse.Logger;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.RandomData;

/**
 * Created by caoky on 1/9/2016.
 */
public class HrsApplet extends Applet implements ISO7816 {
    private static final short _0 = 0;

    private static final boolean FORCE_SM_GET_CHALLENGE = true;

    private static final byte[] HISTORICAL = { 0x00, 0x73, 0x00, 0x00,
            (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00 };

    // returned by vendor specific command f1
    private static final byte[] VERSION = { 0x01, 0x00, 0x12 };

    // Openpgp defines 6983 as AUTHENTICATION BLOCKED
    private static final short SW_AUTHENTICATION_BLOCKED = 0x6983;

    private static final byte[] EXTENDED_CAP = {
            (byte) 0xF0, // Support for GET CHALLENGE
            // Support for Key Import
            // PW1 Status byte changeable
            0x00, // Secure messaging using 3DES
            0x00, (byte) 0xFF, // Maximum length of challenges
            0x04, (byte) 0xC0, // Maximum length Cardholder Certificate
            0x00, (byte) 0xFF, // Maximum length command data
            0x00, (byte) 0xFF  // Maximum length response data
    };
    public static final byte SMI_PLAIN_PLAIN = 0;
    public static final byte SMI_PLAIN_PLAIN_LE_PRESENT = 1;
    public static final byte SMI_PLAIN_MAC = 4;
    public static final byte SMI_PLAIN_MAC_LE_PRESENT = 5;
    public static final byte SMI_PLAIN_ENC = 8;
    public static final byte SMI_PLAIN_ENC_LE_PRESENT = 9;
    public static final byte SMI_MAC_PLAIN = 64;
    public static final byte SMI_MAC_PLAIN_LE_PRESENT = 65;
    public static final byte SMI_MAC_MAC = 68;
    public static final byte SMI_MAC_MAC_LE_PRESENT = 69;
    public static final byte SMI_MAC_ENC = 72;
    public static final byte SMI_MAC_ENC_LE_PRESENT = 73;
    public static final byte SMI_ENC_PLAIN = -128;
    public static final byte SMI_ENC_PLAIN_LE_PRESENT = -127;
    public static final byte SMI_ENC_MAC = -124;
    public static final byte SMI_ENC_MAC_LE_PRESENT = -123;
    public static final byte SMI_ENC_ENC = -120;
    public static final byte SMI_ENC_ENC_LE_PRESENT = -119;

    private static short RESPONSE_MAX_LENGTH = 255;
    private static short RESPONSE_SM_MAX_LENGTH = 231;
    private static short CHALLENGES_MAX_LENGTH = 255;

    private static short BUFFER_MAX_LENGTH = 1221;

    private static short LOGINDATA_MAX_LENGTH = 254;
    private static short URL_MAX_LENGTH = 254;
    private static short NAME_MAX_LENGTH = 39;
    private static short LANG_MAX_LENGTH = 8;
    private static short CERT_MAX_LENGTH = 1216;

    private static short FP_LENGTH = 20;

    private static byte PW1_MIN_LENGTH = 6;
    private static byte PW1_MAX_LENGTH = 127;
    // Default PW1 '123456'
    private static byte[] PW1_DEFAULT = { 0x31, 0x32, 0x33, 0x34, 0x35, 0x36 };
    private static byte PW1_MODE_NO81 = 0;
    private static byte PW1_MODE_NO82 = 1;

    private static final byte RC_MIN_LENGTH = 8;
    private static final byte RC_MAX_LENGTH = 127;

    private static final byte PW3_MIN_LENGTH = 8;
    private static final byte PW3_MAX_LENGTH = 127;
    // Default PW3 '12345678'
    private static final byte[] PW3_DEFAULT = { 0x31, 0x32, 0x33, 0x34, 0x35,
            0x36, 0x37, 0x38 };

    private byte[] buffer;
    private short out_left = 0;
    private short out_sent = 0;
    private short in_received = 0;

    private boolean chain = false;
    private byte chain_ins = 0;
    private short chain_p1p2 = 0;
    private RandomData random;
    private boolean sm_success = false;

    private boolean terminated = false;
    private CipurseSecureMessage cipurseSecureMessage;
    private IAes aes;
    private ILogger logger;
    private HrsKey hrsKey;
    private byte SMI;
    public HrsApplet() throws CipurseException {
        this.aes = new AES();
        this.logger = new Logger();
        this.cipurseSecureMessage = CipurseSecureMessage.getInstance(aes, logger);
        this.random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        String roomKey = "CE55B50169303A9A";
        this.hrsKey = new HrsKey(roomKey.getBytes());
        buffer = JCSystem.makeTransientByteArray(BUFFER_MAX_LENGTH,
                JCSystem.CLEAR_ON_DESELECT);
    }
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        try {
            new HrsApplet().register(bArray, (short) (bOffset + 1),
                    bArray[bOffset]);
        }catch (CipurseException c) {
            c.printStackTrace();
        }
    }
    @Override
    public void process(APDU apdu) throws ISOException {

        if (selectingApplet()) {
            // Reset PW1 modes
//            pw1_modes[PW1_MODE_NO81] = false;
//            pw1_modes[PW1_MODE_NO82] = false;

            return;
        }
        cipurseSecureMessage.init(apdu);
        byte[] buf = apdu.getBuffer();
        byte cla= buf[OFFSET_CLA];
        byte ins = buf[OFFSET_INS];
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];
        short p1p2 = Util.makeShort(p1, p2);
        short lc = (short) (buf[OFFSET_LC] & 0xFF);


        // Secure messaging
        //TODO Force SM if contactless is used
        sm_success = false;
        if(cla == (byte)0x04) {
            SMI = buf[5];
            //CLA’ - INS - P1 - P2 - Lc’ - SMI - {DATA} - {Le} - {Le’}
            try {
                String cmd = MessageUtil.byteArrayToHexString(buf);

                System.out.println("Unwraped command:" + cmd);
                byte[] data = new byte[5 + lc];

                System.arraycopy(buf, 0, data, 0, data.length);
              byte[] command =  cipurseSecureMessage.unWrapCommand(data, SMI);

                cmd = MessageUtil.byteArrayToHexString(command);
                System.out.println("Unwraped command:" + cmd);


                sm_success = true;
            }catch (CipurseException ce) {
                ce.printStackTrace();
            }
        }
//        if ((byte) (cla & (byte) 0x0C) == (byte) 0x0C) {
//            // Force initialization of SSC before using SM to prevent replays
//            if (FORCE_SM_GET_CHALLENGE && !sm.isSetSSC() && (ins != (byte) 0x84))
//                ISOException.throwIt(SW_CONDITIONS_NOT_SATISFIED);
//
//            lc = sm.unwrapCommandAPDU();
//            sm_success = true;
//        }

        short status = SW_NO_ERROR;
        short le = 0;

        try {
            // Support for command chaining
//            commandChaining(apdu);

            // Reset buffer for GET RESPONSE
            if (ins != (byte) 0xC0) {
                out_sent = 0;
                out_left = 0;
            }

            if (terminated == true && ins != 0x44) {
                ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
            }

            // Other instructions
            switch (ins) {
                case (byte) 0xA4:
                    System.out.println("Select ");
                    break;
                // GET RESPONSE
                case (byte) 0xC0:
                    // Will be handled in finally clause
                    break;

                // VERIFY
                case (byte) 0x20:
//                    verify(apdu, p2);
                    break;

                // CHANGE REFERENCE DATA
                case (byte) 0x24:
//                    changeReferenceData(apdu, p2);
                    break;

                // RESET RETRY COUNTER
                case (byte) 0x2C:
                    // Reset only available for PW1
//                    if (p2 != (byte) 0x81)
//                        ISOException.throwIt(SW_INCORRECT_P1P2);
//
//                    resetRetryCounter(apdu, p1);
                    break;

                // PERFORM SECURITY OPERATION
                case (byte) 0x2A:
                    // COMPUTE DIGITAL SIGNATURE
//                    if (p1p2 == (short) 0x9E9A) {
//                        le = computeDigitalSignature(apdu);
//                    }
//                    // DECIPHER
//                    else if (p1p2 == (short) 0x8086) {
//                        le = decipher(apdu);
//                    } else {
//                        ISOException.throwIt(SW_WRONG_P1P2);
//                    }

                    break;

                // INTERNAL AUTHENTICATE
                case (byte) 0x88:
//                    le = internalAuthenticate(apdu);
                    break;

                // GENERATE ASYMMETRIC KEY PAIR
                case (byte) 0x47:
//                    le = genAsymKey(apdu, p1);
                    break;

                // GET CHALLENGE
                case (byte) 0x84:
                    le = getChallenge(apdu, lc);
                    break;
                // MUTUAL AUTHENTICATION
                case (byte) 0x82:

                    le = mutualAuthentication(apdu, (short)16);
                    break;

                // GET DATA
                case (byte) 0xCA:
//                    le = getData(p1p2);
                    break;

                // PUT DATA
                case (byte) 0xDA:
//                    putData(p1p2);
                    break;

                // DB - PUT DATA (Odd)
                case (byte) 0xDB:
                    // Odd PUT DATA only supported for importing keys
                    // 4D - Extended Header list
//                    if (p1p2 == (short) 0x3FFF) {
//                        importKey(apdu);
//                    } else {
//                        ISOException.throwIt(SW_RECORD_NOT_FOUND);
//                    }
                    break;

                // E6 - TERMINATE DF
                case (byte) 0xE6:
//                    if (pw1.getTriesRemaining() == 0 && pw3.getTriesRemaining() == 0) {
//                        terminated = true;
//                    } else {
//                        ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//                    }
                    break;

                // 44 - ACTIVATE FILE
                case (byte) 0x44:
//                    if (terminated == true) {
//                        initialize();
//                        terminated = false;
//                        JCSystem.requestObjectDeletion();
//                    } else {
//                        ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
//                    }
                    break;

                // GET VERSION (vendor specific)
                case (byte) 0xF1:
                    le = Util.arrayCopy(VERSION, _0, buffer, _0, (short) VERSION.length);
                    break;

                // SET RETRIES (vendor specific)
//                case (byte) 0xF2:
//                    if (lc != 3) {
//                        ISOException.throwIt(ISO7816.SW_WRONG_DATA);
//                    }
//                    short offs = ISO7816.OFFSET_CDATA;
//                    setPinRetries(buf[offs++], buf[offs++], buf[offs++]);
//                    break;

                default:
                    // good practice: If you don't know the INStruction, say so:
                    ISOException.throwIt(SW_INS_NOT_SUPPORTED);
            }
        } catch(ISOException e) {
            status = e.getReason();
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (status != (short)0x9000) {
                // Send the exception that was thrown
                sendException(apdu, status);
            } else {
                buffer = apdu.getBuffer();
                // GET RESPONSE
                if (ins == (byte) 0xC0) {
                    sendNext(apdu);
                } else {
                    sendBuffer(apdu, le);
                }
            }
        }
    }
    private short getChallenge(APDU apdu, short len) {
        byte[]buffer = apdu.getBuffer();
        cipurseSecureMessage.buildGetChallenge(buffer, _0);
        cipurseSecureMessage.finishGetChallenge(buffer,_0, len, hrsKey);
//        cipurseSecureMessage.buildMutualAuthenticate(apdu.getBuffer(), 0, cipurseSecureMessage.getKVV(hr) )
        return len;
    }

    private short mutualAuthentication(APDU apdu, short len) {

        cipurseSecureMessage.finishMutualAuthenticate(apdu.getBuffer(), _0, len);

        return len;
    }
    /**
     * Send len bytes from buffer. If len is greater than RESPONSE_MAX_LENGTH,
     * remaining data can be retrieved using GET RESPONSE.
     *
     * @param apdu
     * @param len
     *            The byte length of the data to send
     */
    private void sendBuffer(APDU apdu, short len) {
        out_sent = 0;
        out_left = len;
        sendNext(apdu);
    }

    /**
     * Send provided status
     *
     * @param apdu
     * @param status Status to send
     */
    private void sendException(APDU apdu, short status) {
        out_sent = 0;
        out_left = 0;
        sendNext(apdu, status);
    }

    /**
     * Send next block of data in buffer. Used for sending data in <buffer>
     *
     * @param apdu
     */
    private void sendNext(APDU apdu) {
        sendNext(apdu, SW_NO_ERROR);
    }

    /**
     * Send next block of data in buffer. Used for sending data in <buffer>
     *
     * @param apdu
     * @param status Status to send
     */
    private void sendNext(APDU apdu, short status) {
        byte[] buf = APDU.getCurrentAPDUBuffer();
        apdu.setOutgoing();

        // Determine maximum size of the messages
        short max_length;
        if (sm_success) {
            max_length = RESPONSE_SM_MAX_LENGTH;
        } else {
            max_length = RESPONSE_MAX_LENGTH;
        }

        if (max_length > out_left) {
            max_length = out_left;
        }

        Util.arrayCopyNonAtomic(buffer, out_sent, buf, _0, max_length);

        short len = 0;
        if (out_left > max_length) {
            len = max_length;

            // Compute byte left and sent
            out_left -= max_length;
            out_sent += max_length;

            // Determine new status word
            if (out_left > max_length) {
                status = (short) (SW_BYTES_REMAINING_00 | max_length);
            } else {
                status = (short) (SW_BYTES_REMAINING_00 | out_left);
            }
        } else {
            len = out_left;

            // Reset buffer
            out_sent = 0;
            out_left = 0;
        }

        // If SM is used, wrap response
        if (sm_success) {
//            try {
                String response = "313233343536";
                byte[] dataResponse = MessageUtil.hexStringToByteArray(response);
                System.arraycopy(dataResponse, 0, buf, 0, dataResponse.length);
            len = (short)dataResponse.length;
//                //byte[] wrapResponse  = cipurseSecureMessage.wrapCommand(MessageUtil.hexStringToByteArray(response), SMI);
//             //   len = (short)wrapResponse.length;
//             //   System.arraycopy(wrapResponse, 0, buf, 0, len);
////                status = SW_COMMAND_NOT_ALLOWED;
//
//            } catch (CipurseException e) {
//                e.printStackTrace();
//            }
//
        }

      //  apdu.setOutgoingNoChaining();
        // Send data in buffer
        apdu.setOutgoingLength(len);
        apdu.sendBytes(_0, len);

        // Send status word
        if (status != SW_NO_ERROR)
            ISOException.throwIt(status);
    }

    public byte[] wrapResponse(byte[] response, byte SMI) {

        try {
            return cipurseSecureMessage.wrapCommand(response, SMI);
        } catch (CipurseException e) {
            e.printStackTrace();
            ISOException.throwIt(SW_UNKNOWN);
        }
        return  null;
    }
    /**
     * Get length of TLV element.
     *
     * @param data
     *            Byte array
     * @param offset
     *            Offset within byte array containing first byte
     * @return Length of value
     */
    private short getLength(byte[] data, short offset) {
        short len = 0;

        if ((data[offset] & (byte) 0x80) == (byte) 0x00) {
            len = data[offset];
        } else if ((data[offset] & (byte) 0x7F) == (byte) 0x01) {
            len = data[(short) (offset + 1)];
            len &= 0x00ff;
        } else if ((data[offset] & (byte) 0x7F) == (byte) 0x02) {
            len = Util.makeShort(data[(short) (offset + 1)], data[(short) (offset + 2)]);
        } else {
            ISOException.throwIt(SW_UNKNOWN);
        }

        return len;
    }

    /**
     * Get number of bytes needed to represent length for TLV element.
     *
     * @param length
     *            Length of value
     * @return Number of bytes needed to represent length
     */
    private short getLengthBytes(short length) {
        if (length <= 127) {
            return 1;
        } else if (length <= 255) {
            return 2;
        } else {
            return 3;
        }
    }

}
