package com.android.example.alarmclock.view;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.android.example.alarmclock.R;
import com.android.example.alarmclock.database.model.Alarm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerAlarmAdapter extends RecyclerView.Adapter<RecyclerAlarmAdapter.AlarmViewHolder> {

    private Context context;
    private List<Alarm> alarmList;

    public class AlarmViewHolder extends RecyclerView.ViewHolder {

        private TextView alarmTime;
        private Switch alarmWorking;
        private Switch alarmRepeatable;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            alarmTime = itemView.findViewById(R.id.item_alarm_time);
            alarmWorking = itemView.findViewById(R.id.item_alarm_working);
            alarmRepeatable = itemView.findViewById(R.id.item_alarm_repeatable);

        }
    }

    public RecyclerAlarmAdapter(Context context, List<Alarm> alarmList) {
        this.context = context;
        this.alarmList = alarmList;
    }

    @Override
    public RecyclerAlarmAdapter.AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_recycler_view_item, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerAlarmAdapter.AlarmViewHolder holder, int position) {
        Alarm alarm = alarmList.get(position);

        holder.alarmTime.setText(formatDate(alarm.getTriggerTime()));
        holder.alarmWorking.setChecked(alarm.isWorking());
        holder.alarmRepeatable.setChecked(alarm.isRepeatable());
    }

    @Override
    public int getItemCount() {
        if (!alarmList.isEmpty())
            return alarmList.size();
        else
            return 0;
    }

    private String formatDate(Date date) {
        SimpleDateFormat fmtOut = new SimpleDateFormat("HH:mm");
        return fmtOut.format(date);
    }
}
