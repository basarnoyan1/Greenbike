package io.evall.greenbike;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class DeviceListActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 3600000;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";
    ProgressBar prg;
    TextView textView1;
    View vie;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private BluetoothAdapter mBtAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (device.getName().startsWith("GREENBIKE") && mPairedDevicesArrayAdapter.getPosition(device.getName() + "\n" + device.getAddress()) == -1) {
                                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                                    Log.d("DeviceList", "Cihaz eklendi.");
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            };
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            textView1.setText(getString(R.string.bl_con_key));
            prg.setVisibility(View.VISIBLE);
            vie.setVisibility(View.INVISIBLE);
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            String name = info.replace("\n"+address,"");
            Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            i.putExtra(EXTRA_DEVICE_NAME,name);
            if (mScanning) {
                mBtAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            startActivity(i);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Cihazınız Bluetooth LE özelliğini desteklememektedir.", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = bluetoothManager.getAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), getString(R.string.bl_nosup_key), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        vie = findViewById(R.id.view);
        textView1 = (TextView) findViewById(R.id.connecting);
        prg = (ProgressBar) findViewById(R.id.progressBar);
        CardView hist = findViewById(R.id.hisCard2);
        CardView rank = findViewById(R.id.rankCard2);

        hist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(DeviceListActivity.this, HistoryView.class);
                startActivity(i);
            }
        });
        rank.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isOnline()) {
                    Intent i = new Intent(DeviceListActivity.this, RanklistActivity.class);
                    startActivity(i);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(vie, "Sıralamaya erişebilmek için internet bağlantısı gerekmektedir.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBtAdapter.isEnabled()) {
            if (!mBtAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBtAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBtAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}