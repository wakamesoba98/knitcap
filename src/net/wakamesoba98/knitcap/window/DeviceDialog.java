package net.wakamesoba98.knitcap.window;

import javafx.scene.control.ChoiceDialog;
import javafx.stage.Modality;
import net.wakamesoba98.knitcap.config.Config;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class DeviceDialog {

    void show(Config config) {
        try {
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
            List<String> names = devices.stream().map(PcapNetworkInterface::getName).collect(Collectors.toList());
            ChoiceDialog<String> dialog = new ChoiceDialog<>(names.get(0), names);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setHeaderText("Choose a network interface:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                config.setDevice(dialog.getSelectedItem());
                MainWindow app = new MainWindow("Knitcap");
                app.start(config);
            }
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
    }
}
