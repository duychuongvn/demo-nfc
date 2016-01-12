/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.cardemulation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.IllegalFormatPrecisionException;

import ch.smartlink.javacard.MessageUtil;
import ch.smartlink.javacard.hrs.CardInfo;

/**
 * Generic UI for sample discovery.
 */
public class CardEmulationFragment extends Fragment {

    public static final String TAG = "CardEmulationFragment";

    /** Called when sample is created. Displays generic UI with welcome text. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private Button btnUpdate;
    private EditText account;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.main_fragment, container, false);
        btnUpdate = (Button)v.findViewById(R.id.btnUpdate);
        account = (EditText) v.findViewById(R.id.card_account_field);
        account.setText(AccountStorage.GetAccount(getActivity()));
        account.addTextChangedListener(new AccountUpdater());


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateKey(view);
            }
        });
        return v;
    }

    private void storeCardInfo(String key) {
        if(key==null || key.length() != 16) {
            Toast.makeText(getContext(), "Length of key must be 16", Toast.LENGTH_SHORT);
            throw new IllegalArgumentException("Length of key must be 16");
        }
        Intent intent = new Intent(CardService.INIT_CARD_INTENT);
        int keyLen = 16;
        int lcLen = 1;
        String roomKey = key;
        CardInfo cardInfo = new CardInfo("101", "20160101");
        byte[] createCommand = MessageUtil.hexStringToByteArray("00E00102");
        byte[] cardData = cardInfo.toBytes();
        byte[] storeDataCommand = new byte[createCommand.length + lcLen + keyLen + CardInfo.FILE_LENGTH];

        int posKey = createCommand.length + lcLen;
        int posCard = posKey + keyLen;
        int lc = keyLen + CardInfo.FILE_LENGTH;
        System.arraycopy(createCommand, 0, storeDataCommand, 0, createCommand.length);
        System.arraycopy(roomKey.getBytes(), 0, storeDataCommand, posKey, keyLen);
        System.arraycopy(cardData, 0, storeDataCommand, posCard, cardData.length);
        storeDataCommand[createCommand.length] = (byte) lc;
        intent.putExtra(CardService.INIT_CARD_MESSAGE, MessageUtil.byteArrayToHexString(storeDataCommand));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
    private void selectApplet() {
        byte[] command = MessageUtil.hexStringToByteArray("00A40400085F00000000000001");
        Intent intent = new Intent(CardService.INIT_CARD_INTENT);
        intent.putExtra(CardService.INIT_CARD_MESSAGE, MessageUtil.byteArrayToHexString(command));
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }
    public void updateKey(View view) {


        try{
            selectApplet();
            storeCardInfo(account.getText().toString());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class AccountUpdater implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not implemented.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not implemented.
        }



        @Override
        public void afterTextChanged(Editable s) {
            String account = s.toString();
            AccountStorage.SetAccount(getActivity(), account);


        }




    }
}
