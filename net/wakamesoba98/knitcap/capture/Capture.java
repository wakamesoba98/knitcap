package net.wakamesoba98.knitcap.capture;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.record.Location;
import javafx.application.Platform;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.geoip.GeoIPUtils;
import net.wakamesoba98.knitcap.view.ListViewControllable;
import org.pcap4j.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Capture {

    private static final String IFACE = "wlp3s0";
    private PcapHandle pcapHandle;
    private ExecutorService service;
    private GeoIPUtils geoIPUtils;
    private boolean isCapturing;

    public Capture() {
        geoIPUtils = new GeoIPUtils();
        isCapturing = false;
    }

    // readTimeOut [ms]
    // snapLength [bytes]

    public void capture(int count, int readTimeOut, int snapLength, final ListViewControllable controllable) throws PcapNativeException, NotOpenException {
        if (isCapturing) {
            return;
        }

        isCapturing = true;

        List<PcapNetworkInterface> ifaceList = Pcaps.findAllDevs();
        Map<String, PcapNetworkInterface> ifaceMap = new HashMap<>();
        for (PcapNetworkInterface iface : ifaceList) {
            ifaceMap.put(iface.getName(), iface);
        }

        PcapNetworkInterface networkIface = ifaceMap.get(IFACE);
        if (networkIface == null) {
            return;
        }

        IfaceAddress ifaceAddress = new IfaceAddress(networkIface.getAddresses());

        PacketListener listener = packet -> {
            Platform.runLater(() -> {
                PacketHeader header = new PacketHeader(packet, ifaceAddress);

                /* for debug
                String s = header.getProtocol() + "\t" + header.getSrcAddr() + ":" + header.getSrcPort() + "\t->\t" + header.getDstAddr() + ":" + header.getDstPort();
                System.out.println(s);
                */

                if (geoIPUtils != null && !header.isIpV6()) {
                    Location location = null;
                    try {
                        location = geoIPUtils.lookup(header.getDstAddr());
                        System.out.println(location.getLatitude() + ":" + location.getLongitude());
                    } catch (GeoIp2Exception e) {
                        // do nothing
                    }
                }
                controllable.addItem(header);
            });
        };

        pcapHandle = networkIface.openLive(snapLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeOut);
        service = Executors.newFixedThreadPool(1);
        final Thread thread = new Thread(() -> {
            try {
                pcapHandle.loop(count, listener);
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (InterruptedException | NotOpenException e) {
                // do nothing
            }
        });
        service.execute(thread);
    }

    public void destroy() {
        if (isCapturing) {
            try {
                pcapHandle.breakLoop();
            } catch (NotOpenException e) {
                e.printStackTrace();
            } finally {
                pcapHandle.close();
                service.shutdown();
                isCapturing = false;
            }
        }
    }
}
