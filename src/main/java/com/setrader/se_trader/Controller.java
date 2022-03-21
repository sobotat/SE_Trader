package com.setrader.se_trader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {

    @FXML
    public TextField tf_GPS;
    @FXML
    Text tv_viewButton;
    @FXML
    VBox vBox_List;
    @FXML
    HBox hBox_backHome;
    @FXML
    public ProgressBar pb_status;

    public static LinkedList<Integer> shortestRoutesIndexes = new LinkedList<>();
    public static int shortestRouteDone = 0;
    static boolean viewRoute = false;
    static boolean timerEnd = true;
    static boolean backHome = true;
    static int selectedGPSItem = -1;

    public void loadGPSList(String fileName, boolean firstLoad){
        LinkedList<GPS> arr = null;

        try {
            if (fileName.equals("route.txt")) {
                Main.routesArr = Files.readGPS(fileName);
                arr = Main.routesArr;
            }else{
                if (firstLoad)
                    Main.gpsArr = Files.readGPS(fileName);

                arr = Main.gpsArr;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert arr != null;

        vBox_List.getChildren().clear();
        Node[] nodes = new Node[arr.size()];

        try {
            for (int i = 0; i < nodes.length; i++){
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(Main.class.getResource("item-gps.fxml"));
                nodes[i] = fxmlLoader.load();

                final int h = i;
                ItemGPSController gpsController = fxmlLoader.getController();
                gpsController.setItem(arr.get(i));

                nodes[i].setOnMouseEntered(event -> {
                    if (selectedGPSItem != h)
                        nodes[h].setStyle("-fx-background-color: #2F2F2F");
                });
                nodes[i].setOnMouseExited(event -> {
                    if (selectedGPSItem != h)
                        nodes[h].setStyle("-fx-background-color: #1F1F1F");
                });
                nodes[i].setOnMouseClicked(event -> {
                    if (selectedGPSItem != h) {
                        nodes[h].setStyle("-fx-background-color: #5F5F5F");
                        selectedGPSItem = h;

                        for (Node n: nodes) {
                            if (n != null && n != nodes[h]){
                              n.setStyle("-fx-background-color: #1F1F1F");
                            }
                        }

                    }else{
                        nodes[h].setStyle("-fx-background-color: #1F1F1F");
                        selectedGPSItem = -1;
                    }
                });
                vBox_List.getChildren().add(nodes[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onBackHomeButtonClick() {
        if (backHome) {
            hBox_backHome.setStyle("-fx-background-color: #1F1F1F; -fx-background-radius: 20;");
            backHome = false;
        }else {
            hBox_backHome.setStyle("-fx-background-color: #96c8ff; -fx-background-radius: 20;");
            backHome = true;
        }
    }

    public void onSaveButtonClick() throws IOException {
        Files.writeGPS( "gps.txt",Main.gpsArr);
    }

    public void onLoadButtonClick() throws IOException {
        viewRoute = false;
        tv_viewButton.setText("View Route");
        Main.gpsArr = Files.readGPS("gps.txt");
        loadGPSList("gps.txt", false);
    }

    public void onEnterButtonClick() {
        String text = Api.getFromClipboard();
        if (text.equals("") || !GPS.isGPS(text)) {
            text = tf_GPS.getText();
        }

        if (GPS.isGPS(text)){
            Main.gpsArr.add(GPS.makeFromString(text));

            viewRoute = false;
            loadGPSList("gps.txt", false);
        }else{
            //tv_viewButton.setText("This is not GPS");
            System.err.println("EnterButton: Wrong GPS Input: \t" + text);
        }
    }

    public void onCopyAllButtonClick(){
        StringBuilder allGPS = new StringBuilder();
        LinkedList<GPS> arr = viewRoute ? Main.routesArr : Main.gpsArr;

        for (GPS gps: arr) {
            allGPS.append(gps.toString());
            allGPS.append("\n");
        }
        tf_GPS.clear();
        tf_GPS.setText("All GPS are in Clipboard");
        Api.writeTextToClipboard(allGPS.toString());
    }



    public void onCalculateEntireButtonClick(){
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

                Timer timer;
                while (threadCalculateDist.isAlive()){
                    double p = (double) RouteCalculator.numOfDoneRoutes / (double) RouteCalculator.numOfCombination;
                    pb_status.setProgress(p);

                    if (timerEnd){
                        System.out.printf("%.2f%c\n", (p * 100), '%');
                        String s = RouteCalculator.numOfDoneRoutes + " / " + RouteCalculator.numOfCombination;
                        tf_GPS.clear();
                        Main.controller.tf_GPS.setText(s);

                        timerEnd = false;
                        timer = new Timer(true);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                timerEnd = true;
                            }
                        }, 2000);
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
                    threads.getLast().setDaemon(true);
                    threads.getLast().start();
                    T++;
                }

                int size = Main.rArr.size();

                timerEnd = true;
                while (shortestRouteDone < size && shortestThreadsDone(threads)){
                    double progress = (double) shortestRouteDone / (double) size;

                    if (timerEnd){
                    //if (pb_status.getProgress() != progress){
                        pb_status.setProgress(progress);

                        //if ((progress * 100) %1 == 0){
                        System.out.printf("%.2f%c\n", (progress * 100), '%');
                        String s = shortestRouteDone + " / " + size;
                        tf_GPS.clear();
                        tf_GPS.setText(s);

                        timerEnd = false;
                        timer = new Timer(true);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                timerEnd = true;
                            }
                        }, 2000);
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
        threadCalculate.setDaemon(true);
        threadCalculate.start();

        do {
            if (!threadCalculate.isAlive()){
                viewRoute = true;
                loadGPSList("route.txt", false);
            }
        } while (threadCalculate.isAlive());
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
        threadCalculate.setDaemon(true);
        threadCalculate.start();

        do {
            if (!threadCalculate.isAlive()){
                viewRoute = true;
                loadGPSList("route.txt", false);
            }
        } while (threadCalculate.isAlive());
    }

    public void onViewButtonClick(){
        if (!viewRoute) {
            viewRoute = true;
            tv_viewButton.setText("View GPS");
            loadGPSList("route.txt", false);
        }else{
            viewRoute = false;
            tv_viewButton.setText("View Route");
            loadGPSList("gps.txt", false);
        }
    }

    private boolean shortestThreadsDone(LinkedList<Thread> threads){
        for (Thread thread: threads) {
            if (thread.isAlive())
                return true;
        }
        return false;
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