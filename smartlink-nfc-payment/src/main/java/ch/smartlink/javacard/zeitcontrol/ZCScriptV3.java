package ch.smartlink.javacard.zeitcontrol;

import ch.smartlink.javacard.MessageUtil;
import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.IAes;
import org.osptalliance.cipurse.ICommsChannel;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.commands.*;
import org.osptalliance.cipurse.commands.internal.CipurseOperational;
import org.osptalliance.cipurse.impl.Logger;

/**
 * Created by caoky on 12/23/2015.
 */
public class ZCScriptV3 {

    private static final String AID_PMSE = "A0 00 00 05 07 06 00";
    private static final String AID_PASE = "A0 00 00 05 07 02 00";
    private static final String AID_APP_PAY = "D2 76 00 00 02 80 02 00 00 00 00 00 00 00 02 00";
    private static final String AID_APP_ACCESS = "D2 76 00 00 02 80 02 00 00 00 00 00 00 00 02 01";

    private static final short FID_PXSE_MIRCO_PAYMENT = 0x0001;
    private static final short FID_ADF_MIRCO_PAYMENT = 0x1001;

    private static final short FID_EF_MIRCO_PAYMENT_VALUE = 0x2001;
    private static final short SFID_EF_MIRCO_PAYMENT_VALUE = 0x01;

    private static final short FID_EF_MIRCO_PAYMENT_LOG = 0x2002;
    private static final short SFID_EF_MIRCO_PAYMENT_LOG = 0x02;


    private static final short FID_PXSE_FACILITY_ACCESS = 0x0002;
    private static final short FID_ADF_FACILITY_ACCESS = 0x1002;

    private static final short FID_EF_FACILITY_ACCESS_CONTROL = 0x3007;
    private static final short SFID_EF__FACILITY_ACCESS_CONTROL = 0x07;

    private static final byte keyNumMF = 0x01;
    private static final byte keyNumADF1 = 0x01;
    private static final byte keyNumADF2 = 0x02;

    private static final ByteArray KEY_DEFAULT_MF = new ByteArray("73 73 73 73 73 73 73 73 73 73 73 73 73 73 73 73");
    private static final ByteArray KEY_VALUE_1    = new ByteArray("80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F");
    private static final ByteArray KEY_VALUE_2    = new ByteArray("90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F");

    private ICipurseAdministration cipurseAdministration;
    private ICipurseOperational cipurseOperational;
    private CipurseCardHandler cipurseCardHandler;
    private ILogger logger;
    private ICommsChannel commsChannel;
    private IAes aes;

    // MF1 key KEY_DEFAULT_MF
    // MF 2 key KEY_DEFAULT_MF
    // ADF 1 key KEY_VALUE_1
    // ADF 2 key KEY_VALUE_2
    public ZCScriptV3(ICommsChannel commsChannel, IAes aes, Logger logger) throws CipurseException {

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


    private void coldReset() throws CipurseException {
        cipurseCardHandler.reset(0);
    }

    private void logInfo(String message, byte[] bytes) {
        logger.log(2, message, bytes);
    }
    private void logInfo(String message) {
        logger.log(2, message);
    }

    public void readAdf1() throws CipurseException {
        connect();
        coldReset();
        cipurseOperational.selectMF();
        cipurseCardHandler.secureMessagingFlag = false;

        logInfo("=== Start authenticate with default key === ");
        cipurseCardHandler.setupSecureChannel(keyNumMF, KEY_DEFAULT_MF);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        logInfo("=== Select Micro Application === ");
        ByteArray response = cipurseOperational.selectFilebyAID(new ByteArray(AID_APP_PAY));
        MessageUtil.handleError(response);

        cipurseCardHandler.secureMessagingFlag = false;

        logInfo("=== Authenticate with  key 1 === ");
        cipurseCardHandler.setupSecureChannel(keyNumADF2, KEY_VALUE_2);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        logInfo("=== Select Mirco value EF ===");
        response = cipurseOperational.selectFilebyFID(FID_EF_MIRCO_PAYMENT_VALUE);
        MessageUtil.handleError(response);

        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC_LE_PRESENT;
        logInfo("=== Read Mirco value EF ===");
        response = cipurseOperational.readRecord(SFID_EF_MIRCO_PAYMENT_VALUE, (short) 0x01, (byte)04, (short)0x0C);
        logInfo(" Micro value: " + response);


        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        response = cipurseOperational.selectFilebyFID(FID_EF_MIRCO_PAYMENT_LOG);
        MessageUtil.handleError(response);
        logInfo("=== Read Mirco Log EF ===");
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_ENC_LE_PRESENT;
       // cipurseOperational.appendRecord(new ByteArray(new byte[20]));
        response = cipurseOperational.readRecord((short) 0x01, (byte)04, (short)20);
        logInfo(" Log Record 1: " + response);



    }


    public void readAdf2() throws CipurseException {
        connect();
        coldReset();
        cipurseOperational.selectMF();
        cipurseCardHandler.secureMessagingFlag = false;

        logInfo("=== Start authenticate with default key === ");
        cipurseCardHandler.setupSecureChannel(keyNumMF, KEY_DEFAULT_MF);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        logInfo("=== Select Micro Application === ");
        ByteArray response = cipurseOperational.selectFilebyAID(new ByteArray(AID_APP_ACCESS));
        MessageUtil.handleError(response);

        cipurseCardHandler.secureMessagingFlag = false;

        logInfo("=== Authenticate with  key 1 === ");
        cipurseCardHandler.setupSecureChannel(keyNumADF2, KEY_VALUE_2);
        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC;
        logInfo("=== Select Mirco value EF ===");
        response = cipurseOperational.selectFilebyFID(FID_EF_FACILITY_ACCESS_CONTROL);
        MessageUtil.handleError(response);

        cipurseCardHandler.secureMessagingFlag = true;
        cipurseCardHandler.secureMessagingIndicator = CipurseCardHandler.SMI_MAC_MAC_LE_PRESENT;
        logInfo("=== Read Mirco value EF ===");
        response = cipurseOperational.readRecord(SFID_EF__FACILITY_ACCESS_CONTROL, (short) 0x01, (byte)0x04, (short)0x0C);
        logInfo(" Micro value: " + response);


    }
}

