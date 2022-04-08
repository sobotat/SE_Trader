module com.setrader.se_trader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    requires org.json;
    requires com.google.gson;
    requires org.apache.logging.log4j;

    opens com.setrader.se_trader to javafx.fxml;
    exports com.setrader.se_trader;
}