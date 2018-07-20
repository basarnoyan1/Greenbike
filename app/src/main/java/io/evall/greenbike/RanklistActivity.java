package io.evall.greenbike;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RanklistActivity extends AppCompatActivity {
    SharedPreferences sharedpref;
    List<Userdata> dataList = new ArrayList<>();
    RecyclerView recyclerView;
    UserdataAdapter mAdapter;
    String salt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranklist);
        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar2);
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

        sharedpref = getSharedPreferences("appData", Context.MODE_PRIVATE);
        salt = sharedpref.getString("salt", null);
        recyclerView = (RecyclerView) findViewById(R.id.rank_list);
        mAdapter = new UserdataAdapter(dataList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ///
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://greenbike.evall.io/api.php?actionid=200&salt="+salt;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.wtf("Test",response.toString());
                    try {
                        JSONArray userdata = response.getJSONArray("userdata");
                        for (int i = 0; i < userdata.length(); i++) {
                            JSONObject data = userdata.getJSONObject(i);

                            String name = data.getString("username");
                            String dist = data.getString("dist")+" km";

                            String cycletime = data.getString("cycletime");
                            String speed = data.getString("speed")+" km/h";
                            String energy = data.getString("energy")+" cal";

                            String cycle = data.getString("cycle")+" tur";
                            String tree = data.getString("tree")+" ağaç";
                            String gas = data.getString("gas")+" g CO2";

                            if(!dist.startsWith("null")){
                                Userdata data1 = new Userdata(name,dist,cycletime,speed,energy,cycle,tree,gas);
                                dataList.add(data1);
                            }

                        }
                        Log.w("Test","Bitti!");
                        mAdapter.notifyDataSetChanged();
                    }catch (Exception e){
                        Log.e("Error",e.getMessage());
                    }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) { Log.d("Error", error.toString()); }
                }
        );
        queue.add(getRequest);
        ///

    }

}
