package ch.smartlink.smartticketdemo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.smartlink.smartticketdemo.R;
import ch.smartlink.smartticketdemo.adapter.CardHistoryListAdapter;
import ch.smartlink.smartticketdemo.control.CardHistoryManager;
import ch.smartlink.smartticketdemo.model.CardTransaction;

/**
 * Created by caoky on 11/30/2015.
 */
public class CardHistoryFragment extends CardFragment {
    private List<CardTransaction> cardTransactions = new ArrayList<>();
    private CardHistoryListAdapter cardHistoryListAdapter;
    private ListView lsvCardHistory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_history, container, false);
        lsvCardHistory = (ListView) rootView.findViewById(R.id.lsvCardHistory);
        cardHistoryListAdapter = new CardHistoryListAdapter(getActivity(), cardTransactions);
        lsvCardHistory.setAdapter(cardHistoryListAdapter);
        return rootView;
    }

    public void updateView(List<CardTransaction> cardTransactions) {
        this.cardTransactions = cardTransactions;
        if(cardHistoryListAdapter != null) {
            cardHistoryListAdapter.renewCollections(cardTransactions);
        }
    }
}
