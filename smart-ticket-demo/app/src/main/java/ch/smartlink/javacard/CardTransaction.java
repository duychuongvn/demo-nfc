package ch.smartlink.javacard;
import java.math.BigDecimal;

import ch.smartlink.javacard.CardOperation;
import ch.smartlink.javacard.MessageUtil;

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
    private String walletId;

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
        this.walletId = walletId;
    }

    public byte[] toBytes() {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(this.getTimeInMiliSeconds()).append(" ");
        dataBuilder.append(this.authorization).append(" ");
        dataBuilder.append(this.id).append(" ");
        dataBuilder.append(MessageUtil.formatBalanceToStore(this.remainBalance)).append(" ");
        dataBuilder.append(MessageUtil.formatBalanceToStore(this.amount)).append(" ");
        dataBuilder.append(this.currency).append(" ");
        dataBuilder.append(this.type).append(" ");
        dataBuilder.append(MessageUtil.leftZeroPadding(this.location, CardOperation.LENGTH_LOCATION)).append(" ");
        dataBuilder.append(MessageUtil.leftZeroPadding(this.merchant, CardOperation.LENGTH_MERCHANT));
        return dataBuilder.toString().getBytes();
    }
    public static CardTransaction parseCardTransaction(String walletId, byte[] cardTransactionInBytes) {
        String data = new String(cardTransactionInBytes);
        String[] dataFragment = data.trim().split(" ");
        int columnIndex = 0;
        long timeInMiliSeconds = new Long(dataFragment[columnIndex++]);
        String authorization = dataFragment[columnIndex++];
        String id = dataFragment[columnIndex++];
        BigDecimal remainBalance = new BigDecimal(dataFragment[columnIndex++]);
        BigDecimal amount = new BigDecimal(dataFragment[columnIndex++]);
        String currency = dataFragment[columnIndex++];
        String type = dataFragment[columnIndex++];
        String location = dataFragment[columnIndex++];
        String merchant = dataFragment[columnIndex++];
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

    public String getWalletId() {
        return walletId;
    }
}
