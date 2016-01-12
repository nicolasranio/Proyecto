package com.acme.proyecto;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

/**
 * Created by nico on 08/01/2016.
 */
public class DataAccessGPS extends SQLiteOpenHelper {

    private static String DB_NAME = "DBTracking.db";
    private static int DB_VERSION = 1;
    private Context context;
    private String TABLE_NAME = "TrackInfo";

    public DataAccessGPS(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {  //si la tabla no existe se crea una nueva
        if (db.isReadOnly()) {
            db = getWritableDatabase();
        }
        String createTable = "CREATE TABLE " + TABLE_NAME + " (imei TEXT, fecha TEXT, hora TEXT, coordenada TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void nuevaCoordenada(Bundle datos) {
        String queryInsert = "INSERT INTO " + TABLE_NAME + " (imei,fecha,hora,coordenada) VALUES ('" + datos.getString("imei") +
                "','" + datos.getString("fecha") + "','" + datos.getString("hora") + "','" + datos.getString("coordenada") + "')";
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryInsert);
                db.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}