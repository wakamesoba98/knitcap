package net.wakamesoba98.knitcap.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.view.listview.CellController;

import java.net.URL;
import java.util.ResourceBundle;

public class ListAppController implements Initializable {

    @FXML
    private ListView<PacketHeader> listView;

    private ListProperty<PacketHeader> listProperty;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listProperty = new SimpleListProperty<>();
        listProperty.set(FXCollections.observableArrayList());
        listView.itemsProperty().bind(listProperty);
        listView.setCellFactory(param -> new CellController());
    }

    public void destroy() {
        listProperty.clear();
        listProperty = null;
    }
}
