package com.example.crimeintelcompanion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "crimeintel.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CASES = "cases";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIR_NUMBER = "fir_number";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_POLICE_STATION = "police_station";
    private static final String COLUMN_LAT = "latitude";
    private static final String COLUMN_LON = "longitude";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_COMPLAINANT = "complainant";
    private static final String COLUMN_CONTACT = "contact";
    private static final String COLUMN_INCIDENT = "incident_summary";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_CASES = "CREATE TABLE " + TABLE_CASES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIR_NUMBER + " TEXT UNIQUE, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_TYPE + " TEXT, "
                + COLUMN_STATUS + " TEXT, "
                + COLUMN_POLICE_STATION + " TEXT, "
                + COLUMN_LAT + " TEXT, "
                + COLUMN_LON + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_COMPLAINANT + " TEXT, "
                + COLUMN_CONTACT + " TEXT, "
                + COLUMN_INCIDENT + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_CASES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CASES);
        onCreate(db);
    }

    // Insert a new case
    public boolean insertCase(HashMap<String, String> caseData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIR_NUMBER, caseData.get("fir_number"));
        values.put(COLUMN_TITLE, caseData.get("title"));
        values.put(COLUMN_TYPE, caseData.get("type"));
        values.put(COLUMN_STATUS, caseData.get("status"));
        values.put(COLUMN_POLICE_STATION, caseData.get("police_station"));
        values.put(COLUMN_LAT, caseData.get("latitude"));
        values.put(COLUMN_LON, caseData.get("longitude"));
        values.put(COLUMN_ADDRESS, caseData.get("address"));
        values.put(COLUMN_COMPLAINANT, caseData.get("complainant"));
        values.put(COLUMN_CONTACT, caseData.get("contact"));
        values.put(COLUMN_INCIDENT, caseData.get("incident_summary"));
        long result = db.insert(TABLE_CASES, null, values);
        db.close();
        return result != -1;
    }

    // Fetch all cases (for Spinner)
    public ArrayList<String> getAllCases() {
        ArrayList<String> caseTitles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TITLE + " FROM " + TABLE_CASES, null);
        if (cursor.moveToFirst()) {
            do {
                caseTitles.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return caseTitles;
    }

    // Fetch a specific case overview
    public HashMap<String, String> getCaseOverview(String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CASES + " WHERE " + COLUMN_TITLE + "=?", new String[]{title});
        HashMap<String, String> caseMap = new HashMap<>();
        if (cursor.moveToFirst()) {
            caseMap.put("fir_number", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIR_NUMBER)));
            caseMap.put("title", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            caseMap.put("type", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
            caseMap.put("status", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
            caseMap.put("police_station", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POLICE_STATION)));
            caseMap.put("address", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
            caseMap.put("complainant", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPLAINANT)));
            caseMap.put("incident_summary", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INCIDENT)));
        }
        cursor.close();
        db.close();
        return caseMap;
    }
}
