package net.wakamesoba98.knitcap.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import net.wakamesoba98.knitcap.capture.Capture;
import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.view.canvas.ListDraw;
import net.wakamesoba98.knitcap.view.canvas.NetworkMap;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainAppController implements Initializable, GuiControllable {

    @FXML
    private MenuItem menuStart, menuStop;
    @FXML
    private Canvas listCanvas;
    @FXML
    private Canvas mapCanvas;

    private static final int LIST_ITEM_MAX = 1000;
    private Capture capture;
    private NetworkMap networkMap;
    private ListDraw listDraw;
    private LinkedList<PacketHeader> packetList;
    private PacketHeader lastPacket;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        packetList = new LinkedList<>();
        menuStart.setOnAction(actionEvent -> start());
        menuStop.setOnAction(actionEvent -> destroy());
        listDraw = new ListDraw();
        networkMap = new NetworkMap();
        capture = new Capture(this);
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
            lastPacket = item;
            networkMap.addPacket(item);
            packetList.addFirst(item);
            while (packetList.size() > LIST_ITEM_MAX) {
                packetList.removeLast();
            }
        }
    }

    private boolean isSameHeader(PacketHeader item) {
        if (packetList.size() == 0) {
            return false;
        }
        if (lastPacket != null) {
            return lastPacket.getProtocol() == item.getProtocol()
                    && lastPacket.getSrcIpAddress().equals(item.getSrcIpAddress())
                    && lastPacket.getDstIpAddress().equals(item.getDstIpAddress())
                    && lastPacket.getSrcPort() == item.getSrcPort()
                    && lastPacket.getDstPort() == item.getDstPort();
        } else {
            return false;
        }
    }

    private void start() {
        destroy();
        try {
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
            List<String> names = devices.stream().map(PcapNetworkInterface::getName).collect(Collectors.toList());
            ChoiceDialog<String> dialog = new ChoiceDialog<>(names.get(0), names);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setHeaderText("Choose a network interface:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                listDraw.start(listCanvas, packetList);
                networkMap.start(mapCanvas);
                capture.capture(dialog.getSelectedItem());
            }
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
            destroy();
        }
    }

    public void destroy() {
        if (networkMap != null) {
            networkMap.stop();
        }
        if (listDraw != null) {
            listDraw.stop();
        }
        if (capture != null) {
            capture.destroy();
        }
    }
}
