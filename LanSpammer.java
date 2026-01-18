import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.net.*;
import java.util.*;

public class LanSpammerPro {
    private static List<String> kickMessages;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        // 1. Загрузка конфига
        Yaml yaml = new Yaml();
        InputStream is = LanSpammerPro.class.getClassLoader().getResourceAsStream("config.yml");
        Map<String, Object> config = yaml.load(is);
        
        Map<String, Object> settings = (Map<String, Object>) config.get("settings");
        Map<String, Object> data = (Map<String, Object>) config.get("data");
        
        int amount = (int) settings.get("amount");
        int basePort = (int) settings.get("base_port");
        kickMessages = (List<String>) data.get("kick_messages");

        // 2. Запуск TCP-"ловушек" для каждого порта
        for (int i = 0; i < amount; i++) {
            int port = basePort + i;
            new Thread(() -> startFakeTcpServer(port)).start();
        }

        // 3. Запуск UDP-спамера (код из предыдущего ответа)
        startUdpSpammer(settings, data);
    }

    private static void startFakeTcpServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket client = serverSocket.accept();
                     DataOutputStream out = new DataOutputStream(client.getOutputStream());
                     DataInputStream in = new DataInputStream(client.getInputStream())) {
                    
                    // Читаем Handshake (пропускаем байты)
                    readVarInt(in); // Packet ID
                    readVarInt(in); // Protocol Version
                    readString(in); // Server Address
                    in.readShort(); // Port
                    int nextState = readVarInt(in); // Next State (1 - status, 2 - login)

                    if (nextState == 2) { // Если игрок пытается зайти
                        // Получаем рандомный текст и красим его
                        String rawText = kickMessages.get(RANDOM.nextInt(kickMessages.size()));
                        String jsonMessage = "{\"text\":\"" + rawText.replace('&', '§') + "\"}";
                        
                        // Отправляем пакет Disconnect (Login)
                        // ID пакета для Disconnect в Login state обычно 0x00
                        sendPacket(out, 0x00, jsonMessage);
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки подключения
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось занять порт " + port);
        }
    }

    // --- Вспомогательные методы для протокола Minecraft ---

    private static void sendPacket(DataOutputStream out, int id, String json) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream packetOut = new DataOutputStream(buffer);
        
        writeVarInt(packetOut, id);
        writeString(packetOut, json);
        
        writeVarInt(out, buffer.size());
        out.write(buffer.toByteArray());
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int i = 0, j = 0;
        while (true) {
            byte k = in.readByte();
            i |= (k & 127) << j++ * 7;
            if (j > 5) throw new RuntimeException("VarInt too big");
            if ((k & 128) != 128) break;
        }
        return i;
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes("UTF-8");
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = readVarInt(in);
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, "UTF-8");
    }
    
    // Здесь должен быть метод startUdpSpammer из предыдущего шага...
}
