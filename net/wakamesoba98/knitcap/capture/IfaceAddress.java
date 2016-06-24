package net.wakamesoba98.knitcap.capture;

import org.pcap4j.core.PcapAddress;

import java.util.List;

public class IfaceAddress {
    private String addrV4 = "";
    private String addrV6 = "";

    public IfaceAddress(List<PcapAddress> addressList) {
        for (PcapAddress pcapAddress : addressList) {
            String address = pcapAddress.getAddress().getHostAddress();
            if (address.contains(":")) {
                addrV6 = address;
            } else {
                addrV4 = address;
            }
        }
    }

    public String getAddrV4() {
        return addrV4;
    }

    public String getAddrV6() {
        return addrV6;
    }
}
