module com.setrader.se_trader {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.setrader.se_trader to javafx.fxml;
    exports com.setrader.se_trader;
}