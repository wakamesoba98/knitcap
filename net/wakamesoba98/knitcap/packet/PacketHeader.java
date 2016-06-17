package net.wakamesoba98.knitcap.packet;

import org.pcap4j.packet.*;

public class PacketHeader {

    private String srcAddr, dstAddr, protocol;
    private int srcPort, dstPort;

    public PacketHeader(Packet packet) {
        Packet.Header ipHeader = packet.getPayload().getHeader();
        if (ipHeader instanceof IpV4Packet.IpV4Header) {
            IpV4Packet.IpV4Header ipV4Header = (IpV4Packet.IpV4Header) ipHeader;
            srcAddr = ipV4Header.getSrcAddr().getHostAddress();
            dstAddr = ipV4Header.getDstAddr().getHostAddress();
        } else if (ipHeader instanceof IpV6Packet.IpV6Header) {
            IpV6Packet.IpV6Header ipV6Header = (IpV6Packet.IpV6Header) ipHeader;
            srcAddr = ipV6Header.getSrcAddr().getHostAddress();
            dstAddr = ipV6Header.getDstAddr().getHostAddress();
        }

        Packet.Header payloadHeader = packet.getPayload().getPayload().getHeader();
        if (payloadHeader instanceof TcpPacket.TcpHeader) {
            protocol = "TCP";
            TcpPacket.TcpHeader tcpHeader = (TcpPacket.TcpHeader) payloadHeader;
            srcPort = tcpHeader.getSrcPort().valueAsInt();
            dstPort = tcpHeader.getDstPort().valueAsInt();
        } else if (payloadHeader instanceof UdpPacket.UdpHeader) {
            protocol = "UDP";
            UdpPacket.UdpHeader udpHeader = (UdpPacket.UdpHeader) payloadHeader;
            srcPort = udpHeader.getSrcPort().valueAsInt();
            dstPort = udpHeader.getDstPort().valueAsInt();
        } else if (payloadHeader instanceof IcmpV4CommonPacket.IcmpV4CommonHeader) {
            protocol = "ICMPv4";
            IcmpV4CommonPacket.IcmpV4CommonHeader icmpV4Header
                    = (IcmpV4CommonPacket.IcmpV4CommonHeader) payloadHeader;
            srcPort = -1;
            dstPort = -1;
        } else if (payloadHeader instanceof IcmpV6CommonPacket.IcmpV6CommonHeader) {
            protocol = "ICMPv6";
            IcmpV6CommonPacket.IcmpV6CommonHeader icmpV6Header
                    = (IcmpV6CommonPacket.IcmpV6CommonHeader) payloadHeader;
            srcPort = -1;
            dstPort = -1;
        }
    }

    public String getSrcAddr() {
        return srcAddr;
    }

    public String getDstAddr() {
        return dstAddr;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }
}