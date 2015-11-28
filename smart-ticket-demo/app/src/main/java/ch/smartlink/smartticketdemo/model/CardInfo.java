package ch.smartlink.smartticketdemo.model;


import java.math.BigDecimal;

public class CardInfo {
    private String cardNumber;
    private String expiryDate;
    private String currency;
    private BigDecimal balance;

    public CardInfo(String cardNumber, String expiryDate, String currency, BigDecimal balance) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.currency = currency;
        this.balance = balance;
    }
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
