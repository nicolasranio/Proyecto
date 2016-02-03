package com.acme.proyecto.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;

import android.os.Looper;
import android.support.annotation.Nullable;

import android.util.Log;

import com.acme.proyecto.R;
import com.acme.proyecto.data.DataAccessGPS;

import com.acme.proyecto.data.DataAccessLocal;
import com.acme.proyecto.utils.Constantes;
import com.acme.proyecto.utils.VolleySingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ServicioSincroBD extends Service {

    private DataAccessGPS dataAccessGPS;
    private DataAccessLocal dataAccessLocal;
    TimerTask timerTask;
    private final DateFormat timeFormat = DateFormat.getTimeInstance(); //new SimpleDateFormat("HH/mm/ss");
    private final DateFormat dateFormat = DateFormat.getDateInstance(); //new SimpleDateFormat("dd/MM/yyyy");

    //poner en cada json el imei y la password de admin para que se acepte la coordenada

    public ServicioSincroBD() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("SERVICESincro", "Servicio sincro creado");
        dataAccessGPS = DataAccessGPS.getInstance(getApplicationContext());
        dataAccessLocal = DataAccessLocal.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ServiceSincro", "Servicio iniciado...");

        Bundle data = dataAccessLocal.consultar();
        String serverIp = data.getString("server");
        String port = data.getString("port");
        final String postURL = "http://" + serverIp + ":" + port + "/" + Constantes.postTarget;
        System.out.println(postURL);

        Timer timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                if (getNetworkState()) {
                    try {
                        httpSync(postURL);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("ServiceSincro", "no hay conexion a internet");
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, Constantes.SYNC_INTERVAL);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        timerTask.cancel();
        super.onDestroy();
        Log.d("SERVICESincro", "Servicio sincro destruido");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Fuerza a que se inicie la tarea, saltando TimerTask
     */
    public void forceSync() {
        //forzar a que comienze la tarea. Realizar la llamada desde el boton forzar sincronizacion
        //httpSync();
    }


    /**
     * Valida conexion a internet
     *
     * @return true si hay conexion a internet
     */
    private boolean getNetworkState() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    /**
     * Peticion HTTP mediante Volley
     *
     * @param postURL URL del servicio web
     */
    public void httpSync(String postURL) throws JSONException {

        String stream = dataAccessGPS.crearJSONLocation();
        if (stream != null) {
            JSONArray json = new JSONArray(stream);

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(
                    new JsonArrayRequest(
                            Request.Method.POST,
                            postURL,
                            json,
                            new Response.Listener<JSONArray>() {

                                @Override
                                public void onResponse(JSONArray response) {
                                    procesarRespuesta(response);
                                }
                            },
                            new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Volley", "Error Volley: " + error.getMessage());
                                }

                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            headers.put("Accept", "application/json");
                            return headers;
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8" + getParamsEncoding();
                        }
                    }
            );
        }
    }


    /**
     * Procesa el array JSON de respuesta del lado servidor
     *
     * @param response JSONArray respuesta del servidor
     */
    private void procesarRespuesta(JSONArray response) {

        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = (JSONObject) response.get(i);
                System.out.println(obj.get("id"));
                System.out.println(obj.get("status"));
                dataAccessGPS.updateSyncStatus(obj.get("id").toString(), obj.get("status").toString());
                Date ahora = new Date();
                String syncDate = dateFormat.format(ahora);
                String syncTime = timeFormat.format(ahora);
                dataAccessLocal.updateLastSincro(syncDate, syncTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}