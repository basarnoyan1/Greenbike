package io.evall.greenbike;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    TextView txtArduino, txtString, txtStringLength,
            sensorView2, sensorView3, sensorView4, sensorView5, sensorView6, sensorView7, sensorView8;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;
    boolean first = true;
    int frnum;
    double tire_dia = 0.66;//Lastik çapı 26"
    double wei = 75*9.81;//Ağırlık
    long frtime;
/*
    rp_wr : 75,        // weight of rider (kg)
    rp_wb : 8,         // weight of bike (kg)
    rp_a : 0.509,      // frontal area, rider+bike (m^2)
    rp_cd : 0.63,      // drag coefficient Cd
    rp_dtl : 3,        // drivetrain loss Loss_dt
    ep_crr : 0.005,    // coefficient of rolling resistance Crr
    ep_rho : 1.226,    // air density (kg / m^3)
    ep_g : 0,          // grade of hill (%)
    p2v : 200,         // 200 watts of power for the P2V field
    v2p : 35           // 35kph for the V2P field
* */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        sensorView2 = (TextView) findViewById(R.id.sensorView2);
        sensorView3 = (TextView) findViewById(R.id.sensorView3);
        sensorView4 = (TextView) findViewById(R.id.sensorView4);
        sensorView5 = (TextView) findViewById(R.id.sensorView5);
        sensorView6 = (TextView) findViewById(R.id.sensorView6);
        sensorView7 = (TextView) findViewById(R.id.sensorView7);
        sensorView8 = (TextView) findViewById(R.id.sensorView8);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        String sensor0 = dataInPrint.substring(dataInPrint.indexOf("#b4z8")+5, dataInPrint.length());
                        int value = Integer.parseInt(sensor0);
                        recDataString.delete(0, recDataString.length());
                        if(first){
                            frnum = value;
                            first=!first;
                            frtime = System.nanoTime();}
                        sensorView2.setText(Integer.toString(value-frnum));
                        sensorView4.setText(String.format("%.3f",((value-frnum)*Math.PI*tire_dia/1000))+" km");
                        sensorView7.setText(String.format("%.2f",((value-frnum)*Math.PI*tire_dia/1000)*137)+" g CO2");
                        sensorView8.setText(String.format("%.2f",((value-frnum)*Math.PI*tire_dia/1000)*5)+" ağaç");

                        long tstamp = System.nanoTime();
                        try {
                            double diff = (tstamp - frtime) / 1e6;
                            //sensorView3.setText(Double.toString(diff/1000)+" s");
                            Date date = new Date((long) Math.floor(diff-7200000));
                            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                            String dateFormatted = formatter.format(date);
                            sensorView3.setText(dateFormatted);
                            sensorView5.setText(String.format("%.1f",((value-frnum)*Math.PI*tire_dia/1000)/(Math.floor(diff)/3600000))+" km/h");
                            sensorView6.setText(String.format("%.1f",(wei*((value-frnum)*Math.PI*tire_dia)/(Math.floor(diff)/1000)))+" W");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
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
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Intent i = new Intent(MainActivity.this, DeviceListActivity.class);
                i.putExtra("ConnectionFailure",true);
                startActivity(i);
                finish();
            }
        }
    }
}

