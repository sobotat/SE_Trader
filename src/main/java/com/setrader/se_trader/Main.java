package com.setrader.se_trader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    protected static Controller controller = null;
    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    // Arrays
    protected static LinkedList<GPS> gpsArr = new LinkedList<>();       // GPS markers
    protected static LinkedList<GPS> routeArr = new LinkedList<>();     // Final Route
    protected static LinkedList<Route> routesArr = new LinkedList<>();  // All Routes
    protected static int routeCurrent = 0;
    protected static Stage mainStage;

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("App Started\n-------------------------------------------------------------------------------------------------------------------");
        //Load Settings
        Settings.loadSettings();

        // Setup Scene
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), Settings.winWidth, Settings.winHeight);
        String css = Objects.requireNonNull(this.getClass().getResource("main-view.css")).toExternalForm();
        scene.getStylesheets().add(css);

        mainStage = stage;
        stage.initStyle(StageStyle.DECORATED);
        scene.setFill(Color.TRANSPARENT);

        // Setup Stage
        stage.setTitle("SE Trader");
        stage.setScene(scene);

        Image icon = new Image("/se_trader_logo.png");
        if (icon.isError()) {
            logger.error("Icon Load Failed");
            logger.error(icon.exceptionProperty().get().getMessage());
        }

        stage.getIcons().add( icon);

        stage.setResizable(Settings.winCanResize);
        stage.setMaximized(Settings.winIsMaximize);
        stage.show();

        // Getting Controller
        controller = fxmlLoader.getController();
        controller.animation();

        // Loading GPS from file to Arr and Table
        //controller.pb_status.setVisible(false);
        controller.pb_status.setProgress(1);
        controller.loadGPSList("gps.txt", true);
        controller.updateBackHomeButton(false);

        stage.setOnCloseRequest(e -> {
            Files.writeGPS("gps.txt", gpsArr);

            Settings.winIsMaximize = stage.isMaximized();
            Settings.winWidth = Math.round((scene.getWidth() * 1000)) / (double) 1000;
            Settings.winHeight = Math.round((scene.getHeight() * 1000)) / (double) 1000;
            Settings.saveSettings();

            System.exit(0);
        });

        stage.maximizedProperty().addListener((odd, var, varNew) -> {
            if (controller != null) {
                Timer timer = new Timer(true);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        controller.updateItemsWidth();
                    }
                }, 50);
            }
        });
    }

    protected static void stageResizable(boolean resize){
        if(mainStage == null)
            return;

        mainStage.setResizable(resize);
    }

    public static void main(String[] args) {launch();}
}