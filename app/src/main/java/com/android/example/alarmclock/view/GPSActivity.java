package com.android.example.alarmclock.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.example.alarmclock.R;
import com.android.example.alarmclock.service.GPSTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GPSActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvLatitudeGPS, tvLongitutdeGPS;
    private TextView tvLatitudeNet, tvLongitutdeNet;
    private Button btnRefreshLocation, btnGPSSettings;
    private GPSTracker gpsTracker;
    private int status = 0;

    double latitude = 0.0;
    double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        tvLatitudeGPS = findViewById(R.id.value_latitude_gps);
        tvLongitutdeGPS = findViewById(R.id.value_longitude_gps);
        tvLatitudeNet = findViewById(R.id.value_latitude_net);
        tvLongitutdeNet = findViewById(R.id.value_longitude_net);
        btnRefreshLocation = findViewById(R.id.btn_location);
        btnGPSSettings = findViewById(R.id.btn_gps_settings);
        btnRefreshLocation.setOnClickListener(this);
        btnGPSSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_location:
                if (gpsTracker != null) {
                    getGpsOrNetworkLocation();
                } else {
                    gpsTracker = new GPSTracker(this);
                    getGpsOrNetworkLocation();
                }
                break;
            case R.id.btn_gps_settings:
                GPSTracker.showAlertDialog(this);
                break;
        }
    }

    private void getGpsOrNetworkLocation() {
        if (gpsTracker.canGetLocation()) {
            status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
            if (status == ConnectionResult.SUCCESS) {

                latitude = gpsTracker.getLatitudeGPS();
                longitude = gpsTracker.getLogitudeGPS();

                tvLatitudeGPS.setText(String.valueOf(latitude));
                tvLongitutdeGPS.setText(String.valueOf(longitude));

                latitude = gpsTracker.getLatitudeNet();
                longitude = gpsTracker.getLogitudeNet();

                tvLatitudeNet.setText(String.valueOf(latitude));
                tvLongitutdeNet.setText(String.valueOf(longitude));
            }
        }
    }

    @Override
    public void onBackPressed() {
        gpsTracker.stopUsingGPS();
        super.onBackPressed();
    }
}
