package dev.bedwarsx.scoreboard;

import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.stats.ProgressionManager;
import dev.bedwarsx.team.BedWarsTeam;
import dev.bedwarsx.team.GameTeam;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {

    private final BedWarsX plugin;
    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private BukkitTask updateTask;

    // Color codes for unique team lines
    private static final String[] COLORS = {
        ChatColor.RED.toString(), ChatColor.BLUE.toString(), ChatColor.GREEN.toString(),
        ChatColor.YELLOW.toString(), ChatColor.AQUA.toString(), ChatColor.WHITE.toString(),
        ChatColor.LIGHT_PURPLE.toString(), ChatColor.GRAY.toString(),
        ChatColor.DARK_RED.toString(), ChatColor.DARK_BLUE.toString(),
        ChatColor.DARK_GREEN.toString(), ChatColor.GOLD.toString(),
        ChatColor.DARK_AQUA.toString(), ChatColor.DARK_GRAY.toString(),
        ChatColor.BLACK.toString(), ChatColor.BOLD.toString()
    };

    public ScoreboardManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        int interval = plugin.getConfig().getInt("scoreboard.update-interval", 10);
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Game game = plugin.getGameManager().getPlayerGame(player);
                    updatePlayer(player, game);
                }
            }
        }.runTaskTimer(plugin, 20L, interval);
    }

    public void updatePlayer(Player player, Game game) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) return;

        Scoreboard board = getOrCreateBoard(player);
        Objective obj = board.getObjective("bwx_sidebar");
        if (obj == null) {
            obj = board.registerNewObjective("bwx_sidebar", "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        obj.setDisplayName(ChatUtil.color("&6&lBED WARS"));

        // Clear existing scores
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        List<String> lines;
        if (game == null || game.getState() == GameState.WAITING) {
            lines = buildLobbyLines(player, game);
        } else if (game.getState() == GameState.STARTING) {
            lines = buildStartingLines(player, game);
        } else if (game.getState() == GameState.IN_GAME) {
            lines = buildGameLines(player, game);
        } else {
            lines = buildEndLines(player, game);
        }

        // Set lines (reversed for scoreboard display)
        int score = lines.size();
        for (String line : lines) {
            String unique = makeUnique(line, board);
            obj.getScore(unique).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private List<String> buildLobbyLines(Player player, Game game) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatUtil.color("&7" + new java.text.SimpleDateFormat("MM/dd/yy").format(new java.util.Date())));
        lines.add("");
        lines.add(ChatUtil.color("  &fMap: &eWaiting..."));
        lines.add(ChatUtil.color("  &fPlayers: &e" + (game != null ? game.getPlayerCount() : 0)));
        addProgressionLines(lines, player);
        lines.add("");
        lines.add(ChatUtil.color("&7play.bedwarsx.net"));
        return lines;
    }

    private List<String> buildStartingLines(Player player, Game game) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatUtil.color("&7" + new java.text.SimpleDateFormat("MM/dd/yy").format(new java.util.Date())));
        lines.add("");
        lines.add(ChatUtil.color("  &fMap: &e" + game.getArena().getDisplayName()));
        lines.add(ChatUtil.color("  &fMode: &d" + game.getArena().getMode().getDisplayName()));
        lines.add(ChatUtil.color("  &fPlayers: &e" + game.getPlayerCount() + "/" + game.getArena().getEffectiveMaxPlayers()));
        lines.add(ChatUtil.color("  &fStarting in: &e" + game.getCountdown() + "s"));
        addProgressionLines(lines, player);
        lines.add("");
        lines.add(ChatUtil.color("&7play.bedwarsx.net"));
        return lines;
    }

    private List<String> buildGameLines(Player player, Game game) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatUtil.color("&7" + new java.text.SimpleDateFormat("MM/dd/yy").format(new java.util.Date())));
        lines.add("");

        // Team statuses
        for (Map.Entry<BedWarsTeam, GameTeam> entry : game.getTeams().entrySet()) {
            BedWarsTeam teamType = entry.getKey();
            GameTeam team = entry.getValue();

            String bedStatus;
            if (team.isEliminated()) {
                bedStatus = ChatUtil.color("&c✗");
            } else if (!team.isBedAlive()) {
                // How many alive
                long alive = team.getMembers().stream()
                        .map(uuid -> game.getPlayers().get(uuid))
                        .filter(gp -> gp != null && gp.isAlive())
                        .count();
                bedStatus = ChatUtil.color("&e" + alive);
            } else {
                bedStatus = ChatUtil.color("&a✔");
            }

            String teamLine = "  " + teamType.getColor() + teamType.getDisplayName() + " &f" + bedStatus;
            lines.add(ChatUtil.color(teamLine));
        }

        lines.add("");

        // Player stats
        GamePlayer gp = game.getGamePlayer(player);
        if (gp != null) {
            lines.add(ChatUtil.color("  &fK/F/B: &e" + gp.getKills() + "&7/&b" + gp.getFinalKills() + "&7/&a" + gp.getBedsBroken()));
        }

        addCompactProgressionLine(lines, player);
        lines.add("");
        lines.add(ChatUtil.color("&7play.bedwarsx.net"));
        return lines;
    }

    private void addProgressionLines(List<String> lines, Player player) {
        ProgressionManager.Profile profile = plugin.getProgressionManager().get(player);
        lines.add(ChatUtil.color("  &fLevel: &6" + profile.getLevel() + " &7(" + profile.getLevelPercent() + "%)"));
        lines.add(ChatUtil.color("  &fXP: &a" + profile.getXpIntoLevel() + "&7/&a" + profile.getXpForNextLevel()));
    }

    private void addCompactProgressionLine(List<String> lines, Player player) {
        ProgressionManager.Profile profile = plugin.getProgressionManager().get(player);
        lines.add(ChatUtil.color("  &fLv: &6" + profile.getLevel() + " &7" + profile.getLevelPercent() + "% &8(" + profile.getXpIntoLevel() + "/5k)"));
    }

    private List<String> buildEndLines(Player player, Game game) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        if (game.getWinner() != null) {
            lines.add(ChatUtil.color("  &6Winner: " + game.getWinner().getColoredName()));
        } else {
            lines.add(ChatUtil.color("  &7Draw!"));
        }
        lines.add("");
        lines.add(ChatUtil.color("&7Returning to lobby..."));
        return lines;
    }

    private Scoreboard getOrCreateBoard(Player player) {
        return playerScoreboards.computeIfAbsent(player.getUniqueId(),
                uuid -> Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void removePlayer(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    // Nametag management (teams colored)
    public void updateNametag(Player player, Game game) {
        if (game == null) return;
        GamePlayer gp = game.getGamePlayer(player);
        if (gp == null || gp.getTeam() == null) return;

        Scoreboard board = getOrCreateBoard(player);

        for (Player other : Bukkit.getOnlinePlayers()) {
            Scoreboard otherBoard = getOrCreateBoard(other);

            for (BedWarsTeam teamType : BedWarsTeam.values()) {
                String teamName = "bwx_" + teamType.name().toLowerCase();
                Team scoTeam = otherBoard.getTeam(teamName);
                if (scoTeam == null) {
                    scoTeam = otherBoard.registerNewTeam(teamName);
                    scoTeam.setPrefix(teamType.getColoredName().charAt(0) + " " + teamType.getColor());
                    scoTeam.setSuffix(ChatColor.RESET.toString());
                }

                GamePlayer otherGp = game.getGamePlayer(other);
                if (otherGp != null && otherGp.getTeam() == teamType) {
                    if (!scoTeam.hasEntry(other.getName())) {
                        scoTeam.addEntry(other.getName());
                    }
                }
            }
        }
    }

    private String makeUnique(String line, Scoreboard board) {
        // Ensure line is unique in scoreboard
        if (board.getEntries().contains(line)) {
            // Add invisible characters
            for (String color : COLORS) {
                String candidate = line + color;
                if (!board.getEntries().contains(candidate)) {
                    return candidate;
                }
            }
        }
        return line;
    }

    public void stopTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
}
