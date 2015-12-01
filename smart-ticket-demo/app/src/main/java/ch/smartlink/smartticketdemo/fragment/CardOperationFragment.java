package ch.smartlink.smartticketdemo.fragment;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import ch.smartlink.smartticketdemo.R;
import ch.smartlink.smartticketdemo.activity.CardOperationActivity;
import ch.smartlink.smartticketdemo.control.BaseCardOperationManager;
import ch.smartlink.smartticketdemo.control.CardOperationManager;
import ch.smartlink.smartticketdemo.model.CardInfo;

/**
 * Created by caoky on 11/29/2015.
 */
public class CardOperationFragment extends CardFragment {
    private static final String TAG = "CardOperationFragment";
    private static final int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private CardOperationManager cardOperationManager;
    private EditText edtAmount;
    private TextView txtBalance;
    private TextView txtCardNumber;
    private TextView txtExpiryDate;
    private CardInfo cardInfo;

    public void setCardOperationManager(CardOperationManager cardOperationManager) {
        this.cardOperationManager = cardOperationManager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_operation, container, false);
        edtAmount = (EditText) rootView.findViewById(R.id.edtAmount);
        txtCardNumber = (TextView) rootView.findViewById(R.id.txtCardNumber);
        txtBalance = (TextView) rootView.findViewById(R.id.txtBalance);
        txtExpiryDate = (TextView) rootView.findViewById(R.id.txtExpiryDate);
        bindingData();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void doCredit(View view) {
        BigDecimal amount = new BigDecimal(edtAmount.getText().toString());
        cardOperationManager.doCredit(amount);
        cardOperationManager.readCardInfo();

    }

    public void doDebit(View view) {
        BigDecimal amount = new BigDecimal(edtAmount.getText().toString());
        cardOperationManager.doDebit(amount);
        cardOperationManager.readCardInfo();
    }

    public void loadAccount(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
        if(txtCardNumber !=null) {
            bindingData();
        }
    }


    private void bindingData() {
        txtCardNumber.setText(cardInfo.getCardNumber());
        txtExpiryDate.setText(cardInfo.getExpiryDate());
        txtBalance.setText(cardInfo.getBalance().toPlainString() + " " + cardInfo.getCurrency());
    }

}
