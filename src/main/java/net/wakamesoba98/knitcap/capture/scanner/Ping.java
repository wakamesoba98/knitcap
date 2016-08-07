package net.wakamesoba98.knitcap.capture.scanner;

import net.wakamesoba98.knitcap.capture.NetworkDevice;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.*;
import org.pcap4j.util.MacAddress;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Ping {

    private NetworkDevice device;

    public Ping(NetworkDevice device) {
        this.device = device;
    }

    public void send(PcapHandle handle, NetworkDevice gateway, String dstIpAddress) {
        IcmpV4EchoPacket.Builder echoBuilder = new IcmpV4EchoPacket.Builder();
        echoBuilder
                .identifier((short) 1);

        IcmpV4CommonPacket.Builder icmpV4Builder = new IcmpV4CommonPacket.Builder();
        icmpV4Builder
                .type(IcmpV4Type.ECHO)
                .code(IcmpV4Code.NO_CODE)
                .payloadBuilder(echoBuilder)
                .correctChecksumAtBuild(true);

        IpV4Packet.Builder ipv4Builder = new IpV4Packet.Builder();
        try {
            ipv4Builder
                    .version(IpVersion.IPV4)
                    .tos(IpV4Rfc791Tos.newInstance((byte) 0))
                    .ttl((byte) 100)
                    .protocol(IpNumber.ICMPV4)
                    .srcAddr((Inet4Address) InetAddress.getByName(device.getIpV4Address()))
                    .dstAddr((Inet4Address) InetAddress.getByName(dstIpAddress))
                    .payloadBuilder(icmpV4Builder)
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
        etherBuilder
                .srcAddr(MacAddress.getByName(device.getHardwareAddress(), ":"))
                .dstAddr(MacAddress.getByName(gateway.getHardwareAddress(), ":"))
                .type(EtherType.IPV4)
                .payloadBuilder(ipv4Builder)
                .paddingAtBuild(true);

        Packet p = etherBuilder.build();
        try {
            handle.sendPacket(p);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }
}
