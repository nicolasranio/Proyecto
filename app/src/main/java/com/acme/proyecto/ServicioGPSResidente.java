package com.acme.proyecto;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ServicioGPSResidente extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //   private UpdateTask taskUpdate = null;
    private String IMEI_PHONE;
    private String PHONE_STATE = null;
    private int INTERVAL_UPDATE = 10000;  //10 segundo
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private DataAccessGPS dataAccessGPS;
    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy");

    public ServicioGPSResidente() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICEGPS", "Servicio creado");
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEI_PHONE = mngr.getDeviceId();
        PHONE_STATE = mngr.getNetworkOperator();
      //  Log.i("Operador",PHONE_STATE);
        dataAccessGPS = new DataAccessGPS(getApplicationContext());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
     //if (mobileNetworkEnabled) {
        if (PHONE_STATE!=null){
            startTracking();
        }else{
           Toast.makeText(getApplicationContext(),"Red no disponible \nNo se puede obtener la posicion",Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d("TRACKING", "startTracking");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
        Log.d("SERVICEGPS", "Servicio destruido");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //-------------------------------------------------------------
    //------------------ Metodos LocationListener------------------
    //-------------------------------------------------------------

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(INTERVAL_UPDATE);
        mLocationRequest.setFastestInterval(INTERVAL_UPDATE / 2);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            updateUbicacion(location);
            Log.i("GPS", "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());
            Toast.makeText(getApplicationContext(), "Ubicacion: " + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        } else {
            Log.i("GPS", "Location es null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("API", "GoogleApiClient connection has been suspend");
    }

    @NonNull
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
        stopSelf();
        Log.i("API", "GoogleApiClient connection has failed");
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void updateUbicacion(Location location){

        Date ahora = new Date();
        String time = timeFormat.format(ahora);
        String date = dateFormat.format(ahora);
        Bundle datos = new Bundle();
        datos.putString("imei", IMEI_PHONE);
        datos.putString("fecha", date);
        datos.putString("hora", time);
        datos.putDouble("lat", location.getLatitude());
        datos.putDouble("long", location.getLongitude());
        dataAccessGPS.nuevaCoordenada(datos);
    }

    //--------Clase que define la tarea donde se completan los datos-------------

/*
    private class UpdateTask extends AsyncTask<String, String, String> {

        private static final long INTERVALO_ACTUALIZACION = 10000;  //10seg
        private LocationTask taskLoc;
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
            taskLoc= new LocationTask();
            taskLoc.execute();
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
                datos.putString("coordenada", locActual.toString());
                publishProgress(time, locActual.toString());
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
            Toast.makeText(getApplicationContext(), "Hora: " + values[0] + "\nCoordenada: " + values[1], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.cancel(true);
        }*/



}
