package com.setrader.se_trader;

import java.util.ArrayList;
import java.util.LinkedList;

public class Route {
    // Variables
    ArrayList<Integer> gpsIndex = new ArrayList<>();
    double distance = 0;

    // Default Constructor
    public Route() {}

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("R: ");
        for (Integer index : gpsIndex) {
            out.append(Main.gpsArr.get(index).toString()).append("\n");
        }
        out.append(" -- ").append(distance);
        return out.toString();
    }
    public String toStringName(){
        StringBuilder out = new StringBuilder("R: \n");
        for (Integer index : gpsIndex) {
            out.append(index).append(". ").append(Main.gpsArr.get(index).name).append("\n");
        }
        return out.toString();
    }
    public String toStringDist(){
        StringBuilder out = new StringBuilder("R: ");
        //System.out.println(gpsIndex);
        for (int i = 1; i < gpsIndex.size(); i++){
            out.append( Math.round(GPS.distance(Main.gpsArr.get(gpsIndex.get(i - 1)), Main.gpsArr.get(gpsIndex.get(i))) / 1000)).append(" > ");
        }
        out.append(" -- ").append(Math.round(distance));
        return out.toString();
    }
    public String toStringDistShort(){
        return "Route Dist: " + Math.round(distance);
    }

    // Write into file and Main
    public static void write(Route route, LinkedList<GPS> gpsArr){
        LinkedList<GPS> routeGPS = new LinkedList<>();

        //int index: route.gpsIndex
        for (int i = 0; i < route.gpsIndex.size(); i++){
            GPS gps = gpsArr.get(route.gpsIndex.get(i));
            GPS gpsRoute = GPS.makeFromString(gps.toString());
            gpsRoute.color = "#FFBB00";
            gpsRoute.name = String.format("%02d %s", i, gpsRoute.name.replaceAll("\\d","").trim());
            routeGPS.add(gpsRoute);
        }

        Main.routeArr = routeGPS;
        Files.writeGPS("route.txt", routeGPS);
    }
}