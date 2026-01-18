import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LanWorldSpammer {

    private static final String MULTICAST_ADDRESS = "224.0.2.60";
    private static final int MULTICAST_PORT = 4445;

    public static void main(String[] args) {
        try {
            // 1. Загрузка конфига
            Yaml yaml = new Yaml();
            InputStream inputStream = LanWorldSpammer.class.getClassLoader().getResourceAsStream("config.yml");
            Map<String, Object> config = yaml.load(inputStream);

            Map<String, Object> settings = (Map<String, Object>) config.get("settings");
            Map<String, Object> data = (Map<String, Object>) config.get("data");

            int amount = (int) settings.get("amount");
            int interval = (int) settings.get("interval_ms");
            int basePort = (int) settings.get("base_port");
            List<String> prefixes = (List<String>) data.get("prefixes");
            List<String> motds = (List<String>) data.get("motds");

            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            Random random = new Random();

            System.out.println(">>> Запуск спама локальными мирами (" + amount + " шт.)...");

            // 2. Бесконечный цикл рассылки пакетов
            while (true) {
                for (int i = 0; i < amount; i++) {
                    // Выбор рандомных данных и форматирование
                    String prefix = prefixes.get(random.nextInt(prefixes.size()));
                    String motdTemplate = motds.get(random.nextInt(motds.size()));
                    String rawMotd = String.format(prefix + " " + motdTemplate, i + 1);
                    
                    // Перевод цветовых кодов & -> §
                    String coloredMotd = rawMotd.replace('&', '\u00A7');
                    
                    // Формат пакета Minecraft: [MOTD]текст[/MOTD][AD]порт[/AD]
                    String message = "[MOTD]" + coloredMotd + "[/MOTD][AD]" + (basePort + i) + "[/AD]";
                    byte[] buffer = message.getBytes("UTF-8");

                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
                    socket.send(packet);
                }

                Thread.sleep(interval);
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
