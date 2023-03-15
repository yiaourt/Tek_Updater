module fr.teklauncher.tek_updater {

    requires jdk.crypto.cryptoki;

    requires javafx.base;
    requires javafx.swing;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.google.gson;
    requires org.apache.commons.io;

    opens fr.teklauncher.tek_updater to javafx.fxml;
    exports fr.teklauncher.tek_updater;
}