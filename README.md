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



## 📝 Configuration

You can modify the following variables directly in the source code to change the broadcast behavior:

* `motd`: Change the text displayed in the server list.
* `serverPort`: Change the port the client will attempt to connect to.
* `i <= 5`: Adjust the number of virtual worlds to broadcast.

## ⚠️ Educational Disclaimer

This project is for **educational and testing purposes only**. It is intended to help developers understand network socket programming and the Minecraft protocol. Please use it responsibly within your own private network environments.
