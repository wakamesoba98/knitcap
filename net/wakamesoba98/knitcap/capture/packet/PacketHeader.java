package net.wakamesoba98.knitcap.capture.packet;

import net.wakamesoba98.knitcap.capture.IfaceAddress;
import org.pcap4j.packet.*;

public class PacketHeader {

    private IfaceAddress ifaceAddress;
    private String srcAddr, dstAddr;
    private PayloadProtocol protocol;
    private String otherProtocol;
    private int srcPort, dstPort;
    private boolean isIpV6;

    public PacketHeader(Packet packet, IfaceAddress ifaceAddress) {
        this.ifaceAddress = ifaceAddress;

        Packet.Header ipHeader = packet.getPayload().getHeader();
        if (ipHeader instanceof IpV4Packet.IpV4Header) {
            IpV4Packet.IpV4Header ipV4Header = (IpV4Packet.IpV4Header) ipHeader;
            srcAddr = ipV4Header.getSrcAddr().getHostAddress();
            dstAddr = ipV4Header.getDstAddr().getHostAddress();
            isIpV6 = false;
        } else if (ipHeader instanceof IpV6Packet.IpV6Header) {
            IpV6Packet.IpV6Header ipV6Header = (IpV6Packet.IpV6Header) ipHeader;
            srcAddr = ipV6Header.getSrcAddr().getHostAddress();
            dstAddr = ipV6Header.getDstAddr().getHostAddress();
            isIpV6 = true;
        } else if (ipHeader instanceof ArpPacket.ArpHeader) {
            protocol = PayloadProtocol.ARP;
            ArpPacket.ArpHeader arpHeader = (ArpPacket.ArpHeader) ipHeader;
            srcAddr = arpHeader.getSrcProtocolAddr().getHostAddress();
            dstAddr = arpHeader.getDstProtocolAddr().getHostAddress();
            isIpV6 = false;
        }

        if (protocol == PayloadProtocol.ARP) {
            srcPort = -1;
            dstPort = -1;
        } else {
            Packet.Header payloadHeader = packet.getPayload().getPayload().getHeader();
            if (payloadHeader instanceof TcpPacket.TcpHeader) {
                protocol = PayloadProtocol.TCP;
                TcpPacket.TcpHeader tcpHeader = (TcpPacket.TcpHeader) payloadHeader;
                srcPort = tcpHeader.getSrcPort().valueAsInt();
                dstPort = tcpHeader.getDstPort().valueAsInt();
            } else if (payloadHeader instanceof UdpPacket.UdpHeader) {
                protocol = PayloadProtocol.UDP;
                UdpPacket.UdpHeader udpHeader = (UdpPacket.UdpHeader) payloadHeader;
                srcPort = udpHeader.getSrcPort().valueAsInt();
                dstPort = udpHeader.getDstPort().valueAsInt();
            } else if (payloadHeader instanceof IcmpV4CommonPacket.IcmpV4CommonHeader) {
                protocol = PayloadProtocol.ICMPv4;
                IcmpV4CommonPacket.IcmpV4CommonHeader icmpV4Header
                        = (IcmpV4CommonPacket.IcmpV4CommonHeader) payloadHeader;
                srcPort = -1;
                dstPort = -1;
            } else if (payloadHeader instanceof IcmpV6CommonPacket.IcmpV6CommonHeader) {
                protocol = PayloadProtocol.ICMPv6;
                IcmpV6CommonPacket.IcmpV6CommonHeader icmpV6Header
                        = (IcmpV6CommonPacket.IcmpV6CommonHeader) payloadHeader;
                srcPort = -1;
                dstPort = -1;
            } else {
                protocol = PayloadProtocol.OTHER;
                srcPort = -1;
                dstPort = -1;
                if (ipHeader instanceof IpV4Packet.IpV4Header) {
                    IpV4Packet.IpV4Header ipV4Header = (IpV4Packet.IpV4Header) ipHeader;
                    otherProtocol = ipV4Header.getProtocol().name();
                } else {
                    otherProtocol = "Unknown";
                }
            }
        }
    }

    public IfaceAddress getIfaceAddress() {
        return ifaceAddress;
    }

    public String getSrcAddr() {
        return srcAddr;
    }

    public String getDstAddr() {
        return dstAddr;
    }

    public PayloadProtocol getProtocol() {
        return protocol;
    }

    public String getOtherProtocol() {
        return otherProtocol;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public boolean isIpV6() {
        return isIpV6;
    }

}