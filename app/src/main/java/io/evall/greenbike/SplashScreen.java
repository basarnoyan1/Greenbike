package io.evall.greenbike;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, DeviceListActivity.class));
        finish();
    }
}
