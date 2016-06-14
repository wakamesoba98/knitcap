package net.wakamesoba98.knitcap;

import org.pcap4j.core.*;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Capture {

    public static final String IFACE = "enp0s25";

    // readTimeOut [ms]
    // snapLength [bytes]

    public void capture(int count, int readTimeOut, int snapLength, String filter) throws PcapNativeException, NotOpenException {

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

        if (filter.length() != 0) {
            handle.setFilter(
                    filter,
                    BpfProgram.BpfCompileMode.OPTIMIZE
            );
        }

        PacketListener listener = new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                System.out.println(handle.getTimestamp());

                Packet.Header header = packet.getPayload().getHeader();
                IpV4Packet.IpV4Header ipv4Header = (IpV4Packet.IpV4Header) header;
                System.out.println(ipv4Header.getSrcAddr() + " -> " + ipv4Header.getDstAddr());
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
