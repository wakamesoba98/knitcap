package net.wakamesoba98.knitcap.application;

import javafx.application.Preloader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreenLoader extends Preloader {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Image image = new Image(getClass().getResourceAsStream("../png/splash.png"));
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        ImageView imageView = new ImageView(image);
        Pane pane = new Pane();
        pane.getChildren().add(imageView);
        Scene scene = new Scene(pane, width, height);

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        stage = primaryStage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setX((primaryScreenBounds.getWidth() - width) / 2);
        stage.setY((primaryScreenBounds.getHeight() - height) / 2);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}
