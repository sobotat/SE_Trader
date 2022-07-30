package com.setrader.se_trader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class SettingsController {
    private static final Logger logger = LogManager.getLogger(SettingsController.class.getName());

    @FXML
    VBox vBox_screen;
    @FXML
    HBox hBox_resize, hBox_multithreading;
    @FXML
    TextField tf_color;

    // Init and Update
    public void init(){
        tf_color.setText(RouteCalculator.GPSColor);
        tf_color.textProperty().addListener((observable, oldValue, newValue) -> {
            if(Api.isValidHexaCode(newValue)) {
                RouteCalculator.GPSColor = newValue;
                logger.info("GPS Color was changed to " + RouteCalculator.GPSColor);
            }
        });
    }
    public void updateAll(){
        onResizeExited();
        onMultiThreadingExited();
    }

    private static boolean timerEnd = true;
    public void animation(){
        LinkedList<Node> nodes = new LinkedList<>();
        VBox vbox = (VBox) vBox_screen.getChildren().get(0);
        for(int n = 0; n < vbox.getChildren().size(); n++){
            nodes.add(vbox.getChildren().get(n));
        }

        //TODO Add Animation
        /*
            new SlideInRight(vBox_screen).setSpeed(5).play();
            for(Node node : nodes){
                new FadeIn(node).play();
            }
        */
    }

    // Save Settings
    public void onSaveClick(){
        Settings.saveSettings();
    }

    // Resize
    public void onResizeClick() {
        Settings.winCanResize = !Settings.winCanResize;
        Main.stageResizable(Settings.winCanResize);
        updateHBox(Settings.winCanResize,true, hBox_resize);
    }
    public void onResizeEntered(){
        updateHBox(Settings.winCanResize,true, hBox_resize);
    }
    public void onResizeExited(){
        updateHBox(Settings.winCanResize,false, hBox_resize);
    }


    // Multithreading
    public void onMultiThreadingClick() {
        RouteCalculator.multiThreading = !RouteCalculator.multiThreading;
        updateHBox(RouteCalculator.multiThreading,true, hBox_multithreading);
    }
    public void onMultiThreadingEntered(){
        updateHBox(RouteCalculator.multiThreading,true, hBox_multithreading);
    }
    public void onMultiThreadingExited(){
        updateHBox(RouteCalculator.multiThreading,false, hBox_multithreading);
    }


    // Global method to update color of HBOX
    private void updateHBox(boolean condition, boolean entered, HBox hBox){
        if (condition) {
            hBox.setStyle("-fx-background-color: colorAccent; -fx-background-radius: 20;");
            if (entered)
                hBox.setStyle(hBox.getStyle() + "-fx-border-color: colorDarkGray; -fx-border-radius: 20;");
        }else {
            if (entered) {
                hBox.setStyle("-fx-background-color: colorDarkGray; -fx-background-radius: 20;" +
                        "-fx-border-color: #1F1F1F; -fx-border-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 3, 0, 0, 0);");
            }else
                hBox.setStyle("-fx-background-color: colorLightGray; -fx-background-radius: 20;");
        }
    }
}
