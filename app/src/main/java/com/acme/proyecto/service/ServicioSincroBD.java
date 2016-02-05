package com.acme.proyecto.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.util.Log;

import com.acme.proyecto.R;
import com.acme.proyecto.data.DataAccessGPS;

import com.acme.proyecto.data.DataAccessLocal;
import com.acme.proyecto.utils.Constantes;
import com.acme.proyecto.utils.Hasher;
import com.acme.proyecto.utils.VolleySingleton;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ServicioSincroBD extends Service {

    /**
     * Etiqueta para depuración
     */
    private static final String TAG = ServicioSincroBD.class.getSimpleName();

    private DataAccessGPS dataAccessGPS;
    private DataAccessLocal dataAccessLocal;
    TimerTask timerTask;
    private final DateFormat timeFormat = DateFormat.getTimeInstance(); //new SimpleDateFormat("HH/mm/ss");
    private final DateFormat dateFormat = DateFormat.getDateInstance(); //new SimpleDateFormat("dd/MM/yyyy");
    private String imei;

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
        imei = data.getString("imei");
        final String postURL = "http://" + serverIp + ":" + port + "/" + Constantes.postTarget;
        final String postPwdURL = "http://" + serverIp + ":" + port + "/" + Constantes.passwordSincroTarget;
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
                        passwordSync(postPwdURL);
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
        JSONArray jsonArray;

        String stream = dataAccessGPS.crearJSONLocation();
        if (stream != null) {  //si el stream es vacio => no hay locaciones para postear
            jsonArray = new JSONArray(stream);

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(
                    new JsonArrayRequest(
                            Request.Method.POST,
                            postURL,
                            jsonArray,
                            new Response.Listener<JSONArray>() {

                                @Override
                                public void onResponse(JSONArray response) {
                                    procesarRespuesta(response);
                                }
                            },
                            new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(TAG, "Error Volley Location: " + error.getMessage());
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


    //---------------Sincronizo passwords--------------------------

    public void passwordSync(String postPwdURL) throws JSONException {

        HashMap<String, String> map = new HashMap<>();// Mapeo previo
        map.put("imei", imei);
        map.put("pwd", dataAccessLocal.consultar().getString("password"));
        JSONObject jobject = new JSONObject(map);
        System.out.println(jobject.toString());

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.POST,
                        postPwdURL,
                        jobject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Procesar la respuesta del servidor
                                System.out.println("Respuesta password: " + response.toString());
                                procesarRespuestaPwd(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error Volley Password: " + error.getMessage());
                            }
                        }

                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<String, String>();
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


    /**
     * Procesa el array JSON de respuesta del lado servidor
     *
     * @param response JSONArray respuesta del servidor
     */
    private void procesarRespuesta(JSONArray response) {

        try {
            if (((JSONObject) response.get(0)).get("status").toString().equals(Constantes.RESPONSE_IMEI_FAIL)) {
                //crear log en el registro
            } else {
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = (JSONObject) response.get(i);
                    dataAccessGPS.updateSyncStatus(obj.get("id").toString(), obj.get("status").toString());
                    Date ahora = new Date();
                    String syncDate = dateFormat.format(ahora);
                    String syncTime = timeFormat.format(ahora);
                    dataAccessLocal.updateLastSincro(syncDate, syncTime);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void procesarRespuestaPwd(JSONObject response) {
        try {
            if (!response.get("pwd").toString().equals("null")) {
                Log.i(TAG, "la password es diferente: nueva:" + response.get("pwd").toString());
                //escribir en el log
                dataAccessLocal.actualizarPassword(response.get("pwd").toString());
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