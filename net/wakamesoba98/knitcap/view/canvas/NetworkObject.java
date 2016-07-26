package net.wakamesoba98.knitcap.view.canvas;

import org.newdawn.slick.Image;

class NetworkObject {

    private static final int IMG_SIZE = 72;
    private Image image;
    private String name;
    private int x, y, width, height;

    NetworkObject(Image image, String name, int x, int y) {
        this.image = image;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = IMG_SIZE;
        this.height = IMG_SIZE;
    }

    Image getImage() {
        return image;
    }

    String getName() {
        return name;
    }

    int getX() {
        return x;
    }

    void setX(int x) {
        this.x = x;
    }

    int getY() {
        return y;
    }

    void setY(int y) {
        this.y = y;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }
}
