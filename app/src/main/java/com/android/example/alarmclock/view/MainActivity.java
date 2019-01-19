package com.android.example.alarmclock.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.android.example.alarmclock.R;
import com.android.example.alarmclock.database.DatabaseHelper;
import com.android.example.alarmclock.database.model.Alarm;
import com.android.example.alarmclock.service.RingtoneService;
import com.android.example.alarmclock.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_OPEN_ALARM = 5000;

    private RecyclerAlarmAdapter adapter;
    private List<Alarm> alarmList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddAlarm, fabGeo, fabGallery, fabCamera;

    private DatabaseHelper db;

    private static final int REQUEST_LOCATION_PERMISSON = 1000;
    String[] locationPermissons = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQUEST_GALLERY_PERMISSON = 2000;
    String[] galleryPermissons = {
            Manifest.permission.READ_EXTERNAL_STORAGE   // Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CAMERA_PERMISSON = 3000;
    String[] cameraPermissons = {
            Manifest.permission.CAMERA
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view_alarm);
        fabAddAlarm = findViewById(R.id.fab_add_alarm);
        fabGeo = findViewById(R.id.fab_geo);
        fabGallery = findViewById(R.id.fab_gallery);
        fabCamera = findViewById(R.id.fab_camera);

        db = new DatabaseHelper(this);
        alarmList.addAll(db.getAllAlarms());

        fabAddAlarm.setOnClickListener(this);
        fabGeo.setOnClickListener(this);
        fabGallery.setOnClickListener(this);
        fabCamera.setOnClickListener(this);

        adapter = new RecyclerAlarmAdapter(this, alarmList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent openAlarm = new Intent(MainActivity.this, AlarmDetailsActivity.class);
                int dbId = alarmList.get(position).getId();
                int rvAdapterPosition = recyclerView.getChildAdapterPosition(view);
                openAlarm.putExtra(AlarmDetailsActivity.DB_ID, dbId);
                openAlarm.putExtra(AlarmDetailsActivity.RV_ADAPTER_POSITION, rvAdapterPosition);
        //        openAlarm.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(openAlarm, REQUEST_OPEN_ALARM);
                Toast.makeText(MainActivity.this, "Clicked on " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                int dbId = alarmList.get(position).getId();
                int rvAdapterPosition = recyclerView.getChildAdapterPosition(view);
                Alarm alarmDelete = db.getAlarm(dbId);
                db.deleteAlarm(alarmDelete);
                alarmList.clear();
                alarmList.addAll(db.getAllAlarms());
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Long clicked on " + position, Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // получаем экшн(Stop service) интента из уведомления
        Intent fromNotification = getIntent();
        if (fromNotification != null && fromNotification.getAction().equals("Stop service")) {
            Intent stopIntent = new Intent(this, RingtoneService.class);
            stopService(stopIntent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_add_alarm:
                Alarm alarm = new Alarm();
                alarm.setTriggerTime(Calendar.getInstance().getTime());
                alarm.setWorking(false);
                alarm.setRepeatable(false);

                long id = db.insertAlarm(alarm);
                Alarm alarmFromDB = db.getAlarm(id);

                if (alarmFromDB != null) {
                    alarmList.add(0, alarmFromDB);
                    adapter.notifyDataSetChanged();
                }
                break;

            case R.id.fab_geo:

                if (checkAndRequestPermissons(locationPermissons, REQUEST_LOCATION_PERMISSON)) {
                    Intent geoIntent = new Intent(this, GPSActivity.class);
                    startActivity(geoIntent);
                }

                break;

            case R.id.fab_gallery:

                if (checkAndRequestPermissons(galleryPermissons, REQUEST_GALLERY_PERMISSON)) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivity(galleryIntent);
                }

                break;

            case R.id.fab_camera:

                if (checkAndRequestPermissons(cameraPermissons, REQUEST_CAMERA_PERMISSON)) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null)
                        startActivity(cameraIntent);
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_ALARM && resultCode == RESULT_OK) {
            int id = data.getIntExtra(AlarmDetailsActivity.DB_ID, -1);
            int rvAdapterPotision = data.getIntExtra(AlarmDetailsActivity.RV_ADAPTER_POSITION, -1);
            alarmList.clear();
            alarmList.addAll(db.getAllAlarms());
            adapter.notifyDataSetChanged();
        }
    }

    public boolean checkAndRequestPermissons(String[] wantedPermissions, int requestId) {
        List<String> listPermsNeeded = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                listPermsNeeded.add(perm);
        }

        if (!listPermsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermsNeeded.toArray(new String[listPermsNeeded.size()]), requestId);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted;
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSON:
                granted = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        granted = false;
                }
                if (granted) {
                    Intent geoIntent = new Intent(this, GPSActivity.class);
                    startActivity(geoIntent);
                }
                break;

            case REQUEST_GALLERY_PERMISSON:
                granted = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        granted = false;
                }
                if (granted) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivity(galleryIntent);
                }
                break;

            case REQUEST_CAMERA_PERMISSON:
                granted = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        granted = false;
                }
                if (granted) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null)
                        startActivity(cameraIntent);
                }
                break;
        }
    }
}
