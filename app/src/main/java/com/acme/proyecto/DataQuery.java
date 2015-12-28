package com.acme.proyecto;

import android.app.AlertDialog;
import android.os.Bundle;


public class DataQuery {

    public DataQuery() {
        //crear la conexion con la bd local

    }

    //consulta la base sql local y levanta los datos de la configuracion
    public Bundle Consultar() {
        Bundle datos = new Bundle();

        //consultar bd local y volcar datos en el bundle


        datos.putString("password","12345");
        datos.putString("name","pepito");
        datos.putString("imei","22331122");
        datos.putString("server","192.168.16.104");
        datos.putString("lastsincro","24/12/2015 00:13");
        return datos;
    }

    public boolean Actualizar (Bundle datos){

        String name = datos.getString("name");
        String servidor = datos.getString("server");

        //actualizar bd local con las variables

        //si esta ok
        return true;
    }

}
