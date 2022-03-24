package com.setrader.se_trader;

import java.util.Collections;
import java.util.LinkedList;

public class RouteCalculator {

    // Array of All Routes
    public static Route route = new Route();
    private static Double[][] distM = null;
    private static Double currentMinDist = Double.MAX_VALUE;
    public static long numOfCombination = 0;
    public static long numOfDoneRoutes = 0;
    public static int shortestRouteIndex = -1;
    public static int shortestRouteDone = 0;
    public static boolean calculationStop = false;
    
    public RouteCalculator(){
        calculationStop = false;
        currentMinDist = Double.MAX_VALUE;
    }
    
    public static void distMatrix(LinkedList<GPS> gpsArr){
        distM = new Double[gpsArr.size()][gpsArr.size()];

        for (int n = 0; n < gpsArr.size(); n++){
            for (int c = n; c < gpsArr.size(); c++){
                double dist = GPS.distance(gpsArr.get(n), gpsArr.get(c));

                distM[n][c] = dist;
                distM[c][n] = dist;
            }
        }
    }

    public void routeCalculateByShortestDist( Integer[] arrIndex, Integer[] arrRoute, boolean backHome){
        if (calculationStop)
            return;

        int size = arrIndex.length;
        if (size > 0){
            for (Integer index : arrIndex) {

                int indexNew = 0;
                Integer[] arrIndexNew = new Integer[arrIndex.length - 1];
                for (int num : arrIndex) {
                    if (index != num) {
                        arrIndexNew[indexNew] = num;
                        indexNew++;
                    }
                }

                Integer[] arrRouteNew = new Integer[arrRoute.length + 1];
                System.arraycopy(arrRoute, 0, arrRouteNew, 0, arrRoute.length);

                arrRouteNew[arrRouteNew.length - 1] = index;
                routeCalculateByShortestDist(arrIndexNew, arrRouteNew, backHome);
            }
        }else{
            Route r = new Route();

            if (backHome) {
                Integer[] arrRouteN = new Integer[arrRoute.length + 1];
                System.arraycopy(arrRoute, 0, arrRouteN, 0, arrRoute.length);
                arrRouteN[arrRouteN.length - 1] = 0;
                Collections.addAll(r.gpsIndex, arrRouteN);
            }else {
                Collections.addAll(r.gpsIndex, arrRoute);
            }

            r.distance = RouteCalculator.routeDistance(r);

            if (r.distance < currentMinDist) {
                currentMinDist = r.distance;
                Main.rArr.push(r);
            }

            numOfDoneRoutes++;
        }
    }

    // Writing Routes in Arr in main, Arguments: FirstRoute (with start pos), Arr of GPS
    public void routeCalculateByShortestJump(GPS lastGPS,LinkedList<GPS> gpsArr) {
        if (calculationStop)
            return;

        if (!gpsArr.isEmpty()){
            int i_closestGPS = closestGPS( lastGPS, gpsArr);
            GPS currentRoute = gpsArr.remove(i_closestGPS);

            route.gpsIndex.add(Main.gpsArr.indexOf(currentRoute));

            routeCalculateByShortestJump( currentRoute, gpsArr);
        }else{
            if (Controller.backHome)
                route.gpsIndex.add(0);

            route.distance = RouteCalculator.routeDistance(route);
            Route.write(route, Main.gpsArr);

            //System.out.println(route.toString());
            System.out.println(route.toStringDist());
        }
    }

    // Find the Closest GPS from list, Returns index of the GPS, Arguments From, Arr
    public static int closestGPS(GPS fromGPS, LinkedList<GPS> gpsArr){
        // Variables
        double min_dist = Double.MAX_VALUE, dist;
        int min_index = 0;

        // Finding the smallest distance
        for (int i = 0; i < gpsArr.size(); i++) {
            GPS toGPS = gpsArr.get(i);
            if(toGPS != fromGPS){
                dist = GPS.distance(fromGPS, toGPS);

                if (dist < min_dist){
                    min_dist = dist;
                    min_index = i;
                }
            }
        }
        return min_index;
    }

    // Calculate minimal number of distance from arr of routes, Returns Index
    public static int shortestRoute( LinkedList<Route> routesArr) {
        if (routesArr.isEmpty())
            return -1;

        int index = 0;

        int size = routesArr.size();
        double minDist = routesArr.pop().distance;

        while (!routesArr.isEmpty()){
            if (calculationStop)
                return -2;

            Route route = routesArr.pop();
            double dist = route.distance;
            if (minDist > dist) {
                minDist = dist;
                index = Main.rArr.indexOf(route);
            }
            shortestRouteDone++;
        }

        return index;
    }

    /*
    public static int shortestRoute(int indexFrom, int indexTo,LinkedList<Route> routesArr) {
        int index = 0;

        double minDist = routesArr.get(0).distance;
        for (int i = indexFrom; i < indexTo; i++) {
            if (i < routesArr.size()){
                double dist = routesArr.get(i).distance;
                if (minDist > dist) {
                    minDist = dist;
                    index = i;
                }
                Controller.shortestRouteDone++;
            }
        }

        return index;
    }*/

    // Calculate distance in route in km
    public static double routeDistance(Route route){
        // Variable
        double dist = 0;

        for (int i = 0; i < route.gpsIndex.size() - 1; i++){
            if (distM != null)
                dist += distM[route.gpsIndex.get(i)][route.gpsIndex.get(i + 1)] / 1000;
            else
                dist += GPS.distance(Main.gpsArr.get(route.gpsIndex.get(i)), Main.gpsArr.get(route.gpsIndex.get(i + 1))) / 1000;
        }
        return dist;
    }

    /*

    // Calculate minimal number of distance from arr of routes, Returns Index
    public static int shortestRoute(LinkedList<Route> routesArr) {
        int index = 0;
        int size = routesArr.size();

        double minDist = routesArr.get(0).distance;

        for (int i = 0; i < size; i++) {
            if (minDist > routesArr.get(i).distance) {
                minDist = routesArr.get(i).distance;
                index = i;
            }

            double p = (double) i / (double) size;
            if ((p * 100) %1 == 0)
                System.out.println(p * 100 + "%");
            Main.controller.pb_status.setProgress(p);
        }

        return index;
    }

    public static void shortestRouteR(int index, int indexMinDist, double minDist,LinkedList<Route> routesArr) {
        //System.out.println(index + " / " + sizeOfRoutesArr/2);
        if (index < sizeOfRoutesArr / 2){
            if (minDist > routesArr.get(index).distance) {
                minDist = routesArr.get(index).distance;
                indexMinDist = index;
            }

            index++;
            shortestRouteR( index, indexMinDist, minDist, routesArr);
        }else {
            indexMinDistRoute = indexMinDist;
        }
    }*/
}
