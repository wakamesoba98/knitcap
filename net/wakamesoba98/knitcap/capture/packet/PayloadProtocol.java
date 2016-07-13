package net.wakamesoba98.knitcap.capture.packet;

import javafx.scene.paint.Color;

public enum PayloadProtocol {
    TCP    (Color.BLUE),
    UDP    (Color.RED),
    ICMPv4 (Color.GREEN),
    ICMPv6 (Color.GREEN),
    ARP    (Color.PURPLE),
    OTHER  (Color.BLACK)
    ;

    private final Color color;
    PayloadProtocol(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
