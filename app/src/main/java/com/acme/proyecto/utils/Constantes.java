package com.acme.proyecto.utils;

/**
 * Clase auxiliar con constantes usadas a lo largo del proyecto
 *
 */
public class Constantes {

    /**
     * Destino del webservice para la sincronizacion de bd
     */
    public static String postTarget = "buscadroid/backend/web_service/insertar_locacion.php";
    public static String configSincroTarget = "buscadroid/backend/web_service/sincronizar_config.php";

    public static String GPS_SERVICE_NAME = "com.acme.proyecto.service.ServicioGPSResidente";
    public static String SINCRO_SERVICE_NAME = "com.acme.proyecto.service.ServicioSincroBD";

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
    //public static String RESPONSE_FAIL = "FAIL";
    public static String RESPONSE_IMEI_FAIL = "IMEI_NOT_EXIST";

    //--------UI--------------------

    public static String PING_OK = "Conexion exitosa!";
    public static String PING_FAIL = "Error! Servidor no alcanzable ";

    //-----Hashing------------------

    //public static String SALT = "Ui,Lc6.3=2*c";
    //public static String ADDEDHASH = "$2a$10$";

    //-----Mensajes-----------------

    public static String msjUpdate = "Datos en BD local actualizados";
    public static String msjUpdateName = "Nombre de dispositivo en BD local actualizado";
    public static String msjUpdatePassword = "Password de dispositivo en BD local actualizado";
    public static String msjUpdateEstado = "Estado del dispositivo actualizado";
    public static String msjUpdateIntervaloGPS = "Intervalo de coordenadas GPS actualizado";
    public static String msjUpdateIntervaloSincro = "Intervalo de sincronizacion actualizado";
}

