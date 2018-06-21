package io.evall.greenbike;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.WindowManager;

public class SplashScreen extends Activity {
    SharedPreferences sharedpref;
    private static final int SPLASH_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new BackgroundTask().execute();
    }

    private class BackgroundTask extends AsyncTask {
        Intent intent;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sharedpref = getSharedPreferences("appData", Context.MODE_PRIVATE);
            String name = sharedpref.getString("userName",null);
            if (name != null){
                intent = new Intent(SplashScreen.this, DeviceListActivity.class);
            }
            else {
                intent = new Intent(SplashScreen.this, WelcomeActivity.class);
            }
        }
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.sleep(SPLASH_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            startActivity(intent);
            finish();
        }
    }
}