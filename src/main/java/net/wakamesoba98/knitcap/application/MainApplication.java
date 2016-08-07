package net.wakamesoba98.knitcap.application;

import javafx.application.Application;
import javafx.stage.Stage;
import net.wakamesoba98.knitcap.config.Config;
import net.wakamesoba98.knitcap.window.WindowSizeDialog;

public class MainApplication extends Application {

    public MainApplication() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Config config = new Config();
        WindowSizeDialog dialog = new WindowSizeDialog();
        dialog.show(config);
    }
}
