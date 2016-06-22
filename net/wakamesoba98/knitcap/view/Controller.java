package net.wakamesoba98.knitcap.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import net.wakamesoba98.knitcap.capture.Capture;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable, ListViewControllable {

    @FXML
    private ListView<String> listView;
    @FXML
    private MenuItem menuStart;

    private ListProperty<String> listProperty = new SimpleListProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listProperty.set(FXCollections.observableArrayList());
        listView.itemsProperty().bind(listProperty);

        menuStart.setOnAction(actionEvent -> {
            Capture capture = new Capture();
            try {
                capture.capture(20, 10, 65536, Controller.this);
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void addItem(String item) {
        listProperty.add(0, item);
    }
}
