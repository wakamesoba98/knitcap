package net.wakamesoba98.knitcap.view.canvas;

class NetworkLine {
    private int fromX, fromY, toX, toY;

    NetworkLine(NetworkObject from, NetworkObject to) {
        fromX = from.getX() + from.getWidth()/2;
        fromY = from.getY() + from.getHeight()/2;
        toX = to.getX() + to.getWidth()/2;
        toY = to.getY() + to.getHeight()/2;
    }

    int getFromX() {
        return fromX;
    }

    int getFromY() {
        return fromY;
    }

    int getToX() {
        return toX;
    }

    int getToY() {
        return toY;
    }
}
