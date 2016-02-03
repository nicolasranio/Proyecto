package com.acme.proyecto.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.acme.proyecto.utils.Constantes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DataAccessGPS extends SQLiteOpenHelper {

    private static DataAccessGPS bdInstance;

    private static String DB_NAME = Constantes.TrackingDB_NAME;
    private static int DB_VERSION = Constantes.TrackingDB_VERSION;
    private String TABLE_NAME = Constantes.TrackingTABLE_NAME;

    //Flags estado sincronizacion
    private final int SINCRO_PEND = Constantes.SINCRO_PEND;
    private final int SINCRO_OK = Constantes.SINCRO_OK;


    private DataAccessGPS(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
    }

    //patron Singleton
    public static synchronized DataAccessGPS getInstance(Context context) {

        if (bdInstance == null) {
            bdInstance = new DataAccessGPS(context.getApplicationContext());
        }
        return bdInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {  //si la tabla no existe se crea una nueva
        if (db.isReadOnly()) {
            db = getWritableDatabase();
        }
        String createTable = "CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY, imei TEXT, fecha TEXT, hora TEXT, latitud TEXT, longitud TEXT, " +
                "update_status INTEGER DEFAULT " + SINCRO_PEND + ")";
        db.execSQL(createTable);
        Log.i("SQL creation", createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * Inserta un nuevo registro de coordenada en la tabla
     * @param datos Bundle con los datos de la coordenada
     */
    public void nuevaCoordenada(Bundle datos) {
        String queryInsert = "INSERT INTO " + TABLE_NAME + " (imei,fecha,hora,latitud,longitud) VALUES ('" + datos.getString("imei") +
                "','" + datos.getString("fecha") + "','" + datos.getString("hora") + "','" + Double.toString(datos.getDouble("lat")) + "','"
                + Double.toString(datos.getDouble("long")) + "')";
        Log.i("SQL", queryInsert);
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

    /**
     * Devuelve todos los registros de la tabla
     * @return Cursor con los registros
     */
    public Cursor getAllLocations() {
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        db.close();
        return cursor;
    }

    /**
     * Crea un array JSON con los registros pendientes de sincronizacion
     * @return String JSON si hay pendientes, null si no hay pendientes.
     */
    public String crearJSONLocation() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE update_status = '" + SINCRO_PEND + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        ArrayList <Location> listLoc = new ArrayList<>();
        Gson gson;
        String json=null;
        if (cursor.moveToFirst()){
            do{
                Location obj = new Location(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
             //   Log.i("location",String.valueOf(obj.getLocId()));
                listLoc.add(obj);
            }while (cursor.moveToNext());
            cursor.close();
            //Use GSON to serialize Array List to JSON
            gson = new GsonBuilder().create();
            json = gson.toJson(listLoc);
        } else {
            Log.i("JSON", "no hay resultados que parsear");
        }
        database.close();
        Log.i("JSON", "Parsed: "+json);
        return json;
    }


    /**
     * Comprueba registros pendientes de sincronizacion
     * @return True si existen registros sin sincronizar
     */
    public boolean getSyncStatus() {
        int cont = 0;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE update_status = '" + SINCRO_PEND + "'";
        Log.i("getSyncStatus", selectQuery);
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cont = cursor.getCount();
        cursor.close();
        database.close();
        return cont == 0;
    }


    /**
     * Actualiza en la tabla el estado del registro sincronizado
     * @param id del registro actualizado
     * @param status de la actualizacion (correcta o fallida)
     */
    public void updateSyncStatus(String id, String status) {
        if (status.equals(Constantes.RESPONSE_OK)) {
            SQLiteDatabase database = this.getWritableDatabase();
            String updateQuery = "UPDATE " + TABLE_NAME + " SET update_status = '" + SINCRO_OK + "' WHERE _id ='" + id + "'";
            Log.i("updateSyncStatus", updateQuery);
            database.execSQL(updateQuery);
            database.close();
        }else{
            Log.i("id fallida",id +","+status);
        }
    }

    /**
     * Elimina de la tabla los registros marcados como sincronizados
     */
    public void depurarTabla(){
        String deleteQuery = "DELETE FROM "+ TABLE_NAME +" WHERE update_status = '"+ SINCRO_OK +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL(deleteQuery);
        database.close();
    }

    /*------------------------------------------------------------*/
    //clase privada para generar objetos del tipo Location
    private class Location {

        int id;
        String imei;
        String fecha;
        String hora;
        String latitud;
        String longitud;

        public Location(int id, String imei, String fecha, String hora, String latitud, String longitud) {
            this.id = id;
            this.imei = imei;
            this.fecha = fecha;
            this.hora = hora;
            this.latitud = latitud;
            this.longitud = longitud;
        }

        public int getId() {
            return id;
        }
    }
}