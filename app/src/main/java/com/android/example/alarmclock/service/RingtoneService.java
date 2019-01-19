package com.android.example.alarmclock.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.example.alarmclock.R;
import com.android.example.alarmclock.database.DatabaseHelper;
import com.android.example.alarmclock.database.model.Alarm;
import com.android.example.alarmclock.view.AlarmDetailsActivity;
import com.android.example.alarmclock.view.MainActivity;

public class RingtoneService extends Service {

    public static final String CMD = "Command";
    public static final String DB_ALARM_ID = "Id";
    public static final String RINGTONE_ID = "RingtoneId";

    private static final String CHANNEL_ID = "Alarm clock main channel";

    private MediaPlayer mediaPlayer;
    private DatabaseHelper db;
    private NotificationManager notificationManager;
    private Notification notification;

    public RingtoneService() {
        db = new DatabaseHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", "Handle");
        String cmd = intent.getStringExtra(RingtoneIntentService.CMD);
        Log.e("Service", "Ringtone service got command from Intent: " + cmd);
        int dbId = intent.getIntExtra(RingtoneIntentService.DB_ALARM_ID, -1);
        Log.e("Service", "Ringtone service got database id from Intent: " + dbId);
        long ringtoneId = intent.getLongExtra(RingtoneIntentService.RINGTONE_ID, -1);
        Log.e("Service", "Ringtone service got ringtone id from Intent: " + ringtoneId);

        Alarm triggeredAlarm = db.getAlarm(dbId);
        triggeredAlarm.setWorking(false);
        db.updateAlarm(triggeredAlarm);
        int hour = triggeredAlarm.getTriggerTime().getHours();
        int minute = triggeredAlarm.getTriggerTime().getMinutes();
        String hourStr = String.valueOf(hour);
        String minuteStr = String.valueOf(minute);
        if (minute < 10)
            minuteStr = "0" + minuteStr;

        int ringtoneResource = getRingtone((int) ringtoneId);
        String ringtoneResourceName = getResources().getResourceName(ringtoneResource);
        getResources().getResourcePackageName(ringtoneResource);



        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm clock main channel";
            String description = "Notification channel for MyAlarmClock";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent alarmIntent = new Intent(this, MainActivity.class);
            // действие, по которому при открытии MainActivity через уведомление срабатывает stopService()
            alarmIntent.setAction("Stop service");
            alarmIntent.putExtra(AlarmDetailsActivity.DB_ID, dbId);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, dbId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                    .setContentTitle("Будильник сработал в " + hourStr + ":" + minuteStr)
                    .setContentText("Мелодия: " + ringtoneResourceName +"\nНажми сюда, чтобы открыть будильник")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            notification = mBuilder.build();
        } else {
            Intent alarmIntent = new Intent(this, MainActivity.class);
            // действие, по которому при открытии MainActivity через уведомление срабатывает stopService()
            alarmIntent.setAction("Stop service");
            alarmIntent.putExtra(AlarmDetailsActivity.DB_ID, dbId);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, dbId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_alarm_black_24dp)
                    .setContentTitle("Будильник сработал в " + hourStr + ":" + minuteStr)
                    .setContentText("Мелодия: " + ringtoneResourceName +"\nНажми сюда, чтобы открыть будильник")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            notification = mBuilder.build();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(dbId, notification);
        else
            notificationManager.notify(dbId, notification);

        mediaPlayer = MediaPlayer.create(this, ringtoneResource);
        mediaPlayer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        // срабатывает при нажатии на уведомление будильника

        Log.e("Service", "On Destroy service called");
        Toast.makeText(this, "On Destroy service called", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            stopForeground(true);
        mediaPlayer.stop();
        mediaPlayer.release();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getRingtone(int ringtone) {
        switch (ringtone) {
            case 0: return R.raw.clock_alarm_pacman_1;
            case 1: return R.raw.clock_alert_htc_one;
            case 2: return R.raw.clock_alert_new_ip6;
            case 3: return R.raw.nature_rhitm;
            case 4: return R.raw.super_nature;
            default: return R.raw.clock_alarm_pacman_1;
        }
    }
}
