package ch.smartlink.javacard;

import java.math.BigDecimal;

/**
 * Created by caoky on 12/8/2015.
 */
public class CardOperation {
    public static final int LENGTH_LOCATION = 20;
    public static final int LENGTH_MERCHANT = 20;
    private BigDecimal amount;
    private String merchant;
    private String location;

    public CardOperation(String merchant, String location, BigDecimal amount) {
        this.amount = amount;
        this.location = location;
        this.merchant = merchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getLocation() {
        return location;
    }
}
