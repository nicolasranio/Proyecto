package com.acme.proyecto.data;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.acme.proyecto.utils.Constantes;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DataAccessLocal extends SQLiteAssetHelper {

    private static DataAccessLocal bdInstance;

    private static String DB_NAME = Constantes.LocalDB_NAME;
    private static String TABLE_NAME = Constantes.LocalTABLE_NAME;
    private static int DB_VERSION = Constantes.LocalDB_VERSION;
    private Context context;


    private DataAccessLocal(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }


    //implementacion de Singleton
    public static synchronized DataAccessLocal getInstance(Context context) {

        if (bdInstance == null) {
            bdInstance = new DataAccessLocal(context.getApplicationContext());
        }
        return bdInstance;
    }

    /**
     * Consulta los datos de la bd local
     * @return Bundle del registro recogido
     */
    public Bundle consultar() {
        Bundle arg = new Bundle();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] campos = new String[]{"password", "nombre", "imei", "server", "port", "lastsincro"};
        Cursor cur = db.query(TABLE_NAME, campos, null, null, null, null, null);
        //   if (cur==null) Log.i("Cursor","Cursor nulo");
        if (cur.moveToFirst()) {
            arg.putString("password", cur.getString(0));
            arg.putString("name", cur.getString(1));
            arg.putString("imei", cur.getString(2));
            arg.putString("server", cur.getString(3));
            arg.putString("port", cur.getString(4));
            arg.putString("lastsincro", cur.getString(5));
        }
        cur.close();
        db.close();
        return arg;
    }

    /**
     * Actualiza el registro de la bd local
     * @param datos Bundle con los datos a actualizar
     * @return true si se actualizo correctamente
     */
    public boolean actualizar(Bundle datos) {

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
                e.printStackTrace();
            }
        }
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
     * @param locdate fecha de la actualizacion
     * @param loctime hora de la actualizacion
     */
    public void updateLastSincro(String locdate,String loctime) {
        String parsedDate = locdate+" "+ loctime;
        String queryUpdate = "UPDATE " + TABLE_NAME + " SET lastSincro ='" + parsedDate + "' WHERE id='1'";
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                sendBroadcast();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
