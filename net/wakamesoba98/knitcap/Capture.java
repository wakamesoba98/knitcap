package net.wakamesoba98.knitcap;

import net.wakamesoba98.knitcap.packet.PacketHeader;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Capture {

    public static final String IFACE = "wlp3s0";
    //public static final String IFACE = "enp0s25";

    // readTimeOut [ms]
    // snapLength [bytes]

    public void capture(int count, int readTimeOut, int snapLength) throws PcapNativeException, NotOpenException {

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

        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                PacketHeader header = new PacketHeader(packet);
                System.out.println(header.getProtocol() + " / " + header.getSrcAddr() + ":" + header.getSrcPort() + " -> " + header.getDstAddr() + ":" + header.getDstPort());
            }
        };

        try {
            handle.loop(count, listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        handle.close();
    }
}
