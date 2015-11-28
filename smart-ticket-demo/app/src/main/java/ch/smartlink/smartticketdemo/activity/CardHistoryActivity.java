package ch.smartlink.smartticketdemo.activity;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ch.smartlink.smartticketdemo.R;
import ch.smartlink.smartticketdemo.adapter.CardHistoryListAdapter;
import ch.smartlink.smartticketdemo.control.CardHistoryManager;
import ch.smartlink.smartticketdemo.control.CardOperationManager;
import ch.smartlink.smartticketdemo.model.CardTransaction;

public class CardHistoryActivity extends AppCompatActivity {
    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private ListView lsvCardHistory;
    private List<CardTransaction> cardTransactions = new ArrayList<>();
    private CardHistoryManager cardHistoryManager;;
    private CardHistoryListAdapter cardHistoryListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_history);
        lsvCardHistory = (ListView) findViewById(R.id.lsvCardHistory);
        cardHistoryListAdapter = new CardHistoryListAdapter(this, cardTransactions);
        lsvCardHistory.setAdapter(cardHistoryListAdapter);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.pendingIntent = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (this.nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(
                    this,
                    pendingIntent,
                    new IntentFilter[]{
                            new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                    },
                    new String[][]{
                            new String[]{"android.nfc.tech.NfcA"}
                    });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PENDING_INTENT_TECH_DISCOVERED:
                resolveIntent(data, true);
                break;
        }
    }

    private void resolveIntent(Intent data, boolean foregroundDispatch) {
        this.setIntent(data);

        String action = data.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = data.getParcelableExtra("android.nfc.extra.TAG");
            onTagDiscovered(tag);
        }
    }
    public void onTagDiscovered(Tag tag) {
        try{
            cardHistoryManager = new CardHistoryManager(tag);
            List<CardTransaction> cardTransactions = cardHistoryManager.getCardTransactions();
            if(cardTransactions.isEmpty()) {
                Toast.makeText(getApplicationContext(), "There is not data", Toast.LENGTH_LONG);
            }
            cardHistoryListAdapter.renewCollections(cardTransactions);
        }catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Cannot connect or read card data", Toast.LENGTH_LONG);
        }

    }
}
