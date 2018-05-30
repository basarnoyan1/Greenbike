package io.evall.greenbike;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class DeviceListActivity extends Activity {
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    Button tlbutton;
    ProgressBar prg;
    TextView textView1;
    View vie;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        Boolean errormsg;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                errormsg = false;
            } else {
                errormsg= extras.getBoolean("ConnectionFailure");
            }
        } else {
            errormsg = (Boolean) savedInstanceState.getSerializable("ConnectionFailure");
        }
        vie = findViewById(R.id.view);
        textView1 = (TextView) findViewById(R.id.connecting);
        prg = (ProgressBar) findViewById(R.id.progressBar);
        if(errormsg){
            textView1.setText("Bağlantı hatası!");
            vie.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkBTState();
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "Eşleştirilen Bluetooth cihazı bulunamadı.";
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
                String info = ((TextView) v).getText().toString();
                if (info == "Eşleştirilen Bluetooth cihazı bulunamadı."){
                    Snackbar mySnackbar = Snackbar.make(vie, info, Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
                else {
                    String address = info.substring(info.length() - 17);
                    textView1.setText("Bağlanılıyor...");
                    prg.setVisibility(View.VISIBLE);
                    vie.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
                    i.putExtra(EXTRA_DEVICE_ADDRESS, address);
                    startActivity(i);
                    finish();
                }
        }
    };

    private void checkBTState() {
        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "Cihazınız Bluetooth desteklemiyor.", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth açık!");
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }

}