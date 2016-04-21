package com.acme.proyecto.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.acme.proyecto.R;
import com.acme.proyecto.utils.Constantes;
import com.acme.proyecto.utils.LogFile;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DataAccessLocal extends SQLiteAssetHelper {

    private static DataAccessLocal bdInstance;
    private static final String TAG = DataAccessLocal.class.getSimpleName();

    private static String DB_NAME = Constantes.LocalDB_NAME;
    private static String TABLE_NAME = Constantes.LocalTABLE_NAME;
    private static int DB_VERSION = Constantes.LocalDB_VERSION;
    private Context context;
    LogFile logFile;

    private DataAccessLocal(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        logFile=new LogFile(context,context.getString(R.string.app_name));
    }


    /**
     *
     * @param context contexto
     * @return DataAccessLocal instancia Singleton
     */
    public static synchronized DataAccessLocal getInstance(Context context) {

        if (bdInstance == null) {
            bdInstance = new DataAccessLocal(context.getApplicationContext());
        }
        return bdInstance;
    }

    /**
     * Consulta los datos de la bd local
     *
     * @return Bundle del registro recogido
     */
    public Bundle consultar() {
        Bundle arg = new Bundle();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] campos = new String[]{"password", "nombre", "imei", "server", "port", "lastsincro","gpsscope","syncroscope","estado"};
        Cursor cur = db.query(TABLE_NAME, campos, null, null, null, null, null);
        //   if (cur==null) Log.i("Cursor","Cursor nulo");
        if (cur.moveToFirst()) {
            arg.putString("password", cur.getString(0));
            arg.putString("name", cur.getString(1));
            arg.putString("imei", cur.getString(2));
            arg.putString("server", cur.getString(3));
            arg.putString("port", cur.getString(4));
            arg.putString("lastsincro", cur.getString(5));
            arg.putString("gpsInterval",cur.getString(6));
            arg.putString("syncInterval",cur.getString(7));
            arg.putInt("estado",cur.getInt(8));
        }
        cur.close();
        db.close();
        return arg;
    }


    /**
     *
     * @param datos bundle con datos de configuracion
     * @return true o false
     */
    public boolean actualizarLocal(Bundle datos) {

        Boolean retorno = false;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET nombre='" + datos.getString("name") + "', server='" + datos.getString("server") +
                "', imei='" + datos.getString("imei") + "', port='" + datos.getString("port") + "' WHERE id='1'";
        Log.i("SQL", queryUpdate);
        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG, e.getMessage());
            }
        }
        logFile.appendLog(TAG, Constantes.msjUpdate);
        return retorno;
    }

    /**
     * Actualiza solo la password del registro en la bd local
     *
     * @param password String sin encriptar
     * @return true si se actualizo correctamente
     */
    public boolean actualizarPassword(String password) {
        Boolean retorno = false;
        System.out.println("Hash: "+ password);
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET password='" + password + "' WHERE id='1'";
        Log.i("SQL", queryUpdate);
        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                //sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG, e.getMessage());
            }
        }
        logFile.appendLog(TAG, Constantes.msjUpdatePassword);
        return retorno;
    }


    /**
     * Actualiza el nombre del equipo en la bd local
     *
     * @param nombre String nombre
     * @return true si se actualizo correctamente
     */
    public boolean actualizarNombre(String nombre) {
        Boolean retorno = false;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET nombre='" + nombre + "' WHERE id='1'";
        Log.i("SQL", queryUpdate);
        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG, e.getMessage());
            }
        }
        logFile.appendLog(TAG,Constantes.msjUpdateName);
        return retorno;
    }


    /**
     * Actualiza el estado del equipo en la bd local
     *
     * @param estado int estado  (0=deshabilitado; 1=habilitado)
     * @return true si se actualizo correctamente
     */
    public boolean actualizarEstado(int estado) {
        Boolean retorno = false;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET estado='" + estado + "' WHERE id='1'";
        Log.i("SQL", queryUpdate);
        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG, e.getMessage());
            }
        }
        logFile.appendLog(TAG,Constantes.msjUpdateEstado);
        return retorno;
    }

    /**
     * Actualiza el valor de intervalo GPS en la bd local
     *
     * @param intervalo intervalo gps en segundos
     * @return true o false
     */
    public boolean actualizarIntervaloGPS (String intervalo) {
        Boolean retorno = false;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET gpsscope='" + intervalo + "' WHERE id='1'";
        Log.i("SQL", queryUpdate);
        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG, e.getMessage());
            }
        }
        logFile.appendLog(TAG,Constantes.msjUpdateIntervaloGPS);
        return retorno;
    }

    /**
     * Actualiza el valor de intervalo de sincronizacion en la bd local
     *
     * @param intervalo intervalo de sincronizacion en segundos
     * @return true o false dependiendo del resultado de actualizacion
     */
    public boolean actualizarIntervaloSincro (String intervalo) {
        Boolean retorno = false;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET syncroscope='" + intervalo + "' WHERE id='1'";
        Log.i("SQL", queryUpdate);
        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG, e.getMessage());
            }
        }
        logFile.appendLog(TAG,Constantes.msjUpdateIntervaloSincro);
        return retorno;
    }


    /**
     * Envio un broadcast local alertando que se actualizo la BD local
     */
    private void sendBroadcast() {
        Intent resultsIntent = new Intent("BDLOCAL_UPDATE");
        LocalBroadcastManager.getInstance(context).sendBroadcast(resultsIntent);
    }


    /**
     * Actualiza el registro con la fecha de la ultima actualizacion
     *
     * @param locdate fecha de la actualizacion
     * @param loctime hora de la actualizacion
     */
    public void updateLastSincro(String locdate, String loctime) {
        String parsedDate = locdate + " " + loctime;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET lastSincro ='" + parsedDate + "' WHERE id='1'";
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                sendBroadcast();
            } catch (SQLException e) {
                logFile.appendLog(TAG,e.getMessage());
            }
        }
    }


}
