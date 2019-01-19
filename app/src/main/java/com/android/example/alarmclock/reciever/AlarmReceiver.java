package com.android.example.alarmclock.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.android.example.alarmclock.service.RingtoneIntentService;
import com.android.example.alarmclock.service.RingtoneService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Reciever", "We are in reciever!");

        String cmd = intent.getStringExtra(RingtoneIntentService.CMD);
        int dbId = intent.getIntExtra(RingtoneIntentService.DB_ALARM_ID, -1);
        long ringtoneId = intent.getLongExtra(RingtoneIntentService.RINGTONE_ID, -1);

    /*    Intent ringtoneServiceIntent = new Intent(context, RingtoneIntentService.class);
        ringtoneServiceIntent.putExtra(RingtoneIntentService.CMD, cmd);
        ringtoneServiceIntent.putExtra(RingtoneIntentService.DB_ALARM_ID, dbId);
        ringtoneServiceIntent.putExtra(RingtoneIntentService.RINGTONE_ID, ringtoneId);  */

        Intent ringtoneServiceIntent = new Intent(context, RingtoneService.class);
        ringtoneServiceIntent.putExtra(RingtoneService.CMD, cmd);
        ringtoneServiceIntent.putExtra(RingtoneService.DB_ALARM_ID, dbId);
        ringtoneServiceIntent.putExtra(RingtoneService.RINGTONE_ID, ringtoneId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(ringtoneServiceIntent);
        } else {
            context.startService(ringtoneServiceIntent);
        }
    }
}
