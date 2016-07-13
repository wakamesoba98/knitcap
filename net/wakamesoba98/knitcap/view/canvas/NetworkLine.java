package net.wakamesoba98.knitcap.view.canvas;

public class NetworkLine {
    private int fromX, fromY, toX, toY;

    public NetworkLine(NetworkObject from, NetworkObject to) {
        fromX = from.getX() + from.getWidth()/2;
        fromY = from.getY() + from.getHeight()/2;
        toX = to.getX() + to.getWidth()/2;
        toY = to.getY() + to.getHeight()/2;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public int getToX() {
        return toX;
    }

    public int getToY() {
        return toY;
    }
}
