package dev.bedwarsx.game;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.arena.ArenaState;
import dev.bedwarsx.generator.ArenaGenerator;
import dev.bedwarsx.generator.GeneratorType;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.mission.MissionEvent;
import dev.bedwarsx.team.BedWarsTeam;
import dev.bedwarsx.team.GameTeam;
import dev.bedwarsx.upgrade.UpgradeType;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import dev.bedwarsx.util.ParticleUtil;
import dev.bedwarsx.util.PlayerUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.*;

public class Game {

    private final BedWarsX plugin;
    private final Arena arena;
    private final Map<UUID, GamePlayer> players = new HashMap<>();
    private final Map<BedWarsTeam, GameTeam> teams = new HashMap<>();
    private final List<UUID> spectators = new ArrayList<>();
    private final List<Location> placedBlocks = new ArrayList<>();

    private GameState state = GameState.WAITING;
    private int countdown;
    private BukkitTask countdownTask;
    private BukkitTask generatorTask;
    private BukkitTask nametaskTask;
    private BukkitTask respawnTask;
    private World matchWorld;

    private final Map<UUID, Integer> respawnCountdowns = new HashMap<>();
    private GameTeam winner;

    // Generator levels
    private int diamondLevel = 1;
    private int emeraldLevel = 1;
    private int generatorTick = 0;

    // Map tracking for restoration
    private final Map<Location, Material> originalBlocks = new HashMap<>();

    public Game(BedWarsX plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.arena.resetMatchState();

        try {
            matchWorld = plugin.getWorldInstanceManager().createMatchWorld(arena);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not create map instance for arena " + arena.getId() + ": " + e.getMessage());
        }

        // Initialize teams
        for (BedWarsTeam teamType : arena.getActiveTeams()) {
            GameTeam team = new GameTeam(teamType);
            team.setSpawn(arena.toMatchLocation(arena.getTeamSpawns().get(teamType)));
            team.setBedLocation(arena.toMatchLocation(arena.getTeamBeds().get(teamType)));
            team.setGeneratorLocation(arena.toMatchLocation(arena.getTeamGenerators().get(teamType)));
            team.setShopLocation(arena.toMatchLocation(arena.getTeamShopLocations().get(teamType)));
            team.setUpgradeShopLocation(arena.toMatchLocation(arena.getTeamUpgradeShopLocations().get(teamType)));
            team.setIndicatorLocation(arena.toMatchLocation(arena.getTeamIndicators().get(teamType)));
            team.setIslandCenter(arena.toMatchLocation(arena.getTeamCenters().get(teamType)));
            team.setIslandRadius(arena.getTeamIslandRadius(teamType));
            teams.put(teamType, team);
        }

        countdown = plugin.getConfig().getInt("countdown.waiting", 30);
    }

    public boolean addPlayer(Player player) {
        if (players.size() >= arena.getEffectiveMaxPlayers()) return false;
        if (state != GameState.WAITING && state != GameState.STARTING) return false;

        plugin.getPlayerSessionManager().snapshot(player);
        GamePlayer gp = new GamePlayer(player);
        players.put(player.getUniqueId(), gp);

        // Teleport to lobby
        if (arena.getLobby() != null) {
            player.teleport(arena.getLobby());
        }

        // Clear inventory and give lobby items
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        PlayerUtil.clearPotionEffects(player);

        // Send join message to all in arena
        broadcastArena(ChatUtil.color("&7[&a+&7] " + getPlayerTeamPrefix(player) + player.getName()));

        // Update scoreboard
        plugin.getScoreboardManager().updatePlayer(player, this);

        // Check if we can start countdown
        if (players.size() >= arena.getMinimumToStart() && state == GameState.WAITING && !arena.isAutoStartPaused()) {
            startCountdown();
        }

        return true;
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        GamePlayer gp = players.get(uuid);
        if (gp == null) return;

        // Remove from team
        if (gp.getTeam() != null) {
            GameTeam team = teams.get(gp.getTeam());
            if (team != null) team.removeMember(uuid);
        }

        players.remove(uuid);
        spectators.remove(uuid);

        broadcastArena(ChatUtil.color("&7[&c-&7] " + player.getName() + " &7left."));

        // Reset player
        if (!plugin.getPlayerSessionManager().restore(player, false)) {
            resetPlayer(player);
        }
        teleportToMainLobby(player);

        // Remove scoreboard
        plugin.getScoreboardManager().removePlayer(player);

        // Check if game should end
        if (state == GameState.IN_GAME) {
            checkWinCondition();
        } else if (state == GameState.STARTING && players.size() < arena.getMinimumToStart()) {
            cancelCountdown();
        }
    }

    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
        if (arena.getSpectatorSpawn() != null) {
            player.teleport(arena.getSpectatorSpawn());
        }
        plugin.getScoreboardManager().updatePlayer(player, this);
        ChatUtil.send(player, "&7You are now &bspectating &7this game.");
    }

    private void startCountdown() {
        state = GameState.STARTING;
        arena.setState(ArenaState.STARTING);
        countdown = plugin.getConfig().getInt("countdown.waiting", 30);

        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (players.size() < arena.getMinimumToStart()) {
                    cancelCountdown();
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    startGame();
                    cancel();
                    return;
                }

                if (countdown <= 10 || countdown % 10 == 0) {
                    broadcastArena(ChatUtil.color("&eGame starting in &6" + countdown + " &esecond" + (countdown == 1 ? "" : "s") + "!"));
                    for (UUID uuid : players.keySet()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.playSound(p.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                        }
                    }
                }

                countdown--;
                updateCountdownScoreboard();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        state = GameState.WAITING;
        arena.setState(ArenaState.WAITING);
        countdown = plugin.getConfig().getInt("countdown.waiting", 30);
        broadcastArena(ChatUtil.color("&cNot enough players! Countdown cancelled."));
        updateCountdownScoreboard();
    }

    public void startGame() {
        state = GameState.IN_GAME;
        arena.setState(ArenaState.IN_GAME);

        // Assign teams
        assignTeams();

        // Spawn players
        for (UUID uuid : players.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            GamePlayer gp = players.get(uuid);

            GameTeam team = teams.get(gp.getTeam());
            if (team == null) continue;

            // Teleport to team spawn
            player.teleport(team.getSpawn());
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            PlayerUtil.clearPotionEffects(player);

            // Give starter items
            giveStarterKit(player, gp.getTeam());

            plugin.getScoreboardManager().updatePlayer(player, this);
        }

        // Color bed blocks
        colorBeds();

        // Start generators
        startGenerators();

        broadcastArena(ChatUtil.color("&a&lGame Started! &7Protect your bed and destroy others!"));

        // Update scoreboard for all
        updateAllScoreboards();
    }

    private void assignTeams() {
        List<UUID> unassigned = new ArrayList<>(players.keySet());
        Collections.shuffle(unassigned);

        List<BedWarsTeam> availableTeams = arena.getActiveTeams();
        int teamIndex = 0;

        for (UUID uuid : unassigned) {
            GamePlayer gp = players.get(uuid);
            if (gp.getTeam() != null) continue;

            BedWarsTeam teamType = availableTeams.get(teamIndex % availableTeams.size());
            GameTeam team = teams.get(teamType);

            // Find team with space
            int attempts = 0;
            while (team.size() >= arena.getEffectiveTeamSize() && attempts < availableTeams.size()) {
                teamIndex++;
                teamType = availableTeams.get(teamIndex % availableTeams.size());
                team = teams.get(teamType);
                attempts++;
            }

            gp.setTeam(teamType);
            arena.getPlayerTeams().put(uuid, teamType);
            team.addMember(uuid);
            teamIndex++;

            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                ChatUtil.send(player, "&7You are on team: " + teamType.getColoredName());
            }
        }
    }

    private void giveStarterKit(Player player, BedWarsTeam team) {
        player.getInventory().clear();

        // Colored armor
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        int[] rgb = dev.bedwarsx.util.ColorUtil.toRGB(team);

        // Boots
        ItemStack boots = ItemUtil.createLeather(Material.LEATHER_BOOTS,
                rgb[0], rgb[1], rgb[2],
                team.getColoredName() + " Boots");
        // Leggings
        ItemStack leggings = ItemUtil.createLeather(Material.LEATHER_LEGGINGS,
                rgb[0], rgb[1], rgb[2],
                team.getColoredName() + " Leggings");
        // Chestplate
        ItemStack chestplate = ItemUtil.createLeather(Material.LEATHER_CHESTPLATE,
                rgb[0], rgb[1], rgb[2],
                team.getColoredName() + " Chestplate");
        // Helmet
        ItemStack helmet = ItemUtil.createLeather(Material.LEATHER_HELMET,
                rgb[0], rgb[1], rgb[2],
                team.getColoredName() + " Helmet");

        inv.setBoots(boots);
        inv.setLeggings(leggings);
        inv.setChestplate(chestplate);
        inv.setHelmet(helmet);

        // Wooden sword
        inv.setItem(0, ItemUtil.create(Material.WOOD_SWORD, 1, "&fWooden Sword"));
    }

    private void colorBeds() {
        for (Map.Entry<BedWarsTeam, GameTeam> entry : teams.entrySet()) {
            BedWarsTeam teamType = entry.getKey();
            Location bedLoc = entry.getValue().getBedLocation();
            if (bedLoc == null) continue;

            // Place colored wool under bed
            // (beds in 1.8 are placed by admin)
        }
    }

    private void startGenerators() {
        generatorTask = new BukkitRunnable() {
            @Override
            public void run() {
                generatorTick++;
                arena.setMatchTime(generatorTick);

                for (GameTeam team : teams.values()) {
                    tickTeamForge(team);
                }

                tickLegacyGenerators();

                int diamondRate = diamondLevel == 1 ? 30 : (diamondLevel == 2 ? 20 : 10);
                if (generatorTick % diamondRate == 0) {
                    tickRichGenerators(GeneratorType.DIAMOND);
                }

                int emeraldRate = emeraldLevel == 1 ? 60 : (emeraldLevel == 2 ? 45 : 30);
                if (generatorTick % emeraldRate == 0) {
                    tickRichGenerators(GeneratorType.EMERALD);
                }

                if (generatorTick == 600) { // 10 minutes
                    diamondLevel = 2;
                    arena.setDiamondLevel(2);
                    broadcastArena(ChatUtil.color("&b&lDiamond generators upgraded to level 2!"));
                } else if (generatorTick == 1200) {
                    diamondLevel = 3;
                    arena.setDiamondLevel(3);
                    broadcastArena(ChatUtil.color("&b&lDiamond generators upgraded to level 3!"));
                }

                if (generatorTick == 900) {
                    emeraldLevel = 2;
                    arena.setEmeraldLevel(2);
                    broadcastArena(ChatUtil.color("&2&lEmerald generators upgraded to level 2!"));
                } else if (generatorTick == 1500) {
                    emeraldLevel = 3;
                    arena.setEmeraldLevel(3);
                    broadcastArena(ChatUtil.color("&2&lEmerald generators upgraded to level 3!"));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void tickTeamForge(GameTeam team) {
        Location loc = team.getGeneratorLocation();
        if (loc == null) return;
        int forgeLevel = team.getUpgradeLevel(UpgradeType.FORGE);
        double multiplier = Math.max(0.1, arena.getForgeMultiplier());
        int ironRate = Math.max(1, (int) Math.round(1.0 / multiplier));
        int goldRate = Math.max(2, (int) Math.round((8 - Math.min(4, forgeLevel)) / multiplier));

        if (generatorTick % ironRate == 0) {
            spawnResource(loc, Material.IRON_INGOT, 1);
        }
        if (generatorTick % goldRate == 0) {
            spawnResource(loc, Material.GOLD_INGOT, 1);
        }
    }

    private void tickLegacyGenerators() {
        for (ArenaGenerator generator : arena.getGenerators()) {
            if (generator.getType() == GeneratorType.IRON || generator.getType() == GeneratorType.GOLD) {
                int rate = generator.getType() == GeneratorType.IRON ? 1 : 8;
                if (generatorTick % rate == 0) {
                    spawnResource(arena.toMatchLocation(generator.getLocation()), generator.getType().getMaterial(), 1);
                }
            }
        }
    }

    private void tickRichGenerators(GeneratorType type) {
        for (ArenaGenerator generator : arena.getGenerators()) {
            if (generator.getType() == type) {
                spawnResource(arena.toMatchLocation(generator.getLocation()), generator.getType().getMaterial(), 1);
            }
        }
    }

    private void spawnResource(Location loc, Material material, int amount) {
        if (loc.getWorld() == null) return;
        int limit = plugin.getConfig().getInt("geradores.limite-itens-chao", 32);
        double radius = plugin.getConfig().getDouble("geradores.raio-limite-itens", 2.0);
        long count = loc.getWorld().getNearbyEntities(loc, radius, radius, radius).stream()
                .filter(e -> e instanceof org.bukkit.entity.Item)
                .filter(e -> ((org.bukkit.entity.Item) e).getItemStack().getType() == material)
                .count();
        if (count >= limit) return;

        loc.getWorld().dropItem(loc, new ItemStack(material, amount));

        // Visual generator indicator
        ParticleUtil.spawnFirework(loc.clone().add(0, 0.5, 0), 3);
    }

    public void handlePlayerDeath(Player victim, Player killer) {
        GamePlayer gvictim = players.get(victim.getUniqueId());
        if (gvictim == null) return;
        plugin.getProgressionManager().recordDeath(victim);
        increment(arena.getMatchDeaths(), victim.getUniqueId());

        GameTeam victimTeam = teams.get(gvictim.getTeam());

        if (victimTeam != null && !victimTeam.isBedAlive()) {
            // Final kill - bed already destroyed
            gvictim.setState(GamePlayerState.ELIMINATED);
            gvictim.addDeath();

            if (killer != null) {
                GamePlayer gkiller = players.get(killer.getUniqueId());
                if (gkiller != null) {
                    gkiller.addFinalKill();
                    increment(arena.getMatchKills(), killer.getUniqueId());
                    increment(arena.getMatchFinalKills(), killer.getUniqueId());
                    plugin.getProgressionManager().recordKill(killer, true);
                    plugin.getMissionManager().record(killer, MissionEvent.FINAL_KILL, 1);
                    ChatUtil.sendTitle(killer, "&c&lFINAL KILL!", "&7+" + 40 + " Iron", 5, 40, 10);
                }
            }

            Map<String, String> replacements = new HashMap<>();
            replacements.put("killer", killer != null ? killer.getName() : "Unknown");
            replacements.put("player", victim.getName());
            broadcastArena(plugin.getConfigManager().getMessage("final-kill", replacements));

            // Spectate
            addSpectator(victim);
            victimTeam.removeMember(victim.getUniqueId());

            checkTeamElimination(victimTeam);
        } else {
            // Normal death - respawn
            gvictim.setState(GamePlayerState.DEAD);
            gvictim.addDeath();

            if (killer != null) {
                GamePlayer gkiller = players.get(killer.getUniqueId());
                if (gkiller != null) {
                    gkiller.addKill();
                    increment(arena.getMatchKills(), killer.getUniqueId());
                    plugin.getProgressionManager().recordKill(killer, false);
                    plugin.getMissionManager().record(killer, MissionEvent.KILL, 1);
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("killer", killer.getName());
                replacements.put("player", victim.getName());
                broadcastArena(plugin.getConfigManager().getMessage("player-killed", replacements));
            }

            // Drop their items
            for (ItemStack item : victim.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    victim.getWorld().dropItemNaturally(victim.getLocation(), item);
                }
            }

            victim.getInventory().clear();
            victim.setGameMode(GameMode.SPECTATOR);

            int respawnTime = plugin.getConfig().getInt("game.respawn-time", 5);
            respawnCountdowns.put(victim.getUniqueId(), respawnTime);

            // Respawn countdown
            startRespawnCountdown(victim, respawnTime);
        }

        updateAllScoreboards();
        arena.clearLastDamage(victim.getUniqueId());
    }

    private void startRespawnCountdown(Player player, int seconds) {
        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (!players.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }

                if (remaining <= 0) {
                    respawnPlayer(player);
                    cancel();
                    return;
                }

                ChatUtil.sendActionBar(player, "&cRespawning in &e" + remaining + " &csecond" + (remaining == 1 ? "" : "s") + "...");
                ChatUtil.sendTitle(player, "&c&lYOU DIED!", "&eRespawning in &6" + remaining + "s", 0, 25, 5);
                player.playSound(player.getLocation(), Sound.NOTE_BASS, 1f, 1f);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void respawnPlayer(Player player) {
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null) return;

        gp.setState(GamePlayerState.ALIVE);
        GameTeam team = teams.get(gp.getTeam());
        if (team == null) return;

        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(team.getSpawn());
        player.setHealth(20.0);
        player.setFoodLevel(20);
        PlayerUtil.clearPotionEffects(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));

        giveStarterKit(player, gp.getTeam());

        ChatUtil.sendTitle(player, "&a&lRESPAWNED!", "", 5, 20, 10);
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);

        plugin.getScoreboardManager().updatePlayer(player, this);
    }

    public void handleBedBreak(Location bedLoc, Player breaker) {
        GameTeam victim = null;
        for (GameTeam team : teams.values()) {
            if (team.getBedLocation() != null) {
                Location tLoc = team.getBedLocation();
                if (Math.abs(tLoc.getBlockX() - bedLoc.getBlockX()) <= 1
                        && tLoc.getBlockY() == bedLoc.getBlockY()
                        && Math.abs(tLoc.getBlockZ() - bedLoc.getBlockZ()) <= 1) {
                    victim = team;
                    break;
                }
            }
        }

        if (victim == null) return;
        if (!victim.isBedAlive()) return;

        // Check not own team
        if (breaker != null) {
            GamePlayer gbreaker = players.get(breaker.getUniqueId());
            if (gbreaker != null && gbreaker.getTeam() == victim.getType()) return;
        }

        victim.setBedAlive(false);

        // Mark all team members as bed destroyed
        for (UUID uuid : victim.getMembers()) {
            GamePlayer gp = players.get(uuid);
            if (gp != null) gp.setBedDestroyed(true);
        }

        if (breaker != null) {
            GamePlayer gbreaker = players.get(breaker.getUniqueId());
            if (gbreaker != null) {
                gbreaker.addBedBroken();
                plugin.getProgressionManager().recordBed(breaker);
                plugin.getMissionManager().record(breaker, MissionEvent.BED, 1);
            }
        }

        // Send messages
        Map<String, String> replacements = new HashMap<>();
        replacements.put("team", victim.getColoredName());
        replacements.put("player", breaker != null ? breaker.getName() : "Unknown");

        broadcastArena(plugin.getConfigManager().getMessage("bed-broken", replacements));

        // Notify team members specially
        for (UUID uuid : victim.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                ChatUtil.sendTitle(p, "&c&lBED DESTROYED!", "&7You will not respawn!", 10, 60, 10);
                p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1f, 1f);
            }
        }

        // Play global sound
        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 0.5f, 1f);
            }
        }

        updateAllScoreboards();
        checkWinCondition();
    }

    private void checkTeamElimination(GameTeam team) {
        boolean allEliminated = true;
        for (UUID uuid : team.getMembers()) {
            GamePlayer gp = players.get(uuid);
            if (gp != null && gp.isAlive()) {
                allEliminated = false;
                break;
            }
        }

        if (allEliminated || team.getMembers().isEmpty()) {
            team.setEliminated(true);
            Map<String, String> rep = new HashMap<>();
            rep.put("team", team.getColoredName());
            broadcastArena(plugin.getConfigManager().getMessage("team-eliminated", rep));
            updateAllScoreboards();
            checkWinCondition();
        }
    }

    private void checkWinCondition() {
        List<GameTeam> activeTeams = new ArrayList<>();
        for (GameTeam team : teams.values()) {
            if (!team.isEliminated() && !team.isEmpty()) {
                // Check if all members are alive or dead but have bed
                boolean hasAliveMember = false;
                for (UUID uuid : team.getMembers()) {
                    GamePlayer gp = players.get(uuid);
                    if (gp != null && gp.isAlive()) {
                        hasAliveMember = true;
                        break;
                    }
                }
                if (hasAliveMember || team.isBedAlive()) {
                    activeTeams.add(team);
                }
            }
        }

        if (activeTeams.size() <= 1) {
            winner = activeTeams.isEmpty() ? null : activeTeams.get(0);
            endGame();
        }
    }

    public void endGame() {
        if (state == GameState.ENDING) return;
        state = GameState.ENDING;
        arena.setState(ArenaState.ENDING);

        if (countdownTask != null) countdownTask.cancel();
        if (generatorTask != null) generatorTask.cancel();

        if (winner != null) {
            Map<String, String> rep = new HashMap<>();
            rep.put("team", winner.getColoredName());
            broadcastArena(plugin.getConfigManager().getMessage("game-end", rep));

            // Show win effects for winning team
            for (UUID uuid : winner.getMembers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    ChatUtil.sendTitle(p, "&6&lVICTORY!", "&eYour team won!", 10, 100, 20);
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 5));
                }
            }
        } else {
            broadcastArena(ChatUtil.color("&7The game ended in a draw!"));
        }

        // Save stats
        for (GamePlayer gp : players.values()) {
            Player player = Bukkit.getPlayer(gp.getUuid());
            if (player != null) {
                boolean won = winner != null && gp.getTeam() == winner.getType();
                plugin.getProgressionManager().recordGame(player, won);
                plugin.getMissionManager().record(player, MissionEvent.GAME, 1);
                if (won) {
                    plugin.getMissionManager().record(player, MissionEvent.WIN, 1);
                }
            }
            plugin.getDatabaseManager().saveStats(gp);
        }

        // Schedule restart
        new BukkitRunnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (countdown <= 0) {
                    restartGame();
                    cancel();
                    return;
                }

                for (UUID uuid : players.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        ChatUtil.sendActionBar(p, "&7Returning to lobby in &e" + countdown + "s...");
                    }
                }
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void restartGame() {
        // Kick all players to lobby
        for (UUID uuid : new HashSet<>(players.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                removePlayer(p);
            }
        }

        restoreMap();
        plugin.getWorldInstanceManager().unloadMatchWorld(arena);

        // Reset game state
        state = GameState.WAITING;
        arena.setState(ArenaState.WAITING);
        arena.resetMatchState();
        players.clear();
        teams.clear();
        spectators.clear();
        placedBlocks.clear();
        winner = null;
        diamondLevel = 1;
        emeraldLevel = 1;
        generatorTick = 0;

        try {
            matchWorld = plugin.getWorldInstanceManager().createMatchWorld(arena);
        } catch (IOException e) {
            matchWorld = null;
            plugin.getLogger().warning("Could not create map instance for arena " + arena.getId() + ": " + e.getMessage());
        }

        // Re-init teams
        for (BedWarsTeam teamType : arena.getActiveTeams()) {
            GameTeam team = new GameTeam(teamType);
            team.setSpawn(arena.toMatchLocation(arena.getTeamSpawns().get(teamType)));
            team.setBedLocation(arena.toMatchLocation(arena.getTeamBeds().get(teamType)));
            team.setGeneratorLocation(arena.toMatchLocation(arena.getTeamGenerators().get(teamType)));
            team.setShopLocation(arena.toMatchLocation(arena.getTeamShopLocations().get(teamType)));
            team.setUpgradeShopLocation(arena.toMatchLocation(arena.getTeamUpgradeShopLocations().get(teamType)));
            team.setIndicatorLocation(arena.toMatchLocation(arena.getTeamIndicators().get(teamType)));
            team.setIslandCenter(arena.toMatchLocation(arena.getTeamCenters().get(teamType)));
            team.setIslandRadius(arena.getTeamIslandRadius(teamType));
            teams.put(teamType, team);
        }
    }

    public void forceShutdown() {
        if (countdownTask != null) countdownTask.cancel();
        if (generatorTask != null) generatorTask.cancel();
        if (nametaskTask != null) nametaskTask.cancel();
        if (respawnTask != null) respawnTask.cancel();

        for (UUID uuid : new HashSet<>(players.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (!plugin.getPlayerSessionManager().restore(player, false)) {
                resetPlayer(player);
            }
            teleportToMainLobby(player);
            plugin.getScoreboardManager().removePlayer(player);
        }
        for (UUID uuid : new HashSet<>(spectators)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            resetPlayer(player);
            teleportToMainLobby(player);
            plugin.getScoreboardManager().removePlayer(player);
        }

        restoreMap();
        plugin.getWorldInstanceManager().unloadMatchWorld(arena);
        arena.resetMatchState();
        arena.setState(ArenaState.WAITING);
        state = GameState.WAITING;
        players.clear();
        teams.clear();
        spectators.clear();
        placedBlocks.clear();
        originalBlocks.clear();
        winner = null;
    }

    private void restoreMap() {
        // Restore placed blocks
        for (Location loc : placedBlocks) {
            if (loc.getWorld() != null) {
                originalBlocks.computeIfPresent(loc, (l, m) -> { l.getBlock().setType(m); return m; });
                if (!originalBlocks.containsKey(loc)) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }
        placedBlocks.clear();
    }

    public void trackPlacedBlock(Location loc) {
        if (!originalBlocks.containsKey(loc)) {
            originalBlocks.put(loc, loc.getBlock().getType());
        }
        placedBlocks.add(loc);
        arena.addPlacedBlock(loc);
    }

    private void broadcastArena(String message) {
        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(message);
        }
        for (UUID uuid : spectators) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(message);
        }
    }

    private void increment(Map<UUID, Integer> values, UUID uuid) {
        values.put(uuid, values.getOrDefault(uuid, 0) + 1);
    }

    private String getPlayerTeamPrefix(Player player) {
        GamePlayer gp = players.get(player.getUniqueId());
        if (gp == null || gp.getTeam() == null) return "";
        return gp.getTeam().getColor() + "";
    }

    private void updateCountdownScoreboard() {
        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) plugin.getScoreboardManager().updatePlayer(p, this);
        }
    }

    private void updateAllScoreboards() {
        for (UUID uuid : players.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) plugin.getScoreboardManager().updatePlayer(p, this);
        }
        for (UUID uuid : spectators) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) plugin.getScoreboardManager().updatePlayer(p, this);
        }
    }

    private void teleportToMainLobby(Player player) {
        Location lobby = plugin.getConfigManager().getLobbyLocation();
        if (lobby != null) player.teleport(lobby);
    }

    private void resetPlayer(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        PlayerUtil.clearPotionEffects(player);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setFireTicks(0);
        player.setExp(0);
        player.setLevel(0);
    }

    public boolean isInGame(Player player) {
        return players.containsKey(player.getUniqueId()) || spectators.contains(player.getUniqueId());
    }

    public boolean isInGame(org.bukkit.entity.Player player, Arena checkArena) {
        return this.arena.getId().equals(checkArena.getId()) && isInGame(player);
    }

    // Getters
    public Arena getArena() { return arena; }
    public GameState getState() { return state; }
    public Map<UUID, GamePlayer> getPlayers() { return players; }
    public Map<BedWarsTeam, GameTeam> getTeams() { return teams; }
    public List<UUID> getSpectators() { return spectators; }
    public int getCountdown() { return countdown; }
    public int getPlayerCount() { return players.size(); }
    public GameTeam getWinner() { return winner; }
    public List<Location> getPlacedBlocks() { return placedBlocks; }

    public GamePlayer getGamePlayer(Player player) {
        return players.get(player.getUniqueId());
    }

    public GameTeam getPlayerTeam(Player player) {
        GamePlayer gp = getGamePlayer(player);
        if (gp == null || gp.getTeam() == null) return null;
        return teams.get(gp.getTeam());
    }

    public boolean areSameTeam(Player a, Player b) {
        GamePlayer ga = getGamePlayer(a);
        GamePlayer gb = getGamePlayer(b);
        if (ga == null || gb == null) return false;
        if (ga.getTeam() == null || gb.getTeam() == null) return false;
        return ga.getTeam() == gb.getTeam();
    }
}
