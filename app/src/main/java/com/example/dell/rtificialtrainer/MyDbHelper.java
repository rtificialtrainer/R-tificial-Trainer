package com.example.dell.rtificialtrainer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by DELL on 2016-02-09.
 */
public class MyDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "rtificialtrainer.db";
    public static final int DB_VERSION = 1;

    public MyDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(AthleteTable.CREATE_TABLE_ATHLETE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(AthleteTable.DROP_TABLE_ATHLETE);
        onCreate(db);
    }
}
