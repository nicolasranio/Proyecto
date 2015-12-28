package com.acme.proyecto;

import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class ConfigFragment extends Fragment {

    // Store instance variables
    // private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static ConfigFragment newInstance(int page) {
        ConfigFragment frag = new ConfigFragment();
        Bundle args = new Bundle();
        args.putInt("page", page);
        //    args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("page", 0);
        //   title = getArguments().getString("title");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_config, container, false);

        final DataQuery datos = new DataQuery();
        final Button btnLogin = (Button) rootView.findViewById(R.id.btn_login);
        final EditText etPassword = (EditText) rootView.findViewById(R.id.et_pwrd);
        final Button btnTest = (Button) rootView.findViewById(R.id.btn_test);
        final Button btnSincro = (Button) rootView.findViewById(R.id.btn_sincro);
        final Button btnSave = (Button) rootView.findViewById(R.id.btn_save);
        etPassword.requestFocus();


        //--  Button Handlers --

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CharSequence pwd = etPassword.getText();
                if (pwd.toString().equals(datos.Consultar().getString("password"))) {
                    EditText etName = (EditText) rootView.findViewById(R.id.et_name);
                    EditText etServer = (EditText) rootView.findViewById(R.id.et_server);
                    etName.setEnabled(true);
                    etName.setText(datos.Consultar().getString("name"));
                    etServer.setEnabled(true);
                    etServer.setText(datos.Consultar().getString("server"));
                    btnTest.setEnabled(true);
                    btnSincro.setEnabled(true);
                    btnSave.setEnabled(true);
                } else {
                    etPassword.setText("");
                    Toast.makeText(getContext(),
                            "Password incorrecta", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //desactiva temporalmente bloqueo de acceso a la red en el hilo principal
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.
                        Builder().permitNetwork().build());
                //----
                String ip = datos.Consultar().getString("server");
                int puerto = 7;
                Log.i("log", "socket " + ip + " : " + puerto);
                try {
                    Socket sk = new Socket(ip, puerto);
                    BufferedReader entrada = new BufferedReader(
                            new InputStreamReader(sk.getInputStream()));
                    PrintWriter salida = new PrintWriter(
                            new OutputStreamWriter(sk.getOutputStream()), true);
                    Log.i("log","enviando...");
                    salida.println("Hola Mundo");
                    Log.i("log","recibiendo ... " + entrada.readLine());
                    sk.close();
                } catch (Exception e) {
                    Log.i("log","error: " + e.toString());
                }
            }


        }

    );


    btnSincro.setOnClickListener(new View.OnClickListener()

    {
        public void onClick (View v){

    }
    }

    );


    btnSave.setOnClickListener(new View.OnClickListener()

    {
        public void onClick (View v){

        Bundle args = new Bundle();
        String msj;
        args.putString("name", (rootView.findViewById(R.id.et_name)).toString());
        args.putString("server", (rootView.findViewById(R.id.et_server)).toString());
        if (datos.Actualizar(args)) {
            msj = "Actualizacion correcta";
        } else {
            msj = "Se produjo un error al actualizar los datos";
        }
        Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();
    }
    }

    );

    // -- Fin Button Handlers---

    return rootView;
}
}
