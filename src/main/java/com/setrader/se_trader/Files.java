package com.setrader.se_trader;

import java.io.*;
import java.util.LinkedList;

public class Files {

    // Save GPS in gps.txt in main dir
    public static void writeGPS( String fileName,LinkedList<GPS> gpsArr) throws IOException {
        // Opening File
        String userprofile = System.getenv("USERPROFILE");
        File file = new File( userprofile + "\\Documents\\SE_Trader\\" + fileName);
        file.getParentFile().mkdir();
        FileWriter fileWriter = new FileWriter(file);

        fileWriter.flush();
        //Writing GPS
        for (GPS gps: gpsArr) {
            fileWriter.write(gps.toString() + "\n");
        }
        fileWriter.close();
    }

    // Load GPS from gps.txt from main dir
    public static LinkedList<GPS> readGPS(String fileName) throws IOException {
        LinkedList<GPS> gpsArr = new LinkedList<>();

        try{
            String userprofile = System.getenv("USERPROFILE");
            File file = new File( userprofile + "\\Documents\\SE_Trader\\" + fileName);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while (br.ready()) {
                    gpsArr.add(GPS.makeFromString(br.readLine()));
                }
                br.close();
            }else{
                Files.writeGPS(fileName, gpsArr);
            }
        } catch (Exception e){
            System.err.println("Error: Read File \n" + e.getMessage());
        }

        return gpsArr;
    }
}
