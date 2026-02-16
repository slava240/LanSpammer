P.S. Прога сделана CompotAdmin
# LanSpammer

A lightweight Java-based tool designed to demonstrate how the Minecraft "Local World" discovery protocol works. This application uses **UDP Multicast** to announce virtual game sessions to all Minecraft clients within the same network segment.

## 🚀 Overview

Minecraft uses a specific protocol to find games on a local network. This program mimics a server's "heartbeat" by broadcasting specially formatted packets to a dedicated multicast address.

### Key Features

* **Protocol Simulation:** Implements the `[MOTD]...[/MOTD][AD]...[/AD]` packet structure.
* **Multi-Session Emulation:** Demonstrates the ability to broadcast multiple virtual world entries simultaneously.
* **Custom Formatting:** Supports Minecraft color codes (`§`) and formatting styles.

## 🛠 How It Works

The program operates on the standard Minecraft discovery parameters:

* **Multicast Address:** `224.0.2.60`
* **Destination Port:** `4445`
* **Protocol:** UDP (User Datagram Protocol)

Every 1.5 seconds, the application sends a packet containing the Server Name (MOTD) and a Port. Any Minecraft client listening on the same network will interpret these packets and display the entries in the **Multiplayer** menu under the "LAN Games" section.

## 💻 Usage

### Prerequisites

* Java Development Kit (JDK) 11 or higher.
* Minecraft Java Edition (for testing).

### Running the Application

1. Download the `LanSpammer.java` file.
2. Open your terminal/command prompt in the file's directory.
3. Execute the program:
```bash
java LanSpammer.java

```



## ⚙️ Configuration & Network Binding

By default, Java may pick the wrong network interface (e.g., a virtual adapter or a secondary Wi-Fi card). To ensure others can see your broadcast, you must bind the application to your active network IP.

### 1. Identify Your IP

Open your terminal and find your local IP address (e.g., `192.168.x.x` or your specific virtual network IP):

* **Windows:** `ipconfig`
* **Linux/macOS:** `ifconfig` or `ip a`

### 2. Update the Source Code

Locate the `yourIp` variable in the code and replace it with your actual address:

```java
// IP
String yourIp = "YOUR_IP_HERE"; 

```

### 3. Adjusting TTL (Time To Live)

The `setTimeToLive(int ttl)` method determines how many "hops" (routers) the packet can pass through.

* `1`: Stays within your immediate local subnet (Default).
* `4+`: Recommended for complex virtual networks to ensure the packet reaches all peers.

## 📝 Color Formatting

You can use Minecraft color symbols (§) to style your signature:

* `§b` - Aqua
* `§l` - **Bold**
* `§k` - Obfuscated (Glitch effect)
* `§6` - Gold

**Example:** `§l§6[SERVER] §bMy Signature`



## 📝 Configuration

You can modify the following variables directly in the source code to change the broadcast behavior:

* `motd`: Change the text displayed in the server list.
* `serverPort`: Change the port the client will attempt to connect to.
* `i <= 5`: Adjust the number of virtual worlds to broadcast.

## Screenshot

<img width="1920" height="1080" alt="{7C226097-A9BF-456B-A67B-D4C381256A1F}" src="https://github.com/user-attachments/assets/240532d5-69f7-44c2-9f1a-4332a71467ee" />


## ⚠️ Educational Disclaimer

This project is for **educational and testing purposes only**. It is intended to help developers understand network socket programming and the Minecraft protocol. Please use it responsibly within your own private network environments.
