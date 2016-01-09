package ch.smartlink.javacard;

import javacard.security.Key;

/**
 * Created by caoky on 1/9/2016.
 */
public class HrsKey implements Key {

    private boolean initialized;
    private byte[] keyValue;
    private byte type = 0x09; // AES
    private byte size = 0x10;
    public HrsKey(byte[] keyValue) {
        this.keyValue = keyValue;
        this.initialized = true;
    }

    public void initKey(byte[] keyValue) {
        this.keyValue = keyValue;
        this.initialized = true;
    }
    @Override
    public void clearKey() {
        this.keyValue = null;
        this.initialized = false;
    }

    @Override
    public short getSize() {
        return this.size;
    }

    @Override
    public byte getType() {
        return this.type;
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    public byte[] getKeyValue() {
        return keyValue;
    }
}
