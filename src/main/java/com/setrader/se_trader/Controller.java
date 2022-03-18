package com.setrader.se_trader;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.LinkedList;

public class Controller {

    @FXML
    public TextField tf_GPS;
    @FXML
    TableView<GPS> gpsTable;
    @FXML
    TableColumn<GPS, String> nameCol, xCol, yCol, zCol, colorCol;
    @FXML
    Button btn_Load, btn_Save, btn_View, btn_Enter, btn_Remove, btn_CalculateNext, btn_CalculateEntire;
    @FXML
    CheckBox cb_BackHome;
    @FXML
    public ProgressBar pb_status;

    public static LinkedList<Integer> shortestRoutesIndexes = new LinkedList<>();
    public static int shortestRouteDone = 0;
    static boolean viewRoute = false;

    @FXML
    public void onSaveButtonClick() throws IOException {
        Files.writeGPS( "gps.txt",Main.gpsArr);
    }

    public void onLoadButtonClick() throws IOException {
        viewRoute = false;
        btn_View.setText("View Route");
        Main.gpsArr = Files.readGPS("gps.txt");
        loadTable("gps.txt");
    }

    public void onEnterButtonClick(){
        Main.gpsArr.add(GPS.makeFromString(tf_GPS.getText()));
        gpsTable.getItems().add(Main.gpsArr.getLast());
    }

    public void onRemoveButtonClick(){
        try{
            int index = Main.gpsArr.indexOf(gpsTable.getSelectionModel().getSelectedItem());

            //int index = Integer.parseInt(tf_GPS.getText()) - 1;
            Main.gpsArr.remove(index);
            gpsTable.getItems().remove(index);
        }catch (NumberFormatException e){
            System.err.println("Error Remove: Wrong Input");
        }catch (IndexOutOfBoundsException e){
            System.err.println("Error Remove: Index is too large\n" + e.getMessage());
        }
    }



    public void onCalculateEntireButtonClick(){
        boolean backHome = cb_BackHome.isSelected();
        Runnable runCalculate = () -> {
            if (!Main.gpsArr.isEmpty()) {
                pb_status.setVisible(true);
                pb_status.setProgress(0);

                Integer[] startGPS = new Integer[Main.gpsArr.size()-1];
                for (int i = 1; i < Main.gpsArr.size(); i++) {
                    int j = i - 1;
                    startGPS[j] = i;
                }

                Integer[] routeGPS = new Integer[1];
                routeGPS[0] = 0;

                Main.rArr.clear();
                Main.routesArr.clear();

                RouteCalculator calculator = new RouteCalculator();
                RouteCalculator.numOfDoneRoutes = 0;
                RouteCalculator.numOfCombination = Api.factorial(Main.gpsArr.size() - 1);

                Runnable runCalculateDist = () -> calculator.routeCalculateByShortestDist( startGPS, routeGPS, backHome);

                Thread threadCalculateDist = new Thread(runCalculateDist, "ThreadCalculateDist");
                threadCalculateDist.start();

                while (threadCalculateDist.isAlive()){
                    double p = (double) RouteCalculator.numOfDoneRoutes / (double) RouteCalculator.numOfCombination;
                    pb_status.setProgress(p);

                    if (p * 100 %1 == 0){
                        System.out.println(p * 100 + "%");
                        String s = RouteCalculator.numOfDoneRoutes + " / " + RouteCalculator.numOfCombination;
                        Main.controller.tf_GPS.setText(s);
                    }
                }

                RouteCalculator.distMatrix(Main.gpsArr);
                for (Route r: Main.rArr){
                    r.distance = RouteCalculator.routeDistance(r);
                }

                System.out.println("Done\n\nCalculating Shortest Route: ");

                LinkedList<Thread> threads = new LinkedList<>();

                int numOfThreads = 4;
                int T = 0;
                int blockSize = Main.rArr.size()/numOfThreads;
                shortestRoutesIndexes.clear();
                shortestRouteDone = 0;

                for (int t = 0; t < numOfThreads; t++){
                    Runnable runShortestRoute = new MyRunnable(T, blockSize);
                    threads.add(new Thread(runShortestRoute, ("shortestRoute t" + t)));
                    threads.getLast().start();
                    T++;
                }

                int size = Main.rArr.size();
                while (shortestRouteDone < size && shortestThreadsDone(threads)){
                    double progress = (double) shortestRouteDone / (double) size;

                    if (pb_status.getProgress() != progress){
                        pb_status.setProgress(progress);

                        if ((progress * 100) %1 == 0){
                            System.out.println((progress * 100) + "%");
                            String s = shortestRouteDone + " / " + size;
                            tf_GPS.setText(s);
                        }
                    }
                }
                System.out.println("Done\n");

                int minDistIndex = 0;
                double minDist = Double.MAX_VALUE;
                for (Integer shortestRoutesIndex : shortestRoutesIndexes) {
                    Route r = Main.rArr.get(shortestRoutesIndex);
                    if (r.distance < minDist) {
                        minDist = r.distance;
                        minDistIndex = shortestRoutesIndex;
                    }
                }
                Route shortestRoute = Main.rArr.get(minDistIndex);
                //System.out.println(shortestRoute.toStringName());
                System.out.println(shortestRoute.toStringDist());

                Route.write(shortestRoute, Main.gpsArr);
                tf_GPS.setText("Distance of route is " + Math.round(shortestRoute.distance));
                pb_status.setProgress(1);
            }
        };

        Thread threadCalculate = new Thread( runCalculate, "CalculateThread");
        threadCalculate.start();
        pb_status.setProgress(1);
    }

    public void onCalculateNextButtonClick(){
        Runnable runCalculate = () -> {
            pb_status.setVisible(true);
            pb_status.setProgress(0);
            // Initialization

            // Start route calculation
            //Route.routeCalculate(route, Main.gpsArr, notUsedGPS);
            if (!Main.gpsArr.isEmpty()) {
                pb_status.setProgress(0.05);
                RouteCalculator calculator = new RouteCalculator();
                RouteCalculator.route = new Route();
                LinkedList<GPS> routeGPS = new LinkedList<>(Main.gpsArr);

                GPS startGPS = routeGPS.remove(0);

                pb_status.setProgress(0.25);
                RouteCalculator.route.gpsIndex.add(0);
                calculator.routeCalculateByShortestJump(startGPS, routeGPS);

                pb_status.setProgress(0.95);
                tf_GPS.setText("Distance of route is " + Math.round(RouteCalculator.route.distance));
            }
            pb_status.setProgress(1);
        };

        Thread threadCalculate = new Thread( runCalculate, "CalculateThread");
        threadCalculate.start();
    }

    public void onViewButtonClick() throws IOException {
        if (!viewRoute) {
            viewRoute = true;
            btn_View.setText("View GPS");
            loadTable("route.txt");
        }else{
            viewRoute = false;
            btn_View.setText("View Route");
            loadTable("gps.txt");
        }
    }

    private boolean shortestThreadsDone(LinkedList<Thread> threads){
        for (Thread thread: threads) {
            if (thread.isAlive())
                return true;
        }
        return false;
    }

    // Loads data from gps.txt to table and arr
    public void loadTable(String fileName) throws IOException {
        LinkedList<GPS> arr;

        if (fileName.equals("route.txt")) {
            Main.routesArr = Files.readGPS(fileName);
            arr = Main.routesArr;
        }else{
            if (Main.gpsArr.isEmpty())
                Main.gpsArr = Files.readGPS(fileName);

            arr = Main.gpsArr;
        }

        try{
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            xCol.setCellValueFactory(new PropertyValueFactory<>("strX"));
            yCol.setCellValueFactory(new PropertyValueFactory<>("strY"));
            zCol.setCellValueFactory(new PropertyValueFactory<>("strZ"));
            colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));

            gpsTable.getItems().clear();
            for (GPS item : arr){
                gpsTable.getItems().add(item);
            }
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}

class MyRunnable implements Runnable {

    int T;
    int blockSize;

    public MyRunnable(int T, int blockSize) {
        this.T = T;
        this.blockSize = blockSize;
    }

    public void run() {
        LinkedList<Route> r = new LinkedList<>(Main.rArr);
        Controller.shortestRoutesIndexes.add(RouteCalculator.shortestRoute((T * blockSize), ((T + 1) * blockSize), r));
    }
}