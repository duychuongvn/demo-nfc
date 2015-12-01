package ch.smartlink.smartticketdemo.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.BigDecimal;

import ch.smartlink.smartticketdemo.AccessCardException;
import ch.smartlink.smartticketdemo.R;
import ch.smartlink.smartticketdemo.control.BaseCardOperationManager;
import ch.smartlink.smartticketdemo.control.CardOperationManager;
import ch.smartlink.smartticketdemo.fragment.CardFragment;
import ch.smartlink.smartticketdemo.fragment.CardOperationFragment;
import ch.smartlink.smartticketdemo.model.CardInfo;
import ch.smartlink.smartticketdemo.util.MessageUtil;

public class CardOperationActivity extends AppCompatActivity implements BaseCardOperationManager.NfcRecordCallback<CardInfo>  {
    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
    private static final String TAG = "CardOperationActivity" ;
    private static final int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A;

    private CardOperationFragment cardOperationFragment;
    private CardOperationManager cardOperationManager;
    private CardFragment waitingFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_operation);
        this.cardOperationManager = new CardOperationManager(this);
        cardOperationFragment = new CardOperationFragment();
        cardOperationFragment.setCardOperationManager(cardOperationManager);
        waitingFragment = new CardFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_content, waitingFragment);
        fragmentTransaction.commit();
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

    //  cardOperationFragment.showLog(messageCode);

    //    waitingFragment.showLog(messageCode);
    }

    @Override
    public void onNfcCardReceived(final CardInfo cardInfo) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardOperationFragment.loadAccount(cardInfo);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_content, cardOperationFragment);
                fragmentTransaction.commit();
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

    public void doCredit(View view) {
        cardOperationFragment.doCredit(view);

    }

    public void doDebit(View view) {
        cardOperationFragment.doDebit(view);
    }
    private void enableReaderMode() {
        Log.i(TAG, "Enabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.enableReaderMode(this, cardOperationManager, READER_FLAGS, null);
        }
    }

    private void disableReaderMode() {
        Log.i(TAG, "Disabling reader mode");
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null) {
            nfc.disableReaderMode(this);
        }
    }

//    // @Override
//    public void onTagDiscovered(Tag tag) {
//        try {
//
//            FragmentManager fragmentManager = getFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_content, cardOperationFragment);
//            fragmentTransaction.commit();
//        } catch (Exception ex) {
//            Toast.makeText(getApplicationContext(), "Cannot connect or read card data", Toast.LENGTH_LONG);
//        }
//
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (this.nfcAdapter != null) {
//            this.nfcAdapter.disableForegroundDispatch(this);
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (this.nfcAdapter != null) {
//            nfcAdapter.enableForegroundDispatch(
//                    this,
//                    pendingIntent,
//                    new IntentFilter[]{
//                            new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
//                    },
//                    new String[][]{
//                            new String[]{"android.nfc.tech.NfcA"}
//                    });
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case PENDING_INTENT_TECH_DISCOVERED:
//                resolveIntent(data, true);
//                break;
//        }
//    }
//
//    private void resolveIntent(Intent data, boolean foregroundDispatch) {
//        this.setIntent(data);
//
//        String action = data.getAction();
//        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            Tag tag = data.getParcelableExtra("android.nfc.extra.TAG");
//            onTagDiscovered(tag);
//        }
//
//    }

}
