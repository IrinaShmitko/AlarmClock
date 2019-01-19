package com.android.example.alarmclock.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.example.alarmclock.database.model.Alarm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Версия БД
    private static final int DATABASE_VERSION = 1;
    // Название БД
    private static final String DATABASE_NAME = "alarms_db";

    private int alarmId;
    private Date alarmTriggerTime;
    private boolean alarmWorking;
    private boolean alarmRepeatable;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Создание таблицы
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Alarm.CREATE_TABLE);
    }

    // Обновление таблицы
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // Сбрасываем старую таблицу, если существует
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Alarm.TABLE_NAME);

        // Снова создаем таблицу
        sqLiteDatabase.execSQL(Alarm.CREATE_TABLE);
    }

    // В БД храним статусные переменные как 0 и 1
    // Используем этот метод для заполнение класса модели true и false
    private boolean getBooleanFromInteger(Cursor cursor, String columnIndex) {
        if (cursor.getInt(cursor.getColumnIndex(columnIndex)) == 1)
            return true;
        else if (cursor.getInt(cursor.getColumnIndex(columnIndex)) == 0)
            return false;
        else
            return false;
    }

    // Используем этот метод для заполнение БД
    // Вместо true и false помещаем в базу 1 и 0 соотвественно
    private int setIntegerFromBoolean(boolean b) {
        if (b)
            return 1;
        else
            return 0;
    }

    private String setTriggerTimeToDB(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy--MM--dd HH:mm:ss");
        return dateFormat.format(date);
    }

    private Date getTriggerTimeFromDB(String stringDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy--MM--dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(stringDate);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public long insertAlarm(Alarm alarm) {
        // получили ссылку на БД для записи
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Alarm.COLUMN_TRIGGER_TIME, setTriggerTimeToDB(alarm.getTriggerTime()));
        cv.put(Alarm.COLUMN_WORKING, setIntegerFromBoolean(alarm.isWorking()));
        cv.put(Alarm.COLUMN_REPEATABLE, setIntegerFromBoolean(alarm.isRepeatable()));

        long id = db.insert(Alarm.TABLE_NAME, null, cv);
        db.close();

        return id;
    }

    public Alarm getAlarm(long id) {
        SQLiteDatabase db  = this.getReadableDatabase();

        Cursor cursor = db.query(Alarm.TABLE_NAME,
                new String[] {Alarm.COLUMN_ID, Alarm.COLUMN_TRIGGER_TIME, Alarm.COLUMN_WORKING, Alarm.COLUMN_REPEATABLE},
                Alarm.COLUMN_ID + " = ?",
                new String[] {String.valueOf(id)}, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        alarmId = cursor.getInt(cursor.getColumnIndex(Alarm.COLUMN_ID));
        alarmTriggerTime = getTriggerTimeFromDB(cursor.getString(cursor.getColumnIndex(Alarm.COLUMN_TRIGGER_TIME)));;
        alarmWorking = getBooleanFromInteger(cursor, Alarm.COLUMN_WORKING);
        alarmRepeatable = getBooleanFromInteger(cursor, Alarm.COLUMN_REPEATABLE);

        Alarm alarm = new Alarm(alarmId, alarmTriggerTime, alarmWorking, alarmRepeatable);
        cursor.close();

        return alarm;
    }

    public List<Alarm> getAllAlarms() {
        List<Alarm> alarmList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + Alarm.TABLE_NAME + " ORDER BY " +
                Alarm.COLUMN_ID + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                Alarm alarm = new Alarm();

                alarmId = cursor.getInt(cursor.getColumnIndex(Alarm.COLUMN_ID));
                alarmTriggerTime = getTriggerTimeFromDB(cursor.getString(cursor.getColumnIndex(Alarm.COLUMN_TRIGGER_TIME)));
                alarmWorking = getBooleanFromInteger(cursor, Alarm.COLUMN_WORKING);
                alarmRepeatable = getBooleanFromInteger(cursor, Alarm.COLUMN_REPEATABLE);

                alarm.setId(alarmId);
                alarm.setTriggerTime(alarmTriggerTime);
                alarm.setWorking(alarmWorking);
                alarm.setRepeatable(alarmRepeatable);

                alarmList.add(alarm);

            } while (cursor.moveToNext());
        }

        db.close();

        return alarmList;
    }

    public int getAlarmCount() {
        String countQuery = "SELECT * FROM " + Alarm.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public int updateAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(Alarm.COLUMN_TRIGGER_TIME, setTriggerTimeToDB(alarm.getTriggerTime()));
        cv.put(Alarm.COLUMN_WORKING, setIntegerFromBoolean(alarm.isWorking()));
        cv.put(Alarm.COLUMN_REPEATABLE, setIntegerFromBoolean(alarm.isRepeatable()));

        return db.update(Alarm.TABLE_NAME, cv, Alarm.COLUMN_ID + " = ?",
                new String[]{String.valueOf(alarm.getId())});
    }

    public void deleteAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Alarm.TABLE_NAME, Alarm.COLUMN_ID + " = ?", new String[]{String.valueOf(alarm.getId())});
        db.close();
    }
}
