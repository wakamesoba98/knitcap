package net.wakamesoba98.knitcap.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.wakamesoba98.knitcap.view.ListAppController;

public class ListApplication extends Application {

    private ListAppController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/res/fxml/list.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root, 600, 720);
        primaryStage.setTitle("Knitcap");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        controller.destroy();
    }
}
