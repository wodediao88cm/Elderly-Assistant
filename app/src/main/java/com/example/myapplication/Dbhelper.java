package com.example.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.Contract.ContactEntry;

public class Dbhelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mycontacts.db";
    public static final int DATABASE_VERSION = 1;



    public Dbhelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_TABLE = "CREATE TABLE " + ContactEntry.TABLE_NAME + " ("
                + ContactEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // THIS AUTOMATICALLY INCREMENTS THE ID BY 1
                + ContactEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ContactEntry.COLUMN_EMAIL + " TEXT NOT NULL, "
                + ContactEntry.COLUMN_PHONENUMBER + " TEXT NOT NULL, "
                + ContactEntry.COLUMN_TYPEOFCONTACT + " TEXT NOT NULL, "
                + ContactEntry.COLUMN_PICTURE  + " TEXT);";


        db.execSQL(SQL_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getphone(){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor= db.rawQuery(" Select "+ContactEntry.COLUMN_PHONENUMBER + " from "+ContactEntry.TABLE_NAME,null,null);
        return cursor;
    }
    public Cursor dbEmpty(){
        SQLiteDatabase db= this.getReadableDatabase();
        Cursor cursor= db.rawQuery( " SELECT count(*) FROM " +ContactEntry.TABLE_NAME ,null,null);
        return cursor;
    }
}