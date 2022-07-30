module com.setrader.se_trader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    requires org.json;
    requires com.google.gson;
    requires org.apache.logging.log4j;
    requires aparapi;

    opens com.setrader.se_trader to javafx.fxml;
    exports com.setrader.se_trader;
}