package net.wakamesoba98.knitcap.view.canvas;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.capture.packet.PayloadProtocol;

import java.util.List;

public class ListDraw {

    private static final int ITEM_WIDTH = 640, ITEM_HEIGHT = 70;
    private static final int FONT_BASE = 16;
    private static final int IMG_SIZE = 16;

    private GraphicsContext context;
    private Timeline timeline;
    private Font small, large;
    private Image txImg, rxImg, noneImg;

    public ListDraw() {
        small = new Font(14);
        large = new Font(22);
        txImg = new Image(getClass().getResource("/res/png/tx.png").toExternalForm());
        rxImg = new Image(getClass().getResource("/res/png/rx.png").toExternalForm());
        noneImg = new Image(getClass().getResource("/res/png/none.png").toExternalForm());
    }

    public void start(Canvas canvas, List<PacketHeader> itemList) {
        stop();

        context = canvas.getGraphicsContext2D();
        int max = (int) (canvas.getHeight() / ITEM_HEIGHT) + 1;

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(
            Duration.seconds(0.017), // 60 FPS
            actionEvent -> {
                int count = (itemList.size() > max) ? max : itemList.size();
                for (int i = 0; i < count; i++) {
                    drawItem(itemList.get(i), i);
                }
            }
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void drawItem(PacketHeader item, int position) {
        PayloadProtocol proto = item.getProtocol();
        String protoString = "";
        String addrSrc, addrDst, portSrc, portDst;
        int y = ITEM_HEIGHT * position;
        
        if (position % 2 == 0) {
            context.setFill(Color.WHITE);
        } else {
            context.setFill(Color.rgb(242, 242, 242));
        }
        context.fillRect(0, y, ITEM_WIDTH, ITEM_HEIGHT);

        if (proto != null) {
            if (proto == PayloadProtocol.OTHER) {
                protoString = "Other (" + item.getOtherProtocol() + ")";
            } else {
                protoString = proto.toString();
            }
            context.setFill(item.getProtocol().getColor());
        } else {
            context.setFill(Color.BLACK);
        }

        addrSrc = item.getSrcIpAddress();
        addrDst = item.getDstIpAddress();
        if (item.getSrcPort() > 0 && item.getDstPort() > 0) {
            portSrc = ":" + item.getSrcPort();
            portDst = ":" + item.getDstPort();
        } else {
            portSrc = "";
            portDst = "";
        }
        context.fillRect(0, y, 7, 50);

        context.setFill(Color.BLACK);
        if (item.isIpV6()) {
            context.setFont(small);
        } else {
            context.setFont(large);
        }
        context.fillText(addrSrc, 14, 26 + FONT_BASE +  y);
        context.fillText(addrDst, 324, 26 + FONT_BASE + y);

        context.setFont(small);
        context.fillText("->", 284, 24 + FONT_BASE + y);
        context.fillText(protoString, 14, FONT_BASE + y);
        context.fillText(portSrc, 14, 46 + FONT_BASE + y);
        context.fillText(portDst, 324, 46 + FONT_BASE + y);

        switch (item.getPacketType()) {
            case SEND:
                context.drawImage(txImg, 578, 6 + y, IMG_SIZE, IMG_SIZE);
                break;

            case RECEIVE:
                context.drawImage(rxImg, 578, 6 + y, IMG_SIZE, IMG_SIZE);
                break;

            default:
                context.drawImage(noneImg, 578, 6 + y, IMG_SIZE, IMG_SIZE);
                break;
        }
    }
}
