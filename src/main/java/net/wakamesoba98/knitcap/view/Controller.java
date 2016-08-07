package net.wakamesoba98.knitcap.view;

import net.wakamesoba98.knitcap.capture.Capture;
import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.view.canvas.ListDraw;
import net.wakamesoba98.knitcap.view.canvas.NetworkMap;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

import java.io.IOException;
import java.util.LinkedList;

public class Controller implements GuiControllable {

    private static final int LIST_ITEM_MAX = 1000;
    private Capture capture;
    private NetworkMap networkMap;
    private ListDraw listDraw;
    private LinkedList<PacketHeader> packetList;

    public Controller(int width, int height) {
        packetList = new LinkedList<>();
        try {
            listDraw = new ListDraw(height);
            networkMap = new NetworkMap(width, height);
        } catch (SlickException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openedInterface(NetworkDevice device) {
        networkMap.showLocalhost(device);
    }

    @Override
    public void showGateway(String address) {
        networkMap.showGateway(address);
    }

    @Override
    public void showInternet() {
        networkMap.showInternet();
    }

    @Override
    public void refresh() {
        networkMap.refresh();
    }

    @Override
    public void addItem(PacketHeader item) {
        networkMap.addPacket(item);
        packetList.addFirst(item);
        while (packetList.size() > LIST_ITEM_MAX) {
            packetList.removeLast();
        }
    }

    public void start(String device) {
        destroy();
        try {
            capture = new Capture(this);
            capture.capture(device);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics graphics) {
        networkMap.draw(graphics);
        listDraw.draw(graphics, packetList);
    }

    public void destroy() {
        if (capture != null) {
            capture.destroy();
            capture = null;
        }
    }
}
