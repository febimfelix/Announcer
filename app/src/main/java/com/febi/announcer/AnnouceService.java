package com.febi.announcer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Locale;

/**
 * Created by flock on 20/12/16.
 */

public class AnnouceService extends Service implements
        TextToSpeech.OnInitListener{

    private TextToSpeech tts;
    private String mContactName;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AudioManager audioManager   = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);

        String incomingNumber       = intent.getStringExtra("NAME");
        mContactName                = getContactName(incomingNumber, this);
        Log.e("Incoming call name", mContactName);
        mContactName                = mContactName + " calling. " + mContactName + " calling." + mContactName + " calling.";

        tts = new TextToSpeech(this, this);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
            }

            @Override
            public void onDone(String s) {
                Log.e("Stop Self", "Stopping");
                stopSelf();
            }

            @Override
            public void onError(String s) {
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("DEstroy", "Service");

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onInit(int i) {
        Log.e("TTS", "On init");
        if (i == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                Log.e("TTS", "Annoucing");
                int responseCode = tts.speak(mContactName, TextToSpeech.QUEUE_FLUSH, null);
                if(responseCode == TextToSpeech.ERROR || responseCode == TextToSpeech.SUCCESS) {
                    Log.e("TTS", responseCode + " response");

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopSelf();
                        }
                    }, 20000);
                }
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private String getContactName(String number, Context context) {
        String contactName  = "";

        String[] projection = new String[] {
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.HAS_PHONE_NUMBER };

        Uri contactUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        Cursor cursor = context.getContentResolver().query(contactUri,
                projection, null, null, null);

        if(cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }

        return contactName.equals("") ? number : contactName;

    }
}
