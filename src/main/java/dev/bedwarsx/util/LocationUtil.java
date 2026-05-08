package dev.bedwarsx.util;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class LocationUtil {

    public static String serialize(Location loc) {
        if (loc == null) return "null";
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public static Location deserialize(String data) {
        if (data == null || data.equals("null")) return null;
        String[] parts = data.split(",");
        if (parts.length < 4) return null;
        try {
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void saveToConfig(ConfigurationSection section, String key, Location loc) {
        if (loc == null) return;
        section.set(key + ".world", loc.getWorld().getName());
        section.set(key + ".x", loc.getX());
        section.set(key + ".y", loc.getY());
        section.set(key + ".z", loc.getZ());
        section.set(key + ".yaw", (double) loc.getYaw());
        section.set(key + ".pitch", (double) loc.getPitch());
    }

    public static Location loadFromConfig(ConfigurationSection section, String key) {
        if (section == null || !section.contains(key + ".world")) return null;
        String worldName = section.getString(key + ".world");
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;
        double x = section.getDouble(key + ".x");
        double y = section.getDouble(key + ".y");
        double z = section.getDouble(key + ".z");
        float yaw = (float) section.getDouble(key + ".yaw");
        float pitch = (float) section.getDouble(key + ".pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static boolean isSameBlock(Location a, Location b) {
        if (a == null || b == null) return false;
        if (!a.getWorld().equals(b.getWorld())) return false;
        return a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ();
    }

    public static double distance(Location a, Location b) {
        if (a == null || b == null) return Double.MAX_VALUE;
        if (!a.getWorld().equals(b.getWorld())) return Double.MAX_VALUE;
        return a.distance(b);
    }
}
