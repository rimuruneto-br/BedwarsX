package dev.bedwarsx.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ChatUtil {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> lines) {
        return lines.stream().map(ChatUtil::color).collect(Collectors.toList());
    }

    public static void send(Player player, String message) {
        player.sendMessage(color(message));
    }

    public static void sendAll(Collection<? extends Player> players, String message) {
        String colored = color(message);
        for (Player p : players) {
            p.sendMessage(colored);
        }
    }

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(color(message));
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        // NMS Title packet for 1.8.9
        try {
            Object chatTitle = getNMSClass("IChatBaseComponent")
                    .getDeclaredClasses()[0]
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + title.replace("\"", "\\\"") + "\"}");

            Object chatSubtitle = getNMSClass("IChatBaseComponent")
                    .getDeclaredClasses()[0]
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + subtitle.replace("\"", "\\\"") + "\"}");

            Class<?> titleAction = getNMSClass("PacketPlayOutTitle")
                    .getDeclaredClasses()[0];

            Object titlePacket = getNMSClass("PacketPlayOutTitle")
                    .getConstructor(titleAction, getNMSClass("IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(titleAction.getField("TITLE").get(null), chatTitle, fadeIn, stay, fadeOut);

            Object subtitlePacket = getNMSClass("PacketPlayOutTitle")
                    .getConstructor(titleAction, getNMSClass("IChatBaseComponent"), int.class, int.class, int.class)
                    .newInstance(titleAction.getField("SUBTITLE").get(null), chatSubtitle, fadeIn, stay, fadeOut);

            sendPacket(player, titlePacket);
            sendPacket(player, subtitlePacket);
        } catch (Exception e) {
            // Fallback: send as chat message
            player.sendMessage(color(title));
            if (!subtitle.isEmpty()) {
                player.sendMessage(color(subtitle));
            }
        }
    }

    public static void sendActionBar(Player player, String message) {
        try {
            Object chatComponent = getNMSClass("IChatBaseComponent")
                    .getDeclaredClasses()[0]
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + color(message).replace("\"", "\\\"") + "\"}");

            Object packet = getNMSClass("PacketPlayOutChat")
                    .getConstructor(getNMSClass("IChatBaseComponent"), byte.class)
                    .newInstance(chatComponent, (byte) 2);

            sendPacket(player, packet);
        } catch (Exception e) {
            // Silently fail - action bar not supported
        }
    }

    public static void sendPacket(Player player, Object packet) throws Exception {
        Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
        connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
    }

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    public static Class<?> getCBClass(String path) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("org.bukkit.craftbukkit." + version + "." + path);
    }

    public static String stripColor(String text) {
        return ChatColor.stripColor(color(text));
    }

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) sb.append(str);
        return sb.toString();
    }

    public static String formatTime(int seconds) {
        if (seconds >= 60) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }
}
