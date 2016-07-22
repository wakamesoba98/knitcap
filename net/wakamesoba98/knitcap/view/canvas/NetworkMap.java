package net.wakamesoba98.knitcap.view.canvas;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;

import java.util.*;

public class NetworkMap {

    private static final int QUEUE_MAX = 1000;
    private static final String INTERNET = "INTERNET";
    private static final int INTERNET_X = 250, INTERNET_Y = 20;
    private static final int GATEWAY_X = 250, GATEWAY_Y = 250;
    private static final int LINE_LENGTH = 230;

    private GraphicsContext context;
    private Timeline timeline;
    private Queue<PacketCircle> packetQueue;
    private List<String> localNetworkHostsList;
    private Map<String, NetworkObject> objectMap;
    private List<NetworkLine> lineList;
    private String gatewayIpAddress;

    public void start(Canvas canvas) {
        stop();

        packetQueue = new LinkedList<>();
        objectMap = new HashMap<>();
        localNetworkHostsList = new LinkedList<>();
        lineList = new LinkedList<>();
        context = canvas.getGraphicsContext2D();

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        KeyFrame keyFrame = new KeyFrame(
            Duration.seconds(0.017), // 60 FPS
            actionEvent -> {
                context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                for (NetworkLine line : lineList) {
                    context.setStroke(Color.BLUE);
                    context.strokeLine(line.getFromX(), line.getFromY(), line.getToX(), line.getToY());
                }

                threadSafeQueueControl(false, null);

                context.setFill(Color.BLACK);
                for (NetworkObject object : objectMap.values()) {
                    context.drawImage(object.getImage(), object.getX(), object.getY(), object.getWidth(), object.getHeight());
                }
                for (NetworkObject object : objectMap.values()) {
                    context.fillText(object.getName(), object.getX() + object.getWidth() - 20, object.getY());
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

    public void showLocalhost(NetworkDevice device) {
        // TODO show IPv6 address of localhost
        //if (device.getIpV6Address() != null && !device.getIpV6Address().equals("")) {
        //    localhostIpAddress = device.getIpV6Address();
        //} else {
        detectHostsInNetwork(device.getIpV4Address());
        //}
    }

    public void showGateway(String defaultGateway) {
        if (defaultGateway != null && !defaultGateway.equals("")) {
            gatewayIpAddress = defaultGateway;
            objectMap.put(defaultGateway, new NetworkObject(new Image(getClass().getResource("/res/png/router.png").toExternalForm()), defaultGateway, GATEWAY_X, GATEWAY_Y));
        }
        createNetworkLine();
    }

    public void showInternet() {
        objectMap.put(INTERNET, new NetworkObject(new Image(getClass().getResource("/res/png/internet.png").toExternalForm()), INTERNET, INTERNET_X, INTERNET_Y));
        createNetworkLine();
    }

    private void detectHostsInNetwork(String ipAddress) {
        localNetworkHostsList.add(ipAddress);

        NetworkObject host = new NetworkObject(new Image(getClass().getResource("/res/png/computer.png").toExternalForm()), ipAddress, GATEWAY_X, GATEWAY_Y + LINE_LENGTH);
        objectMap.put(ipAddress, host);

        int count = localNetworkHostsList.size() - 1;

        if (count > 0) {
            int i = 0;
            for (String localNetworkHost : localNetworkHostsList) {
                NetworkObject object = objectMap.get(localNetworkHost);

                double rad = (1.0d + ((double) i / count)) * Math.PI;
                int x = (int) (Math.cos(rad) * LINE_LENGTH + GATEWAY_X);
                int y = (int) (Math.sin(rad) * -1 * LINE_LENGTH + GATEWAY_Y);

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
        if (objectMap.get(gatewayIpAddress) != null) {
            for (String localNetworkHost : localNetworkHostsList) {
                lineList.add(new NetworkLine(objectMap.get(gatewayIpAddress), objectMap.get(localNetworkHost)));
            }
        }
    }

    public void addPacket(PacketHeader header) {
        if (packetQueue.size() < QUEUE_MAX) {
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
                                detectHostsInNetwork(header.getSrcIpAddress());
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
                                    detectHostsInNetwork(header.getSrcIpAddress());
                                } else {
                                    dst = objectMap.get(INTERNET);
                                }
                            } else if (header.isDstAsSameSubnet()) {
                                if (dst == null) {
                                    detectHostsInNetwork(header.getDstIpAddress());
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
                threadSafeQueueControl(true, circle);
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
                threadSafeQueueControl(true, circle);
            }
        }).start();
    }

    private synchronized void threadSafeQueueControl(boolean offer, PacketCircle offerCircle) {
        if (offer) {
            packetQueue.offer(offerCircle);
        } else {
            Iterator<PacketCircle> iterator = packetQueue.iterator();
            while (iterator.hasNext()) {
                PacketCircle circle = iterator.next();
                if (circle.isFinished()) {
                    iterator.remove();
                } else {
                    circle.draw(context);
                }
            }
        }
    }
}
