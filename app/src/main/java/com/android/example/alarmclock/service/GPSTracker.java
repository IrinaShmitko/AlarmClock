package com.android.example.alarmclock.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class GPSTracker extends Service implements LocationListener {

    private Context context;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5; // 5 секунд

    protected LocationManager locationManager;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location locationGPS, locationNet;
    double latitudeGPS;
    double longitudeGPS;
    double latitudeNet;
    double longitudeNet;

    public GPSTracker(Context context) {
        this.context = context;
        getLocation();
    }

    public GPSTracker() {
    }

    private Location getLocation() {
        try {

            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isNetworkEnabled && !isGPSEnabled) {
                Toast.makeText(context, "Провайдеры GPS и Internet НЕДОСТУПНЫ", Toast.LENGTH_SHORT).show();
                return null;
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Нет разрешений для получения геолокации", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (locationNet != null) {
                            latitudeNet = locationNet.getLatitude();
                            longitudeNet = locationNet.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(context, "Нет разрешений для получения геолокации", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Gps enabled", "Gps enabled");
                    if (locationManager != null) {
                        locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (locationGPS != null) {
                            latitudeGPS = locationGPS.getLatitude();
                            longitudeGPS = locationGPS.getLongitude();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (locationNet != null)
            return locationNet;
        else if (locationGPS != null)
            return locationGPS;
        return null;
    }

    public void stopUsingGPS() {
        if (locationManager != null)
            locationManager.removeUpdates(GPSTracker.this);
    }

    public double getLatitudeGPS() {
        if (locationGPS != null)
            latitudeGPS = locationGPS.getLatitude();
        return latitudeGPS;
    }

    public double getLogitudeGPS() {
        if (locationGPS != null)
            longitudeGPS = locationGPS.getLongitude();
        return longitudeGPS;
    }

    public double getLatitudeNet() {
        if (locationNet != null)
            latitudeNet = locationNet.getLatitude();
        return latitudeNet;
    }

    public double getLogitudeNet() {
        if (locationNet != null)
            longitudeNet = locationNet.getLongitude();
        return longitudeNet;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public static void showAlertDialog(final Context context) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("GPS настройки");
        alertDialog.setMessage("Хотите перейти в настройки?");
        alertDialog.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
                dialogInterface.cancel();

            }
        });

        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
        switch (provider) {
            case "gps":
                this.locationGPS = location;
                latitudeGPS = location.getLatitude();
                longitudeGPS = location.getLongitude();
                break;
            case "network":
                this.locationNet = location;
                latitudeNet = location.getLatitude();
                longitudeNet = location.getLongitude();
                break;
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
