package net.wakamesoba98.knitcap.capture.packet;

import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.util.IpV4Utils;
import org.pcap4j.packet.*;

public class PacketHeader {

    private PayloadProtocol protocol;
    private PacketType packetType;
    private String srcIpAddress, dstIpAddress;
    private String srcHardwareAddress, dstHardwareAddress;
    private String otherProtocol;
    private int srcPort, dstPort;
    private boolean isSrcAsSameSubnet, isDstAsSameSubnet;
    private boolean isDstAsBroadcast;
    private boolean isIpV6;

    public PacketHeader(Packet packet, NetworkDevice networkDevice) {
        Packet.Header layer2Header = packet.getHeader();
        Packet.Header layer3Header = packet.getPayload().getHeader();
        if (layer3Header instanceof IpV4Packet.IpV4Header) {
            IpV4Packet.IpV4Header ipV4Header = (IpV4Packet.IpV4Header) layer3Header;
            srcIpAddress = ipV4Header.getSrcAddr().getHostAddress();
            dstIpAddress = ipV4Header.getDstAddr().getHostAddress();
            isIpV6 = false;
        } else if (layer3Header instanceof IpV6Packet.IpV6Header) {
            IpV6Packet.IpV6Header ipV6Header = (IpV6Packet.IpV6Header) layer3Header;
            srcIpAddress = ipV6Header.getSrcAddr().getHostAddress();
            dstIpAddress = ipV6Header.getDstAddr().getHostAddress();
            isIpV6 = true;
        } else if (layer3Header instanceof ArpPacket.ArpHeader) {
            protocol = PayloadProtocol.ARP;
            ArpPacket.ArpHeader arpHeader = (ArpPacket.ArpHeader) layer3Header;
            srcIpAddress = arpHeader.getSrcProtocolAddr().getHostAddress();
            dstIpAddress = arpHeader.getDstProtocolAddr().getHostAddress();
            srcHardwareAddress = arpHeader.getSrcHardwareAddr().toString();
            dstHardwareAddress = arpHeader.getDstHardwareAddr().toString();
            isIpV6 = false;
        }

        if (protocol == PayloadProtocol.ARP) {
            srcPort = -1;
            dstPort = -1;
        } else {
            if (layer2Header instanceof EthernetPacket.EthernetHeader) {
                EthernetPacket.EthernetHeader ethernetHeader = (EthernetPacket.EthernetHeader) layer2Header;
                srcHardwareAddress = ethernetHeader.getSrcAddr().toString();
                dstHardwareAddress = ethernetHeader.getDstAddr().toString();
            }

            Packet.Header layer4Header = packet.getPayload().getPayload().getHeader();
            if (layer4Header instanceof TcpPacket.TcpHeader) {
                protocol = PayloadProtocol.TCP;
                TcpPacket.TcpHeader tcpHeader = (TcpPacket.TcpHeader) layer4Header;
                srcPort = tcpHeader.getSrcPort().valueAsInt();
                dstPort = tcpHeader.getDstPort().valueAsInt();
            } else if (layer4Header instanceof UdpPacket.UdpHeader) {
                protocol = PayloadProtocol.UDP;
                UdpPacket.UdpHeader udpHeader = (UdpPacket.UdpHeader) layer4Header;
                srcPort = udpHeader.getSrcPort().valueAsInt();
                dstPort = udpHeader.getDstPort().valueAsInt();
            } else if (layer4Header instanceof IcmpV4CommonPacket.IcmpV4CommonHeader) {
                protocol = PayloadProtocol.ICMPv4;
                IcmpV4CommonPacket.IcmpV4CommonHeader icmpV4Header
                        = (IcmpV4CommonPacket.IcmpV4CommonHeader) layer4Header;
                srcPort = -1;
                dstPort = -1;
            } else if (layer4Header instanceof IcmpV6CommonPacket.IcmpV6CommonHeader) {
                protocol = PayloadProtocol.ICMPv6;
                IcmpV6CommonPacket.IcmpV6CommonHeader icmpV6Header
                        = (IcmpV6CommonPacket.IcmpV6CommonHeader) layer4Header;
                srcPort = -1;
                dstPort = -1;
            } else {
                protocol = PayloadProtocol.OTHER;
                srcPort = -1;
                dstPort = -1;
                if (layer3Header instanceof IpV4Packet.IpV4Header) {
                    IpV4Packet.IpV4Header ipV4Header = (IpV4Packet.IpV4Header) layer3Header;
                    otherProtocol = ipV4Header.getProtocol().name();
                } else {
                    otherProtocol = "Unknown";
                }
            }
        }

        String ipV4Address = networkDevice.getIpV4Address();
        String ipV6Address = networkDevice.getIpV6Address();
        if (srcIpAddress.equals(ipV4Address) || srcIpAddress.equals(ipV6Address)) {
            packetType = PacketType.SEND;
        } else if (dstIpAddress.equals(ipV4Address) || dstIpAddress.equals(ipV6Address)) {
            packetType = PacketType.RECEIVE;
        } else {
            packetType = PacketType.OTHER;
        }

        if (!isIpV6) {
            IpV4Utils utils = new IpV4Utils();
            isSrcAsSameSubnet = utils.isSameSubnetV4(srcIpAddress, networkDevice.getIpV4Address(), networkDevice.getIpV4SubnetMask());
            isDstAsSameSubnet = utils.isSameSubnetV4(dstIpAddress, networkDevice.getIpV4Address(), networkDevice.getIpV4SubnetMask());
            isDstAsBroadcast = utils.isBroadcastV4(dstIpAddress, networkDevice.getIpV4SubnetMask());
        }
    }



    public PayloadProtocol getProtocol() {
        return protocol;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public String getSrcIpAddress() {
        return srcIpAddress;
    }

    public String getDstIpAddress() {
        return dstIpAddress;
    }

    public String getSrcHardwareAddress() {
        return srcHardwareAddress;
    }

    public String getDstHardwareAddress() {
        return dstHardwareAddress;
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

    public boolean isSrcAsSameSubnet() {
        return isSrcAsSameSubnet;
    }

    public boolean isDstAsSameSubnet() {
        return isDstAsSameSubnet;
    }

    public boolean isDstAsBroadcast() {
        return isDstAsBroadcast;
    }
}