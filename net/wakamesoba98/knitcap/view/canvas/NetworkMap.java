package net.wakamesoba98.knitcap.view.canvas;

import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;
import net.wakamesoba98.knitcap.window.MainWindow;
import org.newdawn.slick.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkMap {

    private static final int QUEUE_MAX = 1000;
    private static final int IMAGE_SIZE = 72;
    private static final String INTERNET = "INTERNET";

    private int line, internetX, internetY, gatewayX, gatewayY;
    private ConcurrentLinkedQueue<PacketCircle> packetQueue;
    private ConcurrentHashMap<String, NetworkObject> objectMap;
    private CopyOnWriteArrayList<NetworkLine> lineList;
    private List<String> localNetworkHostsList;
    private String gatewayIpAddress;
    private SpriteSheet sheet;
    private TrueTypeFont font;
    private PacketHeader lastPacket;
    private Set<String> mobileOuis;

    public NetworkMap(int width, int height) throws SlickException, IOException {
        packetQueue = new ConcurrentLinkedQueue<>();
        objectMap = new ConcurrentHashMap<>();
        localNetworkHostsList = new LinkedList<>();
        lineList = new CopyOnWriteArrayList<>();
        font = new TrueTypeFont(new java.awt.Font("Sans", java.awt.Font.PLAIN, 14), true);
        sheet = new SpriteSheet(Paths.get("res/png/sprite_map.png").toRealPath(LinkOption.NOFOLLOW_LINKS).toString(), IMAGE_SIZE, IMAGE_SIZE);
        setMetrics(width, height);
        mobileOuis = new HashSet<>();
        Files.lines(Paths.get("res/txt/mobile_oui.txt").toRealPath(LinkOption.NOFOLLOW_LINKS)).forEach(s -> mobileOuis.add(s));
    }

    public void draw(Graphics graphics) {
        graphics.setColor(Color.blue);
        graphics.setFont(font);

        for (NetworkLine line : lineList) {
            graphics.drawLine(line.getFromX(), line.getFromY(), line.getToX(), line.getToY());
        }

        Iterator<PacketCircle> iterator = packetQueue.iterator();
        while (iterator.hasNext()) {
            PacketCircle circle = iterator.next();
            if (circle.isFinished()) {
                iterator.remove();
            } else {
                circle.draw(graphics);
            }
        }

        graphics.setColor(Color.black);

        for (NetworkObject object : objectMap.values()) {
            graphics.drawImage(object.getImage(), object.getX(), object.getY());
        }
        for (NetworkObject object : objectMap.values()) {
            graphics.drawString(object.getName(), object.getX() + object.getWidth(), object.getY());
        }
    }

    public void showLocalhost(NetworkDevice device) {
        // TODO show IPv6 address of localhost
        //if (device.getIpV6Address() != null && !device.getIpV6Address().equals("")) {
        //    localhostIpAddress = device.getIpV6Address();
        //} else {
        detectHostsInNetwork(device.getIpV4Address(), device.getHardwareAddress());
        //}
    }

    public void showGateway(String defaultGateway) {
        if (defaultGateway != null && !defaultGateway.equals("")) {
            gatewayIpAddress = defaultGateway;
            objectMap.put(defaultGateway, new NetworkObject(sheet.getSubImage(2, 0), defaultGateway, gatewayX, gatewayY));
        }
        createNetworkLine();
    }

    public void showInternet() {
        objectMap.put(INTERNET, new NetworkObject(sheet.getSubImage(1, 0), INTERNET, internetX, internetY));
        createNetworkLine();
    }

    public void addPacket(PacketHeader header) {
        if (packetQueue.size() < QUEUE_MAX && !isSameHeader(header)) {
            lastPacket = header;
            NetworkObject src, gateway, dst;
            src = objectMap.get(header.getSrcIpAddress());
            gateway = objectMap.get(gatewayIpAddress);
            dst = objectMap.get(header.getDstIpAddress());
            if (src == null || dst == null) {
                if (header.isDstAsBroadcast()) {
                    dst = gateway;
                } else {
                    switch (header.getPacketType()) {
                        case RECEIVE:
                            if (header.isSrcAsSameSubnet()) {
                                // add new host to objectMap
                                detectHostsInNetwork(header.getSrcIpAddress(), header.getSrcHardwareAddress());
                            } else {
                                src = objectMap.get(INTERNET);
                            }
                            break;

                        case SEND:
                            if (header.isDstAsSameSubnet()) {
                                dst = gateway;
                            } else {
                                dst = objectMap.get(INTERNET);
                            }
                            break;

                        default:
                            if (header.isSrcAsSameSubnet()) {
                                if (src == null) {
                                    detectHostsInNetwork(header.getSrcIpAddress(), header.getSrcHardwareAddress());
                                } else {
                                    dst = objectMap.get(INTERNET);
                                }
                            } else if (header.isDstAsSameSubnet()) {
                                if (dst == null) {
                                    detectHostsInNetwork(header.getDstIpAddress(), header.getDstHardwareAddress());
                                } else {
                                    src = objectMap.get(INTERNET);
                                }
                            }
                            break;

                    }
                }
            }
            if (src != null && dst != null) {
                PacketCircle circle = new PacketCircle(this, src, gateway, dst, header);
                packetQueue.offer(circle);
            }
        }
    }

    void sendBroadcast(PacketHeader header) {
        new Thread(() -> {
            for (String address : localNetworkHostsList) {
                if (address.equals(header.getSrcIpAddress())) {
                    continue;
                }
                NetworkObject gateway = objectMap.get(gatewayIpAddress);
                NetworkObject dst = objectMap.get(address);
                PacketCircle circle = new PacketCircle(this, gateway, gateway, dst, header);
                packetQueue.offer(circle);
            }
        }).start();
    }

    private void setMetrics(int width, int height) {
        width  -= ListDraw.ITEM_WIDTH;
        height -= MainWindow.TOOLBAR_HEIGHT;

        int smaller = (width < height) ? width : height;
        line = (smaller - 150) / 2;
        gatewayX = width / 2 + ListDraw.ITEM_WIDTH - IMAGE_SIZE / 2;
        gatewayY = height / 2 + MainWindow.TOOLBAR_HEIGHT - IMAGE_SIZE / 2;
        internetX = gatewayX;
        internetY = gatewayY - line;
    }

    private void detectHostsInNetwork(String ipAddress, String hardwareAddress) {
        if ("00:00:00:00:00:00".equals(hardwareAddress)) {
            return;
        }

        localNetworkHostsList.add(ipAddress);

        Image image;
        String oui = hardwareAddress.replace(":", "").substring(0, 6);
        if (mobileOuis.contains(oui)) {
            image = sheet.getSubImage(3, 0);
        } else {
            image = sheet.getSubImage(0, 0);
        }
        NetworkObject host = new NetworkObject(image, ipAddress, gatewayX, gatewayY + line);
        objectMap.put(ipAddress, host);

        int count = localNetworkHostsList.size() - 1;

        if (count > 0) {
            int i = 0;
            for (String localNetworkHost : localNetworkHostsList) {
                NetworkObject object = objectMap.get(localNetworkHost);

                double rad = (1.0d + ((double) i / count)) * Math.PI;
                int x = (int) (Math.cos(rad) * line + gatewayX);
                int y = (int) (Math.sin(rad) * -1 * line + gatewayY);

                object.setX(x);
                object.setY(y);

                i++;
            }
        }

        createNetworkLine();
    }

    private void createNetworkLine() {
        lineList.clear();
        if (objectMap.get(INTERNET) != null) {
            lineList.add(new NetworkLine(objectMap.get(gatewayIpAddress), objectMap.get(INTERNET)));
        }
        if (gatewayIpAddress != null && objectMap.get(gatewayIpAddress) != null) {
            for (String localNetworkHost : localNetworkHostsList) {
                lineList.add(new NetworkLine(objectMap.get(gatewayIpAddress), objectMap.get(localNetworkHost)));
            }
        }
    }

    private boolean isSameHeader(PacketHeader item) {
        if (lastPacket != null) {
            return lastPacket.getProtocol() == item.getProtocol()
                    && lastPacket.getSrcIpAddress().equals(item.getSrcIpAddress())
                    && lastPacket.getDstIpAddress().equals(item.getDstIpAddress())
                    && lastPacket.getSrcPort() == item.getSrcPort()
                    && lastPacket.getDstPort() == item.getDstPort();
        } else {
            return false;
        }
    }
}
