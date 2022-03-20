package com.setrader.se_trader;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class ItemGPSController implements Initializable {

    @FXML
    private Button btn_copy;
    @FXML
    private Circle dot_color;
    @FXML
    private Text tv_itemName, tv_X, tv_Y, tv_Z;

    public void setItem(GPS gps){
        tv_itemName.setText(gps.getName());

        tv_X.setText(gps.getStrX());
        tv_Y.setText(gps.getStrY());
        tv_Z.setText(gps.getStrZ());

        dot_color.setFill(Paint.valueOf(gps.getColor()));

        btn_copy.setOnMouseClicked(event -> {
            Main.controller.tf_GPS.setText(gps.toString());
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
