package net.wakamesoba98.knitcap.view;

import net.wakamesoba98.knitcap.capture.NetworkDevice;
import net.wakamesoba98.knitcap.capture.packet.PacketHeader;

public interface GuiControllable {
    void openedInterface(NetworkDevice device);
    void showGateway(String address);
    void showInternet();
    void refresh();
    void addItem(PacketHeader item);
}
