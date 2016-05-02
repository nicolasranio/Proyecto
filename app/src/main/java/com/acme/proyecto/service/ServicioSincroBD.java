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
import com.acme.proyecto.utils.LogFile;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private static LogFile logFile;


    public ServicioSincroBD() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("SERVICESincro", "Servicio sincro creado");
        dataAccessGPS = DataAccessGPS.getInstance(getApplicationContext());
        dataAccessLocal = DataAccessLocal.getInstance(getApplicationContext());
        logFile=new LogFile(getApplicationContext(),getString(R.string.app_name));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ServiceSincro", "Servicio iniciado...");

        Bundle data = dataAccessLocal.consultar();
        String serverIp = data.getString("server");
        String port = data.getString("port");
        imei = data.getString("imei");
        int syncInterval = Integer.parseInt(data.getString("syncInterval"));
        final String postURL = "http://" + serverIp + ":" + port + "/" + Constantes.postTarget;
        final String postConfigURL = "http://" + serverIp + ":" + port + "/" + Constantes.configSincroTarget;

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
                        configSync(postConfigURL);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i("ServiceSincro", "no hay conexion a internet");
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0,syncInterval*1000);
        return START_STICKY;
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
            Log.i("JSON","enviado en LocationSync :"+ jsonArray.toString());
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
                                    Log.d(TAG, "Error Volley LocationUpdate: " + error.getMessage());
                                    logFile.appendLog(TAG, error.getMessage());
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

    /**
     *
     * @param postConfigURL URL destino
     * @throws JSONException
     */
    public void configSync(String postConfigURL) throws JSONException {

        HashMap<String, String> map = new HashMap<>();// Mapeo previo
        map.put("imei", imei);
        map.put("name",dataAccessLocal.consultar().getString("name"));
        map.put("pwd", dataAccessLocal.consultar().getString("password"));
        map.put("estado",String.valueOf(dataAccessLocal.consultar().getInt("estado")));
        map.put("gpsInterval", dataAccessLocal.consultar().getString("gpsInterval"));
        map.put("syncInterval",dataAccessLocal.consultar().getString("syncInterval"));

        JSONObject jobject = new JSONObject(map);
        Log.i("JSON","enviado en config :"+ jobject.toString());

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(
                new JsonObjectRequest(
                        Request.Method.POST,
                        postConfigURL,
                        jobject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Procesar la respuesta del servidor
                                procesarRespuestaConfig(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG, "Error Volley ConfigUpdate: " + error.getMessage());
                                logFile.appendLog(TAG,error.getMessage());
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


    /**
     * Procesa el array JSON de respuesta del lado servidor
     *
     * @param response JSONArray respuesta del servidor
     */
    private void procesarRespuesta(JSONArray response) {

        Log.i(TAG,"respuesta location: "+response);
        try {
            if (((JSONObject) response.get(0)).get("status").toString().equals(Constantes.RESPONSE_IMEI_FAIL)) {
                logFile.appendLog(TAG,"Respuesta del servidor: " + Constantes.RESPONSE_IMEI_FAIL);
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


    /**
     * Procesa el array JSON de respuesta de configuracion del lado servidor
     *
     * @param response JSONArray respuesta del servidor
     */
    private void procesarRespuestaConfig(JSONObject response) {
        Log.i(TAG,"respuesta location: "+response);
        try {
            if ((!response.get("pwd").toString().equals("null")) || (!response.get("name").toString().equals("null")) || (!response.get("gpsInterval").toString().equals("null"))
                || (!response.get("syncInterval").toString().equals("null")) || (!response.get("estado").toString().equals("null"))) {
                Date ahora = new Date();
                String syncDate = dateFormat.format(ahora);
                String syncTime = timeFormat.format(ahora);
                dataAccessLocal.updateLastSincro(syncDate, syncTime);
                    if(!response.get("pwd").toString().equals("null")){
                        Log.i(TAG, "la password es diferente: nueva:" + response.get("pwd").toString());
                        dataAccessLocal.actualizarPassword(response.get("pwd").toString());
                    }
                    if (!response.get("name").toString().equals("null")){
                        Log.i(TAG, "el nombre es diferente: nuevo:" + response.get("name").toString());
                        dataAccessLocal.actualizarNombre(response.get("name").toString());
                    }
                    //completar con datos de config
                if (!response.get("gpsInterval").toString().equals("null")){
                    Log.i(TAG, "el intervalo gps es diferente: nuevo:" + response.get("gpsInterval").toString());
                    dataAccessLocal.actualizarIntervaloGPS(response.get("gpsInterval").toString());
                }
                if (!response.get("syncInterval").toString().equals("null")){
                    Log.i(TAG, "el intervalo de sincronizacion es diferente: nuevo:" + response.get("syncInterval").toString());
                    dataAccessLocal.actualizarIntervaloSincro(response.get("syncInterval").toString());
                }
                if (!response.get("estado").toString().equals("null")){
                    Log.i(TAG, "el estado es diferente: nuevo:" + response.get("estado").toString());
                    dataAccessLocal.actualizarEstado(Integer.parseInt(response.getString("estado")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}