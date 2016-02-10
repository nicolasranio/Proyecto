package com.acme.proyecto.fragment;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.acme.proyecto.R;
import com.acme.proyecto.data.DataAccessLocal;
import com.acme.proyecto.service.ServicioGPSResidente;
import com.acme.proyecto.service.ServicioSincroBD;
import com.acme.proyecto.utils.Constantes;
import com.acme.proyecto.utils.CustomAlertDialogBuilder;
import com.acme.proyecto.utils.Hasher;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ConfigFragment extends Fragment {

    private static DataAccessLocal dataAccessLocal;
    private String GPS_SERVICE_NAME = "com.acme.proyecto.service.ServicioGPSResidente";
    private String SINCRO_SERVICE_NAME = "com.acme.proyecto.service.ServicioSincroBD";
    private boolean estadoServicioGPS = false;
    private boolean estadoServicioSincro = false;
    private boolean swGPSOn = false;
    private boolean swSincroOn = false;
    private boolean logged = false;

    // newInstance constructor for creating fragment with arguments
    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataAccessLocal = DataAccessLocal.getInstance(getActivity());
        //LocalReceiver para escuchar el intent de actualizacion de la bd
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    actualizarCampos();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }, new IntentFilter("BDLOCAL_UPDATE"));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        inicializar(0);
        try {
            actualizarCampos();
        }catch (NullPointerException e ){e.printStackTrace();}
    }


    @Override
    public void setUserVisibleHint(boolean visible){
        super.setUserVisibleHint(visible);
        if (visible && isResumed() && (!logged)){
            CustomAlertDialogBuilder dialogBuilder = new CustomAlertDialogBuilder(getContext());
            LayoutInflater inflater = LayoutInflater.from(getContext());
            final View dialogView = inflater.inflate(R.layout.login_layout, null);
            dialogBuilder.setView(dialogView);
            final EditText etPass = (EditText) dialogView.findViewById(R.id.txtPassword);

            dialogBuilder.setIcon(R.drawable.ic_action_key);
            dialogBuilder.setTitle("Login");
            dialogBuilder.setMessage("Ingrese password de admin");

            dialogBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pass = dataAccessLocal.consultar().getString("password");
                    Log.i("Hash almacenado", pass);
                    String hash = Hasher.generateHash(etPass.getText().toString());
                    Log.i("Hash ingresado", hash);
                    if (pass.equals(hash)) {
                        dialog.dismiss();
                        logged = true;
                        inicializar(1);
                    } else {
                        etPass.setText("");
                        Toast.makeText(getContext(), "Password incorrecta", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // (optional) set whether to dismiss dialog when touching outside
            dialogBuilder.setCanceledOnTouchOutside(false);
            dialogBuilder.show();
        }
    }


    /**
     * Inicializa el estado de los widgets dependiendo el estado del flag
     * @param flag 0 = seteo inicial; 1 = seteo post login
     */
    public void inicializar(int flag) {
        if (this.isVisible()) { //si el fragment esta activo
            try {
                final Button btnTest = (Button) this.getView().findViewById(R.id.btn_test);
                final Button btnSincro = (Button) this.getView().findViewById(R.id.btn_sincro);
                final Button btnSave = (Button) this.getView().findViewById(R.id.btn_save);
                final EditText etName = (EditText) this.getView().findViewById(R.id.et_name);
                final EditText etServer = (EditText) this.getView().findViewById(R.id.et_server);
                final EditText etPuerto = (EditText) this.getView().findViewById(R.id.et_puerto);
                final Switch swServiceGPS = (Switch) this.getView().findViewById(R.id.sw_service_gps);
                final Switch swServiceSincro = (Switch) this.getView().findViewById(R.id.sw_service_sincro);

                switch (flag){
                    case 0:{
                        btnTest.setEnabled(false);
                        btnSincro.setEnabled(false);
                        btnSave.setEnabled(false);
                        etName.setEnabled(false);
                        etServer.setEnabled(false);
                        swServiceGPS.setEnabled(false);
                        swServiceSincro.setEnabled(false);
                        etPuerto.setEnabled(false);
                        break;
                    }
                    case 1:{
                        etName.setEnabled(true);
                        etServer.setEnabled(true);
                        etPuerto.setEnabled(true);
                        btnTest.setEnabled(true);
                        btnSincro.setEnabled(true);
                        btnSave.setEnabled(true);
                        actualizarCampos();
                        //inicializo estado Switch GPS
                        estadoServicioGPS = estadoServicio(GPS_SERVICE_NAME);  //actualizo estado del servicio
                        swServiceGPS.setEnabled(true);
                        swGPSOn = true;  //cambio valor para usarlo en el cond del switch handler
                        swServiceGPS.setChecked(estadoServicioGPS);
                        //inicializo estado Switch Sincro
                        estadoServicioSincro = estadoServicio(SINCRO_SERVICE_NAME);
                        swServiceSincro.setEnabled(true);
                        swSincroOn = true;
                        swServiceSincro.setChecked(estadoServicioSincro);
                        break;
                    }
                    case 2:{

                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Verifica si un servicio se encuentra activo
     * @param servicio Nombre completo del servicio
     * @return True si el servicio esta activo
     */
    private boolean estadoServicio(String servicio) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(getContext().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serv : manager.getRunningServices(Integer.MAX_VALUE)) {
            //      Log.i("Servicio", serv.service.getClassName());
            if (servicio.equals(serv.service.getClassName())) {
                Log.i("Servicio", "EL servicio " + servicio + " ya se encuentra iniciado");
                return true;
            }
        }
        return false;
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_config, container, false);
       // final EditText etPassword = (EditText) rootView.findViewById(R.id.et_pwrd);
       // final Button btnLogin = (Button) rootView.findViewById(R.id.btn_login);
        final Button btnTest = (Button) rootView.findViewById(R.id.btn_test);
        final Button btnSincro = (Button) rootView.findViewById(R.id.btn_sincro);
        final Button btnSave = (Button) rootView.findViewById(R.id.btn_save);
        final EditText etName = (EditText) rootView.findViewById(R.id.et_name);
        final EditText etServer = (EditText) rootView.findViewById(R.id.et_server);
        final EditText etPuerto = (EditText) rootView.findViewById(R.id.et_puerto);
        final Switch swServiceGPS = (Switch) rootView.findViewById(R.id.sw_service_gps);
        final Switch swServiceSincro = (Switch) rootView.findViewById(R.id.sw_service_sincro);


        //-----------   Button Handlers   --------------------

    /*    btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String pass=dataAccessLocal.consultar().getString("password");
                Log.i("Hash almacenado", pass);
                String hash = Hasher.generateHash(etPassword.getText().toString());
                Log.i("Hash ingresado",hash);

                if (pass.equals(hash)) {
                    etName.setEnabled(true);
                    etName.setText(dataAccessLocal.consultar().getString("name"));
                    etServer.setEnabled(true);
                    etServer.setText(dataAccessLocal.consultar().getString("server"));
                    etPuerto.setEnabled(true);
                    etPuerto.setText(dataAccessLocal.consultar().getString("port"));
                    btnTest.setEnabled(true);
                    btnSincro.setEnabled(true);
                    btnSave.setEnabled(true);
                    //inicializo estado Switch GPS
                    estadoServicioGPS = estadoServicio(GPS_SERVICE_NAME);  //actualizo estado del servicio
                    swServiceGPS.setEnabled(true);
                    swGPSOn = true;  //cambio valor para usarlo en el cond del switch handler
                    swServiceGPS.setChecked(estadoServicioGPS);
                    //inicializo estado Switch Sincro
                    estadoServicioSincro = estadoServicio(SINCRO_SERVICE_NAME);
                    swServiceSincro.setEnabled(true);
                    swSincroOn = true;
                    swServiceSincro.setChecked(estadoServicioSincro);
                } else {
                    etPassword.setText("");
                    Toast.makeText(getContext(),
                            "Password incorrecta", Toast.LENGTH_SHORT).show();
                }
            }
        });
*/

        //-----------------------------

        btnTest.setOnClickListener(new View.OnClickListener() {

            ProgressDialog ringProgressDialog = null;

            public void onClick(View v) {
                String ip = dataAccessLocal.consultar().getString("server");
                ringProgressDialog = ProgressDialog.show(getContext(), "Un momento por favor ...", "Verificando conexion", true, false);
                new PingTask().execute(ip);
            }

            class PingTask extends AsyncTask<String, Void, Integer> {

                protected Integer doInBackground(String... params) {
                    Log.i("Mi app", "Empezando hilo en segundo plano");
                    Integer exitVal = 10;
                    try {
                        String line;
                        Process proc = Runtime.getRuntime().exec("ping -c 4 " + params[0]);
                        BufferedReader ebr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                        while ((line = ebr.readLine()) != null)
                            Log.e("FXN-BOOTCLASSPATH", line);
                        BufferedReader obr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        while ((line = obr.readLine()) != null)
                            Log.i("FXN-BOOTCLASSPATH", line);
                        exitVal = proc.waitFor();
                        Log.d("FXN-BOOTCLASSPATH", "exitValue: " + exitVal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return exitVal;
                }

                @Override
                protected void onPostExecute(Integer val) {
                    ImageView image = (ImageView) rootView.findViewById(R.id.img_test);
                    String msj;
                    if (val == 0) {
                        image.setImageResource(R.drawable.ic_action_tick);
                        msj = Constantes.PING_OK;
                    } else {
                        image.setImageResource(R.drawable.ic_action_cancel);
                        msj = Constantes.PING_FAIL;
                    }
                    image.setVisibility(View.VISIBLE);
                    ringProgressDialog.dismiss();
                    Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();
                }
            }
        });


        //-------------------------


        //al hacer click sobre el boton se fuerza la sincronizacion
        btnSincro.setOnClickListener(new View.OnClickListener() {

                                         public void onClick(View v) {

                                             if (estadoServicio(SINCRO_SERVICE_NAME)) {
                                                 //comunicarse con el servicio e indicarle que llame a la funcion forceSync

                                             } else {
                                                 Toast.makeText(getContext(), "Debe iniciar el servicio de sincronizacion", Toast.LENGTH_SHORT).show();
                                             }
                                         }
                                     }

        );


        //-----------------------
        swServiceGPS.setOnClickListener(new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getContext(), ServicioGPSResidente.class);
                Log.i("Swith", "Click en el switch");
                if (swServiceGPS.isChecked()) {
                    //enciendo el servicio
                    getContext().startService(serviceIntent);
                } else {
                    //apago el servicio
                    getContext().stopService(serviceIntent);
                }
                Toast.makeText(getContext(), "Guarde cambios para \n " +
                        "actualizar el servicio", Toast.LENGTH_SHORT).show();
            }
        });


        swServiceGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                    @Override
                                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                        Intent serviceIntent = new Intent(getContext(), ServicioGPSResidente.class);
                                                        if ((isChecked) && (!swGPSOn)) {
                                                            //enciendo el servicio
                                                            getContext().startService(serviceIntent);
                                                        } else if (!isChecked) {
                                                            //apago el servicio
                                                            getContext().stopService(serviceIntent);
                                                        }
                                                    }
                                                }
        );


        //---------------------------

        swServiceSincro.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent servIntent = new Intent(getContext(), ServicioSincroBD.class);
                if (swServiceSincro.isChecked()) {
                    //enciendo el servicio
                    Log.i("switch", "el servicio estaba apagado, se intenta encender");
                    getContext().startService(servIntent);
                } else {
                    //apago el servicio
                    getContext().stopService(servIntent);
                }
                Toast.makeText(getContext(), "Guarde cambios para \n " +
                        "actualizar el servicio", Toast.LENGTH_SHORT).show();
            }
        });

        swServiceSincro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent servInt = new Intent(getContext(), ServicioSincroBD.class);
                if ((isChecked) && (!swSincroOn)) {
                    //enciendo el servicio
                    getContext().startService(servInt);
                } else if (!isChecked) {
                    //apago el servicio
                    getContext().stopService(servInt);
                }
            }
        });


        //--------------------------

        btnSave.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Guardar cambios")
                        .setMessage("Desea guardar los cambios realizados?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle aux = dataAccessLocal.consultar();
                                //obtengo el imei del telefono llamando a SystemService
                                TelephonyManager mngr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                                String IMEI_PHONE = mngr.getDeviceId();
                                String msj;
                                if ((etName.getText().toString().equals(aux.getString("name"))) && (etServer.getText().toString().equals(dataAccessLocal.consultar().getString("server")))
                                        && (etPuerto.getText().toString().equals(dataAccessLocal.consultar().getString("port"))) && (IMEI_PHONE.equals(aux.getString("imei"))) && (swServiceGPS.isChecked() == estadoServicioGPS)
                                        && (swServiceSincro.isChecked() == estadoServicioSincro)) {
                                    //no se produjeron cambios
                                    msj = "No se produjeron cambios...";
                                } else if ((swServiceGPS.isChecked() != estadoServicioGPS) || (swServiceSincro.isChecked() != estadoServicioSincro)) {
                                    //se cambio el estado del servicio
                                    // if (swServiceGPS.isChecked()) {
                                    msj = "Servicio modificado";
                                    // }
                                } else {
                                    //hay cambios que guardar
                                    Bundle args = new Bundle();
                                    args.putString("name", etName.getText().toString());
                                    args.putString("server", etServer.getText().toString());
                                    args.putString("port", etPuerto.getText().toString());
                                    args.putString("imei", IMEI_PHONE);
                                    if (dataAccessLocal.actualizar(args)) {
                                        actualizarCampos();
                                        msj = "Actualizacion correcta";
                                    } else {
                                        msj = "Se produjo un error\n al actualizar los dataAccessLocal";
                                    }
                                }
                                logged=false;
                                inicializar(0);
                                Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                actualizarCampos();
                                inicializar(0);
                            }
                        })
                        .show();
            }
        });

        // -- Fin Button Handlers---

        return rootView;
    }


    /**
     * Actualiza los widgets de la vista con los datos de la bd local
     */
    private void actualizarCampos() throws NullPointerException {

        Bundle args = dataAccessLocal.consultar();
            EditText etName = (EditText) this.getView().findViewById(R.id.et_name);
            EditText etServer = (EditText) this.getView().findViewById(R.id.et_server);
            EditText etPuerto = (EditText) this.getView().findViewById(R.id.et_puerto);
        etName.setText(args.getString("name"));
        etServer.setText(args.getString("server"));
        etPuerto.setText(args.getString("port"));
    }
}

