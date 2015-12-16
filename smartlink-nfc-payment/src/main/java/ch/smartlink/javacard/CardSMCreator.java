package ch.smartlink.javacard;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.ICommsChannel;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.commands.*;
import org.osptalliance.cipurse.impl.AES;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by caoky on 12/3/2015.
 */
public class CardSMCreator {
    private static final int COLD_RESET = 0;
    private ICommsChannel commsChannel;
    private ILogger logger;
    private CipurseCardHandler cipurseCardHandler;
    private ICipurseOperational cipurseOperational;
    private ICipurseAdministration cipurseAdministration;
    private ByteArray MF_PrivacyKey  = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73"); // Key 1.
    private ByteArray MF_CardMgmtKey = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73"); // Key 2.

    private ByteArray MF_UpdatePrivacyKey = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73"); // Key 1.
    private ByteArray MF_UpdateCardMgmtKey = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73"); // Key 2.
    private ByteArray ADF_AuthenticationKey = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73"); // Key 1.
    private ByteArray ADF_PersonalizedKey = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73"); // Key 2


//    private ByteArray MF_UpdatePrivacyKey = new ByteArray("THISISPRIVACYKEY".getBytes());
//    private ByteArray MF_UpdateCardMgmtKey = new ByteArray("THISISCARDMGNKEY".getBytes());
//    private ByteArray ADF_AuthenticationKey = new ByteArray("THAPPLICATIONKEY".getBytes());
//    private ByteArray ADF_PersonalizedKey = new ByteArray("THPERSONALIZEKEY".getBytes());

    private short keyNumberPrivacy = 0x01;
    private short keyNumberCardMgnt = 0x02;
    private short keyNumberApplication = 0x01;
    private short keyNumberPerso = 0x02;

    private void a() throws CipurseException {
        cipurseOperational.selectFilebyFID(1);
    }
    public CardSMCreator(ICommsChannel commsChannel, ILogger logger) {
        this.commsChannel = commsChannel;
        this.logger = logger;

    }

    public void setupCard() throws CipurseException {
        initCommand();
        System.out.println("------------ Restore Sample Card -------------");

        ByteArray baAtr = cipurseCardHandler.reset(COLD_RESET );
        System.out.println("ATR after default reset received : "+ baAtr);

        // Select MF
        ByteArray response = cipurseOperational.selectMF();
        errorHandler(response, new ByteArray("90 00"));

        System.out.println("Setup secure channel with card management key:");
        if(!cipurseCardHandler.setupSecureChannel((byte) 0x02, MF_CardMgmtKey))
            return;
//        if(!cipurseCardHandler.setupSecureChannel((byte) 0x02, MF_UpdateCardMgmtKey))
//            return;
        System.out.println("Authentication MF_CardMgmtKey");
//        cipurseCardHandler.secureMessagingFlag = true;
//        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_PLAIN_PLAIN;
        // Format ALL
//        response = cipurseAdministration.formatAll();
//        errorHandler(response, new ByteArray("90 00"));

      //  updateMFKeys();

        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC_LE_PRESENT;
        System.out.println(" > Setup secure channel with Card management key.");
        if(!cipurseCardHandler.setupSecureChannel((byte) keyNumberCardMgnt, MF_CardMgmtKey))
            return;

        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;

//        System.out.println("Create ADF");
//        createADF();
//

        response = cipurseOperational.selectFilebyAID(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
        handleError(response);

//        createEF_5F02();
//        response = cipurseOperational.selectFilebyAID(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
//        handleError(response);


        System.out.println("Read EF_5F02");
        cipurseCardHandler.secureMessagingFlag = false;
        if(!cipurseCardHandler.setupSecureChannel((byte) keyNumberApplication, ADF_AuthenticationKey)) {
            return;
        }

        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_ENC;
        response = cipurseOperational.selectFilebyFID( (short)0x5F02);

        System.out.println("Select 5F02: " + response);

        cipurseCardHandler.secureMessagingFlag = false;
        if(!cipurseCardHandler.setupSecureChannel((byte) keyNumberPerso, ADF_PersonalizedKey)) {
            return;
        }
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_ENC;

        CardInfo cardInfo = new CardInfo(UUID.randomUUID().toString(), new BigDecimal("10000"), "EUR");
       // cardInfo.setBalance(BigDecimal.TEN);
        byte[]data = new byte[50];
        byte[]cardInfoBytes = new byte[]{0x10, 0x11,0x12, 0x13};
        System.out.println("Card info size: " + cardInfoBytes.length);
        System.out.println("Card Info byte: " + cardInfoBytes);
        System.arraycopy(cardInfoBytes, 0, data, 0, cardInfoBytes.length);
        response = cipurseOperational.updateBinary((short)0, new ByteArray(cardInfoBytes));

//       response = cipurseOperational.readBinary((short)0, (short)0);
        System.out.println("Read EF 5F02 Response: " + response);
        handleError(response);

        cipurseCardHandler.secureMessagingFlag = false;
        if(!cipurseCardHandler.setupSecureChannel((byte) keyNumberApplication, ADF_AuthenticationKey)) {
            return;
        }

     //   cipurseCardHandler.secureMessagingFlag = false;
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_ENC_LE_PRESENT;
        response = cipurseOperational.readBinary((short)0, (short)0);
        System.out.println("Read EF 5F02 Response: " + response);
        handleError(response);
//        System.out.println("Restore MF Keys");
//        response = cipurseOperational.selectMF();
//        if(!errorHandler(response, new ByteArray("90 00")))
//            return;
//
//        System.out.println("------------- RESTORE MF Keys ------------");
//
//        // Prepare Restore Key information for Update key.
//        UpdateKeyInfo restoreKeyInfo_MF = new UpdateKeyInfo();
//        restoreKeyInfo_MF.keyAddInfo = 0x01;
//        restoreKeyInfo_MF.keyLength = 0x10;
//        restoreKeyInfo_MF.keyAlgoId = 0x09;
//        restoreKeyInfo_MF.updateKeyNumber = 0x01; // Restore the Key number 1.
//        restoreKeyInfo_MF.encKeyNumber = 0x00; // Key is in PLAIN in the UPDATE_KEY command.
//        restoreKeyInfo_MF.updateKeyValue= new ByteArray("FB E0 AA 29 65 54 92 64 D1 C8 4B B1 54 C1 56 A4").getBytes();
//        cipurseCardHandler.secureMessagingFlag = false;
//        System.out.println("Setup secure channel with card management key:");
//        if(!cipurseCardHandler.setupSecureChannel((byte) 0x02, MF_CardMgmtKey))
//            return;
//
//        cipurseCardHandler.secureMessagingFlag = true;
//        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
//        cipurseOperational.updateKey(restoreKeyInfo_MF);
//        cipurseOperational.performTransaction();
    }


    private void createEF_5F02() throws CipurseException {


        EFFileAttributes efFileAttributes = new EFFileAttributes();
       // efFileAttributes.numOfKeys = 3;
        efFileAttributes.fileID = 0x5F02;
        efFileAttributes.fileSize = 50;
        efFileAttributes.numOfRecs = 1;
        efFileAttributes.SFID= 0x00;
        efFileAttributes.fileType = EFFileAttributes.BINARY_FILE_TYPE;

        SMR smrEF5F02 = new SMR();
        smrEF5F02.SMGroup_1 = SMR.SM_PLAIN_PLAIN; // READ BINARY
        smrEF5F02.SMGroup_2 = SMR.SM_PLAIN_PLAIN; // UPDATE_BINARY
        smrEF5F02.SMGroup_3 = SMR.SM_PLAIN_PLAIN; // READ_FILE_ATTRIBUTES
        smrEF5F02.SMGroup_4 = SMR.SM_PLAIN_PLAIN; // UPDATE_FILE_ATTRIBUTES

        ART[] artEF_5F02 = new ART[2 + 1];

        ART artAlways = new ART(0x00);
        ART artAuthenticationKey = new ART(0x00);
        ART artPersoKey = new ART(0x00);

        artEF_5F02[0] = artAlways;
        artEF_5F02[1] = artAuthenticationKey;
        artEF_5F02[2] = artPersoKey;
        artAuthenticationKey.ACGroup_1 = true; // READ_BINARY
        artPersoKey.ACGroup_2 = true; // UPDATE BINARY
        artAuthenticationKey.ACGroup_7 = true; // READ FILE ATTRIBUTES
        SecurityAttributes securityAttributes = new SecurityAttributes(smrEF5F02, artEF_5F02);

        System.out.println("Setup secure channel with perso key.");
        if(!cipurseCardHandler.setupSecureChannel((byte) keyNumberPerso, ADF_PersonalizedKey))
            return;

        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_ENC_MAC;
        ByteArray response = cipurseAdministration.createFileEF(efFileAttributes, securityAttributes);
        handleError(response);
        cipurseOperational.performTransaction();

    }
    private void updateMFKeys() throws CipurseException {
        UpdateKeyInfo keyAttributeInfo = new UpdateKeyInfo();
        keyAttributeInfo.setUpdateKeyValues(MF_UpdatePrivacyKey, null);
        cipurseCardHandler.secureMessagingFlag = false;
        System.out.println("Setup secure channel with card privacy key:");
        if(!cipurseCardHandler.setupSecureChannel((byte) 0x01, MF_PrivacyKey))
            return;

        updateKey(keyNumberPrivacy, MF_UpdatePrivacyKey);
        cipurseOperational.performTransaction();
        System.out.println("Setup secure channel with card privacy key:");
        if(!cipurseCardHandler.setupSecureChannel((byte) 0x02, MF_CardMgmtKey))
            return;
        updateKey(keyNumberCardMgnt, MF_UpdateCardMgmtKey);
        cipurseOperational.performTransaction();
    }

    private void createADF() throws CipurseException {
        System.out.println("############ CREATE ADF USING Card MNGT KEY ###########");
        System.out.println("Setup secure channel with card MF_UpdateCardMgmtKey:");

        DFFileAttributes dfFileAttributes = new DFFileAttributes();
        dfFileAttributes.fileDescriptor = DFFileAttributes.ADF_FILE_TYPE;
        dfFileAttributes.numOfEFs = 2;
        dfFileAttributes.fileID = Constant.ID_FILE_ADF_SHORT;
        dfFileAttributes.appProfile = DFFileAttributes.PROFILE_S;
        dfFileAttributes.numOfKeys = 2;
        dfFileAttributes.numOfSFIDs = 2;
        dfFileAttributes.aidValue = new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET).getBytes();

        dfFileAttributes.encKeyNo = (short) 0x00;
        dfFileAttributes.tag86Presence = false;

        KeyAttributeInfo[] keySet = new KeyAttributeInfo[dfFileAttributes.numOfKeys];
        ByteArray[] keys = new ByteArray[]{ADF_AuthenticationKey, ADF_PersonalizedKey};
        for (int i = 0; i < dfFileAttributes.numOfKeys; i++) {
            KeyAttributeInfo keyAttributeInfo = new KeyAttributeInfo();
            keyAttributeInfo.keyAddInfo = 0x01; //version
            keyAttributeInfo.keyAlgoId = 0x09;
            keyAttributeInfo.keyLength = 0x10;
            keyAttributeInfo.setKeyValue(keys[i]);
            keyAttributeInfo.keySecAttrib = 0x02;
            keySet[i] = keyAttributeInfo;

        }

        SMR smrADF = new SMR();
        smrADF.SMGroup_1 = SMR.SM_PLAIN_PLAIN; // ACTIVE ADF, DEACTIVE ADF, DELETE ADF OR ER
        smrADF.SMGroup_2 = SMR.SM_PLAIN_PLAIN; // UPDATE KEY, UPDATE KEY ATTRIBUTE
        smrADF.SMGroup_3 = SMR.SM_PLAIN_PLAIN; // READ FILE ATTRIBUTE
        smrADF.SMGroup_4 = SMR.SM_PLAIN_PLAIN; // UPDATE FILE ATTRIBUTE, CREATE FILE

        ART[] artADF = new ART[dfFileAttributes.numOfKeys + 1];
        ART always = new ART(0x00);
        ART artAuthenticationKey = new ART(0x00);
        ART artPersoKey = new ART(0x00);

        always.ACGroup_7 = true; // READ_FILE_ATTRIBUTES;
        artPersoKey.ACGroup_2 = true;// PERFORM TRANSACTION;
        artPersoKey.ACGroup_5 = true;// CREATE, DELETE FILE
        artAuthenticationKey.ACGroup_1 = true; // Update key

        artADF[0] = always;
        artADF[1] = artAuthenticationKey;
        artADF[2] = artPersoKey;

        SecurityAttributes securityAttributes = new SecurityAttributes(smrADF, artADF);
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        ByteArray response = cipurseAdministration.createFileADF(dfFileAttributes, securityAttributes, keySet);
        handleError(response);


    }

    private void updateKey(short keyNumber, ByteArray keyValue) throws CipurseException {
        UpdateKeyInfo keyAttributeInfo = new UpdateKeyInfo();
        keyAttributeInfo.setUpdateKeyValues(keyValue, null);
        keyAttributeInfo.keyLength = 0x10;
        keyAttributeInfo.keyAlgoId = 0x09;
        keyAttributeInfo.keyAddInfo = 0x01; // any - version
        keyAttributeInfo.updateKeyNumber = keyNumber;
        cipurseOperational.updateKey(keyAttributeInfo);

    }

    private void handleError(ByteArray response) throws CipurseException {
        errorHandler(response, new ByteArray("90 00"));
    }
    static boolean errorHandler(ByteArray receivedStatus, ByteArray expectedStatus) throws CipurseException
    {
        ByteArray recStatus = new ByteArray(receivedStatus.getBytes());
        ByteArray expStatus = new ByteArray(expectedStatus.getBytes());

        int recSize = recStatus.size();
        if(recSize > 2)
        {
            recStatus = recStatus.subArray(recSize-2, recSize);
        }

        System.out.println("Received Status: " + recStatus);
        System.out.println("Expected Status: " + expStatus);

        boolean result = false;
        result = recStatus.equals(expStatus);

        if(result)
            System.out.println("Expected result MATCHED");
        else
        {
            System.out.println("************************************************************");
            System.out.println("-------------- Expected result does NOT MATCH --------------");
            System.out.println("-------------- Script Execution Terminated    --------------");
            System.out.println("************************************************************");
            throw new CipurseException("Expected result does NOT MATCH");
        }
        return result;
    }


//    private UpdateKeyInfo createKeyInfo() {
//        UpdateKeyInfo updateKeyInfo = new UpdateKeyInfo();
//        updateKeyInfo.setUpdateKeyValues();
//    }
    private void initCommand() throws CipurseException {
        cipurseCardHandler = new CipurseCardHandler(commsChannel, new AES(), logger);
        CommandAPI cmdApi = CommandAPIFactory.getInstance().buildCommandAPI();
        cmdApi.setVersion(CommandAPI.Version.V3);
        cipurseAdministration = cmdApi.getCipurseAdministration(cipurseCardHandler);
        cipurseOperational = cmdApi.getCipurseOperational(cipurseCardHandler);

        cipurseCardHandler.open();

    }


}
