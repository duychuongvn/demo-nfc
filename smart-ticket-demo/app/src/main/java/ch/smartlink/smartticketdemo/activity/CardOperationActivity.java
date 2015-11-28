package ch.smartlink.smartticketdemo.activity;

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
import ch.smartlink.smartticketdemo.control.CardOperationManager;
import ch.smartlink.smartticketdemo.model.CardInfo;
import ch.smartlink.smartticketdemo.util.MessageUtil;

public class CardOperationActivity extends AppCompatActivity  {
    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
    private EditText edtAmount;
    private TextView txtBalance;
    private TextView txtCardNumber;
    private TextView txtExpiryDate;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private CardInfo cardInfo;
    private CardOperationManager cardOperationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_operation);
        edtAmount = (EditText) findViewById(R.id.edtAmount);
        txtCardNumber = (TextView) findViewById(R.id.txtCardNumber);
        txtBalance = (TextView) findViewById(R.id.txtBalance);
        txtExpiryDate = (TextView) findViewById(R.id.txtExpiryDate);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        this.pendingIntent = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);

    }
    @Override
    public void onNewIntent(Intent paramIntent)
    {
        resolveIntent(paramIntent, false);
    }

    private static final int INDEX_AMOUNT = 22;
    public void doCredit(View view) {

        if(cardOperationManager != null) {
            BigDecimal amount = new BigDecimal(edtAmount.getText().toString());
            cardOperationManager.doCredit(amount,cardInfo);
            loadAccount();
        }
//        readFileMF();
//        selectADF();
//        selectFileAccount();
//        BigDecimal amount = new BigDecimal(edtAmount.getText().toString());
//        cardInfo.setBalance(cardInfo.getBalance().add(amount));
//
//        updateBalance(MessageUtil.formatBalanceToStore(cardInfo.getBalance()));
//        readAccount();
    }



    public void doDebit(View view) {
        if(cardOperationManager != null) {
            BigDecimal amount = new BigDecimal(edtAmount.getText().toString());
            cardOperationManager.doDebit(amount, cardInfo);
            loadAccount();
        }
//        readFileMF();
//        selectADF();
//        selectFileAccount();
//        BigDecimal amount = new BigDecimal(edtAmount.getText().toString());
//        cardInfo.setBalance(cardInfo.getBalance().subtract(amount));
//
//        updateBalance(MessageUtil.formatBalanceToStore(cardInfo.getBalance()));
//        readAccount();
    }


   // @Override
    public void onTagDiscovered(Tag tag) {
//        isoDep = IsoDep.get(tag);
//        if(isoDep != null) {
//            try {
//                  isoDep.connect();
//                // SELECT FM
//                readFileMF();
//                selectADF();
//                selectFileAccount();
//                readAccount();
//            } catch (Exception e) {
//                Toast.makeText(getApplicationContext(),"Cannot connect or read card data", Toast.LENGTH_LONG);
//                return;
//            }
//
//        }

        try{
            cardOperationManager = new CardOperationManager(tag);
          loadAccount();
        }catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"Cannot connect or read card data", Toast.LENGTH_LONG);
        }

    }

    private void loadAccount() {
        cardInfo = cardOperationManager.getCardInfo();
        txtCardNumber.setText(cardInfo.getCardNumber());
        txtExpiryDate.setText(cardInfo.getExpiryDate());
        txtBalance.setText(cardInfo.getBalance().toPlainString() + " " + cardInfo.getCurrency());
    }

//
//    private void updateBalance(String data) {
//        transceive("00D600160E" + MessageUtil.byteArrayToHexString(data.getBytes()));
//    }
//    private void readFileMF() {
//        transceive("00A40000");
//    }
//    private void selectADF() {
//        transceive("00A404000DD2760000041502000003000101");
//    }
//
//    private void selectFileAccount() {
//        transceive("00A40000023001");
//    }

//    private void readAccount() {
//       String response = transceive("00B0000040");
//
//        String plainText = new String(MessageUtil.hexStringToByteArray(response));
//        String[] data= plainText.trim().split(" ");
//        cardInfo = new CardInfo(data[0], data[1], data[3].substring(0,3), new BigDecimal(data[2]).setScale(2));
//        txtCardNumber.setText(cardInfo.getCardNumber());
//        txtExpiryDate.setText(cardInfo.getExpiryDate());
//        txtBalance.setText(cardInfo.getBalance().toPlainString() + " " + cardInfo.getCurrency());
//
//    }
//
//    private String transceive(String command) {
//        try {
//            byte[] arrayOfByte = isoDep.transceive(MessageUtil.hexStringToByteArray(command));
//            String response = MessageUtil.byteArrayToHexString(arrayOfByte);
//            Log.d(this.getClass().getName(), "data: " + response);
//            return response;
//        }catch (IOException e) {
//
//            throw new AccessCardException();
//        }
//    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (this.nfcAdapter != null) {
            this.nfcAdapter.disableForegroundDispatch(this);
        }
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
}
