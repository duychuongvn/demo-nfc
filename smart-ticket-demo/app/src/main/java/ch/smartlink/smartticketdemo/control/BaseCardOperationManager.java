package ch.smartlink.smartticketdemo.control;


import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.util.MessageUtil;

public class BaseCardOperationManager {
    private IsoDep isoDep;

    public BaseCardOperationManager(Tag tag) {

        isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            throw new IllegalStateException("Cannot get NFC Tag");
        }
        try {
            isoDep.connect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Cannot get NFC Tag");
        }
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
        String response = MessageUtil.byteArrayToHexString(arrayOfByte);
        Log.d(this.getClass().getName(), "data: " + response);
        return response;

    }

    protected byte[] sendAndReceiveByte(String command) {
        try {
            return isoDep.transceive(MessageUtil.hexStringToByteArray(command));

        } catch (IOException e) {
            throw new AccessCardException();
        }
    }
}
