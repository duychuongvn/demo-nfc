package ch.smartlink.smartticketdemo.control;

import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import org.osptalliance.cipurse.CipurseException;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.util.Constant;
import ch.smartlink.smartticketdemo.util.MessageUtil;

/**
 * Created by caoky on 11/27/2015.
 */
public class CardHistoryManager extends BaseCardOperationManager {

    public CardHistoryManager(NfcRecordCallback nfcRecordCallback) {
        super(new WeakReference<NfcRecordCallback>(nfcRecordCallback));
    }
    @Override
    public void onTagDiscovered(Tag tag) {
        super.onTagDiscovered(tag);
        try {
            initCommand();

            getNfcRecordCallback().get().onNfcCardReceived(getCardTransactions());
        } catch (Exception ex) {
            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
        }

    }

    private List<CardTransaction> getCardTransactions() throws CipurseException{
        openApp();
        selectTransactionFile();
        return readTransactionRecords();
    }
    private List<CardTransaction> readTransactionRecords() throws CipurseException{
        int totalRecords = 10;
        List<CardTransaction> cardTransactions = new ArrayList<>();
        int index = 0;
        byte[] emptyData = new byte[49];
        byte readModeSingleRecord = 04;
        while (index++ < totalRecords) {
           // String command = "00B2%02X0431";
            byte[] result = getCipurseOperational().readRecord((short)index, readModeSingleRecord,(short) Constant.LENGH_CARD_TRANSACTION_BIN).getBytes();

          //  byte[] data = sendAndReceiveByte(String.format(command, (++index)));
            int resultLength = result.length;
            byte[] statusWord = {result[resultLength-2], result[resultLength-1]};
            byte[] payload = Arrays.copyOf(result, resultLength - 2);
            if(!Arrays.equals(statusWord, new byte[]{(byte) 0x90, (byte) 0x00})){
                throw new AccessCardException("Response: ");
            }

            if(Arrays.equals(payload, emptyData)) {
                Log.i(getClass().getName(), "End data");
                break;
            }
//            if(!planData.startsWith("1")) {
//                break;
//            }
            String planData = new String(payload);
            String[] datas = planData.trim().split(" ");
            CardTransaction cardTransaction = new CardTransaction(new Long(datas[0]),
                                                        new BigDecimal(datas[1])
                    , new BigDecimal(datas[2]),
                    datas[3], datas[4].substring(0,1));
            cardTransactions.add(cardTransaction);
        }
        return cardTransactions;
    }
}
