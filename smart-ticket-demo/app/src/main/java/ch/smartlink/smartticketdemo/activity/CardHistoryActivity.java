package ch.smartlink.smartticketdemo.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import ch.smartlink.smartticketdemo.control.BaseCardOperationManager;
import ch.smartlink.smartticketdemo.control.CardHistoryManager;
import ch.smartlink.smartticketdemo.control.CardOperationManager;
import ch.smartlink.smartticketdemo.fragment.CardFragment;
import ch.smartlink.smartticketdemo.fragment.CardHistoryFragment;
import ch.smartlink.smartticketdemo.model.CardInfo;
import ch.smartlink.smartticketdemo.model.CardTransaction;

public class CardHistoryActivity extends AppCompatActivity implements BaseCardOperationManager.NfcRecordCallback<List<CardTransaction>>{
    private static final int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A;
    private static final String TAG = "CardHistoryActivity";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private ListView lsvCardHistory;
    private List<CardTransaction> cardTransactions = new ArrayList<>();
    private CardHistoryManager cardHistoryManager;;
    private CardHistoryListAdapter cardHistoryListAdapter;
    private CardHistoryFragment cardHistoryFragment;
    private CardFragment waitingFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_history);
        cardHistoryManager = new CardHistoryManager(this);
        cardHistoryFragment = new CardHistoryFragment();
        waitingFragment = new CardFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_history_content, waitingFragment);
        fragmentTransaction.commit();
    }
    @Override
    public void onNfcCardReceived(final List<CardTransaction> cardTransactions) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {


                cardHistoryFragment.updateView(cardTransactions);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_history_content, cardHistoryFragment);
                fragmentTransaction.commit();

            }
        });

    }

    @Override
    public void onNfcCardError(final String messageCode) {
        final Activity activity = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, "Error:  " + messageCode, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        disableReaderMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }

    private void enableReaderMode() {
        Log.i(TAG, "Enabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.enableReaderMode(this, cardHistoryManager, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        Log.i(TAG, "Disabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableReaderMode(this);
        }
    }
}
