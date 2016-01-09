package ch.smartlink.javacard.cipurse;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.ICommsChannel;

import java.io.IOException;

import ch.smartlink.javacard.CannotConnectNFCCardException;

/**
 * Created by caoky on 12/2/2015.
 */
public class CommsChannel implements ICommsChannel {
    private IsoDep isoDep;

    public CommsChannel(Tag tag) {
        isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            throw new CannotConnectNFCCardException();
        }

    }

    @Override
    public byte[] transmit(byte[] bytes) throws CipurseException {
        try {
            return isoDep.transceive(bytes);

        }catch (IOException ex) {
            throw  new CipurseException(ex);
        }
    }

    @Override
    public void open() throws CipurseException {
        try {
            isoDep.connect();
        } catch (IOException ex) {
            throw  new CipurseException(ex);
        }
    }

    @Override
    public void close() throws CipurseException {
        try {
            isoDep.close();
        } catch (IOException ex) {
            throw  new CipurseException(ex);
        }
    }

    @Override
    public byte[] reset(int i) throws CipurseException {
        return new byte[0];
    }

    @Override
    public boolean isOpen() throws CipurseException {
        return false;
    }
}
