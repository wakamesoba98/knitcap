Knitcap
=====

TCP/IP Packet Visualizer

![Knitcap](https://raw.githubusercontent.com/wakamesoba98/knitcap/master/screenshot.png)

# Setup

## Requirements

* Maven (`mvn` command)
* Java 8 Update 40 (8u40)
* libcap
* libpcap
* xorg-xrandr

## Install dependencies

in Arch Linux:

```bash
$ yaourt -S maven jdk libcap libpcap xorg-xrandr
```

## Set capability

For non-root user to run Knitcap, set capability to your Java binary.

```bash
$ sudo setcap cap_net_raw,cap_net_admin=eip $(readlink $(which java))
```

# Run

```bash
$ mvn nativedependencies:copy
$ mvn clean compile assembly:single
$ mvn exec:exec
```

# Run Standalone JAR

```bash
$ cd target
$ java -Djava.library.path=natives -jar Knitcap-0.1-SNAPSHOT-jar-with-dependencies.jar
```

# License

The MIT License (MIT)

## Libraries

[Pcap4J](https://github.com/kaitoy/pcap4j) is distributed under the MIT license.

[Slick2D](http://slick.ninjacave.com/) is distributed under the libral BSD License.

## Icons

[Google Material design icons](https://github.com/google/material-design-icons/) is licensed under the [CC-BY 4.0](https://creativecommons.org/licenses/by/4.0/).
