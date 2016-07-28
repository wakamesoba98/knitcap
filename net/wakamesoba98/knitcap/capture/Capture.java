package net.wakamesoba98.knitcap.capture;

import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.capture.packet.PacketType;
import net.wakamesoba98.knitcap.capture.scanner.Arp;
import net.wakamesoba98.knitcap.capture.scanner.HostScanner;
import net.wakamesoba98.knitcap.capture.scanner.Ping;
import net.wakamesoba98.knitcap.view.GuiControllable;
import org.pcap4j.core.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Capture {

    private static final String PING_DST = "8.8.8.8";
    private static final int COUNT = -1;
    private static final int READ_TIME_OUT = 10; // [ms]
    private static final int SNAP_LENGTH = 65536; // [bytes]

    private PcapHandle pcapHandle;
    private NetworkDevice localhost, gateway;
    private GuiControllable guiControllable;
    private boolean isCapturing, isGatewayDetected, isPingReceived;

    public Capture(GuiControllable gui) {
        guiControllable = gui;
        isCapturing = false;
    }

    public void capture(String device) throws PcapNativeException, NotOpenException {
        if (isCapturing) {
            return;
        }

        isCapturing = true;
        isGatewayDetected = false;
        isPingReceived = false;

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
                    if (!isGatewayDetected) {
                        receivedArpReply(gatewayAddress, header, networkIface);
                    }
                    break;

                case ICMPv4: case ICMPv6:
                    if (!isPingReceived) {
                        receivedPingReply(header, networkIface);
                    }
                    break;
            }

            guiControllable.addItem(header);
        };

        pcapHandle = networkIface.openLive(SNAP_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIME_OUT);
        new Thread(() -> {
            try {
                pcapHandle.loop(COUNT, listener);
            } catch (PcapNativeException e) {
                e.printStackTrace();
            } catch (InterruptedException | NotOpenException e) {
                // do nothing
            }
        }).start();


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
                pcapHandle.close();
            } catch (NotOpenException e) {
                e.printStackTrace();
            }
        }
    }

    private void receivedArpReply(String gatewayAddress, PacketHeader header, PcapNetworkInterface networkIface) {
        if (header.getPacketType() == PacketType.RECEIVE
            && header.getSrcIpAddress().equals(gatewayAddress)) {

            isGatewayDetected = true;

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

    private void receivedPingReply(PacketHeader header, PcapNetworkInterface networkIface) {
        if (header.getPacketType() == PacketType.RECEIVE
            && header.getSrcIpAddress().equals(PING_DST)) {

            isPingReceived = true;
            guiControllable.showInternet();

            try {
                PcapHandle sendHandle = networkIface.openLive(SNAP_LENGTH, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIME_OUT);
                new Thread(() -> {
                    HostScanner scanner = new HostScanner();
                    scanner.sendArpHostScan(sendHandle, localhost, 255);
                    sendHandle.close();
                }).start();
            } catch (PcapNativeException e) {
                e.printStackTrace();
            }
        }
    }
}