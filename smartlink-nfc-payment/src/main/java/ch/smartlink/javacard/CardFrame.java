package ch.smartlink.javacard;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;


public class CardFrame extends JFrame {


    public void showSuccessMessage() {
        JOptionPane.showMessageDialog(this, "Write Card Successful");
    }
    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, "Write Card Error: " + message, "Smartlink Card", JOptionPane.OK_OPTION);
    }
    public boolean isUserAcceptFormatAndClearCardInfo() {
       int result = JOptionPane.showConfirmDialog(this, "Card will be formated. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION);
        return result == 0;
    }
    public Account requestInput() {
        Account account = new Account();
        String cardNumber = getCardNumber();
        if(cardNumber == null) {
            return null;
        }
        String expiryDate = getExpiryDate();
        if(expiryDate == null) {
            return  null;
        }
        BigDecimal balance = getBalance();

        if( balance == null) {
            return  null;
        }
        account.setBalance(balance);
        account.setCurrency("EUR");
        account.setExpiryDate(expiryDate);
        account.setCardNumber(cardNumber);
        return account;
    }

    private BigDecimal getBalance(){
        String s = (String)JOptionPane.showInputDialog(
                this,
                "Balance (#.##):",
                "Card Information",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        if(s==null||s.isEmpty()) {
            return null;
        }

        return new BigDecimal(s);
    }

    private String getExpiryDate(){
        String s = (String)JOptionPane.showInputDialog(
                this,
                "Expiry (mm-yy):",
                "Card Information",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        if(s==null||s.isEmpty()) {
            return null;
        }
        String expiried = s.replaceAll("[ -/]", "");
        if(expiried.length()!=4) {
            return getExpiryDate();
        }
        return expiried;
    }

    private String getCardNumber(){
        String s = (String)JOptionPane.showInputDialog(
                this,
                "Card Number (xxxx-xxxx-xxxx-xxxx):",
                "Card Information",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        if(s==null||s.isEmpty()) {
            return null;
        }
        String cardNumber = s.replaceAll("[ -]", "");
        if(cardNumber.length()!=16) {
            return getCardNumber();
        }
        return cardNumber;
    }

}
