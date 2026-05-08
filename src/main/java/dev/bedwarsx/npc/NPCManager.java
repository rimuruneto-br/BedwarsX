package dev.bedwarsx.npc;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class NPCManager {

    private final BedWarsX plugin;
    private final List<BedWarsNPC> npcs = new ArrayList<>();
    private final String version;

    public NPCManager(BedWarsX plugin) {
        this.plugin = plugin;
        this.version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public void spawnLobbyNPCs() {
        FileConfiguration cfg = plugin.getConfigManager().getConfig("npcs");
        ConfigurationSection section = cfg.getConfigurationSection("npcs");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            ConfigurationSection npcSection = section.getConfigurationSection(id);
            if (npcSection == null) continue;

            String name = npcSection.getString("name", "NPC");
            String skin = npcSection.getString("skin", "");
            String signature = npcSection.getString("signature", "");
            String type = npcSection.getString("type", "lobby");
            double x = npcSection.getDouble("x", 0);
            double y = npcSection.getDouble("y", 64);
            double z = npcSection.getDouble("z", 0);
            float yaw = (float) npcSection.getDouble("yaw", 0);
            String worldName = npcSection.getString("world", "world");

            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            Location loc = new Location(world, x, y, z, yaw, 0);
            BedWarsNPC npc = createNPC(id, name, loc, skin, signature, type);
            if (npc != null) {
                npcs.add(npc);
                spawnNPCForAll(npc);
            }
        }
    }

    public BedWarsNPC createNPC(String id, String name, Location loc, String skinValue, String skinSignature, String type) {
        try {
            // Create GameProfile with skin
            UUID uuid = UUID.randomUUID();
            Object gameProfile = createGameProfile(uuid, name.length() > 16 ? name.substring(0, 16) : name);

            // Set skin property
            if (skinValue != null && !skinValue.isEmpty() && skinSignature != null && !skinSignature.isEmpty()) {
                setSkinProperty(gameProfile, skinValue, skinSignature);
            }

            // Create NMS EntityPlayer
            Object nmsServer = getMinecraftServer();
            Object worldServer = getWorldServer(loc.getWorld());
            Object interactManager = createPlayerInteractManager(worldServer);

            Constructor<?> entityPlayerCtor = getNMSClass("EntityPlayer").getConstructor(
                    getNMSClass("MinecraftServer"),
                    getNMSClass("WorldServer"),
                    getGameProfileClass(),
                    getNMSClass("PlayerInteractManager")
            );

            Object nmsPlayer = entityPlayerCtor.newInstance(nmsServer, worldServer, gameProfile, interactManager);

            // Set location
            Method setLocation = getNMSClass("Entity").getDeclaredMethod("setLocation",
                    double.class, double.class, double.class, float.class, float.class);
            setLocation.setAccessible(true);
            setLocation.invoke(nmsPlayer, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            BedWarsNPC npc = new BedWarsNPC(id, name, loc, nmsPlayer, uuid, type);
            return npc;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create NPC: " + e.getMessage());
            return null;
        }
    }

    public void spawnNPCForAll(BedWarsNPC npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            spawnNPCForPlayer(npc, player);
        }
    }

    public void spawnNPCForPlayer(BedWarsNPC npc, Player player) {
        try {
            Object nmsPlayer = npc.getNmsPlayer();

            // Add to player list (required for skin display)
            Object packetPlayerInfo = createPlayerInfoPacket(nmsPlayer, 0, 0); // ADD_PLAYER, LISTED
            sendPacket(player, packetPlayerInfo);

            // Spawn entity packet
            Object packetSpawn = createNamedEntitySpawnPacket(nmsPlayer);
            sendPacket(player, packetSpawn);

            // Metadata packet (show all skin layers)
            Object packetMetadata = createEntityMetadataPacket(nmsPlayer);
            sendPacket(player, packetMetadata);

            // Remove from tab list after a tick
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    Object removePacket = createPlayerInfoPacket(nmsPlayer, 4, 0); // REMOVE_PLAYER
                    sendPacket(player, removePacket);
                } catch (Exception ignored) {}
            }, 60L);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn NPC for player: " + e.getMessage());
        }
    }

    public void despawnNPCForAll(BedWarsNPC npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            despawnNPCForPlayer(npc, player);
        }
    }

    public void despawnNPCForPlayer(BedWarsNPC npc, Player player) {
        try {
            Object entityId = getNMSClass("Entity").getMethod("getId").invoke(npc.getNmsPlayer());
            int id = (int) entityId;

            Object destroyPacket = getNMSClass("PacketPlayOutEntityDestroy")
                    .getConstructor(int[].class)
                    .newInstance((Object) new int[]{id});
            sendPacket(player, destroyPacket);
        } catch (Exception e) {
            // Silently fail
        }
    }

    public void removeAllNPCs() {
        for (BedWarsNPC npc : npcs) {
            despawnNPCForAll(npc);
        }
        npcs.clear();
    }

    public void saveNPC(BedWarsNPC npc) {
        FileConfiguration cfg = plugin.getConfigManager().getConfig("npcs");
        String path = "npcs." + npc.getId();
        cfg.set(path + ".name", npc.getName());
        cfg.set(path + ".world", npc.getLocation().getWorld().getName());
        cfg.set(path + ".x", npc.getLocation().getX());
        cfg.set(path + ".y", npc.getLocation().getY());
        cfg.set(path + ".z", npc.getLocation().getZ());
        cfg.set(path + ".yaw", (double) npc.getLocation().getYaw());
        cfg.set(path + ".type", npc.getType());
        if (npc.getSkinValue() != null) cfg.set(path + ".skin", npc.getSkinValue());
        if (npc.getSkinSignature() != null) cfg.set(path + ".signature", npc.getSkinSignature());
        plugin.getConfigManager().saveConfig("npcs");
    }

    public void deleteNPC(String id) {
        BedWarsNPC toRemove = null;
        for (BedWarsNPC npc : npcs) {
            if (npc.getId().equals(id)) {
                toRemove = npc;
                break;
            }
        }
        if (toRemove != null) {
            despawnNPCForAll(toRemove);
            npcs.remove(toRemove);

            FileConfiguration cfg = plugin.getConfigManager().getConfig("npcs");
            cfg.set("npcs." + id, null);
            plugin.getConfigManager().saveConfig("npcs");
        }
    }

    public List<BedWarsNPC> getNpcs() {
        return npcs;
    }

    public BedWarsNPC getNPCById(String id) {
        for (BedWarsNPC npc : npcs) {
            if (npc.getId().equals(id)) return npc;
        }
        return null;
    }

    // ─── NMS Helpers ─────────────────────────────────────────────────────────────

    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version + "." + name);
    }

    private Class<?> getCBClass(String path) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version + "." + path);
    }

    private Class<?> getGameProfileClass() throws ClassNotFoundException {
        return Class.forName("com.mojang.authlib.GameProfile");
    }

    private Object createGameProfile(UUID uuid, String name) throws Exception {
        return getGameProfileClass().getConstructor(UUID.class, String.class).newInstance(uuid, name);
    }

    private void setSkinProperty(Object gameProfile, String value, String signature) throws Exception {
        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");
        Object property = propertyClass.getConstructor(String.class, String.class, String.class)
                .newInstance("textures", value, signature);

        Object propertyMap = getGameProfileClass().getMethod("getProperties").invoke(gameProfile);
        propertyMap.getClass().getMethod("put", Object.class, Object.class).invoke(propertyMap, "textures", property);
    }

    private Object getMinecraftServer() throws Exception {
        return getCBClass("CraftServer").getMethod("getServer").invoke(Bukkit.getServer());
    }

    private Object getWorldServer(org.bukkit.World world) throws Exception {
        return getCBClass("CraftWorld").getMethod("getHandle").invoke(world);
    }

    private Object createPlayerInteractManager(Object worldServer) throws Exception {
        return getNMSClass("PlayerInteractManager").getConstructor(getNMSClass("World")).newInstance(worldServer);
    }

    private Object createPlayerInfoPacket(Object nmsPlayer, int action, int ping) throws Exception {
        Class<?> packetClass = getNMSClass("PacketPlayOutPlayerInfo");
        Class<?> actionClass = null;
        for (Class<?> inner : packetClass.getDeclaredClasses()) {
            actionClass = inner;
        }

        Object[] actions = actionClass.getEnumConstants();
        Object packet = packetClass.newInstance();

        Field actionField = packetClass.getDeclaredField("a");
        actionField.setAccessible(true);
        actionField.set(packet, actions[action]);

        // Add player data
        Field dataField = packetClass.getDeclaredField("b");
        dataField.setAccessible(true);
        List<Object> dataList = new ArrayList<>();

        Class<?> dataClass = null;
        for (Class<?> inner : packetClass.getDeclaredClasses()) {
            if (inner != actionClass) {
                dataClass = inner;
                break;
            }
        }

        if (dataClass != null) {
            Constructor<?> dataCtor = dataClass.getDeclaredConstructors()[0];
            dataCtor.setAccessible(true);
            // (PacketPlayOutPlayerInfo, EntityPlayer)
            Object data = dataCtor.newInstance(packet, nmsPlayer);
            dataList.add(data);
        }

        dataField.set(packet, dataList);
        return packet;
    }

    private Object createNamedEntitySpawnPacket(Object nmsPlayer) throws Exception {
        return getNMSClass("PacketPlayOutNamedEntitySpawn")
                .getConstructor(getNMSClass("EntityHuman"))
                .newInstance(nmsPlayer);
    }

    private Object createEntityMetadataPacket(Object nmsPlayer) throws Exception {
        Object dataWatcher = getNMSClass("Entity").getMethod("getDataWatcher").invoke(nmsPlayer);
        int entityId = (int) getNMSClass("Entity").getMethod("getId").invoke(nmsPlayer);

        // Set all skin layers bit (0x7F)
        Method watcherMethod = getNMSClass("DataWatcher").getMethod("watch", int.class, Object.class);
        watcherMethod.setAccessible(true);
        try {
            watcherMethod.invoke(dataWatcher, 10, (byte) 0x7F);
        } catch (Exception ignored) {}

        return getNMSClass("PacketPlayOutEntityMetadata").getConstructor(int.class, getNMSClass("DataWatcher"), boolean.class)
                .newInstance(entityId, dataWatcher, true);
    }

    private void sendPacket(Player player, Object packet) throws Exception {
        Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
        Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
        connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
    }
}
