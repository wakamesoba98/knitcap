package net.wakamesoba98.knitcap.capture;

import javafx.application.Platform;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.view.ListViewControllable;
import org.pcap4j.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Capture {

    private static final String IFACE = "wlp3s0";

    // readTimeOut [ms]
    // snapLength [bytes]

    public void capture(int count, int readTimeOut, int snapLength, final ListViewControllable controllable) throws PcapNativeException, NotOpenException {

        List<PcapNetworkInterface> ifaceList = Pcaps.findAllDevs();
        Map<String, PcapNetworkInterface> ifaceMap = new HashMap<>();
        for (PcapNetworkInterface iface : ifaceList) {
            ifaceMap.put(iface.getName(), iface);
        }

        PcapNetworkInterface networkInterface = ifaceMap.get(IFACE);
        if (networkInterface == null) {
            return;
        }

        final PcapHandle handle = networkInterface.openLive(snapLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeOut);

        PacketListener listener = packet -> {
            PacketHeader header = new PacketHeader(packet);
            String s = header.getProtocol() + "\t" + header.getSrcAddr() + ":" + header.getSrcPort() + "\t->\t" + header.getDstAddr() + ":" + header.getDstPort();
            Platform.runLater(() -> controllable.addItem(s));
        };

        final Thread thread = new Thread(() -> {
            try {
                handle.loop(count, listener);
                handle.close();
                Platform.runLater(() -> controllable.addItem("Done."));
            } catch (PcapNativeException | InterruptedException | NotOpenException e) {
                e.printStackTrace();
            }
        });
        final ExecutorService service = Executors.newFixedThreadPool(5);
        service.execute(thread);
    }
}
