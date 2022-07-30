package com.setrader.se_trader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class.getName());

    @FXML
    public TextField tf_InfoBar;
    @FXML
    Text tv_viewButton, tv_RouteSelector;
    @FXML
    VBox vBox_List, vBox_Table;
    @FXML
    ScrollPane scl_List;
    @FXML
    public HBox hBox_backHome;
    @FXML
    public ProgressBar pb_status;
    @FXML
    BorderPane bPane_screen;

    static boolean viewRoute = false;
    static boolean timerEnd = true;
    private Node bp_RouteSelectorNode;
    private Node bp_SettingsNode;
    private SettingsController settingsController;

    // Load Table of GPS
    public void loadGPSList(String fileName, boolean firstLoad){
        LinkedList<GPS> arr = null;

        try {
            if (fileName.equals("route.txt")) {
                if (bp_RouteSelectorNode != null && vBox_Table.getChildren().size() <= 1 && Main.routesArr.size() > 1) {
                    vBox_Table.getChildren().add(bp_RouteSelectorNode);
                }

                if (vBox_Table.getChildren().size() > 1) {
                    Platform.runLater(() -> {
                        double dist = Main.routesArr.get(Main.routeCurrent).distance;
                        tv_RouteSelector.setText(String.format("< %d/%d > <-> %.1f", (Main.routeCurrent + 1), (Main.routesArr.size()), dist));
                    });
                }

                if (!Main.routesArr.isEmpty())
                    Route.write(Main.routesArr.get(Main.routeCurrent), Main.gpsArr, RouteCalculator.GPSColor);

                Main.routeArr = Files.readGPS(fileName);
                arr = Main.routeArr;
            }else{
                if (vBox_Table.getChildren().size() > 1) {
                    bp_RouteSelectorNode = vBox_Table.getChildren().get(1);
                    vBox_Table.getChildren().remove(1);
                }

                if (firstLoad)
                    Main.gpsArr = Files.readGPS(fileName);

                arr = Main.gpsArr;

                if (RouteCalculator.homeIndex >= Main.gpsArr.size())
                    RouteCalculator.homeIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert arr != null;

        vBox_List.getChildren().clear();
        ItemGPSController.GPScontrollers.clear();
        Node[] nodes = new Node[arr.size()];

        try {
            for (int i = 0; i < nodes.length; i++){
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(Main.class.getResource("item-gps.fxml"));
                nodes[i] = fxmlLoader.load();

                final int h = i;
                ItemGPSController gpsController = fxmlLoader.getController();
                gpsController.setItem(arr.get(i));

                nodes[i].setOnMouseEntered(event -> nodes[h].setStyle("-fx-background-color: colorLightGray"));
                nodes[i].setOnMouseExited(event -> nodes[h].setStyle("-fx-background-color: colorDarkGray"));

                ItemGPSController.GPScontrollers.add(gpsController);
                vBox_List.getChildren().add(nodes[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateItemsWidth();
    }

    // Home Button
    public void onBackHomeButtonClick() {
        RouteCalculator.backHome = !RouteCalculator.backHome;
        updateBackHomeButton(true);
    }

    public void onBackHomeButtonEntered(){
        updateBackHomeButton(true);
    }

    public void onBackHomeButtonExited(){
        updateBackHomeButton(false);
    }

    public void updateBackHomeButton(boolean entered){
        if (RouteCalculator.backHome) {
            hBox_backHome.setStyle("-fx-background-color: colorAccent; -fx-background-radius: 20;");
            if (entered)
                hBox_backHome.setStyle(hBox_backHome.getStyle() + "-fx-border-color: colorDarkGray; -fx-border-radius: 20;");
        }else {
            if (entered) {
                hBox_backHome.setStyle("-fx-background-color: colorLightGray; -fx-background-radius: 20;" +
                        "-fx-border-color: #1F1F1F; -fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 3, 0, 0, 0);");
            }else
                hBox_backHome.setStyle("-fx-background-color: colorDarkGray; -fx-background-radius: 20;");
        }
    }

    // Control Emergency Stop
    public void onStopButtonClick(){
        RouteCalculator.calculationStop = true;
    }

    public void onSettingsButtonClick(){
        try {
            if(bp_SettingsNode == null){
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(Main.class.getResource("settings-view.fxml"));
                bp_SettingsNode = fxmlLoader.load();

                settingsController = fxmlLoader.getController();
                settingsController.init();
                settingsController.updateAll();
                settingsController.animation();
                bPane_screen.setRight(bp_SettingsNode);

            }else{
                if(bPane_screen.getRight() != null)
                    bPane_screen.setRight(null);
                else {
                    bPane_screen.setRight(bp_SettingsNode);
                    settingsController.updateAll();
                    settingsController.animation();
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateItemsWidth();
            }
        }, 50);
    }

    // Save Button
    public void onSaveButtonClick() {
        Files.writeGPS( "gps.txt",Main.gpsArr);
    }

    // Load Button
    public void onLoadButtonClick() throws IOException {
        viewRoute = false;
        tv_viewButton.setText("Route");
        Main.gpsArr = Files.readGPS("gps.txt");
        loadGPSList("gps.txt", false);
    }

    // Add to List
    public void onEnterButtonClick() {
        String text = Api.getFromClipboard();
        if (text.equals("") || !GPS.isGPS(text)) {
            text = tf_InfoBar.getText();
        }

        if (GPS.isGPS(text)){
            Main.gpsArr.add(GPS.makeFromString(text));

            viewRoute = false;
            loadGPSList("gps.txt", false);
        }else{
            //tv_viewButton.setText("This is not GPS");
            logger.error("EnterButton: Wrong GPS Input: \t" + text);
        }
    }

    // Copy All to ClipBoard
    public void onCopyAllButtonClick(){
        StringBuilder allGPS = new StringBuilder();
        LinkedList<GPS> arr = viewRoute ? Main.routeArr : Main.gpsArr;

        for (GPS gps: arr) {
            allGPS.append(gps.toString());
            allGPS.append("\n");
        }
        tf_InfoBar.clear();
        tf_InfoBar.setText("All GPS are in Clipboard");
        Api.writeTextToClipboard(allGPS.toString());
    }

    // Calculation
    public void onCalculateEntireButtonClick(){
        Runnable runCalculate = () -> {
            if (!Main.gpsArr.isEmpty()) {
                logger.info("Calculating By Dist Starting");
                pb_status.setVisible(true);
                pb_status.setProgress(0);

                RouteCalculator calculator = new RouteCalculator();
                Runnable runCalculateDist = () -> {
                    if(RouteCalculator.multiThreading)
                        calculator.routeCalculateByShortestMultiThread( Main.gpsArr);
                    else
                        calculator.routeCalculateByShortestSingleThread( Main.gpsArr);
                };

                Main.routesArr.clear();
                Main.routeArr.clear();

                Thread threadCalculateDist = new Thread(runCalculateDist, "ThreadCalculateDist");
                threadCalculateDist.start();

                Timer timer;
                while (threadCalculateDist.isAlive()){
                    double progress = (double) RouteCalculator.numOfDoneRoutes / (double) RouteCalculator.numOfCombination;
                    pb_status.setProgress(progress);


                    if (timerEnd){
                        double percents = (progress * 100);
                        System.out.printf("%.2f%c\n", percents, '%');
                        String s;
                        if (RouteCalculator.currentMinDist != Double.MAX_VALUE)
                            s = String.format("[%3d%c] <-> %d/%d -- [Current %.0f]", (int)percents, '%', RouteCalculator.numOfDoneRoutes, RouteCalculator.numOfCombination, RouteCalculator.currentMinDist);
                        else
                            s = String.format("[%3d%c] <-> %d/%d", (int)percents, '%', RouteCalculator.numOfDoneRoutes, RouteCalculator.numOfCombination);

                        Platform.runLater(() -> {
                            tf_InfoBar.clear();
                            tf_InfoBar.setText(s);
                        });

                        timerEnd = false;
                        timer = new Timer(true);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                timerEnd = true;
                            }
                        }, 500);
                    }
                }

                RouteCalculator.distMatrix(Main.gpsArr);
                for (Route r: Main.routesArr){
                    r.distance = RouteCalculator.routeDistance(r);
                }

                Main.routeCurrent = 0;
                logger.info("Calculating Possible Routes Done");

                if (!Main.routesArr.isEmpty()) {
                    Route shortestRoute = Main.routesArr.get(Main.routeCurrent);
                    logger.info(shortestRoute.toStringDist());

                    Route.write(shortestRoute, Main.gpsArr, RouteCalculator.GPSColor);

                    Platform.runLater(() -> {
                        viewRoute = true;
                        updateViewButton();
                        tf_InfoBar.clear();

                        if (!RouteCalculator.calculationStop)
                            tf_InfoBar.setText("Distance of route is " + Math.round(shortestRoute.distance));
                        else
                            tf_InfoBar.setText("Route Calculation Canceled");
                    });
                }else{
                    logger.error("Error Routes Arr is Empty");

                    Platform.runLater(() -> {
                        tf_InfoBar.clear();
                        tf_InfoBar.setText("Route Calculation Failed");
                    });
                }
                pb_status.setProgress(1);
                System.out.println(RouteCalculator.numOfDoneRoutes + "/" + RouteCalculator.numOfCombination);
            }
        };

        Thread t = Api.getThreadByName("CalculateThread");
        if(t != null) {
            logger.info("Calculation still running");
            return;
        }

        Thread threadCalculate = new Thread(runCalculate, "CalculateThread");
        threadCalculate.setDaemon(true);
        threadCalculate.start();
    }

    public void onCalculateNextButtonClick(){
        Runnable runCalculate = () -> {
            logger.info("Calculating By Jump Started");
            pb_status.setVisible(true);
            pb_status.setProgress(0);
            // Initialization

            // Start route calculation
            if (!Main.gpsArr.isEmpty()) {
                pb_status.setProgress(0.05);
                RouteCalculator calculator = new RouteCalculator();
                LinkedList<GPS> routeGPS = new LinkedList<>(Main.gpsArr);

                GPS startGPS = routeGPS.remove(RouteCalculator.homeIndex);

                Route route = new Route();
                route.gpsIndex.add(RouteCalculator.homeIndex);

                pb_status.setProgress(0.25);
                calculator.routeCalculateByShortestJump(startGPS, routeGPS, route);

                pb_status.setProgress(0.95);
                tf_InfoBar.setText("Distance of route is " + Math.round(Main.routesArr.getFirst().distance));
                logger.info("Calculating By Jump Done");
                Main.routeCurrent = 0;
            }
            pb_status.setProgress(1);

            Platform.runLater(() -> {
                viewRoute = true;
                loadGPSList("route.txt", false);
            });
        };

        Thread threadCalculate = new Thread( runCalculate, "CalculateThread");
        threadCalculate.setDaemon(true);
        threadCalculate.start();
    }

    // View GPS/Route
    public void onViewButtonClick(){
        viewRoute = !viewRoute;
        updateViewButton();
    }

    // Updates style of View button and loads table
    protected void updateViewButton(){
        if (viewRoute) {
            tv_viewButton.setText("GPS");
            loadGPSList("route.txt", false);
        }else{
            tv_viewButton.setText("Route");
            loadGPSList("gps.txt", false);
        }
    }

    // Open Folder
    public void onFolderButtonClick(){
        Desktop desktop;
        String userprofile = System.getenv("USERPROFILE");
        File file = new File( userprofile + "\\Documents\\SE_Trader");

        try {
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(file);
            } else
                logger.error("Open Folder: Desktop is not supported");
        }catch (IOException e){
            logger.error("Open Folder: Folder Not Found");
        }

    }

    // Switch Current Route
    public void onRoutePrevClick(){
        if (Main.routeCurrent - 1 >= 0) {
            Main.routeCurrent--;

            if (Controller.viewRoute)
                loadGPSList("route.txt", false);
        }
    }

    public void onRouteNextClick(){
        if (Main.routeCurrent + 1 < Main.routesArr.size()) {
            Main.routeCurrent++;

            if (Controller.viewRoute)
                loadGPSList("route.txt", false);
        }
    }

    // Update Size of Items in the Table
    protected void updateItemsWidth(){
        for (ItemGPSController controller : ItemGPSController.GPScontrollers) {
            //controller.hBox_Item.setMinWidth(vBox_List.widthProperty().get() - 30);
            controller.hBox_Item.setMaxWidth(vBox_List.widthProperty().get() - 30);
        }
    }

    protected void animation(){
        //TODO Add Animation
    }

    // On Start
    public void initialize(){

        scl_List.widthProperty().addListener((obs, oldVal, newVal) -> {
            vBox_List.setMaxWidth(scl_List.widthProperty().get());
            vBox_List.setMinWidth(scl_List.widthProperty().get());

            updateItemsWidth();
        });

        scl_List.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (vBox_List.getChildren().size() * 30 < scl_List.heightProperty().get()){
                vBox_List.setMaxHeight(scl_List.heightProperty().get());
                vBox_List.setMinHeight(scl_List.heightProperty().get());
            }else {
                vBox_List.setMaxHeight(vBox_List.getChildren().size() * 30);
                vBox_List.setMinHeight(vBox_List.getChildren().size() * 30);
            }
        });

    }
}