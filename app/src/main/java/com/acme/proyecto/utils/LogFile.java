package com.acme.proyecto.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class LogFile {

    private final DateFormat timeFormat = DateFormat.getTimeInstance(); //new SimpleDateFormat("HH/mm/ss");
    private final DateFormat dateFormat = DateFormat.getDateInstance(); //new SimpleDateFormat("dd/MM/yyyy");
    String name;
    Context context;

    public LogFile(Context context, String name) {

        this.context = context;
        this.name = "log_" + name + ".log";

    }

    /**
     *
     * @param tag generador del log
     * @param msj mensaje a loguear
     */
    public void appendLog(String tag, String msj) {

        long maxWeight = 512000;
        File logFile = new File(context.getExternalFilesDir(null), name);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (logFile.length() > maxWeight) {
            logFile.delete();
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(getActualTime());
            buf.append(" - ");
            buf.append(tag);
            buf.append(" - ");
            buf.append(msj);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return formated time string
     */
    private String getActualTime() {
        Date ahora = new Date();
        return dateFormat.format(ahora) + " " + timeFormat.format(ahora);
    }

    /**
     *
     * @return log string
     */
    public String leerLog() {

        File logFile = new File(context.getExternalFilesDir(null), name);
        String log = "";

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String linea;
            while ((linea = br.readLine()) != null) {
                log += linea;
                log += "\n";
            }
            br.close();
        } catch (Exception ex) {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }
        return log;
    }
}