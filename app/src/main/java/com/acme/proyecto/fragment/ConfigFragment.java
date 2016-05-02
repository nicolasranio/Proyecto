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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.acme.proyecto.R;
import com.acme.proyecto.data.DataAccessLocal;
import com.acme.proyecto.service.ServicioGPSResidente;
import com.acme.proyecto.service.ServicioSincroBD;
import com.acme.proyecto.utils.Constantes;
import com.acme.proyecto.utils.Hasher;
import com.acme.proyecto.utils.LogFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ConfigFragment extends Fragment {

    private static DataAccessLocal dataAccessLocal;
    private boolean estadoServicioGPS = false;
    private boolean estadoServicioSincro = false;
    private boolean swGPSOn = false;
    private boolean swSincroOn = false;
    private boolean logged = false;
    private LogFile logfile;


    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataAccessLocal = DataAccessLocal.getInstance(getActivity());
        logfile=new LogFile(getContext(),getString(R.string.app_name));
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
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed() && (!logged)) {
            final Dialog dial = new Dialog(getContext());
            dial.setContentView(R.layout.login_layout);
            dial.setTitle("Login");
            final EditText etPass = (EditText) dial.findViewById(R.id.txtPassword);
            final Button btnLogin = (Button) dial.findViewById(R.id.btn_login);

            btnLogin.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View v) {
                    String pass = dataAccessLocal.consultar().getString("password");
                    Log.i("Hash almacenado", pass);
                    String hash = Hasher.generateHash(etPass.getText().toString());
                    Log.i("Hash ingresado", hash);
                    if (pass.equals(hash)) {
                        dial.dismiss();
                        logged = true;
                        inicializar(1);
                    } else {
                        etPass.setText("");
                        etPass.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.vibration);
                        ((ViewGroup)dial.getWindow().getDecorView()).getChildAt(0).startAnimation(shake);
                        Toast.makeText(getContext(), "Password incorrecta", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dial.setCanceledOnTouchOutside(false);
            dial.show();
        }
    }


    /**
     * Inicializa el estado de los widgets dependiendo el estado del flag
     *
     * @param flag 0 = seteo inicial; 1 = seteo post login
     */
    public void inicializar(int flag) {
        if (this.isVisible()) { //si el fragment esta activo
            try {
                final Button btnTest = (Button) this.getView().findViewById(R.id.btn_test);
                final Button btnLog = (Button) this.getView().findViewById(R.id.btn_viewlog);
                final Button btnSave = (Button) this.getView().findViewById(R.id.btn_save);
                final EditText etName = (EditText) this.getView().findViewById(R.id.et_name);
                final EditText etServer = (EditText) this.getView().findViewById(R.id.et_server);
                final EditText etPuerto = (EditText) this.getView().findViewById(R.id.et_puerto);
                final Switch swServiceGPS = (Switch) this.getView().findViewById(R.id.sw_service_gps);
                final Switch swServiceSincro = (Switch) this.getView().findViewById(R.id.sw_service_sincro);

                switch (flag) {
                    case 0: {
                        btnTest.setEnabled(false);
                        btnLog.setEnabled(false);
                        btnSave.setEnabled(false);
                        etName.setEnabled(false);
                        etServer.setEnabled(false);
                        swServiceGPS.setEnabled(false);
                        swServiceSincro.setEnabled(false);
                        etPuerto.setEnabled(false);
                        break;
                    }
                    case 1: {
                        etName.setEnabled(true);
                        etServer.setEnabled(true);
                        etPuerto.setEnabled(true);
                        btnTest.setEnabled(true);
                        btnLog.setEnabled(true);
                        btnSave.setEnabled(true);
                        actualizarCampos();
                        //inicializo estado Switch GPS
                        estadoServicioGPS = estadoServicio(Constantes.GPS_SERVICE_NAME);  //actualizo estado del servicio
                        swServiceGPS.setEnabled(true);
                        swGPSOn = true;  //cambio valor para usarlo en el cond del switch handler
                        swServiceGPS.setChecked(estadoServicioGPS);
                        //inicializo estado Switch Sincro
                        estadoServicioSincro = estadoServicio(Constantes.SINCRO_SERVICE_NAME);
                        swServiceSincro.setEnabled(true);
                        swSincroOn = true;
                        swServiceSincro.setChecked(estadoServicioSincro);
                        break;
                    }
                    case 2: {

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
     *
     * @param servicio Nombre completo del servicio
     * @return True si el servicio esta activo
     */
    private boolean estadoServicio(String servicio) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(getContext().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serv : manager.getRunningServices(Integer.MAX_VALUE)) {
                  Log.i("Servicio", serv.service.getClassName());
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
        final Button btnLog = (Button) rootView.findViewById(R.id.btn_viewlog);
        final Button btnSave = (Button) rootView.findViewById(R.id.btn_save);
        final EditText etName = (EditText) rootView.findViewById(R.id.et_name);
        final EditText etServer = (EditText) rootView.findViewById(R.id.et_server);
        final EditText etPuerto = (EditText) rootView.findViewById(R.id.et_puerto);
        final Switch swServiceGPS = (Switch) rootView.findViewById(R.id.sw_service_gps);
        final Switch swServiceSincro = (Switch) rootView.findViewById(R.id.sw_service_sincro);


        //-----------   Button Handlers   --------------------


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


        //al hacer click sobre el boton se muestra el archivo log
        btnLog.setOnClickListener(new View.OnClickListener() {

                                      public void onClick(View v) {
                                          final Dialog logDiag = new Dialog(getContext(), R.style.PauseDialog);
                                          logDiag.setContentView(R.layout.logview_layout);
                                          logDiag.setTitle("Log de aplicacion");
                                          TextView logText = (TextView) logDiag.findViewById(R.id.tv_log);
                                          logText.setText(logfile.leerLog());
                                          Button btnBack = (Button) logDiag.findViewById(R.id.btn_logBack);
                                          // if button is clicked, close the custom dialog
                                          btnBack.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  logDiag.dismiss();
                                              }
                                          });
                                          logDiag.show();
                                      }
                                  }
        );


        //-----------------------
        swServiceGPS.setOnClickListener(new CompoundButton.OnClickListener() {

            @Override
            public void onClick(View v) {
      Intent serviceIntent = new Intent(getContext(), ServicioGPSResidente.class);
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

                                    if (dataAccessLocal.actualizarLocal(args)) {
                                        actualizarCampos();
                                        msj = "Actualizacion correcta";
                                    } else {
                                        msj = "Se produjo un error\n al actualizar los dataAccessLocal";
                                    }
                                }
                                logged = false;
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
     *
     * @throws NullPointerException
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

