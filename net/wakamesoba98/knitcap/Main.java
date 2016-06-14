package net.wakamesoba98.knitcap;


import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

public class Main {
    public static void main(String[] args) {
        Capture capture = new Capture();
        try {
            capture.capture(5, 10, 65536, "tcp");
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }
}
