module com.ztftrue.unzip {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.ztftrue.unzip to javafx.fxml;
    exports com.ztftrue.unzip;
}