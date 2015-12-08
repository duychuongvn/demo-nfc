package ch.smartlink.javacard;


import java.math.BigDecimal;

public class CardInfo {

    private String walletId;
    private BigDecimal balance;
    private String currency;

    public CardInfo(String walletId, BigDecimal balance, String currency) {
        this.walletId = walletId;
        this.balance = balance;
        this.currency = currency;
    }
    public String getWalletId() {
        return walletId;
    }

    public static CardInfo parseData(byte[] cardInfoInBytes) {
        String plainData = new String(cardInfoInBytes);
        String[] dataFragments = plainData.trim().split(" ");
        String walletId = dataFragments[0];
        BigDecimal amount = new BigDecimal(dataFragments[1]);
        String currency = dataFragments[2];
        return new CardInfo(walletId, amount, currency);
    }

    public byte[] toBytes() {
        StringBuilder cardInfoBuider = new StringBuilder();
        cardInfoBuider.append(this.walletId).append(" ");
        cardInfoBuider.append(MessageUtil.formatBalanceToStore(this.balance)).append(" ");
        cardInfoBuider.append(this.currency);
        return cardInfoBuider.toString().getBytes();

    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
