package com.acme.proyecto.utils;

/**
 * Clase auxiliar con constantes usadas a lo largo del proyecto
 *
 */
public class Constantes {

    /**
     * Destino del webservice para la sincronizacion de bd
     */
    public static String postTarget = "android_tracker/insertar_locacion.php";

    /**
     * Intervalo de sincronizacion (milisegundos)
     */
    public static int SYNC_INTERVAL = 250000; //25 segundos

    /**
     * Intervalo de trackeo (milisegundos)
     */
    public static int GPS_UPDATE = 10000;  //10 segundos


    //------Base de datos local-----------

    public static String LocalDB_NAME = "DBAppLocal.db";
    public static String LocalTABLE_NAME = "DataApp";
    public static int LocalDB_VERSION = 1;


    //------Base de datos Tracking-----------

    public static String TrackingDB_NAME = "DBTracking.db";
    public static int TrackingDB_VERSION = 1;
    public static String TrackingTABLE_NAME = "TrackInfo";

    public static int SINCRO_PEND = 0;
    public static int SINCRO_OK = 1;

    public static String RESPONSE_OK = "OK";
    public static String RESPONSE_FAIL = "FAIL";

    //--------UI--------------------

    public static String PING_OK = "Conexion exitosa!";
    public static String PING_FAIL = "Error! Servidor no alcanzable ";

}

