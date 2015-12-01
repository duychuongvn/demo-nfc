package ch.smartlink.smartticketdemo.control;

import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.model.CardTransaction;
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
            getNfcRecordCallback().get().onNfcCardReceived(getCardTransactions());
        } catch (AccessCardException ex) {
            getNfcRecordCallback().get().onNfcCardError(ex.getMessage());
        }

    }

    private List<CardTransaction> getCardTransactions() {
        openApp();
        selectTransactionFile();
        return readTransactionRecords();
    }
    private List<CardTransaction> readTransactionRecords() {
        int totalRecords = 10;
        List<CardTransaction> cardTransactions = new ArrayList<>();
        int index = 0;
        byte[] emptyData = new byte[49];
        while (index < totalRecords) {
            String command = "00B2%02X0431";
            byte[] data = sendAndReceiveByte(String.format(command, (++index)));
            String planData = new String(data);
            if(Arrays.equals(data, emptyData)) {
                Log.i(getClass().getName(), "End data");
            }
            if(!planData.startsWith("1")) {
                break;
            }

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
