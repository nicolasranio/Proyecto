package com.acme.proyecto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by nico on 23/12/2015.
 */

public class StateFragment extends Fragment {

    // Store instance variables
    //private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static StateFragment newInstance(int page) {
        StateFragment frag = new StateFragment();
        Bundle args = new Bundle();
        args.putInt("page", page);
  //      args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("page", 0);
    //    title = getArguments().getString("title");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_state, container, false);

        DataQuery datos = new DataQuery();
        TextView etName = (TextView) rootView.findViewById(R.id.et_name);
        etName.setText(datos.Consultar().getString("name"));
        TextView etImei = (TextView) rootView.findViewById(R.id.et_imei);
        etImei.setText(datos.Consultar().getString("imei"));
        TextView etServer = (TextView) rootView.findViewById(R.id.et_server);
        etServer.setText(datos.Consultar().getString("server"));
        TextView etLastSincro = (TextView) rootView.findViewById(R.id.et_lastact);
        etLastSincro.setText(datos.Consultar().getString("lastsincro"));
        return rootView;
    }
}