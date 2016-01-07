package com.acme.proyecto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;


public class StateFragment extends Fragment {

    private static DataQuery datos;

    // newInstance constructor for creating fragment with arguments
    public static StateFragment newInstance() {
        return new StateFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        datos = new DataQuery(getActivity());
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ActualizarCampos();
            }
        }, new IntentFilter("BDLOCAL_UPDATE"));
    }

    // Inflate the view for the fragment based on layout XML
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

    private void ActualizarCampos() {
        Bundle args = datos.Consultar();
        try {
            EditText etName = (EditText) this.getView().findViewById(R.id.et_name_r);
            EditText etImei = (EditText) this.getView().findViewById(R.id.et_imei_r);
            EditText etServer = (EditText) this.getView().findViewById(R.id.et_server_r);
            EditText etLastSincro = (EditText) this.getView().findViewById(R.id.et_lastact_r);
            etLastSincro.setText(args.getString("lastsincro"));
            etName.setText(args.getString("name"));
            etImei.setText(args.getString("imei"));
            etServer.setText(args.getString("server"));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}

