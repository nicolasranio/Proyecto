package com.acme.proyecto;

import android.app.ActivityManager;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class ConfigFragment extends Fragment {

    private static DataAccessLocal datos;
    private String SERVICE_NAME = "com.acme.proyecto.ServicioGPSResidente";
    private boolean estadoServicioGPS;

    // newInstance constructor for creating fragment with arguments
    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datos = new DataAccessLocal(getActivity());
        // Local Broadcast Receiver
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ActualizarCampos();
            }
        }, new IntentFilter("BDLOCAL_UPDATE"));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        inicializar();
        ActualizarCampos();

    }

    public void inicializar() {
        if (this.isVisible()) { //si el fragment esta activo
            try {
                EditText pwd = (EditText) this.getView().findViewById(R.id.et_pwrd);
                pwd.setText("");
                this.getView().findViewById(R.id.btn_test).setEnabled(false);
                this.getView().findViewById(R.id.btn_sincro).setEnabled(false);
                this.getView().findViewById(R.id.btn_save).setEnabled(false);
                this.getView().findViewById(R.id.et_name).setEnabled(false);
                this.getView().findViewById(R.id.et_server).setEnabled(false);
                this.getView().findViewById(R.id.sw_service).setEnabled(false);
                this.getView().findViewById(R.id.et_pwrd).requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //verifica el estado encendido del servicio
    private void EstadoServicioGPS() {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(getContext().ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serv : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.i("Servicio", serv.service.getClassName());
            if (SERVICE_NAME.equals(serv.service.getClassName())) {
                Toast.makeText(getContext(), "EL servicio GPS ya se encuentra iniciado", Toast.LENGTH_SHORT).show();
                estadoServicioGPS = true;
            }
        }
        Toast.makeText(getContext(), "EL servicio GPS no esta iniciado", Toast.LENGTH_SHORT).show();
        estadoServicioGPS = false;
    }


    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_config, container, false);
        final Button btnLogin = (Button) rootView.findViewById(R.id.btn_login);
        final EditText etPassword = (EditText) rootView.findViewById(R.id.et_pwrd);
        final Button btnTest = (Button) rootView.findViewById(R.id.btn_test);
        final Button btnSincro = (Button) rootView.findViewById(R.id.btn_sincro);
        final Button btnSave = (Button) rootView.findViewById(R.id.btn_save);
        final EditText etName = (EditText) rootView.findViewById(R.id.et_name);
        final EditText etServer = (EditText) rootView.findViewById(R.id.et_server);
        final Switch swService = (Switch) rootView.findViewById(R.id.sw_service);


        //-----------   Button Handlers   --------------------

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CharSequence pwd = etPassword.getText();
                if (pwd.toString().equals(datos.Consultar().getString("password"))) {
                    etName.setEnabled(true);
                    etName.setText(datos.Consultar().getString("name"));
                    etServer.setEnabled(true);
                    etServer.setText(datos.Consultar().getString("server"));
                    btnTest.setEnabled(true);
                    btnSincro.setEnabled(true);
                    btnSave.setEnabled(true);
                    swService.setEnabled(true);
                    swService.setChecked(estadoServicioGPS);

                } else {
                    etPassword.setText("");
                    Toast.makeText(getContext(),
                            "Password incorrecta", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //-----------------------------

        btnTest.setOnClickListener(new View.OnClickListener() {

            ProgressDialog ringProgressDialog = null;

            public void onClick(View v) {
                String ip = datos.Consultar().getString("server");
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
                        msj = "Conexion exitosa!";
                    } else {
                        image.setImageResource(R.drawable.ic_action_cancel);
                        msj = "Error! Servidor no alcanzable ";
                    }
                    image.setVisibility(View.VISIBLE);
                    ringProgressDialog.dismiss();
                    Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();
                }
            }
        });


        //-------------------------


        btnSincro.setOnClickListener(new View.OnClickListener() {

                                         public void onClick(View v) {
                                         /* realizar un intent al servicio SincroBD para que sincronice las bd.
                                            Ver si hay que hacerlo AsyncTask */
                                         }
                                     }

        );


        //-----------------------

        swService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                 @Override
                                                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                     Intent serviceIntent = new Intent(getContext(), ServicioGPSResidente.class);
                                                     if (isChecked) {
                                                         //enciendo el servicio
                                                         try {
                                                             getContext().startService(serviceIntent);
                                                         } catch (Exception e) {
                                                             e.printStackTrace();
                                                         }
                                                     } else {
                                                         //apago el servicio
                                                         try {
                                                             getContext().stopService(serviceIntent);
                                                         } catch (Exception e) {
                                                             e.printStackTrace();
                                                         }
                                                     }
                                                 }
                                             }
        );


        //---------------------------

        btnSave.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("Guardar cambios")
                        .setMessage("Desea guardar los cambios realizados?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle aux = datos.Consultar();
                                //obtengo el imei del telefono llamando a SystemService
                                TelephonyManager mngr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                                String IMEI_PHONE = mngr.getDeviceId();
                                String msj;
                                //if ((etName.getText().toString().equals(aux.getString("name"))) && (etServer.getText().toString().equals(aux.getString("server"))) && (IMEI_PHONE.equals(aux.getString("imei")))) {
                                if ((etName.getText().toString().equals(aux.getString("name"))) && (etServer.getText().toString().equals(aux.getString("server"))) && (IMEI_PHONE.equals(aux.getString("imei"))) && (swService.isChecked() == estadoServicioGPS)) {
                                    //no se produjeron cambios
                                    msj = "No se produjeron cambios...";
                                } else if (swService.isChecked() != estadoServicioGPS) {
                                    //se cambio el estado del servicio
                                    if (swService.isChecked()) {
                                        msj = "Servicio iniciado";
                                    } else {
                                        msj = "Servicio detenido";
                                    }
                                } else {
                                    //hay cambios que guardar
                                    Bundle args = new Bundle();
                                    args.putString("name", etName.getText().toString());
                                    args.putString("server", etServer.getText().toString());
                                    args.putString("imei", IMEI_PHONE);
                                    if (datos.Actualizar(args)) {
                                        ActualizarCampos();
                                        msj = "Actualizacion correcta";
                                    } else {
                                        msj = "Se produjo un error\n al actualizar los datos";
                                    }
                                }
                                inicializar();
                                Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActualizarCampos();
                                inicializar();
                            }
                        })
                        .show();
            }
        });

        // -- Fin Button Handlers---

        return rootView;
    }

    private void ActualizarCampos() {

        Bundle args = datos.Consultar();
        try {
            EditText etName = (EditText) this.getView().findViewById(R.id.et_name);
            EditText etServer = (EditText) this.getView().findViewById(R.id.et_server);
            etName.setText(args.getString("name"));
            etServer.setText(args.getString("server"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
