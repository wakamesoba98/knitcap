package net.wakamesoba98.knitcap.capture;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.record.Location;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.capture.packet.PacketType;
import net.wakamesoba98.knitcap.capture.scanner.Arp;
import net.wakamesoba98.knitcap.capture.scanner.HostScanner;
import net.wakamesoba98.knitcap.capture.scanner.Ping;
import net.wakamesoba98.knitcap.geoip.GeoIPUtils;
import net.wakamesoba98.knitcap.view.GuiControllable;
import org.pcap4j.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Capture {

    private static final String PING_DST = "8.8.8.8";
    private static final int COUNT = -1;
    private static final int READ_TIME_OUT = 10; // [ms]
    private static final int SNAP_LENGTH = 65536; // [bytes]

    private PcapHandle pcapHandle;
    private ExecutorService service;
    private NetworkDevice localhost, gateway;
    private GeoIPUtils geoIPUtils;
    private GuiControllable guiControllable;
    private boolean isCapturing;

    public Capture(GuiControllable gui) {
        guiControllable = gui;
        geoIPUtils = new GeoIPUtils();
        isCapturing = false;
    }

    public void capture(String device) throws PcapNativeException, NotOpenException {
        if (isCapturing) {
            return;
        }

        isCapturing = true;

        List<PcapNetworkInterface> ifaceList = Pcaps.findAllDevs();
        Map<String, PcapNetworkInterface> ifaceMap = new HashMap<>();
        for (PcapNetworkInterface iface : ifaceList) {
            ifaceMap.put(iface.getName(), iface);
        }

        PcapNetworkInterface networkIface = ifaceMap.get(device);
        if (networkIface == null) {
            return;
        }
        localhost = new NetworkDevice(networkIface.getLinkLayerAddresses().get(0).toString(), networkIface.getAddresses());

        HostScanner scanner = new HostScanner();
        String gatewayAddress = scanner.getDefaultGateway();
        guiControllable.openedInterface(localhost);

        PacketListener listener = packet -> {
            if (packet == null || packet.getPayload() == null) {
                return;
            }
            PacketHeader header = new PacketHeader(packet, localhost);

            switch (header.getProtocol()) {
                case ARP:
                    receivedArpReply(gatewayAddress, header, networkIface);
                    break;

                case ICMPv4: case ICMPv6:
                    receivedPingReply(header);
                    break;
            }

            guiControllable.addItem(header);
        };

        pcapHandle = networkIface.openLive(SNAP_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIME_OUT);
        service = Executors.newFixedThreadPool(1);
        final Thread thread = new Thread(() -> {
            try {
                pcapHandle.loop(COUNT, listener);
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (InterruptedException | NotOpenException e) {
                // do nothing
            }
        });
        service.execute(thread);

        Arp arp = new Arp();
        PcapHandle sendHandle = networkIface.openLive(SNAP_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIME_OUT);
        new Thread(() -> {
            arp.send(sendHandle, localhost, gatewayAddress);
            sendHandle.close();
        }).start();
    }

    public void destroy() {
        if (isCapturing) {
            gateway = null;
            isCapturing = false;
            try {
                pcapHandle.breakLoop();
            } catch (NotOpenException e) {
                e.printStackTrace();
            } finally {
                pcapHandle.close();
                service.shutdown();
            }
        }
    }

    private void receivedArpReply(String gatewayAddress, PacketHeader header, PcapNetworkInterface networkIface) {
        if (gateway == null) {
            if (header.getPacketType() == PacketType.RECEIVE
                && header.getSrcIpAddress().equals(gatewayAddress)) {

                // TODO Subnet mask of IPv6
                gateway = new NetworkDevice(header.getSrcHardwareAddress(), header.getSrcIpAddress(), localhost.getIpV4SubnetMask());

                guiControllable.showGateway(gateway.getIpV4Address());
                Ping ping = new Ping(localhost);
                try {
                    PcapHandle sendHandle = networkIface.openLive(SNAP_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIME_OUT);
                    new Thread(() -> {
                        ping.send(sendHandle, gateway, PING_DST);
                        sendHandle.close();
                    }).start();
                } catch (PcapNativeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void receivedPingReply(PacketHeader header) {
        if (header.getPacketType() == PacketType.RECEIVE
            && header.getSrcIpAddress().equals(PING_DST)) {

            guiControllable.showInternet();
        }
    }

    private void debug(PacketHeader header) {
        String s = header.getProtocol() + "\t" + header.getSrcIpAddress() + ":" + header.getSrcPort() + "\t->\t" + header.getDstIpAddress() + ":" + header.getDstPort();
        System.out.println(s);
    }

    private void lookupGeoIP(PacketHeader header) {
        if (geoIPUtils != null && !header.isIpV6()) {
            Location location;
            try {
                location = geoIPUtils.lookup(header.getDstIpAddress());
                System.out.println(location.getLatitude() + ":" + location.getLongitude());
            } catch (GeoIp2Exception e) {
                // do nothing
            }
        }
    }
}