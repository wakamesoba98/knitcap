package net.wakamesoba98.knitcap.capture.packet;

import org.newdawn.slick.Color;

public enum PayloadProtocol {
    TCP    (Color.blue),
    UDP    (Color.red),
    ICMPv4 (Color.green),
    ICMPv6 (Color.green),
    ARP    (Color.magenta),
    OTHER  (Color.black);

    private final Color color;
    PayloadProtocol(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
