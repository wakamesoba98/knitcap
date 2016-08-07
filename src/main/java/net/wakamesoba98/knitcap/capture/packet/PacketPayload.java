package net.wakamesoba98.knitcap.capture.packet;

import org.pcap4j.packet.Packet;

public class PacketPayload {

    private String packet;

    public PacketPayload(Packet packet) {

    }

    public String getPacket() {
        return packet;
    }
}
