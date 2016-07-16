package net.wakamesoba98.knitcap.capture.packet;

import net.wakamesoba98.knitcap.capture.NetworkDevice;
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
        Packet.Header ipHeader = packet.getPayload().getHeader();
        if (ipHeader instanceof IpV4Packet.IpV4Header) {
            IpV4Packet.IpV4Header ipV4Header = (IpV4Packet.IpV4Header) ipHeader;
            srcIpAddress = ipV4Header.getSrcAddr().getHostAddress();
            dstIpAddress = ipV4Header.getDstAddr().getHostAddress();
            isIpV6 = false;
        } else if (ipHeader instanceof IpV6Packet.IpV6Header) {
            IpV6Packet.IpV6Header ipV6Header = (IpV6Packet.IpV6Header) ipHeader;
            srcIpAddress = ipV6Header.getSrcAddr().getHostAddress();
            dstIpAddress = ipV6Header.getDstAddr().getHostAddress();
            isIpV6 = true;
        } else if (ipHeader instanceof ArpPacket.ArpHeader) {
            protocol = PayloadProtocol.ARP;
            ArpPacket.ArpHeader arpHeader = (ArpPacket.ArpHeader) ipHeader;
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
            isSrcAsSameSubnet = isSameSubnetV4(srcIpAddress, networkDevice.getIpV4Address(), networkDevice.getIpV4SubnetMask());
            isDstAsSameSubnet = isSameSubnetV4(dstIpAddress, networkDevice.getIpV4Address(), networkDevice.getIpV4SubnetMask());
            isDstAsBroadcast = isBroadcastV4(dstIpAddress, networkDevice.getIpV4SubnetMask());
        }
    }

    private boolean isSameSubnetV4(String srcAddress, String dstAddress, String mask) {
        long srcValue = convertLong(srcAddress);
        long dstValue = convertLong(dstAddress);
        long maskValue = convertLong(mask);
        return (srcValue & maskValue) == (dstValue & maskValue);
    }

    private boolean isBroadcastV4(String dstAddress, String mask) {
        byte[] dstValue = convertByte(dstAddress);
        byte[] maskValue = convertByte(mask);
        for (int i = 0; i < 4; i++) {
            if (~maskValue[i] != (byte) (dstValue[i] & ~maskValue[i])) {
                return false;
            }
        }
        return true;
    }

    private long convertLong(String address) {
        String[] octet = address.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int n = Integer.valueOf(octet[i]);
            result = result << 8;
            result += n;
        }
        return result;
    }

    private byte[] convertByte(String address) {
        String[] octet = address.split("\\.");
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = Short.valueOf(octet[i]).byteValue();
        }
        return result;
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