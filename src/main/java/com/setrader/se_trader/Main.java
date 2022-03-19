package com.setrader.se_trader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;

public class Main extends Application {

    protected static Controller controller = null;

    // Arrays
    protected static LinkedList<GPS> gpsArr = new LinkedList<>();
    protected static LinkedList<GPS> routesArr = new LinkedList<>();
    protected static LinkedList<Route> rArr = new LinkedList<>();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 915, 440);
        String css = Objects.requireNonNull(this.getClass().getResource("main-view.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("SE Trader");
        stage.setScene(scene);
        stage.show();

        // Getting Controller
        controller = fxmlLoader.getController();

        // Loading GPS from file to Arr and Table
        controller.pb_status.setVisible(false);
        controller.loadTable("gps.txt");

        stage.setOnCloseRequest(e -> {
            try {
                Files.writeGPS("gps.txt", gpsArr);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}