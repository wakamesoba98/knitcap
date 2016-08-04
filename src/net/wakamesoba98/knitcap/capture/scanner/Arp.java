package net.wakamesoba98.knitcap.capture.scanner;

import net.wakamesoba98.knitcap.capture.NetworkDevice;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Arp {
    public void send(PcapHandle handle, NetworkDevice device, String gatewayIpAddress) {
        ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
        try {
            arpBuilder
                    .hardwareType(ArpHardwareType.ETHERNET)
                    .protocolType(EtherType.IPV4)
                    .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                    .protocolAddrLength((byte) ByteArrays.INET4_ADDRESS_SIZE_IN_BYTES)
                    .operation(ArpOperation.REQUEST)
                    .srcHardwareAddr(MacAddress.getByName(device.getHardwareAddress(), ":"))
                    .srcProtocolAddr(InetAddress.getByName(device.getIpV4Address()))
                    .dstHardwareAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                    .dstProtocolAddr(InetAddress.getByName(gatewayIpAddress));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder
                .dstAddr(MacAddress.ETHER_BROADCAST_ADDRESS)
                .srcAddr(MacAddress.getByName(device.getHardwareAddress(), ":"))
                .type(EtherType.ARP)
                .payloadBuilder(arpBuilder)
                .paddingAtBuild(true);

        Packet p = etherBuilder.build();
        try {
            handle.sendPacket(p);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }
}
