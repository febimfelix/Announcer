package com.febi.announcer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

/**
 * Created by flock on 20/12/16.
 */

public class CallerReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        Log.e("Phone state", state + " ");
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            Log.e("Incoming call", incomingNumber);

            Intent startActivityIntent = new Intent(context, AnnouceService.class);
            startActivityIntent.putExtra("NAME", incomingNumber);
            context.startService(startActivityIntent);
        }
    }
}
