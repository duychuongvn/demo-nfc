package ch.smartlink.smartticketdemo.control;

import android.nfc.Tag;

import java.math.BigDecimal;
import java.util.Calendar;

import ch.smartlink.smartticketdemo.model.CardInfo;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.util.Constant;
import ch.smartlink.smartticketdemo.util.MessageUtil;


public class CardOperationManager extends BaseCardOperationManager {
    public CardOperationManager(Tag tag) {
        super(tag);
    }

    public CardInfo getCardInfo() {
        openApp();
        selectFileAccount();
        return readAccount();
    }

    public void doCredit(BigDecimal amount, CardInfo cardInfo) {
        CardTransaction cardTransaction = new CardTransaction(Calendar.getInstance().getTimeInMillis(),
                cardInfo.getBalance(), amount, cardInfo.getCurrency(), Constant.OPERATION_CREDIT);
        cardInfo.setBalance(cardInfo.getBalance().add(amount));

        updateBalance(MessageUtil.formatBalanceToStore(cardInfo.getBalance()));
        storeTransaction(cardTransaction);
    }
    public void doDebit(BigDecimal amount, CardInfo cardInfo) {
        CardTransaction cardTransaction = new CardTransaction(Calendar.getInstance().getTimeInMillis(),
                cardInfo.getBalance(), amount, cardInfo.getCurrency(), Constant.OPERATION_DEBIT);
        cardInfo.setBalance(cardInfo.getBalance().subtract(amount));
        updateBalance(MessageUtil.formatBalanceToStore(cardInfo.getBalance()));
        storeTransaction(cardTransaction);
    }

    private void storeTransaction(CardTransaction cardTransaction) {

        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(Calendar.getInstance().getTimeInMillis()).append(" ");
        commandBuilder.append(MessageUtil.formatBalanceToStore(cardTransaction.getCurrentBalance())).append(" ");
        commandBuilder.append(MessageUtil.formatBalanceToStore(cardTransaction.getAmount())).append(" ");
        commandBuilder.append(cardTransaction.getCurrency()).append(" ");
        commandBuilder.append(cardTransaction.getOperationType());
        selectTransactionFile();
        appendTransaction(commandBuilder.toString());

    }
    private void appendTransaction(String data) {
        sendAndReceive("00E2000031" + MessageUtil.byteArrayToHexString(data.getBytes()));
    }

    private void updateBalance(String data) {
        sendAndReceive("00D600160E" + MessageUtil.byteArrayToHexString(data.getBytes()));
    }
    private void selectFileAccount() {
        sendAndReceive("00A40000023001");
    }
    private CardInfo readAccount() {
       // String response = sendAndReceive("00B0000040");
        String response = sendAndReceive("00B00000C8");
        String plainText = new String(MessageUtil.hexStringToByteArray(response));
        String[] data= plainText.trim().split(" ");
        return new CardInfo(data[0], data[1], data[3].substring(0,3), new BigDecimal(data[2]).setScale(2));
    }
}
