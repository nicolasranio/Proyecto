package com.acme.proyecto.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.acme.proyecto.R;
import com.acme.proyecto.data.DataAccessGPS;
import com.acme.proyecto.data.DataAccessLocal;
import com.acme.proyecto.utils.LogFile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;


public class ServicioGPSResidente extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private String IMEI_PHONE;
    private String PHONE_STATE = null;
    private int INTERVAL_UPDATE;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private DataAccessGPS dataAccessGPS;
    private DataAccessLocal dataAccessLocal;
    private final DateFormat timeFormat = DateFormat.getTimeInstance(); //new SimpleDateFormat("HH/mm/ss");
    private final DateFormat dateFormat = DateFormat.getDateInstance(); //new SimpleDateFormat("dd/MM/yyyy");
    private static LogFile logFile;

    public ServicioGPSResidente() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICEGPS", "Servicio creado");
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        IMEI_PHONE = mngr.getDeviceId();
        PHONE_STATE = mngr.getNetworkOperator();
        dataAccessGPS = DataAccessGPS.getInstance(getApplicationContext());
        dataAccessLocal = DataAccessLocal.getInstance(getApplicationContext());
        INTERVAL_UPDATE = Integer.parseInt(dataAccessLocal.consultar().getString("gpsInterval"));
        logFile=new LogFile(getApplicationContext(),getString(R.string.app_name));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (PHONE_STATE!=null){
            startTracking();
        }else{
           Toast.makeText(getApplicationContext(),"Red no disponible \nNo se puede obtener la posicion",Toast.LENGTH_SHORT).show();
        }
        return START_NOT_STICKY;
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


    /**
     * Comienza la tarea de trackeo utilizando la Api de Google Location Services
     */

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

    /**
     * Genera un registro en la bd con los datos de la nueva locacion
     *
     * @param location objeto Location con la latitud y longitud registrados
     */
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

    //-------------------------------------------------------------
    //------------------ Metodos LocationListener------------------
    //-------------------------------------------------------------

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(INTERVAL_UPDATE*1000);
        mLocationRequest.setFastestInterval(INTERVAL_UPDATE*1000 / 2);
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
         //   Toast.makeText(getApplicationContext(), "Ubicacion: " + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        } else {
            Log.i("GPS", "Location es null");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("API", "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopLocationUpdates();
        stopSelf();
        logFile.appendLog("Google Location Services", "GoogleApiClient connection has failed");
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            logFile.appendLog("Google Location Services","GoogleApiClient has been stoped receiving locations");
        }
    }
}
