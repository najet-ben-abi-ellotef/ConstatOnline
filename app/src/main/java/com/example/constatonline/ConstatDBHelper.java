package com.example.constatonline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ConstatDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "constat.db";
    private static final int DB_VERSION = 2; // Augmenter si modification structure DB

    // Table utilisateur unique
    private static final String TABLE_USER = "user";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";

    // Table constats
    private static final String TABLE_CONSTATS = "constats";
    private static final String COL_ID = "id";
    private static final String COL_DATE = "date";
    private static final String COL_V1 = "vehicule1";
    private static final String COL_V2 = "vehicule2";
    private static final String COL_LAT = "latitude";
    private static final String COL_LON = "longitude";
    private static final String COL_PLACE = "place";
    private static final String COL_PHOTOS = "photos"; // Stockage photos encodées en String (Base64)

    public ConstatDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table utilisateur
        String createUser = "CREATE TABLE " + TABLE_USER + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_USER_EMAIL + " TEXT UNIQUE," +
                COL_USER_PASSWORD + " TEXT)";
        db.execSQL(createUser);

        // Table constats
        String createConstats = "CREATE TABLE " + TABLE_CONSTATS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_DATE + " TEXT," +
                COL_V1 + " TEXT," +
                COL_V2 + " TEXT," +
                COL_LAT + " REAL," +
                COL_LON + " REAL," +
                COL_PLACE + " TEXT," +
                COL_PHOTOS + " TEXT)";
        db.execSQL(createConstats);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSTATS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // ----------------- Gestion utilisateur -----------------
    public long registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_EMAIL, email);
        cv.put(COL_USER_PASSWORD, password);
        return db.insert(TABLE_USER, null, cv);
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null,
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        boolean success = cursor.moveToFirst();
        cursor.close();
        return success;
    }

    public boolean isUserRegistered() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }
    // ----------------- Gestion constats -----------------
    public long addConstat(String date, String v1, String v2, ArrayList<byte[]> photos, double lat, double lon, String place) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Convertir les photos en Base64 concaténées
        StringBuilder photosStr = new StringBuilder();
        for (byte[] p : photos) {
            String encoded = android.util.Base64.encodeToString(p, android.util.Base64.DEFAULT);
            photosStr.append(encoded).append(";");
        }

        ContentValues cv = new ContentValues();
        cv.put(COL_DATE, date);
        cv.put(COL_V1, v1);
        cv.put(COL_V2, v2);
        cv.put(COL_LAT, lat);
        cv.put(COL_LON, lon);
        cv.put(COL_PLACE, place);
        cv.put(COL_PHOTOS, photosStr.toString());

        return db.insert(TABLE_CONSTATS, null, cv);
    }
    public int updateConstat(Constat c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_DATE, c.getDate());
        cv.put(COL_V1, c.getVehicule1());
        cv.put(COL_V2, c.getVehicule2());
        cv.put(COL_LAT, c.getLatitude());
        cv.put(COL_LON, c.getLongitude());
        cv.put(COL_PLACE, c.getPlace());

        // Convertir photos en Base64 concaténées
        StringBuilder photosStr = new StringBuilder();
        for (byte[] p : c.getPhotos()) {
            String encoded = android.util.Base64.encodeToString(p, android.util.Base64.DEFAULT);
            photosStr.append(encoded).append(";");
        }
        cv.put(COL_PHOTOS, photosStr.toString());

        return db.update(TABLE_CONSTATS, cv, COL_ID + "=?", new String[]{String.valueOf(c.getId())});
    }
    public int deleteConstat(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CONSTATS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }
    public ArrayList<Constat> getAllConstats() {
        ArrayList<Constat> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONSTATS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String v1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_V1));
                String v2 = cursor.getString(cursor.getColumnIndexOrThrow(COL_V2));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON));
                String place = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLACE));
                String photosStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTOS));

                ArrayList<byte[]> photosList = new ArrayList<>();
                if (photosStr != null && !photosStr.isEmpty()) {
                    String[] arr = photosStr.split(";");
                    for (String s : arr) {
                        if (!s.isEmpty()) {
                            photosList.add(android.util.Base64.decode(s, android.util.Base64.DEFAULT));
                        }
                    }
                }

                list.add(new Constat(id, date, v1, v2, photosList, lat, lon, place));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public Constat getConstat(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONSTATS, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
            String v1 = cursor.getString(cursor.getColumnIndexOrThrow(COL_V1));
            String v2 = cursor.getString(cursor.getColumnIndexOrThrow(COL_V2));
            double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LAT));
            double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LON));
            String place = cursor.getString(cursor.getColumnIndexOrThrow(COL_PLACE));
            String photosStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTOS));

            ArrayList<byte[]> photosList = new ArrayList<>();
            if (photosStr != null && !photosStr.isEmpty()) {
                String[] arr = photosStr.split(";");
                for (String s : arr) {
                    if (!s.isEmpty()) {
                        photosList.add(android.util.Base64.decode(s, android.util.Base64.DEFAULT));
                    }
                }
            }

            cursor.close();
            return new Constat(id, date, v1, v2, photosList, lat, lon, place);
        }

        return null;
    }
}
