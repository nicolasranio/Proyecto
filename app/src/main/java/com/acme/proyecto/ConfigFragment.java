package com.acme.proyecto;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by nico on 23/12/2015.
 */
public class ConfigFragment extends Fragment{

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
        View rootView = inflater.inflate(R.layout.fragment_config, container, false);

        DataQuery datos = new DataQuery();
        TextView etPassword = (TextView) rootView.findViewById(R.id.et_pwrd);
       // etPassword.setText(datos.Consultar().getString("password"));

        //poner esto en el handler del boton login
        if (etPassword.equals((datos.Consultar().getString("password")))){


        }else{


        }
        return rootView;
    }
}
