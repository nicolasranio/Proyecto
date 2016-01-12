package com.acme.proyecto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by nico on 08/01/2016.
 */
public class BootReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Booting Completed", Toast.LENGTH_SHORT).show();
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent intentGPSService = new Intent(context, ServicioGPSResidente.class);
            context.startService(intentGPSService);
            Intent intentSincroService = new Intent(context,ServicioSincroBD.class);
            context.startService(intentSincroService);
       /* if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            //here we start the service
            Intent serviceIntent = new Intent(context, ServicioGPSResidente.class);
            context.startService(serviceIntent);*/
        }
    }
}
