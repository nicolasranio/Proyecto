package com.acme.proyecto;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DataQuery extends SQLiteAssetHelper {

    private static String DB_NAME = "DBAppLocal.db";
    private static int DB_VERSION = 1;
    private Context context;

    //   private static String sqlCreate = "CREATE TABLE DataApp (password TEXT, nombre TEXT, imei TEXT, server TEXT, lastsincro TEXT)";


    public DataQuery(Context context) {

        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

   /* @Override
    public void onCreate(SQLiteDatabase db) {
        if (db.isReadOnly()) {
            db = getWritableDatabase();
        }
        db.execSQL(sqlCreate);
    }*/


    //consulta la base sql local y levanta los datos de la configuracion
    public Bundle Consultar() {
        Bundle arg = new Bundle();
        //consultar bd local y volcar datos en el bundle
        SQLiteDatabase db = this.getReadableDatabase();  //lectura de base local
        //     Log.i("BASE","RUTA : "+ db.getPath());
        String[] campos = new String[]{"password", "nombre", "imei", "server", "lastsincro"};
        Cursor cur = db.query("DataApp", campos, null, null, null, null, null);
        //   if (cur==null) Log.i("Cursor","Cursor nulo");
        if (cur.moveToFirst()) {
            arg.putString("password", cur.getString(0));
            arg.putString("name", cur.getString(1));
            arg.putString("imei", cur.getString(2));
            arg.putString("server", cur.getString(3));
            arg.putString("lastsincro", cur.getString(4));
            Log.i("Datos leidos", cur.getString(0) + "," + cur.getString(1) + "," + cur.getString(2) + "," + cur.getString(3) + "," +
                    cur.getString(4) + ".");
        }
        db.close();
        return arg;
    }

    public boolean Actualizar(Bundle datos) {

        Boolean retorno = false;
        String queryUpdate = "UPDATE DataApp SET nombre='" + datos.getString("name") + "', server='" + datos.getString("server") +
                "', imei='" + datos.getString("imei") + "' WHERE id='1'";

        //actualizar bd local con las variables
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            try {
                db.execSQL(queryUpdate);
                db.close();
                retorno = true;
                //envio broadcast avisando que se actualizo la BD
                Intent resultsIntent = new Intent("BDLOCAL_UPDATE");
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultsIntent);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //forzar sincronizacion con la bd remota

        //si esta ok
        return retorno;
    }
}
