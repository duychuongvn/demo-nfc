package ch.smartlink.smartticketdemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import ch.smartlink.smartticketdemo.R;
import ch.smartlink.smartticketdemo.model.CardTransaction;
import ch.smartlink.smartticketdemo.util.MessageUtil;

/**
 * Created by caoky on 11/26/2015.
 */
public class CardHistoryListAdapter extends BaseAdapter {

    private List<CardTransaction> cardTransactions;
    private LayoutInflater layoutInflater;
    private Activity activity;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public CardHistoryListAdapter(Activity activity, List<CardTransaction> cardTransactions) {
        this.activity = activity;
        this.cardTransactions = cardTransactions;
        this.layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void renewCollections(List<CardTransaction> cardTransactions) {
        this.cardTransactions.clear();
        this.cardTransactions.addAll(cardTransactions);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.cardTransactions.size();
    }

    @Override
    public Object getItem(int position) {
        return cardTransactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.card_history, null);
        }
        TextView txtTime = (TextView) convertView.findViewById(R.id.txtTime);
        TextView txtCurrentBalance = (TextView) convertView.findViewById(R.id.txtCurrentBalance);
        TextView txtAmount = (TextView) convertView.findViewById(R.id.txtAmount);
        TextView txtCurrency = (TextView) convertView.findViewById(R.id.txtCurrency);
        TextView txtOperatorType = (TextView) convertView.findViewById(R.id.txtOperationType);
        CardTransaction cardTransaction = this.cardTransactions.get(position);
        txtTime.setText(simpleDateFormat.format(cardTransaction.getTime()));
        txtCurrentBalance.setText(MessageUtil.getDisplayBalance(cardTransaction.getCurrentBalance()));
        txtAmount.setText(MessageUtil.getDisplayBalance(cardTransaction.getAmount()));
        txtCurrency.setText(cardTransaction.getCurrency());
        txtOperatorType.setText("0".equals(cardTransaction.getOperationType())?"Credit" : "Debit");
        return convertView;
    }
}
