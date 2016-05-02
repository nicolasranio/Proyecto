package com.acme.proyecto.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.acme.proyecto.utils.Constantes;

public class BootReceiver extends BroadcastReceiver {

    /**
     *
     * @param context contexto
     * @param intent intent recibido
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Booting Completed", Toast.LENGTH_SHORT).show();
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            /*Intent intentGPSService = new Intent(context, ServicioGPSResidente.class);
            context.startService(intentGPSService);
            Intent intentSincroService = new Intent(context,ServicioSincroBD.class);
            context.startService(intentSincroService);*/
            Intent intentGPSService = new Intent();
            intentGPSService.setAction(Constantes.GPS_SERVICE_NAME);
            context.startService(intentGPSService);
            Intent intentSincroService = new Intent();
            intentSincroService.setAction(Constantes.SINCRO_SERVICE_NAME);
            context.startService(intentSincroService);
        }
    }
}
