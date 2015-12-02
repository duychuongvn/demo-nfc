package ch.smartlink.smartticketdemo.cipurse;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.IAes;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES implements IAes {
    private static Cipher aesCipher = null;
    private SecretKeySpec aesKey = null;

    public AES() throws CipurseException {
        try {
            aesCipher = Cipher.getInstance("AES/ECB/NoPadding");
        } catch (Exception var2) {
            throw new CipurseException(var2);
        }
    }

    public byte[] aesEncrypt(byte[] key, byte[] data) throws CipurseException {
        try {
            this.aesKey = new SecretKeySpec(key, "AES");
            aesCipher.init(1, this.aesKey);
            return aesCipher.doFinal(data);
        } catch (Exception var4) {
            throw new CipurseException(var4);
        }
    }

    public byte[] aesDecrypt(byte[] key, byte[] data) throws CipurseException {
        try {
            this.aesKey = new SecretKeySpec(key, "AES");
            aesCipher.init(2, this.aesKey);
            return aesCipher.doFinal(data);
        } catch (Exception var4) {
            throw new CipurseException(var4);
        }
    }
}