package net.wakamesoba98.knitcap.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import net.wakamesoba98.knitcap.capture.Capture;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.view.listview.PacketListCell;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable, ListViewControllable {

    @FXML
    private ListView<PacketHeader> listView;
    @FXML
    private MenuItem menuStart, menuStop;

    private Capture capture;
    private ListProperty<PacketHeader> listProperty = new SimpleListProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listProperty.set(FXCollections.observableArrayList());
        listView.setCellFactory(param -> new PacketListCell());
        listView.itemsProperty().bind(listProperty);

        capture = new Capture();

        menuStart.setOnAction(actionEvent -> {
            listProperty.clear();
            try {
                capture.capture(-1, 10, 65536, Controller.this);
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
            }
        });

        menuStop.setOnAction(actionEvent -> capture.destroy());
    }

    @Override
    public void addItem(PacketHeader item) {
        listProperty.add(0, item);
        if (listProperty.size() > 1000) {
            listProperty.remove(1000);
        }
    }

    public void destroy() {
        capture.destroy();
    }
}
