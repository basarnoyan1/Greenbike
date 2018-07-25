package io.evall.greenbike;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    private static String mDeviceAddress;
    boolean mConnected = false;
    SharedPreferences sharedpref;
    TextView nametxt, sensorView2, sensorView4, sensorView5, sensorView6, sensorView7, sensorView8;
    Chronometer sensorView3;
    String name, salt, date, dist, time, speed, energy, pedal, tree, carbo;
    CardView hist, save, rank;
    View img, img2;
    long lasttime;
    boolean first = true, isStarted = false;
    int frnum;
    double tire_dia = 0.66; //Lastik çapı 26"
    int wei, hei, age;
    String gen;
    double peri = Math.PI * tire_dia;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private StringBuilder recDataString = new StringBuilder();
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    public final static UUID HM_RX_TX =
            UUID.fromString(SampleGattAttributes.HM_RX_TX);
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("MainActivity", "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                if (mNotifyCharacteristic != null) {
                    final int charaProp = mNotifyCharacteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        mBluetoothLeService.readCharacteristic(mNotifyCharacteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                    }
                }
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                onDataReceived(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        nametxt = findViewById(R.id.name);
        img = findViewById(R.id.view11);
        img2 = findViewById(R.id.view4);
        sharedpref = getSharedPreferences("appData", Context.MODE_PRIVATE);

        name = sharedpref.getString("userName", null);
        salt = sharedpref.getString("salt", null);
        wei = sharedpref.getInt("userWei", 1);
        hei = sharedpref.getInt("userHei", 1);
        age = sharedpref.getInt("userAge", 1);
        gen = sharedpref.getString("userGender", "Erkek");

        nametxt.setText(getString(R.string.hello_key) + ", " + name + "!");
        sensorView2 = findViewById(R.id.sensorView2);
        sensorView3 = findViewById(R.id.sensorView3);
        sensorView3.setText("00:00:00");
        sensorView3.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
                chronometer.setText(t);
                if ((time - lasttime) > 5000) {
                    Snackbar snackbar = Snackbar
                            .make(sensorView3, getString(R.string.autosave_key), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    save.callOnClick();
                }
            }
        });

        sensorView4 = findViewById(R.id.sensorView4);
        sensorView5 = findViewById(R.id.sensorView5);
        sensorView6 = findViewById(R.id.sensorView6);
        sensorView7 = findViewById(R.id.sensorView7);
        sensorView8 = findViewById(R.id.sensorView8);
        hist = findViewById(R.id.hisCard);
        save = findViewById(R.id.saveCard);
        rank = findViewById(R.id.rankCard);

        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nametxt.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
            img2.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            nametxt.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
            img2.setVisibility(View.VISIBLE);
        }

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                date = sdf.format(cal.getTime());
                dist = sensorView4.getText().toString();
                time = sensorView3.getText().toString();
                speed = sensorView5.getText().toString();
                energy = sensorView6.getText().toString();
                pedal = sensorView2.getText().toString();
                tree = sensorView8.getText().toString();
                carbo = sensorView7.getText().toString();

                //
                try {
                    long chrtime = SystemClock.elapsedRealtime() - sensorView3.getBase();
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            MainActivity.this).create();
                    alertDialog.setTitle("Ürettiğin elektrik enerjisiyle:");
                    alertDialog.setMessage(
                            appr_time(chrtime, 0) + "su ısıtıcısı,\n" +
                                    appr_time(chrtime, 1) + "ampul,\n" +
                                    appr_time(chrtime, 2) + "klima çalıştırabilir ve\n" +
                                    appr_time(chrtime, 3) + "basınçlı hava üretebilirdin."
                    );
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }

                //

                first = !first;
                sensorView3.stop();
                isStarted = false;
                sensorView3.setBase(SystemClock.elapsedRealtime());
                sensorView3.setText("00:00:00");
                sensorView2.setText("0 " + getString(R.string.tour_key));
                sensorView4.setText("0 km");
                sensorView5.setText("0 km/h");
                sensorView6.setText("0 cal");
                sensorView7.setText("0 g CO2");
                sensorView8.setText("0 " + getString(R.string.tree_key));

                String strText = date + "\t" + dist + "\t" + time + "\t" +
                        speed + "\t" + energy + "\t" + pedal + "\t" + tree + "\t" + carbo + "\n";


                Log.w("Log", strText);
                Snackbar snackbar = Snackbar
                        .make(sensorView2, getString(R.string.saved_key), Snackbar.LENGTH_SHORT);
                try {
                    FileOutputStream fos = openFileOutput("history.txt", getApplicationContext().MODE_APPEND);
                    fos.write(strText.getBytes());
                    fos.close();
                    Log.w("Success", "İşlem başarılı! (1)");
                    snackbar.show();
                } catch (IOException e) {
                }
                first = true;

                if (isOnline()) try {
                    String url = "http://greenbike.evall.io/api.php";
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e("Error", error.toString());
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("username", name);
                            params.put("salt", salt);
                            params.put("dist", dist.substring(0, dist.lastIndexOf(" km")).replace(",", "."));
                            params.put("cycletime", time);
                            params.put("speed", speed.substring(0, speed.lastIndexOf(" km/h")).replace(",", "."));
                            params.put("energy", energy.substring(0, energy.lastIndexOf(" cal")).replace(",", "."));
                            params.put("cycle", pedal.substring(0, pedal.lastIndexOf(" " + getString(R.string.tour_key))));
                            params.put("tree", tree.substring(0, tree.lastIndexOf(" " + getString(R.string.tree_key))).replace(",", "."));
                            params.put("gas", carbo.substring(0, carbo.lastIndexOf(" g")).replace(",", "."));
                            params.put("actionid", "400");
                            return params;
                        }
                    };
                    queue.add(putRequest);
                } catch (Exception e) {
                }
            }
        });
        hist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HistoryView.class);
                i.putExtra("device_address", mDeviceAddress);
                startActivity(i);
            }
        });
        rank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isOnline()) {
                    Intent i = new Intent(MainActivity.this, RanklistActivity.class);
                    i.putExtra("device_address", mDeviceAddress);
                    startActivity(i);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(sensorView3, "Sıralamaya erişebilmek için internet bağlantısı gerekmektedir.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d("MainActivity", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        if (isStarted) {
            save.callOnClick();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nametxt.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
            img2.setVisibility(View.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            nametxt.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
            img2.setVisibility(View.VISIBLE);
        }
    }

    private void onDataReceived(String data) {
        recDataString.append(data);
        int endOfLineIndex = recDataString.indexOf("~");
        if (endOfLineIndex > 0) {
            String dataInPrint = recDataString.substring(0, endOfLineIndex);
            String sensor0 = dataInPrint.substring(dataInPrint.indexOf("#b4z8") + 5, dataInPrint.length());
            int value = Integer.parseInt(sensor0);
            recDataString.delete(0, recDataString.length());
            if (first) {
                frnum = value;
                first = !first;
                sensorView3.setBase(SystemClock.elapsedRealtime());
                sensorView3.setText("00:00:00");
                sensorView3.start();
                isStarted = true;
            }

            sensorView2.setText(Integer.toString(value +1 - frnum) + " tur");
            sensorView4.setText(String.format(Locale.US, "%.3f", ((value - frnum) * peri / 1000)) + " km");
            long chrtime = SystemClock.elapsedRealtime() - sensorView3.getBase();

            BigDecimal co = new BigDecimal(chrtime / 1000 * 0.125);
            co = co.setScale(2, BigDecimal.ROUND_DOWN);
            sensorView7.setText(co.toString().replace(",", ".") + " g CO2");

            BigDecimal tr = new BigDecimal(chrtime * 6.25 / 100000);
            tr = tr.divide(new BigDecimal(1000));
            tr = tr.setScale(2, BigDecimal.ROUND_DOWN);
            sensorView8.setText(tr.toString().replace(",", ".") + " ağaç");

            sensorView5.setText(String.format(Locale.US, "%.1f", 3600 * peri / (chrtime - lasttime)) + " km/h");
            double sped = Double.parseDouble(String.format(Locale.US, "%.1f", 3600 * peri / (chrtime - lasttime)));
            sensorView6.setText(getCal(gen, hei, wei, age, sped));
            lasttime = chrtime;
            dataInPrint = " ";
        }
    }

    public String getCal(String gender, int he, int we, int ag, double spe) {
        double bmr;
        double mets;
        if (gender == "Erkek") {
            bmr = 10 * we + 6.25 * he - 5 * ag + 5;
        } else {
            bmr = 10 * we + 6.25 * he - 5 * ag - 161;
        }

        if (spe < 0) {
            mets = 1;
        } else if (spe < 5) {
            mets = 3.8 - (5 - spe) * 2 / 9;
        } else if (spe < 10) {
            mets = 4.8 - (10 - spe) * 2 / 10;
        } else if (spe < 15) {
            mets = 5.9 - (15 - spe) * 2 / 11;
        } else if (spe < 20) {
            mets = 7.1 - (20 - spe) * 2 / 12;
        } else if (spe < 25) {
            mets = 8.4 - (25 - spe) * 2 / 13;
        } else if (spe < 30) {
            mets = 9.8 - (30 - spe) * 2 / 14;
        } else if (spe < 35) {
            mets = 11.3 - (35 - spe) * 2 / 15;
        } else if (spe < 40) {
            mets = 12.9 - (40 - spe) * 2 / 16;
        } else if (spe < 45) {
            mets = 14.6 - (45 - spe) * 2 / 17;
        } else if (spe < 50) {
            mets = 16.4 - (50 - spe) * 2 / 18;
        } else {
            mets = 18.3;
        }

        long chrt = SystemClock.elapsedRealtime() - sensorView3.getBase();
        BigDecimal tim = new BigDecimal(chrt / 3600);
        BigDecimal bd1 = new BigDecimal(bmr * mets / 24);
        BigDecimal res = tim.multiply(bd1);
        res = res.divide(new BigDecimal(1000));
        res = res.setScale(1, BigDecimal.ROUND_DOWN);
        return res.toString().replace(",", ".") + " cal";
    }

    public String appr_time(long tim, int code) {
        String res = null;
        switch (code) {
            case 0://Su ısıtıcısı
                try {
                    BigDecimal t = new BigDecimal(tim);
                    t = t.divide(new BigDecimal(21600));
                    t = t.divide(new BigDecimal(1000));
                    t = t.setScale(2, BigDecimal.ROUND_DOWN);
                    res = t.toString().replace(",", ".") + " kez ";
                } catch (Exception e) {
                    res = "0 kez ";
                }
                break;

            case 1://Ampul
                long millis = tim * 60 / 16;
                res = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))) + " ";
                break;

            case 2://Klima
                long milli = tim * 10 / 35;
                res = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(milli),
                        TimeUnit.MILLISECONDS.toMinutes(milli) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milli)),
                        TimeUnit.MILLISECONDS.toSeconds(milli) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milli))) + " ";
                break;

            case 3://Basınçlı hava
                long mill = tim / 540;
                res = mill / 1000 + " s ";
                break;
        }

        return res;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            String serviceString = SampleGattAttributes.lookup(uuid);
            if (serviceString != null) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    mNotifyCharacteristic = gattCharacteristic;
                    //return;
                }
            }
        }
    }
}