package ch.smartlink.javacard.hrs;


import ch.smartlink.javacard.MessageUtil;
import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.IAes;
import org.osptalliance.cipurse.ICommsChannel;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.commands.*;
import org.osptalliance.cipurse.impl.Logger;

import java.util.Arrays;

public class HrsCardCreator {

    private static final String ID_ADF_AID = "5F 00 00 00 00 00 00 01";
    private static final short ID_ADF_SFID = 0x5F00;
    private static final short ID_EF_CARD_INFO_FID = 0x5F01;
    private static final short ID_EF_CARD_INFO_SFID = 0x00;

    private static final int KEY_NUM_ADF = 1;
    private ByteArray keyMFPrivacyDefault = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73");

//    private ByteArray keyADFAuthentication = new ByteArray("F0 F1 F2 F3 A0 A1 A2 A3 B0 B1 B2 B3 C0 C1 C2 C3");
    private ByteArray keyADFPersonalize = new ByteArray("E0 E1 E2 E3 F0 F1 F2 F3 D0 D1 D2 D3 C0 C1 C2 C3");

    private ByteArray keyMFPrivacyUpdated = new ByteArray("A3 B3 C3 D3 E3 F3 03 13 23 33 43 53 63 73 83 93");
//    private ByteArray keyMFCardMgmtUpdated = new ByteArray("03 13 23 33 73 73 73 73 73 73 73 73 73 73 73 73");

    private static final byte keyNoPrivacy = 0x01;
    private static final byte keyNoPersonalize = 0x01;

    private static final byte keyAlgoMFPrivacy = 0x09; // AES
    private static final byte keyAddInfoMFPrivacy = 0x01;
    private static final byte keyLengthMFPrivacy = 0x10;

    private static final byte keyAlgoADFPersonalize = 0x09;
    private static final byte keyAddInfoADFPersonalize = 0x01;
    private static final byte keyLengthADFPersonalize = 0x10;

    private ICipurseAdministration cipurseAdministration;
    private ICipurseOperational cipurseOperational;
    private CipurseCardHandler cipurseCardHandler;
    private ILogger logger;
    private ICommsChannel commsChannel;
    private IAes aes;
    public HrsCardCreator(ICommsChannel commsChannel, IAes aes, Logger logger) throws CipurseException {

        this.logger = logger;
        this.aes = aes;
        this.commsChannel = commsChannel;
        initCommand();
    }

    private void initCommand() throws CipurseException {

        cipurseCardHandler = new CipurseCardHandler(commsChannel, aes, logger);
        CommandAPI commandAPI = CommandAPIFactory.getInstance().buildCommandAPI();
        commandAPI.setVersion(CommandAPI.Version.V3);
        cipurseAdministration = commandAPI.getCipurseAdministration(cipurseCardHandler);
        cipurseOperational = commandAPI.getCipurseOperational(cipurseCardHandler);
    }

    private void connect() throws CipurseException {
        if(!commsChannel.isOpen()) {
            commsChannel.open();
        }
    }

    public void restoreCard() throws CipurseException {
        connect();
        if(!isNotCardInitialized()) {
            cipurseCardHandler.reset(0);
            UpdateKeyInfo updateKeyInfo = new UpdateKeyInfo();
            updateKeyInfo.keyLength = keyLengthMFPrivacy;
            updateKeyInfo.keyAlgoId = keyAlgoMFPrivacy;
            updateKeyInfo.keyAddInfo = keyAddInfoMFPrivacy;
            updateKeyInfo.updateKeyNumber = keyNoPrivacy;
            updateKeyInfo.encKeyNumber = 0x00;
            updateKeyInfo.setUpdateKeyValues(keyMFPrivacyDefault, null);
            cipurseOperational.selectMF();
            cipurseCardHandler.secureMessagingFlag = false;
            cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyMFPrivacyUpdated);
            ByteArray response = cipurseOperational.updateKey(updateKeyInfo);
            MessageUtil.handleError(response);
            cipurseAdministration.formatAll();
        }
    }
    public CardInfo getCardInfo() throws CipurseException {
        connect();
        cipurseCardHandler.reset(0);
        cipurseOperational.selectMF();
        cipurseCardHandler.secureMessagingFlag = false;
        cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyMFPrivacyUpdated);
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        ByteArray response = cipurseOperational.selectFilebyAID(new ByteArray(ID_ADF_AID));
        MessageUtil.handleError(response);

        cipurseCardHandler.setupSecureChannel(keyNoPersonalize, keyADFPersonalize);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        response = cipurseOperational.selectFilebyFID(ID_EF_CARD_INFO_FID);


        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_ENC_ENC_LE_PRESENT;
        response = cipurseOperational.readBinary((short)0, (short)0);
        MessageUtil.handleError(response);
        ByteArray data =  response.subArray(0, CardInfo.FILE_LENGTH);
        return CardInfo.deserialize(data.getBytes());
    }

    public void storeRoomInfo(CardInfo cardInfo) throws CipurseException {
        connect();
        if(isNotCardInitialized()) {
            cipurseAdministration.formatAll();
            initializeCardInfo();
        }
        updateCardInfo(cardInfo.toBytes());
    }
    public void initializeCardInfo() throws CipurseException {
        connect();
        updateMFKeys();
        createADF();
        createCardInfo();

    }

    private void createCardInfo() throws CipurseException {
        EFFileAttributes efFileAttributes = new EFFileAttributes();
        efFileAttributes.fileType = EFFileAttributes.BINARY_FILE_TYPE;
        efFileAttributes.fileSize = CardInfo.FILE_LENGTH;
        efFileAttributes.SFID = ID_EF_CARD_INFO_SFID;
        efFileAttributes.fileID = ID_EF_CARD_INFO_FID;
        efFileAttributes.numOfRecs = 1;

        SMR smrCardInfo = new SMR();
        smrCardInfo.SMGroup_1 = SMR.SM_ENC_ENC; // READ BINARY
        smrCardInfo.SMGroup_2 = SMR.SM_ENC_PLAIN; // UPDATE BINARY
        smrCardInfo.SMGroup_3 = SMR.SM_MAC_ENC; // READ FILE ATTRIBUTE
        smrCardInfo.SMGroup_4 = SMR.SM_ENC_MAC; // UPDATE FILE ATTRIBUTE

        ART[] artCardInfo = new ART[KEY_NUM_ADF + 1];
        ART artAlways = new ART(0x00);
        ART artPersonalize = new ART(0x00);
        artPersonalize.ACGroup_1 = true;// READ BINARY
        artPersonalize.ACGroup_2 = true; // UPDATE BINARY
        artPersonalize.ACGroup_7 = true; // READ_FILE_ATTRIBUTE

        artCardInfo[0] = artAlways;
        artCardInfo[1] = artPersonalize;

        SecurityAttributes securityAttributes = new SecurityAttributes(smrCardInfo, artCardInfo);
        cipurseCardHandler.secureMessagingFlag = false;
        cipurseCardHandler.setupSecureChannel(keyNoPersonalize, keyADFPersonalize);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        ByteArray response = cipurseAdministration.createFileEF(efFileAttributes, securityAttributes);
        MessageUtil.handleError(response);

    }

    private void createADF() throws CipurseException {
        DFFileAttributes dfFileAttributes = new DFFileAttributes();
        dfFileAttributes.numOfKeys = KEY_NUM_ADF;
        dfFileAttributes.numOfSFIDs = 1;
        dfFileAttributes.appProfile = DFFileAttributes.PROFILE_L;
        dfFileAttributes.fileDescriptor = DFFileAttributes.ADF_FILE_TYPE;
        dfFileAttributes.numOfEFs = 1;
        dfFileAttributes.fileID = ID_ADF_SFID;
        dfFileAttributes.setAIDValue(new ByteArray(ID_ADF_AID));

        dfFileAttributes.encKeyNo = (short) 0x00;
        dfFileAttributes.tag86Presence = false;

        KeyAttributeInfo[] keySet = new KeyAttributeInfo[KEY_NUM_ADF];

        KeyAttributeInfo personalizeKeyInfo = new KeyAttributeInfo();
        personalizeKeyInfo.setKeyValue(keyADFPersonalize);
        personalizeKeyInfo.keyLength = keyLengthADFPersonalize;
        personalizeKeyInfo.keyAlgoId = keyAlgoADFPersonalize;
        personalizeKeyInfo.keyAddInfo = keyAddInfoADFPersonalize;
        personalizeKeyInfo.keySecAttrib = 0x02;

        keySet[0] = personalizeKeyInfo;

        SMR smrADf = new SMR();
        smrADf.SMGroup_1 = SMR.SM_PLAIN_PLAIN; // ACTIVE_FILE, DEACTIVE_FILE
        smrADf.SMGroup_2 = SMR.SM_PLAIN_PLAIN; // UPDATE_KEY, UPATE_KEY_ATTRIBUTES
        smrADf.SMGroup_3 = SMR.SM_PLAIN_PLAIN; // READ_FILE_ATTRIBUTES
        smrADf.SMGroup_4 = SMR.SM_PLAIN_PLAIN; // UPDATE FILE ATTRIBUTES

        ART[] artADF = new ART[KEY_NUM_ADF + 1];
        ART artAlways = new ART(0x00);
        ART artPersonalize = new ART(0x00);

        artAlways.ACGroup_7 = true; // READ_FILE_ATTRIBUTES
        artPersonalize.ACGroup_2 = true; // PERFORM_TRANSACTION
        artPersonalize.ACGroup_5 = true; // RFU - CREATE, DELETE FILE
        artADF[0] = artAlways;
        artADF[1] = artPersonalize;

        SecurityAttributes securityAttributes = new SecurityAttributes(smrADf, artADF);
        cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyMFPrivacyUpdated);
        cipurseCardHandler.secureMessagingFlag =true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_ENC;
        ByteArray response = cipurseAdministration.createFileADF(dfFileAttributes, securityAttributes, keySet);
        MessageUtil.handleError(response);


    }

    private void updateMFKeys() throws CipurseException {
        UpdateKeyInfo updateKeyInfo = new UpdateKeyInfo();
        updateKeyInfo.updateKeyNumber = keyNoPrivacy;
        updateKeyInfo.setUpdateKeyValues(keyMFPrivacyUpdated, null);
        updateKeyInfo.keyAddInfo = keyAddInfoMFPrivacy;
        updateKeyInfo.keyAlgoId = keyAlgoMFPrivacy;
        updateKeyInfo.keyLength = keyLengthMFPrivacy;

        ByteArray response= cipurseOperational.updateKey(updateKeyInfo);
        MessageUtil.handleError(response);
        cipurseOperational.performTransaction();
    }

    private void authenticateWithDefaultKey() throws CipurseException {
        cipurseOperational.selectMF();
        System.out.println("==== Start authentication with default key ");
        if(!cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyMFPrivacyDefault)) {
            return;
        }
    }



    private void updateCardInfo(byte[] data) throws CipurseException {
        cipurseCardHandler.reset(0);
        cipurseOperational.selectMF();
        cipurseCardHandler.secureMessagingFlag = false;
        cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyMFPrivacyUpdated);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        ByteArray response = cipurseOperational.selectFilebyAID(new ByteArray(ID_ADF_AID));
        MessageUtil.handleError(response);
        cipurseCardHandler.secureMessagingFlag = false;
        cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyADFPersonalize);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_PLAIN;
        response = cipurseOperational.selectFilebyFID(ID_EF_CARD_INFO_FID);
        MessageUtil.handleError(response);

        // Read file attribute
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_ENC_LE_PRESENT;
        response = cipurseOperational.readFileAttributes((short)0);
        System.out.println("File attribute: " + response);

//        smrCardInfo.SMGroup_1 = SMR.SM_ENC_ENC; // READ BINARY
//        smrCardInfo.SMGroup_2 = SMR.SM_ENC_PLAIN; // UPDATE BINARY
//        smrCardInfo.SMGroup_3 = SMR.SM_MAC_ENC; // READ FILE ATTRIBUTE
//        smrCardInfo.SMGroup_4 = SMR.SM_ENC_MAC; // UPDATE FILE ATTRIBUTE


        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_ENC_PLAIN;
        response = cipurseOperational.updateBinary((short)0, new ByteArray(data));
        MessageUtil.handleError(response);
    }
    public boolean isNotCardInitialized() throws CipurseException {
        boolean isNotInitialized = true;
        cipurseOperational.selectMF();
        try {
            if (!cipurseCardHandler.setupSecureChannel(keyNoPrivacy, keyMFPrivacyUpdated)) {
                return false;
            }

            cipurseCardHandler.secureMessagingFlag = true;
            cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
            ByteArray response = cipurseOperational.selectFilebyAID(new ByteArray(ID_ADF_AID));
            MessageUtil.handleError(response);
            isNotInitialized = false;

        } catch (CipurseException ex) {
            // still not initialized
        }
        return isNotInitialized;
    }
}
