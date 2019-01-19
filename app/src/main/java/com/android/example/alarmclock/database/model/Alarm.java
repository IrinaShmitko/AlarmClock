package com.android.example.alarmclock.database.model;

import java.util.Date;

public class Alarm {

    public static final String TABLE_NAME = "alarms";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TRIGGER_TIME = "trigger_time";
    public static final String COLUMN_WORKING = "working";
    public static final String COLUMN_REPEATABLE = "repeatable";

    private int id;
    private Date triggerTime;
    private boolean working;
    private boolean repeatable;

    // SQL запрос на создание таблицы
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TRIGGER_TIME + " TEXT, "
                + COLUMN_WORKING + " INTEGER, "
                + COLUMN_REPEATABLE + " INTEGER"
                + ")";

    public Alarm() {

    }

    public Alarm(int id, Date triggerTime, boolean working, boolean repeatable) {
        this.id = id;
        this.triggerTime = triggerTime;
        this.working = working;
        this.repeatable = repeatable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
