Knitcap
=====

TCP/IP Packet Visualizer

# Setup

## Required library

* Java 8 Update 40 (8u40)
* libcap
* libpcap
* xorg-xrandr

## Install dependencies

in Arch Linux:

```
$ yaourt -S libcap libpcap xorg-xrandr jdk
```

## Required Java library

* Pcap4J
    * lib/pcap4j-core-1.6.4.jar
    * lib/pcap4j-packetfactory-static-1.6.4.jar
    * lib/slf4j-api-1.7.21.jar
    * lib/slf4j-nop-1.7.21.jar (to disable Pcap4J logging)
    * lib/jna-4.2.2.jar
* Slick2D
    * lib/slick.jar
    * lib/lwjgl.jar
    * lib/native/libjinput-linux.so
    * lib/native/libjinput-linux64.so
    * lib/native/liblwjgl.so
    * lib/native/liblwjgl64.so
    * lib/native/libopenal.so
    * lib/native/libopenal64.so

## Set capability

For non-root user to run Knitcap, set capability to your Java binary.

```
$ sudo setcap cap_net_raw,cap_net_admin=eip $(readlink $(which java))
```

# Run

Compile JAR, and hit the following command to run Knitcap.

```
java -Djava.library.path=lib/native/ -jar Knitcap.jar
```

# License

The MIT License (MIT)
