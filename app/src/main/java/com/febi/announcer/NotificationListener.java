package com.febi.announcer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Locale;

public class NotificationListener extends NotificationListenerService implements
        TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private String mText;

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("TEXT***", "Onbind Service");
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        TelephonyManager telephonyManager   = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        int state           = telephonyManager.getCallState();
        Log.e("Phone state", state + " ");

        LocationManager locationManager     = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location location   = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        boolean isAtOffice  = false;
        if(location != null) {
            Location officeLocation         = new Location("");//My office Location
            officeLocation.setLatitude(10.0177998);
            officeLocation.setLongitude(76.3043092);

            float distance  = officeLocation.distanceTo(location);

            if(distance < 100) {
                isAtOffice  = true;
                Log.e("TEXT****", "Hey you are at office!!!");
            }
        }

        //Don' speak while on a call or at office
        if(state != TelephonyManager.CALL_STATE_OFFHOOK && !isAtOffice) {
            String packageName = sbn.getPackageName();

            if (packageName.contains("talk")) {
                mText = "Hey , Hangout Notification";
                initTTS(sbn.getNotification().tickerText + "");
            } else if (packageName.contains("gm") || packageName.contains("inbox")) {
                mText = "Hey , Gmail Notification";
                initTTS(sbn.getNotification().tickerText + "");
            } else if (packageName.contains("whatsapp")) {
                mText = "Hey , Whatsapp Notification";
                initTTS(sbn.getNotification().tickerText + "");
            } else if (packageName.contains("messenger")) {
                mText = "Hey , Messenger Notification";
                initTTS(sbn.getNotification().tickerText + "");
            } else if (packageName.contains("instagram")) {
                mText = "Hey , Instagram Notification";
                initTTS(sbn.getNotification().tickerText + "");
            }
        }
    }

    private void initTTS(String tickerText) {
        mText = mText + " " + tickerText + " ";
        Log.e("TEXT****", mText);

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
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        Log.e("TEXT****", "Notification removed");
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
                int responseCode = tts.speak(mText, TextToSpeech.QUEUE_FLUSH, null);
                if(responseCode == TextToSpeech.ERROR || responseCode == TextToSpeech.SUCCESS) {
                    Log.e("TTS", responseCode + " response");
                }

                mText = "";

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (tts != null) {
                            tts.stop();
                            tts.shutdown();
                        }
                    }
                }, 20000);
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
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
}
