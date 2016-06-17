package net.wakamesoba98.knitcap;


import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;

public class Main {
    public static void main(String[] args) {
        Capture capture = new Capture();
        try {
            capture.capture(20, 10, 65536);
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }
}
