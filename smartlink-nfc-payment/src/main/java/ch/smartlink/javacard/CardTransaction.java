package ch.smartlink.javacard;

import java.math.BigDecimal;
import java.util.Calendar;

public class CardTransaction {
    private long timeInMiliSeconds;
    private String id;
    private String authorization;
    private String location;
    private String merchant;
    private BigDecimal amount;
    private BigDecimal remainBalance;
    private String currency;
    private String type;
    private String waletId;

    public CardTransaction(String walletId, long timeInMiliSeconds, String id, String authorization, String location, String merchant,
                           BigDecimal amount, BigDecimal remainBalance, String currency, String type) {
        this.timeInMiliSeconds = timeInMiliSeconds;
        this.id = id;
        this.authorization = authorization;
        this.location = location;
        this.merchant = merchant;
        this.amount = amount;
        this.remainBalance = remainBalance;
        this.currency = currency;
        this.type = type;
        this.waletId = walletId;
    }

    public byte[] toBytes() {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(this.getTimeInMiliSeconds()).append(Constant.SPLITTER);
        dataBuilder.append(this.authorization).append(Constant.SPLITTER);
        dataBuilder.append(this.id).append(Constant.SPLITTER);
        dataBuilder.append(MessageUtil.formatBalanceToStore(this.remainBalance)).append(Constant.SPLITTER);
        dataBuilder.append(MessageUtil.formatBalanceToStore(this.amount)).append(Constant.SPLITTER);
        dataBuilder.append(this.currency).append(Constant.SPLITTER);
        dataBuilder.append(this.type).append(Constant.SPLITTER);
        dataBuilder.append(MessageUtil.leftSpacePadding(this.location, CardOperation.LENGTH_LOCATION)).append(Constant.SPLITTER);
        dataBuilder.append(MessageUtil.leftSpacePadding(this.merchant, CardOperation.LENGTH_MERCHANT));
        return dataBuilder.toString().getBytes();
    }

    public static void main(String[] args) {
        String data = "1449658990121:::05D14E381:::71709981:::0001000000.00:::0001000000.00:::EUR:::0:::             Default:::              Mobile";

        CardTransaction cardTransaction = CardTransaction.parseCardTransaction("sasa", data.getBytes());

        System.out.println(cardTransaction.getMerchant());
    }

    private String getFragment(String[] dataFragment, int columnIndex) {
        return dataFragment[columnIndex].trim();
    }
    public static CardTransaction parseCardTransaction(String walletId, byte[] cardTransactionInBytes) {
        String data = new String(cardTransactionInBytes);
        String[] dataFragment = data.trim().split(Constant.SPLITTER);
        int columnIndex = 0;
        long timeInMiliSeconds = new Long(dataFragment[columnIndex++]);
        String authorization = dataFragment[columnIndex++];
        String id = dataFragment[columnIndex++];
        BigDecimal remainBalance = new BigDecimal(dataFragment[columnIndex++]);
        BigDecimal amount = new BigDecimal(dataFragment[columnIndex++]);
        String currency = dataFragment[columnIndex++];
        String type = dataFragment[columnIndex++];
        String location = dataFragment[columnIndex++].trim();
        String merchant = dataFragment[columnIndex++].trim();
        return new CardTransaction(walletId, timeInMiliSeconds, id, authorization, location, merchant, amount, remainBalance, currency, type);



    }
    public String getId() {
        return id;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getLocation() {
        return location;
    }

    public String getMerchant() {
        return merchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getRemainBalance() {
        return remainBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public long getTimeInMiliSeconds() {
        return timeInMiliSeconds;
    }

    public String getType() {
        return type;
    }
}
