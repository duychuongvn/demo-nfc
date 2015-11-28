package ch.smartlink.smartticketdemo.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import ch.smartlink.smartticketdemo.util.MessageUtil;

/**
 * Created by caoky on 11/26/2015.
 */
public class CardTransaction {
    private long time;
    private BigDecimal currentBalance;
    private BigDecimal amount;
    private String operationType;
    private String currency;
    public CardTransaction(long time, BigDecimal currentBalance, BigDecimal amount, String currency, String operationType) {
        this.time = time;
        this.currentBalance = currentBalance;
        this.amount = amount;
        this.currency = currency;
        this.operationType = operationType;
    }

    @Override
    public String toString() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.time);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(simpleDateFormat.format(calendar.getTime()));
        stringBuilder.append(" ");
        stringBuilder.append(this.currentBalance.setScale(2).toPlainString()).append(" ");
        stringBuilder.append(this.amount.setScale(2).toPlainString()).append(" ").append(currency);

        return stringBuilder.toString();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
