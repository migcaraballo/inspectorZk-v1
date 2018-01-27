package com.migs.zk.inspectorzk.drivers;


import com.migs.zk.inspectorzk.ui.controllers.MainController;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InspectorZKMain extends Application {

    private static final Logger log = LogManager.getLogger(InspectorZKMain.class);
    private static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting InspectorZKMain...");

        FXMLLoader fldr = new FXMLLoader(getClass().getResource("/com/migs/zk/inspectorzk/ui/controllers/inspectorzk-window-main.fxml"));
        BorderPane bpMainPane = fldr.load();

        Scene scene = new Scene(bpMainPane, 800, 720);

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        primaryStage.setTitle("InspectorZK");
        primaryStage.show();

        MainController.setMainStage(primaryStage);
        hostServices = getHostServices();
    }

    public static void showDoc(String url){
        hostServices.showDocument(url);
    }

    public static void main(String[] args ){
        launch(args);
    }
}
