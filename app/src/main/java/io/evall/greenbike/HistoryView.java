package io.evall.greenbike;

import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistoryView extends AppCompatActivity {
    SimpleAdapter simpadapt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_view);
        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.back);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ListView lst = (ListView) findViewById(R.id.hist_list);
        List<Map<String, String>> prolist  = new ArrayList<Map<String, String>>();
        String[] from = {"A","B","C","D","E","F","G","H"};
        int[] views = { R.id.txtc_date, R.id.txtc_time,R.id.txtc_dist,
                R.id.txtc_co2,R.id.txtc_speed,R.id.txtc_tree, R.id.txtc_rev, R.id.txtc_energy};
        simpadapt = new SimpleAdapter(this, prolist, R.layout.hcard_layout, from, views);
        lst.setAdapter(simpadapt);
        File file = new File("/data/data/io.evall.greenbike/files/history.txt");

        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            try {
                while (bufferedReader.readLine() != null) {
                    String get = bufferedReader.readLine();
                    Map<String, String> datanum = new HashMap<String, String>();
                    String[] rs = get.split("\t");
                    datanum.put("A", rs[0]);//cycledate
                    datanum.put("B", rs[2]);//dist
                    datanum.put("C", rs[1]);//cycletime
                    datanum.put("D", rs[7]);//speed
                    datanum.put("E", rs[3]);//energy
                    datanum.put("F", rs[6]);//cycle
                    datanum.put("G", rs[5]);//tree
                    datanum.put("H", rs[4]);//gas
                    prolist.add(datanum);
                }
                Collections.reverse(prolist);
                bufferedReader.close();
            }
            catch (NullPointerException ex){}
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Intent i = new Intent(HistoryView.this, MainActivity.class);
        i.putExtra("device_address", getIntent().getStringExtra("device_address"));
        startActivity(i);
    }
}
