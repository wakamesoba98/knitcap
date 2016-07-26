package net.wakamesoba98.knitcap.view.canvas;

import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.capture.packet.PayloadProtocol;
import net.wakamesoba98.knitcap.window.MainWindow;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.*;

import java.awt.Font;
import java.util.List;

public class ListDraw {

    static final int ITEM_WIDTH = 600;
    private static final int ITEM_HEIGHT = 70;

    private TrueTypeFont small, large;
    private Image txImg, rxImg, noneImg;
    private int height;

    public ListDraw(int height) throws SlickException {
        small = new TrueTypeFont(new Font("Sans", Font.PLAIN, 14), true);
        large = new TrueTypeFont(new Font("Sans", Font.PLAIN, 22), true);
        SpriteSheet sheet = new SpriteSheet("res/png/sprite_cell.png", 16, 16);
        txImg = sheet.getSprite(0, 0);
        rxImg = sheet.getSprite(1, 0);
        noneImg = sheet.getSprite(2, 0);
        this.height = height;
    }

    public void draw(Graphics graphics, List<PacketHeader> packetList) {
        int max = (height / ITEM_HEIGHT) + 1;
        int count = (packetList.size() > max) ? max : packetList.size();
        for (int i = 0; i < count; i++) {
            drawItem(graphics, packetList.get(i), i);
        }
    }

    private void drawItem(Graphics graphics, PacketHeader packet, int position) {
        PayloadProtocol proto = packet.getProtocol();
        String protoString = "";
        String addrSrc, addrDst, portSrc, portDst;
        int y = ITEM_HEIGHT * position + MainWindow.TOOLBAR_HEIGHT;
        
        if (position % 2 == 0) {
            graphics.setColor(Color.white);
        } else {
            graphics.setColor(Color.decode("#f2f2f2"));
        }
        graphics.fillRect(0, y, ITEM_WIDTH, ITEM_HEIGHT);

        if (proto != null) {
            if (proto == PayloadProtocol.OTHER) {
                protoString = "Other (" + packet.getOtherProtocol() + ")";
            } else {
                protoString = proto.toString();
            }
            graphics.setColor(packet.getProtocol().getColor());
        } else {
            graphics.setColor(Color.black);
        }

        addrSrc = packet.getSrcIpAddress();
        addrDst = packet.getDstIpAddress();
        if (packet.getSrcPort() > 0 && packet.getDstPort() > 0) {
            portSrc = ":" + packet.getSrcPort();
            portDst = ":" + packet.getDstPort();
        } else {
            portSrc = "";
            portDst = "";
        }
        graphics.fillRect(0, y, 7, 50);

        graphics.setColor(Color.black);
        int base = 0;
        if (packet.isIpV6()) {
            graphics.setFont(small);
        } else {
            graphics.setFont(large);
            base = 6;
        }
        graphics.drawString(addrSrc, 14, 26 - base + y);
        graphics.drawString(addrDst, 324, 26 - base + y);

        graphics.setFont(small);
        graphics.drawString("->", 284, 24 + y);
        graphics.drawString(protoString, 14, 4 + y);
        graphics.drawString(portSrc, 14, 46 + y);
        graphics.drawString(portDst, 324, 46 + y);

        switch (packet.getPacketType()) {
            case SEND:
                txImg.draw(578, 6 + y);
                break;

            case RECEIVE:
                rxImg.draw(578, 6 + y);
                break;

            default:
                noneImg.draw(578, 6 + y);
                break;
        }
    }
}
