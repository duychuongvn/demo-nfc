package ch.smartlink.smartticketdemo.control;

import android.nfc.Tag;

import org.osptalliance.cipurse.CipurseException;
import org.osptalliance.cipurse.commands.ByteArray;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.Account;
import ch.smartlink.smartticketdemo.cipurse.CommsChannel;
import ch.smartlink.smartticketdemo.cipurse.Logger;
import ch.smartlink.smartticketdemo.model.CardInfo;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.model.LogModel;
import ch.smartlink.smartticketdemo.util.Constant;
import ch.smartlink.smartticketdemo.util.MessageUtil;


public class CardOperationManager extends BaseCardOperationManager  {
    private CardInfo cardInfo;
    public CardOperationManager(NfcRecordCallback nfcRecordCallback) {
        super(new WeakReference<NfcRecordCallback>(nfcRecordCallback));
    }

    public void onTagDiscovered(Tag tag) {
//        try {
//            super.onTagDiscovered(tag);
//            initCommand();
//            readCardInfo();
//        }catch (AccessCardException ex) {
//            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
//        }catch (CipurseException ex) {
//            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
//        }
        try {
            Account account = new Account();
            account.setCardNumber("1000200030005000");
            account.setExpiryDate("1220");
            account.setCurrency("EUR");
            account.setBalance(BigDecimal.TEN);
            CommsChannel commsChannel = new CommsChannel(tag);
            PaymentCardCreator paymentCardCreator = new PaymentCardCreator(commsChannel, new Logger());
            paymentCardCreator.installApplication();
            paymentCardCreator.initCardInfo(account);

           // super.onTagDiscovered(tag);
            readCardInfo();
        }catch (CipurseException ex) {
            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
        }
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void readCardInfo() {
        try {
            openApp();
            selectFileAccount();
            this.cardInfo = readAccount();
            getNfcRecordCallback().get().onNfcCardReceived(cardInfo);
        }catch (AccessCardException ex) {
            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
        };
    }

    public void doCredit(BigDecimal amount) {
        try {

            CardTransaction cardTransaction = new CardTransaction(Calendar.getInstance().getTimeInMillis(),
                    cardInfo.getBalance(), amount, cardInfo.getCurrency(), Constant.OPERATION_CREDIT);
            cardInfo.setBalance(cardInfo.getBalance().add(amount));

            updateBalance(MessageUtil.formatBalanceToStore(cardInfo.getBalance()));
            storeTransaction(cardTransaction);
        }catch (AccessCardException ex) {
            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
        };
    }
    public void doDebit(BigDecimal amount) {
        try {
            CardTransaction cardTransaction = new CardTransaction(Calendar.getInstance().getTimeInMillis(),
                    cardInfo.getBalance(), amount, cardInfo.getCurrency(), Constant.OPERATION_DEBIT);
            cardInfo.setBalance(cardInfo.getBalance().subtract(amount));
            updateBalance(MessageUtil.formatBalanceToStore(cardInfo.getBalance()));
            storeTransaction(cardTransaction);
        }catch (AccessCardException ex) {
            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
        };
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
        try {
            getCipurseOperational().appendRecord(new ByteArray(data.getBytes()));
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        }

       //sendAndReceive("00E2000031" + MessageUtil.byteArrayToHexString(data.getBytes()));
    }

    private void updateBalance(String data) {
       // sendAndReceive("00D600160E" + MessageUtil.byteArrayToHexString(data.getBytes()));
        try {
          //  initCommand();
            openApp();
            selectFileAccount();
            getCipurseOperational().updateBinary((short)22, new ByteArray(data.getBytes()));
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        }
    }
    private void selectFileAccount() {
        try {
            getCipurseOperational().selectFilebyFID(Constant.ID_FILE_USER_DATA);
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        };
    }
    private CardInfo readAccount() {
       // String response = sendAndReceive("00B0000040");

        try {

            byte[] result = getCipurseOperational().readBinary((short) 0, (short) Constant.LENGH_USER_DATA_BIN).getBytes();
            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, resultLength - 2);
            if(!Arrays.equals(statusWord, new byte[]{(byte) 0x90, (byte) 0x00})){
                throw new AccessCardException("Response: ");
            }
            String plainText = new String(payload);
            String[] data = plainText.trim().split(" ");
            return new CardInfo(data[0], data[1], data[3].substring(0, 3), new BigDecimal(data[2]).setScale(2));
        }catch (CipurseException ex) {
            throw new AccessCardException(ex.getMessage());
        }
    }
}
