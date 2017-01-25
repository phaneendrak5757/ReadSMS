package com.fincare.readsms;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;

/**
 * Created by Phaneendra on 25-Nov-16.
 */

public class DbHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SMS_fincare";
    private static final String Tbl = "fincare_sms_reg";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PH_NO = "phone_number";
    private static final String email = "email";
    private static final String imie = "imie";
    private static final String last_upd="last_update";

    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + Tbl + "("
                + ID + " INTEGER PRIMARY KEY," + NAME + " TEXT,"
                + PH_NO + " TEXT," + email+ " TEXT, "+imie+" TEXT, "+last_upd+" DATETIME)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + Tbl);

        // Create tables again
        onCreate(db);
    }


    void InsReg(Rege r) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NAME, r.getName()); // Contact Name
        values.put(PH_NO, r.getPhone_number()); // Contact Phone
        values.put(email, r.getEmail());
        values.put(imie, r.getImie());
        values.put(last_upd,"1970-01-01 00:00:01");

        // Inserting Row
        db.insert(Tbl, null, values);
        db.close(); // Closing database connection
    }

    void delte()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Tbl, null,null);
        db.close();
    }

    @SuppressLint("NewApi")
    private String getDateTime() {
         SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public Cursor getReg() {
        String countQuery = "SELECT  * FROM " + Tbl;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();

        // return count
        return cursor;
    }

    public  void update_sts()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("update "+Tbl+" set "+last_upd+"='"+getDateTime()+"'");
        db.close();
    }
}
