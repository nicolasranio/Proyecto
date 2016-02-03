package com.acme.proyecto.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.acme.proyecto.R;
import com.acme.proyecto.data.DataAccessLocal;


public class StateFragment extends Fragment {

    private static DataAccessLocal dataAccessLocal;

    // newInstance constructor for creating fragment with arguments
    public static StateFragment newInstance() {
        return new StateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataAccessLocal = DataAccessLocal.getInstance(getActivity());
        //LocalReceiver para escuchar el intent de actualizacion de la bd
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ActualizarCampos();
            }
        }, new IntentFilter("BDLOCAL_UPDATE"));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_state, container, false);
    }

    //---------------------------------------------------

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ActualizarCampos();
    }


    /**
     * Actualiza los widgets de la vista con los datos de la bd local
     */
    private void ActualizarCampos() {
        Bundle args = dataAccessLocal.consultar();
        try {
            EditText etName = (EditText) this.getView().findViewById(R.id.et_name_r);
            EditText etImei = (EditText) this.getView().findViewById(R.id.et_imei_r);
            EditText etServer = (EditText) this.getView().findViewById(R.id.et_server_r);
            EditText etPort = (EditText) this.getView().findViewById(R.id.et_puerto_r);
            EditText etLastSincro = (EditText) this.getView().findViewById(R.id.et_lastact_r);
            etLastSincro.setText(args.getString("lastsincro"));
            etName.setText(args.getString("name"));
            etImei.setText(args.getString("imei"));
            etServer.setText(args.getString("server"));
            etPort.setText(args.getString("port"));

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}

