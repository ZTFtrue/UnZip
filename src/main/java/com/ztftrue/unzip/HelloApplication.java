package com.ztftrue.unzip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class HelloApplication extends Application {
    static String filePath = null;
    HelloController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        controller.setHostServices(getHostServices());
        if (filePath != null && !"".equals(filePath) && filePath.endsWith(".zip")) {
            File file = new File(filePath);
            if (file.exists() && file.isFile())
                controller.setZipFile(file);
        }
        Scene scene = new Scene(root, 320, 540);
        stage.setTitle("此广告位出租");
        stage.setScene(scene);
        stage.show();
    }


    @Override
    public void stop() throws Exception {
        controller.appRunning = false;
        controller.searching = false;
        synchronized (controller.searchThread) {
            controller.searchThread.notify();
            // when thread is not waiting
            controller.searchThread.interrupt();
        }
        super.stop();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            filePath = args[0];
        }
        launch();
    }
}