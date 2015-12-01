package ch.smartlink.smartticketdemo.control;


import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.CannotConnectNFCCardException;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.model.LogModel;
import ch.smartlink.smartticketdemo.util.MessageUtil;

public class BaseCardOperationManager  implements NfcAdapter.ReaderCallback{
    private IsoDep isoDep;
    private WeakReference<NfcRecordCallback> nfcRecordCallback;

    private static final List<LogModel> logModels = new ArrayList<>();
    public interface NfcRecordCallback <T> {
        public void onNfcCardReceived(T data);
        public void onNfcCardError(String messageCode);
    }

    public static void clearLog() {
        logModels.clear();
    }

    public static String getLogs() {
        StringBuilder logStringBuilder = new StringBuilder();
        for (LogModel logModel : logModels) {
            logStringBuilder.append(logModel.toString()).append("\n");
        }
        return logStringBuilder.toString();
    }

    protected WeakReference<NfcRecordCallback> getNfcRecordCallback() {
        return this.nfcRecordCallback;
    }
    @Override
    public void onTagDiscovered(Tag tag) {
        isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            getNfcRecordCallback().get().onNfcCardError("Cannot init IsoDep");
        }
        try {
            isoDep.connect();
        } catch (IOException e) {
            getNfcRecordCallback().get().onNfcCardError("Cannot Communicate with NFC Card");
        }
    }



    public BaseCardOperationManager(WeakReference<NfcRecordCallback> nfcRecordCallback) {

        this.nfcRecordCallback = nfcRecordCallback;
    }

    protected void openApp() {
        readFileMF();
        selectADF();
    }

    protected void readFileMF() {
        sendAndReceive("00A40000");
    }

    protected void selectADF() {
        sendAndReceive("00A404000DD2760000041502000003000101");
    }

    protected void selectTransactionFile() {
        sendAndReceive("00A40000023002");
    }

    protected String sendAndReceive(String command) {

        byte[] arrayOfByte = sendAndReceiveByte(command);
        return MessageUtil.byteArrayToHexString(arrayOfByte);

    }


    protected byte[] sendAndReceiveByte(String command) {
        LogModel logModel = new LogModel();
        logModels.add(logModel);
        try {
            logModel.setCommand(command);
            byte[] result = isoDep.transceive(MessageUtil.hexStringToByteArray(command));
            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, resultLength - 2);
            logModel.setResponse(MessageUtil.byteArrayToHexString(result));
            if(Arrays.equals(statusWord, new byte[]{(byte) 0x90, (byte) 0x00})){
               return payload;
            }
            throw new AccessCardException("Response: " + logModel.getResponse());
        } catch (IOException e) {
            logModel.setResponse("Cannot communicate with NFC Card: " + e.getMessage());
            throw new AccessCardException(logModel.getResponse());
        }
    }
}
