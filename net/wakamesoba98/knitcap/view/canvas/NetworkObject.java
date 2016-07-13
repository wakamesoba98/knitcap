package net.wakamesoba98.knitcap.view.canvas;

import javafx.scene.image.Image;

public class NetworkObject {

    private static final int IMG_SIZE = 96;
    private Image image;
    private String name;
    private int x, y, width, height;

    public NetworkObject(Image image, String name, int x, int y) {
        this.image = image;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = IMG_SIZE;
        this.height = IMG_SIZE;
    }

    public Image getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
