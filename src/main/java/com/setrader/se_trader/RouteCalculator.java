package com.setrader.se_trader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedList;

public class RouteCalculator {
    private static final Logger logger = LogManager.getLogger(RouteCalculator.class.getName());

    // Array of All Routes
    private static Double[][] distM = null;
    protected static Double currentMinDist = Double.MAX_VALUE;
    protected static long numOfCombination = 0;
    protected static long numOfDoneRoutes = 0;
    private static int[] numOfDoneRoutesArr = null; // For Multithreading
    protected static boolean calculationStop = false;
    protected static int homeIndex = 0;

    // Settings
    protected static boolean backHome = true;
    protected static boolean multiThreading = true;
    protected static String GPSColor = "#FFBB00";

    public RouteCalculator(){
        calculationStop = false;
        currentMinDist = Double.MAX_VALUE;

        numOfDoneRoutes = 0;
        numOfCombination = Api.factorial(Main.gpsArr.size() - 1);
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

    public void routeCalculateByShortestMultiThread( LinkedList<GPS> gpsArr){
        int threads = gpsArr.size();
        RouteCalculator.numOfDoneRoutesArr = new int[threads];

        if(threads < 1) {
            logger.error("Incorrect number of Threads");
            return;
        }

        LinkedList<Runnable> tasks = new LinkedList<>();
        for (int t = 0; t < threads; t++){
            if(t == RouteCalculator.homeIndex)
                continue;

            RouteCalculator.numOfDoneRoutesArr[t] = 0;
            Integer[] startGPS = new Integer[gpsArr.size()-2];
            int j = 0;
            for (int i = 0; i < gpsArr.size(); i++) {
                if (i != RouteCalculator.homeIndex && i != t) {
                    startGPS[j] = i;
                    j++;
                }
            }

            Integer[] routeGPS = new Integer[2];
            routeGPS[0] = RouteCalculator.homeIndex;
            routeGPS[1] = t;

            int finalT = t;

            Runnable runCalculateDist = () -> generateCombinations( startGPS, routeGPS, finalT);
            tasks.push(runCalculateDist);
        }

        logger.info("Calculating Possible Routes");

        LinkedList<Thread> arrThreads = new LinkedList<>();
        for (int t = 0; t < tasks.size(); t++){
            Thread threadCalculateDist = new Thread(tasks.get(t), "ThreadGenerateCombination_" + (t + 1));
            arrThreads.add(threadCalculateDist);
            arrThreads.getLast().start();
            logger.info( threadCalculateDist.getName() + " Started");
        }

        int lastAlive = arrThreads.size();
        boolean notDone = true;
        while(notDone){
            notDone = false;

            int alive = 0;
            for (Thread thread : arrThreads){
                if (thread.isAlive()) {
                    notDone = true;
                    alive++;
                }
            }
            if(lastAlive != alive){
                lastAlive = alive;
                logger.info("Done Threads " + (arrThreads.size() - alive));
            }

            int numOfDone = 0;
            for(int t = 0; t < numOfDoneRoutesArr.length; t++){
                //System.out.println("T" + t + " DoneRoutes " + numOfDoneRoutesArr[t]);
                numOfDone += numOfDoneRoutesArr[t];
            }
            RouteCalculator.numOfDoneRoutes = numOfDone;
        }
    }

    public void routeCalculateByShortestSingleThread(LinkedList<GPS> gpsArr){
        Integer[] startGPS = new Integer[gpsArr.size()-1];
        RouteCalculator.numOfDoneRoutesArr = new int[1];

        int j = 0;
        for (int i = 0; i < Main.gpsArr.size(); i++) {
            if (i != RouteCalculator.homeIndex) {
                startGPS[j] = i;
                j++;
            }
        }

        Integer[] routeGPS = new Integer[1];
        routeGPS[0] = RouteCalculator.homeIndex;

        logger.info("Calculating Possible Routes");

        Runnable runCalculateDist = () -> generateCombinations( startGPS, routeGPS, 0);

        Thread threadCalculateDist = new Thread(runCalculateDist, "ThreadGenerateCombination");
        threadCalculateDist.start();

        while(threadCalculateDist.isAlive()){
            int numOfDone = 0;
            for(int t = 0; t < numOfDoneRoutesArr.length; t++){
                numOfDone += numOfDoneRoutesArr[t];
            }
            RouteCalculator.numOfDoneRoutes = numOfDone;
        }
    }

    private void generateCombinations( Integer[] arrIndex, Integer[] arrRoute, int thread){
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
                generateCombinations(arrIndexNew, arrRouteNew, thread);
            }
        }else{
            Route r = new Route();

            if (backHome) {
                Integer[] arrRouteN = new Integer[arrRoute.length + 1];
                System.arraycopy(arrRoute, 0, arrRouteN, 0, arrRoute.length);
                arrRouteN[arrRouteN.length - 1] = homeIndex;
                Collections.addAll(r.gpsIndex, arrRouteN);
            }else {
                Collections.addAll(r.gpsIndex, arrRoute);
            }

            r.distance = RouteCalculator.routeDistance(r);

            if (r.distance < currentMinDist) {
                currentMinDist = r.distance;
                Main.routesArr.push(r);
            }

            //numOfDoneRoutes++;
            numOfDoneRoutesArr[thread]++;
            //System.out.println(thread + " : " + numOfDoneRoutes + "/" + numOfCombination);
        }
    }

    // Writing Routes in Arr in main, Arguments: FirstRoute (with start pos), Arr of GPS
    public void routeCalculateByShortestJump( GPS lastGPS, LinkedList<GPS> gpsArr, Route route) {
        if (calculationStop)
            return;

        if (!gpsArr.isEmpty()){
            int i_closestGPS = closestGPS( lastGPS, gpsArr);
            GPS currentRoute = gpsArr.remove(i_closestGPS);

            route.gpsIndex.add(Main.gpsArr.indexOf(currentRoute));

            routeCalculateByShortestJump( currentRoute, gpsArr, route);
        }else{
            if (backHome)
                route.gpsIndex.add(homeIndex);

            route.distance = RouteCalculator.routeDistance(route);

            Main.routesArr.clear();
            Main.routesArr.add(route);

            Route.write(route, Main.gpsArr, RouteCalculator.GPSColor);

            logger.info(route.toStringDist());
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
    /*
    public static int shortestRoute( LinkedList<Route> routesArr) {
        if (routesArr.isEmpty())
            return -1;

        int index = 0;
        double minDist = routesArr.pop().distance;

        while (!routesArr.isEmpty()){
            if (calculationStop)
                return -2;

            Route route = routesArr.pop();
            double dist = route.distance;
            if (minDist > dist) {
                minDist = dist;
                index = Main.routesArr.indexOf(route);
            }
            shortestRouteDone++;
        }

        return index;
    }
    */

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

}
