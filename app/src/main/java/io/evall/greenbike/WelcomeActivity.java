package io.evall.greenbike;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WelcomeActivity extends Activity {
    SharedPreferences sharedpref;
    Button setbtn;
    EditText name, age, hei, wei;
    RadioButton male, female;
    boolean isMale = true, error = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        name = (EditText) findViewById(R.id.editName);
        age = (EditText) findViewById(R.id.editAge);
        hei = (EditText) findViewById(R.id.editHeight);
        wei = (EditText) findViewById(R.id.editMass);

        if (Integer.valueOf(android.os.Build.VERSION.SDK) > 22) {
            if (ContextCompat.checkSelfPermission(WelcomeActivity.this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(WelcomeActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                } else {
                    ActivityCompat.requestPermissions(WelcomeActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            210);
                }
            } else {
            }
        }


        if (!isOnline()) {
            AlertDialog alertDialog = new AlertDialog.Builder(
                    WelcomeActivity.this).create();
            alertDialog.setTitle("İnternet bağlantınızı kontrol ediniz.");
            alertDialog.setMessage("Uygulamanın ilk ayarlarının yapılabilmesi için internet bağlantısı gerekmektedir." +
                    "Devam etmek için internetinizi açtıktan sonra uygulamayı yeniden başlatınız.");
            alertDialog.setButton("Tamam", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alertDialog.show();
        }
        sharedpref = getSharedPreferences("appData", Context.MODE_PRIVATE);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup2);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioMale:
                        isMale = true;
                        break;
                    case R.id.radieFemale:
                        isMale = false;
                        break;
                }
            }
        });
        setbtn = (Button) findViewById(R.id.setbutton);
        setbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!name.getText().toString().isEmpty() && !age.getText().toString().isEmpty()
                        && !hei.getText().toString().isEmpty() && !wei.getText().toString().isEmpty()) {
                    final SharedPreferences.Editor editor = sharedpref.edit();
                    editor.putString("userName", name.getText().toString());
                    editor.putInt("userAge", Integer.parseInt(age.getText().toString()));
                    editor.putInt("userHei", Integer.parseInt(hei.getText().toString()));
                    editor.putInt("userWei", Integer.parseInt(wei.getText().toString()));
                    if (isMale) {
                        editor.putString("userGender", "Erkek");
                    } else {
                        editor.putString("userGender", "Kadın");
                    }
                    editor.commit();
                    String url = "http://greenbike.evall.io/api.php";
                    RequestQueue queue = Volley.newRequestQueue(WelcomeActivity.this);
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject reader = new JSONObject(response);
                                        JSONObject sys = reader.getJSONObject("userdata");
                                        String resp = sys.getString("salt");
                                        Log.d("salt", resp);
                                        SharedPreferences sharedprf = getSharedPreferences("appData", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editr = sharedprf.edit();
                                        editr.putString("salt", resp);
                                        editr.commit();
                                        error = false;
                                    } catch (JSONException j) {
                                        j.getMessage();
                                        error = true;
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Error", error.getMessage());
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("username", name.getText().toString());
                            params.put("actionid", "200");
                            return params;
                        }
                    };
                    queue.add(postRequest);

                    if (error == false){
                        Intent intent = new Intent(WelcomeActivity.this, DeviceListActivity.class);
                        startActivity(intent);
                        finish();
                    }

                } else {
                    Snackbar snackbar = Snackbar
                            .make(name, getString(R.string.wlc_check_key), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }

            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 210: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
            }
        }
    }
}
