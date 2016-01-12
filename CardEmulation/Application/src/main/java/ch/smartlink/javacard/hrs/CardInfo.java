package ch.smartlink.javacard.hrs;


import java.util.Arrays;

public class CardInfo {
    private static final int LENGTH_DOOR_ID = 3;
    private static final int LENGTH_EXPIRED_DATE = 8;
    public static final int FILE_LENGTH = 2 + LENGTH_DOOR_ID + LENGTH_EXPIRED_DATE;


    private static int POS_DOOR_ID = 0;
    private static int POS_EXPIRED_DATE = 1;
    private String  doorId;
    private String expiryDate;


    public CardInfo(String doorId, String expiryDate) {
        this.doorId = doorId;
        this.expiryDate = expiryDate;
    }

    public byte[] toBytes() {

        byte[] data = new byte[FILE_LENGTH];
        data[POS_DOOR_ID] = (byte)doorId.length();
        data[POS_EXPIRED_DATE] = (byte)expiryDate.length();
        int dataIndex = 2;
        System.arraycopy(doorId.getBytes(), 0, data, dataIndex, doorId.length());
        dataIndex+=doorId.length();
        System.arraycopy(expiryDate.getBytes(), 0, data, dataIndex, expiryDate.length());
        return data;
    }

    public static CardInfo deserialize(byte[] dataBytes) {

        int doorLength = dataBytes[POS_DOOR_ID];
        int expiredLength = dataBytes[POS_EXPIRED_DATE];
        if(dataBytes.length != FILE_LENGTH) {
            throw  new IllegalStateException("Data not valid");
        }

        int dataIndex = 2;
        String doorId =  new String(Arrays.copyOfRange(dataBytes, dataIndex, dataIndex + doorLength));
        dataIndex+=doorLength;
        String expiredDate = new String(Arrays.copyOfRange(dataBytes, dataIndex, dataIndex + expiredLength));
        return new CardInfo(doorId, expiredDate);
    }

    public String getDoorId() {
        return doorId;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}
