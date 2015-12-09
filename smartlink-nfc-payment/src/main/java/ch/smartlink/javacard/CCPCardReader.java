package ch.smartlink.javacard;


import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.ICommsChannel;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.commands.*;

public class CCPCardReader {

    private static final int COLD_RESET = 0;
    private ICommsChannel commsChannel;
    private ILogger logger;
    private CipurseCardHandler cipurseCardHandler;
    private ICipurseOperational cipurseOperational;
    private ICipurseAdministration cipurseAdministration;

    public CCPCardReader(ICommsChannel commsChannel, ILogger logger) {
        this.commsChannel = commsChannel;
        this.logger = logger;

    }

    private void initCommand() throws CipurseException {
        cipurseCardHandler = new CipurseCardHandler(commsChannel, null, logger);
        CommandAPI cmdApi = CommandAPIFactory.getInstance().buildCommandAPI();
        cmdApi.setVersion(CommandAPI.Version.V2);
        cipurseOperational = cmdApi.getCipurseOperational(cipurseCardHandler);
        cipurseAdministration = cmdApi.getCipurseAdministration(cipurseCardHandler);
        cipurseCardHandler.open();
    }

    public void initCard(CardInfo cardInfo) throws CipurseException {
        installApplication();
        storeCardInfo(cardInfo);
    }

    private void storeCardInfo(CardInfo cardInfo) throws CipurseException {
        cipurseOperational.selectMF();
        ByteArray response = cipurseOperational.selectFilebyAID(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
        handleError(response);
        response = cipurseOperational.selectFilebyFID(Constant.ID_FILE_CARD_INFO);
        handleError(response);
        response = cipurseOperational.updateBinary((short) 0, new ByteArray(cardInfo.toBytes()));
        handleError(response);
    }

    public void installApplication() throws CipurseException {
        initCommand();
        System.out.println("Format card...");
        cipurseAdministration.formatAll();
        System.out.println("Select MF...");
        cipurseOperational.selectMF();
        System.out.println("Create ADF...");
        createADF();
        System.out.println("Select ADF...");
        cipurseOperational.selectFilebyAID(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
        System.out.println("Create card info...");
        createFileCardInfo();
        System.out.println("Select ADF...");
        cipurseOperational.selectFilebyAID(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
        System.out.println("Create Transaction...");
        createFileCardTransaction();
    }

    private void createFileCardTransaction() throws CipurseException {
        EFFileAttributes efFileAttributes = new EFFileAttributes();
        efFileAttributes.fileID = Constant.ID_FILE_CARD_HISTORY;
        efFileAttributes.fileType = 0x06;
        efFileAttributes.numOfRecs = Constant.TOTAL_RECORD_CARD_TRANSACTION;
        efFileAttributes.RecSize = Constant.LENGH_CARD_TRANSACTION_BIN;
        efFileAttributes.SFID = 0x00;

        ByteArray response = cipurseAdministration.createFileEF(efFileAttributes);
        handleError(response);
        response = cipurseOperational.performTransaction();
        handleError(response);
    }

    private void createFileCardInfo() throws CipurseException {
        EFFileAttributes efFileAttributes = new EFFileAttributes();
        efFileAttributes.fileID = Constant.ID_FILE_CARD_INFO;
        efFileAttributes.fileType = EFFileAttributes.BINARY_FILE_TYPE;
        efFileAttributes.numOfRecs = 1;
        efFileAttributes.fileSize = Constant.LENGH_CARD_DATA_BIN;
        efFileAttributes.SFID = 0x01;
        ByteArray response = cipurseAdministration.createFileEF(efFileAttributes);
        handleError(response);
        cipurseOperational.performTransaction();
    }

    private void createADF() throws CipurseException {
        DFFileAttributes smartlinkDFAttribute = new DFFileAttributes();
        smartlinkDFAttribute.appProfile = DFFileAttributes.PROFILE_S;
        smartlinkDFAttribute.fileID = Constant.ID_FILE_ADF_SHORT;
        smartlinkDFAttribute.numOfEFs = 5;
        smartlinkDFAttribute.numOfSFIDs = 5;
        smartlinkDFAttribute.fileDescriptor = DFFileAttributes.ADF_FILE_TYPE;
        smartlinkDFAttribute.setAIDValue(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
        ByteArray response = cipurseAdministration.createFileADF(smartlinkDFAttribute);
        handleError(response);

    }

    private void handleError(ByteArray response) throws CipurseException {
        errorHandler(response, new ByteArray("90 00"));
    }

    private boolean errorHandler(ByteArray receivedStatus, ByteArray expectedStatus) throws CipurseException {
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
            throw new CipurseException("Expected result does NOT MATCH");
        }
        return result;
    }
}
