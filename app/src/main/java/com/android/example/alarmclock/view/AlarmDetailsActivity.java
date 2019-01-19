package com.android.example.alarmclock.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.example.alarmclock.R;
import com.android.example.alarmclock.database.DatabaseHelper;
import com.android.example.alarmclock.database.model.Alarm;
import com.android.example.alarmclock.reciever.AlarmReceiver;
import com.android.example.alarmclock.service.RingtoneIntentService;

import java.util.Calendar;
import java.util.Date;

public class AlarmDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    public static final String DB_ID = "DB_ID";
    public static final String RV_ADAPTER_POSITION = "RV_ADAPTER_POSITION";

    private TimePicker timePicker;
    private Switch swWorking, swRepeatable;
    private TextView tvMemo;
    private Button btnStart, btnStop;
    private Spinner spinnerRingtone;

    private int dbId;
    private int rvAdapterPosition;
    private long ringtoneId;

    private DatabaseHelper db;
    private boolean backPressed = false;
    private Alarm alarm;

    private Intent intentAlarm;
    private PendingIntent pendingIntentAlarm;

    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_details);

        timePicker = findViewById(R.id.alarm_details_time_picker);
        swWorking = findViewById(R.id.alarm_details_working);
        swRepeatable = findViewById(R.id.alarm_details_repeatable);
        tvMemo = findViewById(R.id.alarm_details_memo);
        btnStart = findViewById(R.id.alarm_details_btn_start);
        btnStop = findViewById(R.id.alarm_details_btn_stop);
        btnStop.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        spinnerRingtone = findViewById(R.id.alarm_details_song_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.ringtones, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRingtone.setAdapter(spinnerAdapter);
        spinnerRingtone.setOnItemSelectedListener(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent fromMain = getIntent();
        dbId = fromMain.getIntExtra(AlarmDetailsActivity.DB_ID, -1);
        rvAdapterPosition = fromMain.getIntExtra(AlarmDetailsActivity.RV_ADAPTER_POSITION, -1);

        db = new DatabaseHelper(this);
        if (dbId != -1) {
            refreshAlarmDetailsUI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        intentAlarm = null;
        intentAlarm = new Intent(AlarmDetailsActivity.this, AlarmReceiver.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!backPressed) {
            updateDBreturnToMain();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        updateDBreturnToMain();
        backPressed = true;
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_details_btn_start:
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0);

                alarm.setTriggerTime(calendar.getTime());
                alarm.setWorking(true);
                alarm.setRepeatable(swRepeatable.isChecked());
                db.updateAlarm(alarm);

                refreshAlarmDetailsUI();

                intentAlarm.putExtra(RingtoneIntentService.CMD, "Set");
                intentAlarm.putExtra(RingtoneIntentService.DB_ALARM_ID, dbId);
                intentAlarm.putExtra(RingtoneIntentService.RINGTONE_ID, ringtoneId);
                pendingIntentAlarm = PendingIntent.getBroadcast(this, dbId, intentAlarm,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getTriggerTime().getTime(), pendingIntentAlarm);
                Toast.makeText(this, "Будильник установлен", Toast.LENGTH_SHORT).show();

                updateDBreturnToMain();
                finish();
                break;

            case R.id.alarm_details_btn_stop:

                alarm.setWorking(false);
                db.updateAlarm(alarm);

                refreshAlarmDetailsUI();

                pendingIntentAlarm = PendingIntent.getBroadcast(this, dbId, intentAlarm,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntentAlarm);

                Toast.makeText(this, "Будильник остановлен", Toast.LENGTH_SHORT).show();

                updateDBreturnToMain();
                finish();
                break;
        }
    }

    public void refreshAlarmDetailsUI() {
        alarm = db.getAlarm(dbId);
        int hour = alarm.getTriggerTime().getHours();
        int minute = alarm.getTriggerTime().getMinutes();
        timePicker.setHour(hour);
        timePicker.setMinute(minute);

        swWorking.setChecked(alarm.isWorking());
        swRepeatable.setChecked(alarm.isRepeatable());

        if (swWorking.isChecked()) {
            String hourStr = String.valueOf(hour);
            String minuteStr = String.valueOf(minute);
            if (minute < 10) {
                minuteStr = "0" + minuteStr;
            }
            tvMemo.setText("Установлен на " + hourStr + ":" + minuteStr);
        } else {
            tvMemo.setText("Не работает");
        }

    }

    public void updateDBreturnToMain() {
        if (dbId != -1) {
            if (!alarm.isWorking()) {

                Date dateUpdate = alarm.getTriggerTime();
                dateUpdate.setHours(timePicker.getHour());
                dateUpdate.setMinutes(timePicker.getMinute());

                alarm.setTriggerTime(dateUpdate);
                alarm.setWorking(swWorking.isChecked());
                alarm.setRepeatable(swRepeatable.isChecked());
                db.updateAlarm(alarm);

            }
            Intent toMain = new Intent(this, MainActivity.class);
            toMain.putExtra(AlarmDetailsActivity.DB_ID, dbId);
            toMain.putExtra(AlarmDetailsActivity.RV_ADAPTER_POSITION, rvAdapterPosition);
            setResult(RESULT_OK, toMain);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ringtoneId = l;
        Toast.makeText(this, "Position " + i + "\nId " + l, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        ringtoneId = 0;
    }
}
