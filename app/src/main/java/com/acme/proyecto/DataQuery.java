package com.acme.proyecto;

import android.os.Bundle;

/**
 * Created by nico on 25/11/2015.
 */
public class DataQuery {

    public DataQuery() {
        //crear la conexion con la bd local

    }

    //consulta la base sql local y levanta los datos de la configuracion
    public Bundle Consultar() {
        Bundle datos = new Bundle();

        String pwd = "12345";
        datos.putString("password",pwd);
        datos.putString("name","pepito");
        datos.putString("imei","22331122");
        datos.putString("server","10.220.332.44");
        datos.putString("lastsincro","24/12/2015 00:13");
        return datos;
    }

}
