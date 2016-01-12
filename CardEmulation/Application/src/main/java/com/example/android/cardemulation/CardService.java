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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.licel.jcardsim.base.Simulator;
import com.licel.jcardsim.utils.AIDUtil;

import java.util.Arrays;

import ch.smartlink.javacard.applet.HrsApplet;
import ch.smartlink.javacard.MessageUtil;
import ch.smartlink.javacard.cipurse.crypto.CipurseSimulator;

/**
 * This is a sample APDU Service which demonstrates how to interface with the card emulation support
 * added in Android 4.4, KitKat.
 * <p/>
 * <p>This sample replies to any requests sent with the string "Hello World". In real-world
 * situations, you would need to modify this code to implement your desired communication
 * protocol.
 * <p/>
 * <p>This sample will be invoked for any terminals selecting AIDs of 0xF11111111, 0xF22222222, or
 * 0xF33333333. See src/main/res/xml/aid_list.xml for more details.
 * <p/>
 * <p class="note">Note: This is a low-level interface. Unlike the NdefMessage many developers
 * are familiar with for implementing Android Beam in apps, card emulation only provides a
 * byte-array based communication channel. It is left to developers to implement higher level
 * protocol support as needed.
 */
public class CardService extends HostApduService {

    public static final String INIT_CARD_INTENT = "initCardIntent";
    public static final String INIT_CARD_MESSAGE = "initCardMessage";

    public static final String EXTRA_CAPDU = "MSG_CAPDU";
    public static final String EXTRA_RAPDU = "MSG_RAPDU";
    public static final String EXTRA_ERROR = "MSG_ERROR";
    public static final String EXTRA_DESELECT = "MSG_DESELECT";
    public static final String EXTRA_INSTALL = "MSG_INSTALL";

    public static final String TAG = "CardService";
    public static final String TAG_CREATE_APPLET = "CreateHrsApplet";
    private static Simulator simulator = null;

    /**
     * Called if the connection to the NFC card is lost, in order to let the application know the
     * cause for the disconnection (either a lost link, or another AID being selected by the
     * reader).
     *
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    @Override
    public void onDeactivated(int reason) {
    }


    // BEGIN_INCLUDE(processCommandApdu)
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        return transmit(commandApdu);
    }

    private byte[] transmit(byte[] apdu) {
        Intent i = new Intent(TAG);
        String extra_error = "";
        byte[] rapdu = null;
        i.putExtra(EXTRA_CAPDU, MessageUtil.byteArrayToHexString(apdu));
        try {
            rapdu = simulator.transmitCommand(apdu);
            if (rapdu != null) {
                i.putExtra(EXTRA_RAPDU, MessageUtil.byteArrayToHexString(rapdu));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage(), e);
            extra_error += "Internal error";
        }
        if (!extra_error.isEmpty()) {
            i.putExtra(EXTRA_ERROR, extra_error);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        return rapdu;
    }

    private void createSimulator() {
        String aid, name, extra_install = "", extra_error = "";
        simulator = new Simulator(new CipurseSimulator());
        name = getResources().getString(R.string.hrs_name);
        aid = getResources().getString(R.string.hrs_aid);
        try {
            byte[] aid_bytes = MessageUtil.hexStringToByteArray(aid);
            byte[] inst_params = new byte[aid.length() + 1];
            inst_params[0] = (byte) aid_bytes.length;
            System.arraycopy(aid_bytes, 0, inst_params, 1, aid_bytes.length);
            simulator.installApplet(AIDUtil.create(aid), HrsApplet.class, inst_params, (short) 0, (byte) inst_params.length);
            extra_install += "\n" + name + " (AID: " + aid + ")";
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            extra_error += "\n" + "Could not install " + name + " (AID: " + aid + ")";
        }

        Intent i = new Intent(TAG);
        if (!extra_error.isEmpty())
            i.putExtra(EXTRA_ERROR, extra_error);
        if (!extra_install.isEmpty())
            i.putExtra(EXTRA_INSTALL, extra_install);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (simulator == null) {
            createSimulator();
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(INIT_CARD_INTENT));
        }

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                String createFileHex = intent.getStringExtra(INIT_CARD_MESSAGE);
                byte[] response = transmit(MessageUtil.hexStringToByteArray(createFileHex));
                Intent responseIntent = new Intent(TAG_CREATE_APPLET);
                responseIntent.putExtra(EXTRA_RAPDU, MessageUtil.byteArrayToHexString(response));
                responseIntent.putExtra(INIT_CARD_MESSAGE, createFileHex);
                LocalBroadcastManager.getInstance(context).sendBroadcast(responseIntent);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
        }
    };

}
