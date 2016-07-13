package net.wakamesoba98.knitcap.view.canvas;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;

import java.util.*;

public class NetworkMap {

    private static final int QUEUE_MAX = 20;
    private static final double FRAME_RATE = 60;

    private GraphicsContext context;
    private AnimationTimer animationTimer;
    private Queue<PacketCircle> packetQueue;
    private Map<String, NetworkObject> objectMap;
    private List<NetworkLine> lineList;
    private String gatewayIpAddress, localhostIpAddress;
    private long beforeNanoTime;
    private boolean isAnimationStarted = false;
    private int hostsCount;

    public NetworkMap(Canvas canvas) {
        packetQueue = new LinkedList<>();
        objectMap = new HashMap<>();
        lineList  = new ArrayList<>();
        context = canvas.getGraphicsContext2D();
        isAnimationStarted = false;

        animationTimer = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // 30fps
                if (currentNanoTime-beforeNanoTime >= ((1/FRAME_RATE)*1000*1000*1000)) {
                    context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                    for (NetworkLine line : lineList) {
                        context.setStroke(Color.BLUE);
                        context.strokeLine(line.getFromX(), line.getFromY(), line.getToX(), line.getToY());
                    }

                    Iterator<PacketCircle> iterator = packetQueue.iterator();
                    while (iterator.hasNext()) {
                        PacketCircle circle = iterator.next();
                        if (circle.isFinished()) {
                            iterator.remove();
                        } else {
                            circle.draw(context);
                        }
                    }
                    beforeNanoTime = currentNanoTime;

                    for (NetworkObject object : objectMap.values()) {
                        context.drawImage(object.getImage(), object.getX(), object.getY(), object.getWidth(), object.getHeight());
                        context.setFill(Color.BLACK);
                        context.fillText(object.getName(), object.getX()+object.getWidth()-20, object.getY());
                    }
                }
            }
        };
    }

    public void start() {
        if (isAnimationStarted) {
            stop();
        }
        animationTimer.start();
        isAnimationStarted = true;
    }

    public void stop() {
        animationTimer.stop();
        isAnimationStarted = false;
    }

    public void showLocalhost(NetworkDevice device) {
        // TODO show IPv6 address of localhost
        //if (device.getIpV6Address() != null && !device.getIpV6Address().equals("")) {
        //    localhostIpAddress = device.getIpV6Address();
        //} else {
            localhostIpAddress = device.getIpV4Address();
        //}
        objectMap.put(localhostIpAddress, new NetworkObject(new Image(getClass().getResourceAsStream("../../png/computer.png")), localhostIpAddress, 20, 420));
        hostsCount = 1;
    }

    public void showGateway(String defaultGateway) {
        if (defaultGateway != null && !defaultGateway.equals("")) {
            gatewayIpAddress = defaultGateway;
            objectMap.put(defaultGateway, new NetworkObject(new Image(getClass().getResourceAsStream("../../png/router.png")), defaultGateway, 20, 220));
            lineList.add(new NetworkLine(objectMap.get(localhostIpAddress), objectMap.get(defaultGateway)));
        }
    }

    public void showInternet() {
        objectMap.put("INTERNET", new NetworkObject(new Image(getClass().getResourceAsStream("../../png/internet.png")), "Internet", 20, 20));
        lineList.add(new NetworkLine(objectMap.get(gatewayIpAddress), objectMap.get("INTERNET")));
    }

    private void detectHostsInNetwork(String ipAddress) {
        hostsCount++;
        NetworkObject host = new NetworkObject(new Image(getClass().getResourceAsStream("../../png/computer.png")), ipAddress, (20 + 120*(hostsCount-1)), 420);
        NetworkLine line = new NetworkLine(objectMap.get(gatewayIpAddress), host);
        objectMap.put(ipAddress, host);
        lineList.add(line);
    }

    public void addPacket(PacketHeader header) {
        if (packetQueue.size() < QUEUE_MAX) {
            NetworkObject src, gateway, dst;
            src = objectMap.get(header.getSrcIpAddress());
            gateway = objectMap.get(gatewayIpAddress);
            dst = objectMap.get(header.getDstIpAddress());
            if (src == null || dst == null) {
                switch (header.getPacketType()) {
                    case RECEIVE:
                        if (header.isSameSubnet()) {
                            // add new host to objectMap
                            detectHostsInNetwork(header.getSrcIpAddress());
                        } else {
                            src = objectMap.get("INTERNET");
                        }
                        break;

                    case SEND:
                        if (header.isSameSubnet()) {
                            dst = objectMap.get(gatewayIpAddress);
                        } else {
                            dst = objectMap.get("INTERNET");
                        }
                        break;

                    default:
                        if (header.isSameSubnet()) {
                            // add new host to objectMap
                            if (src == null) {
                                detectHostsInNetwork(header.getSrcIpAddress());
                            } else {
                                detectHostsInNetwork(header.getDstIpAddress());
                            }
                        }
                        break;

                }
            }
            if (src != null && dst != null) {
                PacketCircle circle = new PacketCircle(src, gateway, dst, header.getProtocol());
                packetQueue.offer(circle);
            }
        }
    }
}
