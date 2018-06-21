package io.evall.greenbike;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import static java.lang.Math.round;

public class MainActivity extends Activity {
    SharedPreferences sharedpref;
    TextView nametxt, sensorView2, sensorView4, sensorView5, sensorView6, sensorView7, sensorView8;
    Chronometer sensorView3;
    String name, salt, date, dist, time, speed, energy, pedal, tree, carbo;
    Handler bluetoothIn;
    CardView hist, save, rank;
    View img;
    long lasttime;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address;

    boolean first = true;
    int frnum;
    double tire_dia = 0.66;//Lastik çapı 26"
    double acc = 9.80665;//
    int wei, hei, age;
    String gen;
    double total_energy = 0.0;
    double peri = Math.PI * tire_dia;

        /*
            rp_wr : 75,        // weight of rider (kg)
            rp_wb : 8,         // weight of bike (kg)
            rp_a : 0.509,      // frontal area, rider+bike (m^2)
            rp_cd : 0.63,      // drag coefficient Cd
            rp_dtl : 3,        // drivetrain loss Loss_dt
            ep_crr : 0.005,    // coefficient of rolling resistance Crr
            ep_rho : 1.226,    // air density (kg / m^3)
            ep_g : 0,          // grade of hill (%)
            p2v : 200,         // 200 watts of energy for the P2V field
            v2p : 35           // 35kph for the V2P field
        */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        nametxt = (TextView) findViewById(R.id.name);
        img = findViewById(R.id.view11);
        sharedpref = getSharedPreferences("appData", Context.MODE_PRIVATE);

        name = sharedpref.getString("userName", null);
        salt = sharedpref.getString("salt", null);
        wei =  sharedpref.getInt("userWei", 1);
        hei =  sharedpref.getInt("userHei", 1);
        age =  sharedpref.getInt("userAge", 1);
        gen = sharedpref.getString("userGender", "Erkek");

        nametxt.setText(getString(R.string.hello_key) + ", " + name + "!");
        sensorView2 = (TextView) findViewById(R.id.sensorView2);
        sensorView3 = (Chronometer) findViewById(R.id.sensorView3);
        sensorView3.setText("00:00:00");
        sensorView3.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener(){
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                int h   = (int)(time /3600000);
                int m = (int)(time - h*3600000)/60000;
                int s= (int)(time - h*3600000- m*60000)/1000 ;
                String t = (h < 10 ? "0"+h: h)+":"+(m < 10 ? "0"+m: m)+":"+ (s < 10 ? "0"+s: s);
                chronometer.setText(t);
                if((SystemClock.elapsedRealtime() - sensorView3.getBase() - lasttime)>20000){
                    Snackbar snackbar = Snackbar
                            .make(sensorView3, getString(R.string.autosave_key), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    save.callOnClick();
                }
                boolean bl = btSocket.isConnected();
                if(!btSocket.isConnected()){
                try { btSocket.connect();
                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.start();
                    mConnectedThread.write("x");}
                    catch (IOException e) { }
            }}
        });

        sensorView4 = (TextView) findViewById(R.id.sensorView4);
        sensorView5 = (TextView) findViewById(R.id.sensorView5);
        sensorView6 = (TextView) findViewById(R.id.sensorView6);
        sensorView7 = (TextView) findViewById(R.id.sensorView7);
        sensorView8 = (TextView) findViewById(R.id.sensorView8);
        hist = (CardView) findViewById(R.id.hisCard);
        save = (CardView) findViewById(R.id.saveCard);
        rank = (CardView) findViewById(R.id.rankCard);

        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            nametxt.setVisibility(View.INVISIBLE);
            img.setVisibility(View.INVISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            nametxt.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
        }

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
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
                        }
                        sensorView2.setText(Integer.toString(value - frnum) + " tur");
                        sensorView4.setText(String.format("%.3f", ((value - frnum) * peri / 1000)) + " km");
                        sensorView7.setText(String.format("%.2f", ((value - frnum) * peri / 1000) * 137) + " g CO2");
                        sensorView8.setText(String.format("%.2f", ((value - frnum) * peri / 1000) * 5) + " ağaç");
                        long chrtime = SystemClock.elapsedRealtime() - sensorView3.getBase();
                        sensorView5.setText(String.format("%.1f", 3600 * peri/(chrtime-lasttime)) + " km/h");
                        double sped =  Double.parseDouble(String.format("%.1f", 3600 * peri/(chrtime-lasttime)));
                        sensorView6.setText(getCal(gen, hei, wei, age, sped));
                        lasttime = chrtime;
                        dataInPrint = " ";
                    }
                }
            }
        };

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

                first = !first;
                sensorView3.stop();
                sensorView3.setBase(SystemClock.elapsedRealtime());
                sensorView3.setText("00:00:00");
                sensorView2.setText("0 "+getString(R.string.tour_key));
                sensorView4.setText("0 km");
                sensorView5.setText("0 km/h");
                sensorView6.setText("0 cal");
                sensorView7.setText("0 g CO2");
                sensorView8.setText("0 "+getString(R.string.tree_key));


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
                    try {
                        FileOutputStream fos = openFileOutput("history.txt", getApplicationContext().MODE_PRIVATE);
                        fos.write(strText.getBytes());
                        fos.close();
                        Log.w("Success", "İşlem başarılı! (2)");
                        snackbar.show();
                    } catch (IOException e2) {
                    }
                }

                first = true;


                ///////////////////////////
                if(isOnline()){try{
                String url = "http://greenbike.evall.io/api.php";
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                StringRequest putRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() { @Override public void onResponse(String response) { }},
                        new Response.ErrorListener() { @Override public void onErrorResponse(VolleyError error) { Log.e("Error",error.toString()); }}
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("username", name);
                        params.put("salt", salt);
                        params.put("dist", dist.substring(0, dist.lastIndexOf(" km")).replace(",","."));
                        params.put("cycletime", time);
                        params.put("speed", speed.substring(0, speed.lastIndexOf(" km/h")).replace(",","."));
                        params.put("energy", energy.substring(0, energy.lastIndexOf(" cal")).replace(",","."));
                        params.put("cycle", pedal.substring(0, pedal.lastIndexOf(" "+getString(R.string.tour_key))));
                        params.put("tree", tree.substring(0, tree.lastIndexOf(" "+getString(R.string.tree_key))).replace(",","."));
                        params.put("gas", carbo.substring(0, carbo.lastIndexOf(" g")).replace(",","."));
                        params.put("actionid","400");
                        return params;
                    }
                };
                queue.add(putRequest);}catch(Exception e){}}
                /////////////////////////////////

            }
        });

        hist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HistoryView.class);
                startActivity(i);
            }
        });

        rank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RanklistActivity.class);
                startActivity(i);
            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        try {
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            btSocket = createBluetoothSocket(device);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                    //btSocket.close();
            } catch (Exception e2) {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
        mConnectedThread.write("x");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(dist != null || dist != "0 km"){
            save.callOnClick();}
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                if(pedal == null) {
                    Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
                    i.putExtra("ConnectionFailure", true);
                    startActivity(i);
                    finish();
                }
            }
        }
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
            nametxt.setVisibility(View.INVISIBLE);
            img.setVisibility(View.INVISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            nametxt.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
        }
    }

    public String getCal(String gender, int he, int we, int ag, double spe){
        double bmr;
        double mets;
        if(gender == "Erkek"){ bmr = 10 * we + 6.25 * he - 5 * ag + 5; }
        else{ bmr = 10 * we + 6.25 * he - 5 * ag - 161; }

        if(spe<0){mets= 1;}
        else if(spe<5){mets= 3.8 - (5-spe)*2/9;}
        else if(spe <10){mets= 4.8 - (10-spe)*2/10;}
        else if (spe <15){mets= 5.9 - (15-spe)*2/11;}
        else if (spe <20){mets= 7.1 - (20-spe)*2/12;}
        else if (spe <25){mets= 8.4 - (25-spe)*2/13;}
        else if (spe <30){mets= 9.8 - (30-spe)*2/14;}
        else if (spe <35){mets= 11.3 - (35-spe)*2/15;}
        else if (spe <40){mets= 12.9 - (40-spe)*2/16;}
        else if (spe <45){mets= 14.6 - (45-spe)*2/17;}
        else if (spe <50){mets= 16.4 - (50-spe)*2/18;}
        else {mets = 18.3;}

        long chrt = SystemClock.elapsedRealtime() - sensorView3.getBase();;
        BigDecimal tim = new BigDecimal(chrt/3600);
        BigDecimal bd1 = new BigDecimal(bmr * mets/24);
        BigDecimal res  = tim.multiply(bd1);
        res = res.divide(new BigDecimal(1000));
        res = res.setScale(1, BigDecimal.ROUND_DOWN);
        return res.toString()+ " cal";
    }

}

