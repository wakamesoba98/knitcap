package net.wakamesoba98.knitcap.view.canvas;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import net.wakamesoba98.knitcap.capture.packet.PayloadProtocol;

public class PacketCircle {
    private static final int STEP = 20;
    private static final int RADIUS = 15;
    private int srcX, srcY, gatewayX, gatewayY, dstX, dstY;
    private Color color;
    private int animation = 0;
    private boolean isPassedGateway = false, isFinished = false;

    public PacketCircle(NetworkObject src, NetworkObject gateway, NetworkObject dst, PayloadProtocol protocol) {
        srcX = src.getX() + src.getWidth()/2;
        srcY = src.getY() + src.getWidth()/2;
        gatewayX = gateway.getX() + gateway.getWidth()/2;
        gatewayY = gateway.getY() + gateway.getHeight()/2;
        dstX = dst.getX() + dst.getWidth()/2;
        dstY = dst.getY() + dst.getHeight()/2;
        this.color = protocol.getColor();
    }

    public void draw(GraphicsContext context) {
        int x, y, fromX, fromY, toX, toY;
        animation++;

        if (srcX == gatewayX && srcY == gatewayY) {
            isPassedGateway = true;
        }

        if (isPassedGateway) {
            fromX = gatewayX;
            fromY = gatewayY;
            toX = dstX;
            toY = dstY;
        } else {
            fromX = srcX;
            fromY = srcY;
            toX = gatewayX;
            toY = gatewayY;
        }

        x = fromX + ((toX - fromX) / STEP) * animation;
        y = fromY + ((toY - fromY) / STEP) * animation;

        context.setFill(color);
        context.fillOval(x - RADIUS/2, y - RADIUS/2, RADIUS, RADIUS);

        if (animation >= STEP) {
            if (isPassedGateway) {
                isFinished = true;
            } else {
                if (dstX == gatewayX && dstY == gatewayY) {
                    isFinished = true;
                } else {
                    isPassedGateway = true;
                    animation = 0;
                }
            }

        }
    }

    public boolean isFinished() {
        return isFinished;
    }
}