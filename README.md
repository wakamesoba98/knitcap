Knitcap
=====

TCP/IP Packet Visualizer

![Knitcap](https://raw.githubusercontent.com/wakamesoba98/knitcap/master/screenshot.png)

# Setup

## Required library

* Maven (`mvn` command)
* Java 8 Update 40 (8u40)
* libcap
* libpcap
* xorg-xrandr

## Install dependencies

in Arch Linux:

```
$ yaourt -S maven jdk libcap libpcap xorg-xrandr
```

## Set capability

For non-root user to run Knitcap, set capability to your Java binary.

```
$ sudo setcap cap_net_raw,cap_net_admin=eip $(readlink $(which java))
```

# Run

```
mvn nativedependencies:copy
mvn clean compile assembly:single
mvn exec:exec
```

# Run Standalone JAR

```
cd target
java -Djava.library.path=natives -jar Knitcap-0.1-SNAPSHOT-jar-with-dependencies.jar
```

# License

The MIT License (MIT)
