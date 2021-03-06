package com.setrader.se_trader;

public class GPS {

    protected String name;
    protected final double x;
    protected final double y;
    protected final double z;
    protected String strX;
    protected String strY;
    protected String strZ;
    protected String color;


    public GPS(String name, double x, double y, double z, String color){
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.strX = String.format("%.2f", x).replaceAll(",", ".");
        this.strY = String.format("%.2f", y).replaceAll(",", ".");
        this.strZ = String.format("%.2f", z).replaceAll(",", ".");
        this.color = color;
    }

    // Will made GPS object from game string
    public static GPS makeFromString(String gpsString){

        // Split
        // Try to parse string to double
        String[] gpsArgs;
        double x, y, z;
        String name, color;
        try{
            gpsArgs = gpsString.split(":", -1);

            x = Double.parseDouble(gpsArgs[2]);
            y = Double.parseDouble(gpsArgs[3]);
            z = Double.parseDouble(gpsArgs[4]);

            name = gpsArgs[1];
            color = gpsArgs[5];
        } catch(NumberFormatException ex){ // handle your exception
            name = "Error";
            color = "#FF0000";
            x = 0; y = 0; z = 0;
            System.err.println("Error GPS: Co-ordinates in the wrong format");
            System.err.println(ex.getMessage());
        } catch (ArrayIndexOutOfBoundsException e){
            name = "Error";
            color = "#FF0000";
            x = 0; y = 0; z = 0;
            System.err.println("Error GPS: Array out off index");
            System.err.println(e.getMessage());
        }

        // Create GPS
        return new GPS(name, x, y, z, color);
    }

    // To string
    public String toString(){
        return "GPS:" + name + ":" + strX + ":" + strY + ":" + strZ + ":" + color + ":";
    }

    public static boolean isGPS(String gpsString){
        try {
            String[] gpsArgs = gpsString.split(":", -1);
            return gpsArgs[0].equals("GPS");
        }catch (Exception e){
            return false;
        }
    }

    // Will return distance in meters, Arguments From, To
    public static Double distance(GPS gps1, GPS gps2){
        // Count difference between X, Y, Z
        double dist_x = gps1.x - gps2.x;
        double dist_y = gps1.y - gps2.y;
        double dist_z = gps1.z - gps2.z;

        return  Math.sqrt(dist_x * dist_x + dist_y * dist_y + dist_z * dist_z);
    }
}
