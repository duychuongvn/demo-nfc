package ch.smartlink.smartticketdemo.control;


import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.ICommsChannel;
import org.osptalliance.cipurse.ILogger;
import org.osptalliance.cipurse.commands.ByteArray;
import org.osptalliance.cipurse.commands.CipurseCardHandler;
import org.osptalliance.cipurse.commands.CommandAPI;
import org.osptalliance.cipurse.commands.CommandAPIFactory;
import org.osptalliance.cipurse.commands.ICipurseAdministration;
import org.osptalliance.cipurse.commands.ICipurseOperational;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.CannotConnectNFCCardException;
import ch.smartlink.smartticketdemo.cipurse.CommsChannel;
import ch.smartlink.smartticketdemo.cipurse.Logger;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.model.LogModel;
import ch.smartlink.smartticketdemo.util.Constant;
import ch.smartlink.smartticketdemo.util.MessageUtil;

public class BaseCardOperationManager implements NfcAdapter.ReaderCallback {
    private ICommsChannel commsChannel;
    private ILogger logger;
    private CipurseCardHandler cipurseCardHandler;
    private ICipurseOperational cipurseOperational;
    private ICipurseAdministration cipurseAdministration;

    public BaseCardOperationManager(WeakReference<NfcRecordCallback> nfcRecordCallback) {
        this.nfcRecordCallback = nfcRecordCallback;
        this.logger = new Logger();
    }
    private IsoDep isoDep;
    private WeakReference<NfcRecordCallback> nfcRecordCallback;

    public interface NfcRecordCallback <T> {
        public void onNfcCardReceived(T data);
        public void onNfcCardError(String messageCode);
    }
    public void initCommand() throws CipurseException {
        cipurseCardHandler = new CipurseCardHandler(commsChannel, null, logger);
        CommandAPI cmdApi = CommandAPIFactory.getInstance().buildCommandAPI();
        cmdApi.setVersion(CommandAPI.Version.V2);
        cipurseOperational = cmdApi.getCipurseOperational(cipurseCardHandler);
        cipurseAdministration = cmdApi.getCipurseAdministration(cipurseCardHandler);
        cipurseCardHandler.open();
    }

    protected WeakReference<NfcRecordCallback> getNfcRecordCallback() {
        return this.nfcRecordCallback;
    }
    @Override
    public void onTagDiscovered(Tag tag) {
        this.commsChannel = new CommsChannel(tag);
        this.logger = new Logger();
    }



    protected void openApp() {
        readFileMF();
        selectADF();
    }

    protected void readFileMF() {
        try {
            cipurseOperational.selectMF();
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        }
    }
    protected void selectADF() {
        try {
            cipurseOperational.selectFilebyAID(new ByteArray(Constant.ID_ADF_SMARTLINK_TICKET));
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        }
    }

    protected void selectTransactionFile() {
        try {
            cipurseOperational.selectFilebyFID(Constant.ID_FILE_CARD_HISTROY);
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        };
    }

    protected String sendAndReceive(String command) {

        byte[] arrayOfByte = sendAndReceiveByte(command);
        return MessageUtil.byteArrayToHexString(arrayOfByte);

    }


    protected byte[] sendAndReceiveByte(String command) {

        try {
            byte[] result = cipurseCardHandler.transmit(new ByteArray(MessageUtil.hexStringToByteArray(command))).getBytes();
            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, resultLength - 2);
            if(Arrays.equals(statusWord, new byte[]{(byte) 0x90, (byte) 0x00})){
               return payload;
            }
            throw new AccessCardException("Response: " + MessageUtil.byteArrayToHexString(result));
        } catch (CipurseException e) {
            throw new AccessCardException("Cannot communicate with NFC Card: " + e.getMessage());
        }
    }

    protected ICipurseOperational getCipurseOperational() {
        return cipurseOperational;
    }

}
