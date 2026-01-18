import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.net.*;
import java.util.*;

public class LanSpammer {
    private static final Random RANDOM = new Random();
    private static List<String> kickMessages;

    public static void main(String[] args) throws Exception {
        // Загрузка конфига
        Yaml yaml = new Yaml();
        File configFile = new File("config.yml");
        if (!configFile.exists()) {
            System.out.println("Ошибка: config.yml не найден!");
            return;
        }

        InputStream is = new FileInputStream(configFile);
        Map<String, Object> config = yaml.load(is);

        Map<String, Object> conn = (Map<String, Object>) config.get("connection");
        Map<String, Object> settings = (Map<String, Object>) config.get("settings");
        Map<String, Object> data = (Map<String, Object>) config.get("data");

        String sourceIp = (String) conn.get("source_ip");
        String groupIp = (String) conn.get("multicast_group");
        int mPort = (int) conn.get("multicast_port");
        
        int amount = (int) settings.get("amount");
        int basePort = (int) settings.get("base_port");
        int interval = (int) settings.get("interval_ms");

        kickMessages = (List<String>) data.get("kick_messages");
        List<String> prefixes = (List<String>) data.get("prefixes");
        List<String> motds = (List<String>) data.get("motds");

        System.out.println("[*] Привязка к интерфейсу: " + sourceIp);
        System.out.println("[*] Запуск ловушек на портах: " + basePort + " - " + (basePort + amount - 1));

        // 1. Запуск TCP ловушек
        for (int i = 0; i < amount; i++) {
            int port = basePort + i;
            new Thread(() -> startTcpServer(port)).start();
        }

        // 2. Запуск UDP спамера с выбором интерфейса
        InetAddress group = InetAddress.getByName(groupIp);
        InetAddress localAddr = InetAddress.getByName(sourceIp);

        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(localAddr, 0))) {
            socket.setBroadcast(true);
            
            while (true) {
                for (int i = 0; i < amount; i++) {
                    String rawText = prefixes.get(RANDOM.nextInt(prefixes.size())) + " " + 
                                     motds.get(RANDOM.nextInt(motds.size()));
                    String colored = rawText.replace('&', '§');
                    
                    // Формат: [MOTD]текст[/MOTD][AD]порт[/AD]
                    String payload = "[MOTD]" + colored + "[/MOTD][AD]" + (basePort + i) + "[/AD]";
                    byte[] buf = payload.getBytes("UTF-8");
                    
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, group, mPort);
                    socket.send(packet);
                }
                Thread.sleep(interval);
            }
        }
    }

    private static void startTcpServer(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                try (Socket client = server.accept();
                     DataOutputStream out = new DataOutputStream(client.getOutputStream());
                     DataInputStream in = new DataInputStream(client.getInputStream())) {
                    
                    // Читаем пакет Handshake
                    readVarInt(in); // length
                    readVarInt(in); // packet id
                    readVarInt(in); // protocol
                    readString(in); // address
                    in.readShort(); // port
                    int nextState = readVarInt(in); 

                    if (nextState == 2) { // Login state
                        String msg = kickMessages.get(RANDOM.nextInt(kickMessages.size())).replace('&', '§');
                        sendKick(out, "{\"text\":\"" + msg + "\"}");
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.err.println("Порт " + port + " уже занят.");
        }
    }

    private static void sendKick(DataOutputStream out, String json) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream p = new DataOutputStream(b);
        writeVarInt(p, 0x00); // Packet ID: Disconnect (Login)
        writeString(p, json);
        writeVarInt(out, b.size());
        out.write(b.toByteArray());
    }

    // Методы протокола
    private static void writeVarInt(DataOutputStream out, int v) throws IOException {
        while ((v & -128) != 0) { out.writeByte(v & 127 | 128); v >>>= 7; }
        out.writeByte(v);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int i = 0, j = 0;
        while (true) { byte k = in.readByte(); i |= (k & 127) << j++ * 7; if ((k & 128) != 128) break; }
        return i;
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes("UTF-8"); writeVarInt(out, b.length); out.write(b);
    }

    private static String readString(DataInputStream in) throws IOException {
        int l = readVarInt(in); byte[] b = new byte[l]; in.readFully(b); return new String(b, "UTF-8");
    }
}
