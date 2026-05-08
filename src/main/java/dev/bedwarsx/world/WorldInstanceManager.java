package dev.bedwarsx.world;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.main.BedWarsX;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldInstanceManager {
    private static final String TEMPLATE_PREFIX = "bwx_map_";
    private static final String MATCH_PREFIX = "bwx_match_";

    private final BedWarsX plugin;
    private final File templateFolder;

    public WorldInstanceManager(BedWarsX plugin) {
        this.plugin = plugin;
        this.templateFolder = new File(plugin.getDataFolder(), "maps");
    }

    public void initialize() {
        if (!templateFolder.exists()) templateFolder.mkdirs();
        cleanupOrphanMatchWorlds();
    }

    public List<String> listTemplates() {
        List<String> names = new ArrayList<>();
        File[] files = templateFolder.listFiles();
        if (files == null) return names;
        for (File file : files) {
            if (file.isDirectory()) names.add(file.getName().toLowerCase(Locale.ROOT));
        }
        java.util.Collections.sort(names);
        return names;
    }

    public boolean hasTemplate(String name) {
        String clean = cleanName(name);
        return !clean.isEmpty() && getTemplateFolder(clean).isDirectory();
    }

    public String normalizeTemplateName(String name) {
        return cleanName(name);
    }

    public String importWorld(String worldName) throws IOException {
        String clean = cleanName(worldName);
        if (clean.isEmpty()) throw new IOException("Invalid world name.");
        File source = new File(Bukkit.getWorldContainer(), worldName);
        if (!source.isDirectory()) throw new IOException("World folder not found: " + worldName);
        File target = getTemplateFolder(clean);
        if (target.exists()) throw new IOException("Template already exists: " + clean);
        copyWorld(source.toPath(), target.toPath());
        return clean;
    }

    public World createMatchWorld(Arena arena) throws IOException {
        if (arena == null || arena.getMapName() == null || arena.getMapName().trim().isEmpty()) return null;
        String map = cleanName(arena.getMapName());
        if (!hasTemplate(map)) throw new IOException("Map template not found: " + map);

        String worldName = MATCH_PREFIX + cleanName(arena.getId()) + "_" + System.currentTimeMillis();
        File target = new File(Bukkit.getWorldContainer(), worldName);
        copyWorld(getTemplateFolder(map).toPath(), target.toPath());
        World world = Bukkit.createWorld(new WorldCreator(worldName));
        if (world == null) throw new IOException("Could not load match world: " + worldName);
        world.setAutoSave(false);
        arena.setMatchWorldName(worldName);
        arena.setDynamicInstance(true);
        return world;
    }

    public void unloadMatchWorld(Arena arena) {
        if (arena == null || arena.getMatchWorldName() == null) return;
        String worldName = arena.getMatchWorldName();
        if (!worldName.startsWith(MATCH_PREFIX)) {
            arena.setMatchWorldName(null);
            return;
        }
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            World fallback = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            for (Player player : new ArrayList<>(world.getPlayers())) {
                if (fallback != null) player.teleport(fallback.getSpawnLocation());
            }
            Bukkit.unloadWorld(world, false);
        }
        deleteManagedWorld(new File(Bukkit.getWorldContainer(), worldName));
        arena.setMatchWorldName(null);
        arena.setDynamicInstance(false);
    }

    public void cleanupOrphanMatchWorlds() {
        File container = Bukkit.getWorldContainer();
        File[] files = container.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory() && file.getName().startsWith(MATCH_PREFIX)) {
                World world = Bukkit.getWorld(file.getName());
                if (world != null) Bukkit.unloadWorld(world, false);
                deleteManagedWorld(file);
            }
        }
    }

    private File getTemplateFolder(String clean) {
        return new File(templateFolder, clean);
    }

    private String cleanName(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
    }

    private void copyWorld(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relative = source.relativize(dir);
                Files.createDirectories(target.resolve(relative));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                if (name.equalsIgnoreCase("uid.dat") || name.equalsIgnoreCase("session.lock")) {
                    return FileVisitResult.CONTINUE;
                }
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteManagedWorld(File file) {
        try {
            Path container = Bukkit.getWorldContainer().getCanonicalFile().toPath();
            Path target = file.getCanonicalFile().toPath();
            if (!target.startsWith(container) || !file.getName().startsWith(MATCH_PREFIX)) return;
            deleteRecursively(target);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not delete match world " + file.getName() + ": " + e.getMessage());
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
