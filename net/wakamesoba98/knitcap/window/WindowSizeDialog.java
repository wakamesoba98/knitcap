package net.wakamesoba98.knitcap.window;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import net.wakamesoba98.knitcap.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WindowSizeDialog {

    public void show(Config config) {
        List<String> screenSizeList = new ArrayList<>();
        screenSizeList.add("1280x720");
        screenSizeList.add("1920x1080");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(screenSizeList.get(0), screenSizeList);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);
        dialog.setHeaderText("Choose a window size:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            config.setScreenSize(dialog.getSelectedItem());
            DeviceDialog deviceDialog = new DeviceDialog();
            deviceDialog.show(config);
        }
    }
}
