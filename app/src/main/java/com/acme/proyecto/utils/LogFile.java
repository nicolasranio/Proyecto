package com.acme.proyecto.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;

public class LogFile {

    String name;
    Context context;
    private final DateFormat timeFormat = DateFormat.getTimeInstance(); //new SimpleDateFormat("HH/mm/ss");
    private final DateFormat dateFormat = DateFormat.getDateInstance(); //new SimpleDateFormat("dd/MM/yyyy");

    public LogFile(Context context, String name) {

        this.context = context;
        this.name = "log_" + name + ".log";

    }

    public void appendLog(String tag, String msj) {

        File logFile = new File(context.getExternalFilesDir(null), name);

        if (!logFile.exists()) {
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

    private String getActualTime() {
        Date ahora = new Date();
        return dateFormat.format(ahora) + " " + timeFormat.format(ahora);
    }

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
            while((linea=br.readLine())!=null) {
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