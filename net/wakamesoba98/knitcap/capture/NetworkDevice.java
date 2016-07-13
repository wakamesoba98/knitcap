package net.wakamesoba98.knitcap.capture;

import org.pcap4j.core.PcapAddress;

import java.util.List;

public class NetworkDevice {
    private String ipV4Address = "", ipV6Address = "";
    private String ipV4SubnetMask = "", ipV6SubnetMask = "";
    private String hardwareAddress;

    public NetworkDevice(String hardwareAddress, List<PcapAddress> addressList) {
        this.hardwareAddress = hardwareAddress;
        for (PcapAddress pcapAddress : addressList) {
            String address = pcapAddress.getAddress().getHostAddress();
            String mask = pcapAddress.getNetmask().getHostAddress();
            if (address.contains(":")) {
                this.ipV6Address = address;
                this.ipV6SubnetMask = mask;
            } else {
                this.ipV4Address = address;
                this.ipV4SubnetMask = mask;
            }
        }
    }

    public NetworkDevice(String hardwareAddress, String address, String mask) {
        this.hardwareAddress = hardwareAddress;
        if (address.contains(":")) {
            this.ipV6Address = address;
            this.ipV6SubnetMask = mask;
        } else {
            this.ipV4Address = address;
            this.ipV4SubnetMask = mask;
        }
    }

    public String getIpV4Address() {
        return ipV4Address;
    }

    public String getIpV6Address() {
        return ipV6Address;
    }

    public String getHardwareAddress() {
        return hardwareAddress;
    }

    public String getIpV4SubnetMask() {
        return ipV4SubnetMask;
    }

    public String getIpV6SubnetMask() {
        return ipV6SubnetMask;
    }
}
