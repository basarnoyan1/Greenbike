package io.evall.greenbike;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public class HistoryView extends AppCompatActivity {
    ArrayAdapter<String> histArrayAdapter;
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ListView lst = (ListView) findViewById(R.id.hist_list);
        histArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        lst.setAdapter(histArrayAdapter);
        try{
            FileInputStream fis = getApplicationContext().openFileInput("history.txt");
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            try {
                while (bufferedReader.readLine().toString() != null) {
                    String bfrd = bufferedReader.readLine().toString();
                    histArrayAdapter.add(bfrd);
                }
            }
            catch (NullPointerException ex){}
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                try{
                String nl = "";
                FileOutputStream fos = openFileOutput("history.txt", getApplicationContext().MODE_PRIVATE);
                fos.write(nl.getBytes());
                fos.close();
                histArrayAdapter.clear();}
                catch (Exception e){ }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
