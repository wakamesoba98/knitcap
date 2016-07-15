package net.wakamesoba98.knitcap.view;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import net.wakamesoba98.knitcap.capture.Capture;
import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.view.canvas.NetworkMap;
import net.wakamesoba98.knitcap.view.listview.CellController;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable, GuiControllable {

    @FXML
    private ListView<PacketHeader> listView;
    @FXML
    private MenuItem menuStart, menuStop;
    @FXML
    private Canvas canvas;

    private Capture capture;
    private NetworkMap networkMap;
    private ListProperty<PacketHeader> listProperty;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listProperty = new SimpleListProperty<>();
        listProperty.set(FXCollections.observableArrayList());
        listView.itemsProperty().bind(listProperty);
        listView.setCellFactory(param -> new CellController());

        capture = new Capture(this);
        menuStart.setOnAction(actionEvent -> start());
        menuStop.setOnAction(actionEvent -> destroy());
    }

    @Override
    public void openedInterface(NetworkDevice device) {
        networkMap.showLocalhost(device);
    }

    @Override
    public void showGateway(String address) {
        networkMap.showGateway(address);
    }

    @Override
    public void showInternet() {
        networkMap.showInternet();
    }

    @Override
    public void addHost(String address) {

    }

    @Override
    public void addItem(PacketHeader item) {
        if (!isSameHeader(item)) {
            listProperty.add(0, item);
            if (listProperty.size() > 1000) {
                listProperty.remove(1000);
            }
            networkMap.addPacket(item);
        }
    }

    private boolean isSameHeader(PacketHeader item) {
        if (listProperty.size() == 0) {
            return false;
        }
        PacketHeader before = listProperty.get(0);
        if (before != null) {
            return before.getProtocol() == item.getProtocol()
                    && before.getSrcIpAddress().equals(item.getSrcIpAddress())
                    && before.getDstIpAddress().equals(item.getDstIpAddress())
                    && before.getSrcPort() == item.getSrcPort()
                    && before.getDstPort() == item.getDstPort();
        } else {
            return false;
        }
    }

    private void start() {
        listProperty.clear();
        networkMap = new NetworkMap(canvas);
        networkMap.start();
        try {
            capture.capture();
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        if (networkMap != null) {
            networkMap.stop();
        }
        capture.destroy();
    }
}
