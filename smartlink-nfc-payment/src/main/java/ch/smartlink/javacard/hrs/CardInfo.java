package ch.smartlink.javacard.hrs;


import java.util.Arrays;

public class CardInfo {
    private static final int LENGTH_WALLET_ID = 36;
    private static final int LENGTH_DOOR_ID = 4;
    private static final int LENGTH_EXPIRED_DATE = 8;
    private static final int LENGHT_LOCKED_STATUS = 1;
    public static final int FILE_LENGTH = 4 + LENGTH_WALLET_ID + LENGTH_DOOR_ID + LENGTH_EXPIRED_DATE + LENGHT_LOCKED_STATUS;

    private static int POS_WALLET = 0;
    private static int POS_DOOR_ID = 1;
    private static int POS_EXPIRED_DATE = 2;
    private static int POS_LOCKED = 3;

    private String walletId;
    private String  doorId;
    private String expiryDate;
    private boolean locked;


    public CardInfo(String walletId, String doorId, String expiryDate, boolean isLocked) {
        this.walletId = walletId;
        this.doorId = doorId;
        this.expiryDate = expiryDate;
        this.locked = isLocked;
    }

    public byte[] toBytes() {

        byte[] data = new byte[FILE_LENGTH];
        data[POS_WALLET] = (byte)walletId.length();
        data[POS_DOOR_ID] = (byte)doorId.length();
        data[POS_EXPIRED_DATE] = (byte)expiryDate.length();
        data[POS_LOCKED] = (byte)1;
        int dataIndex = 4;
        System.arraycopy(walletId.getBytes(), 0, data, dataIndex, walletId.length());
        dataIndex+= walletId.length();
        System.arraycopy(doorId.getBytes(), 0, data, dataIndex, doorId.length());
        dataIndex+=doorId.length();
        System.arraycopy(expiryDate.getBytes(), 0, data, dataIndex, expiryDate.length());
        dataIndex+=expiryDate.length();
        data[dataIndex] = locked? (byte) 0x01 : 0x00;
        return data;
    }

    public static CardInfo deserialize(byte[] dataBytes) {
        int walletLength = dataBytes[POS_WALLET];
        int doorLength = dataBytes[POS_DOOR_ID];
        int expiredLength = dataBytes[POS_EXPIRED_DATE];
        int lockStatusLength = 1;
        if(dataBytes.length != FILE_LENGTH) {
            throw  new IllegalStateException("Data not valid");
        }
        if(walletLength == 0) {
            throw  new IllegalStateException("This card is not initialed");
        }
        int dataIndex = 4;
        String walletId = new String(Arrays.copyOfRange(dataBytes, dataIndex, dataIndex + walletLength));
        dataIndex+=walletLength;
        String doorId =  new String(Arrays.copyOfRange(dataBytes, dataIndex, dataIndex + doorLength));
        dataIndex+=doorLength;
        String expiredDay = new String(Arrays.copyOfRange(dataBytes, dataIndex, dataIndex + expiredLength));
        dataIndex+=expiredLength;
        boolean isLocked = dataBytes[dataIndex] == 0x01? true : false;
        return new CardInfo(walletId, doorId, expiredDay, isLocked);
    }
    public String getWalletId() {
        return walletId;
    }

    public String getDoorId() {
        return doorId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public boolean isLocked() {
        return locked;
    }
}
