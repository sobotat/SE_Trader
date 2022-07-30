package com.setrader.se_trader;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.LinkedList;

public class ItemGPSController{

    @FXML
    private Button btn_copy, btn_remove, btn_home;
    @FXML
    private Circle dot_color;
    @FXML
    private Text tv_itemName, tv_X, tv_Y, tv_Z;
    @FXML
    protected HBox hbox_btn, hBox_Item;

    protected static LinkedList<ItemGPSController> GPScontrollers = new LinkedList<>();

    public void setItem(GPS gps){
        tv_itemName.setText(gps.name);

        tv_X.setText(gps.strX);
        tv_Y.setText(gps.strY);
        tv_Z.setText(gps.strZ);

        dot_color.setFill(Paint.valueOf(gps.color));

        if (RouteCalculator.homeIndex == Main.gpsArr.indexOf(gps))
            btn_home.setStyle("-fx-background-color: colorAccent; -fx-background-radius: 20;");

        btn_copy.setOnMouseClicked(event -> {
            Main.controller.tf_InfoBar.setText(gps.toString());
            Api.writeTextToClipboard(gps.toString());
        });

        btn_home.setOnMouseClicked(event -> {
            if (RouteCalculator.homeIndex != Main.gpsArr.indexOf(gps)) {
                RouteCalculator.homeIndex = Main.gpsArr.indexOf((gps));

                ItemGPSController thisController = GPScontrollers.get(Main.gpsArr.indexOf(gps));
                for (ItemGPSController controller : GPScontrollers) {
                    if (thisController != controller){
                        controller.btn_home.setStyle("-fx-background-color: colorLightGray; -fx-background-radius: 20;");
                    }else{
                        controller.btn_home.setStyle("-fx-background-color: colorAccent; -fx-background-radius: 20;");
                    }
                }
            }
        });

        btn_home.setOnMouseEntered(event -> {
            if (RouteCalculator.homeIndex != Main.gpsArr.indexOf(gps)) {
                ItemGPSController controller = GPScontrollers.get(Main.gpsArr.indexOf(gps));
                controller.btn_home.setStyle("-fx-background-color: colorDarkGray; -fx-background-radius: 20;");
            }
        });

        btn_home.setOnMouseExited(event -> {
            if (RouteCalculator.homeIndex != Main.gpsArr.indexOf(gps)) {
                ItemGPSController controller = GPScontrollers.get(Main.gpsArr.indexOf(gps));
                controller.btn_home.setStyle("-fx-background-color: colorLightGray; -fx-background-radius: 20;");
            }
        });

        if (!Controller.viewRoute) {
            btn_remove.setDisable(false);
            btn_remove.setOnMouseClicked(event -> {
                Main.gpsArr.remove(gps);
                Controller.viewRoute = false;
                Main.controller.loadGPSList("gps.txt", false);
            });
        }else{
            hbox_btn.getChildren().remove(2);
            hbox_btn.getChildren().remove(0);
            btn_remove.setDisable(true);
        }
    }
}
