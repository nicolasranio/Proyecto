package com.acme.proyecto;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ServicioGPSResidente extends Service {

    private GPSTask task = null;
    private String IMEI_PHONE;
    private boolean firstTime = true;
    private  boolean mobileNetworkEnabled=false;

    public ServicioGPSResidente() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICEGPS", "Servicio creado");
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEI_PHONE = mngr.getDeviceId();
        mobileNetworkEnabled=getNetworkState();
        if (task == null) task = new GPSTask();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ((firstTime)&&(mobileNetworkEnabled)) {
            task.execute();
            Log.d("TASKGPS", "Tarea iniciada");
            firstTime = false;
        } else if (!mobileNetworkEnabled) {
            Log.d("RED", "Red de datos no disponible");
        }else{Log.d("TASKGPS", "Ya se esta ejecutando la tarea");}
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        task.setContinuar(false);
        Log.d("TASKGPS", "Tarea detenida");
        //     task.cancel(true);
        super.onDestroy();
        Log.d("SERVICEGPS", "Servicio destruido");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean getNetworkState() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(getApplicationContext().getContentResolver(), "mobile_data", 1) == 1;
        } else {
            return Settings.Secure.getInt(getApplicationContext().getContentResolver(), "mobile_data", 1) == 1;
        }
    }


    //--------Clase que define la tarea -------------


    private class GPSTask extends AsyncTask<String, String, String> {

        private static final long INTERVALO_ACTUALIZACION = 10000;  //60seg
        private DataAccessGPS dataAccess;
        private DateFormat timeFormat;
        private DateFormat dateFormat;
        private String time;
        private String date;
        private boolean continuar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataAccess = new DataAccessGPS(getApplicationContext());
            timeFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat = new SimpleDateFormat("dd:MM:yyyy");
            continuar = true;
            //iniciar valores de coordenada
        }


        @Override
        protected String doInBackground(String... params) {
            while (continuar) {
                Date ahora = new Date();
                time = timeFormat.format(ahora);
                date = dateFormat.format(ahora);
                Bundle datos = new Bundle();
                datos.putString("imei", IMEI_PHONE);
                datos.putString("fecha", date);
                datos.putString("hora", time);
                //ver coordenada
                datos.putString("coordenada", null);
                publishProgress(time);
                dataAccess.nuevaCoordenada(datos);
                try {
                    Thread.sleep(INTERVALO_ACTUALIZACION);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        public void setContinuar(boolean c) {
            this.continuar = c;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(getApplicationContext(), "Hora actual: " + values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.cancel(true);
        }
    }
}
