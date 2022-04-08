package com.setrader.se_trader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.LinkedList;

public class Files {

    private static final Logger logger = LogManager.getLogger(Files.class.getName());

    // Save GPS in gps.txt in main dir
    public static void writeGPS( String fileName,LinkedList<GPS> gpsArr){
        try {
            // Opening File
            String userprofile = System.getenv("USERPROFILE");
            File file = new File( userprofile + "\\Documents\\SE_Trader\\" + fileName);
            if (file.getParentFile().mkdir()){
                logger.info("Dir was created");
            }
            FileWriter fileWriter = new FileWriter(file);

            fileWriter.flush();
            //Writing GPS
            for (GPS gps: gpsArr) {
                fileWriter.write(gps.toString() + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            logger.error("File GPS/Route IOException -> " + fileName);
            e.printStackTrace();
        }
    }

    public static void writeString( String fileName, String text) {
        try {
            // Opening File
            String userprofile = System.getenv("USERPROFILE");
            File file = new File( userprofile + "\\Documents\\SE_Trader\\" + fileName);
            if (file.getParentFile().mkdir()){
                logger.info("Dir was created");
            }
            FileWriter fileWriter;

            fileWriter = new FileWriter(file);

            fileWriter.flush();
            fileWriter.write(text);
            fileWriter.close();
        } catch (IOException e) {
            logger.error("WriteString IOException -> " + fileName);
            e.printStackTrace();
        }
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
            logger.error("Read GPS -> " + fileName + "\n" + e.getMessage());
        }

        return gpsArr;
    }

    public static String readString(String fileName){
       StringBuilder result = new StringBuilder();

        try{
            String userprofile = System.getenv("USERPROFILE");
            File file = new File( userprofile + "\\Documents\\SE_Trader\\" + fileName);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while (br.ready()) {
                    result.append(br.readLine()).append("\n");
                }
                br.close();
            }else{
                Files.writeString(fileName, "");
            }
        } catch (Exception e){
            logger.error("Read String -> " + fileName + "\n" + e.getMessage());
        }

        return result.toString();
    }
}
