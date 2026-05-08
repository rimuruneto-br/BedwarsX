package dev.bedwarsx.command;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.npc.BedWarsNPC;
import dev.bedwarsx.team.BedWarsTeam;
import dev.bedwarsx.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class BedWarsCommand implements CommandExecutor, TabCompleter {

    private final BedWarsX plugin;

    public BedWarsCommand(BedWarsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help": sendHelp(sender); break;
            case "arena": handleArena(sender, args); break;
            case "setlobby": handleSetLobby(sender); break;
            case "npc": handleNPC(sender, args); break;
            case "reload": handleReload(sender); break;
            case "stats": handleStats(sender, args); break;
            case "forcestart": handleForceStart(sender, args); break;
            case "forcestop": handleForceStop(sender, args); break;
            default:
                ChatUtil.send((Player) sender, "&cUnknown subcommand. Use &e/bw help&c.");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtil.color("&6&l━━━ BedWarsX Admin ━━━"));
        sender.sendMessage(ChatUtil.color("&e/bw arena create <id> &7- Create arena"));
        sender.sendMessage(ChatUtil.color("&e/bw arena delete <id> &7- Delete arena"));
        sender.sendMessage(ChatUtil.color("&e/bw arena setlobby <id> &7- Set arena lobby"));
        sender.sendMessage(ChatUtil.color("&e/bw arena setspawn <id> <team> &7- Set team spawn"));
        sender.sendMessage(ChatUtil.color("&e/bw arena setbed <id> <team> &7- Set team bed"));
        sender.sendMessage(ChatUtil.color("&e/bw arena addgen <id> <iron|gold|diamond|emerald> &7- Add generator"));
        sender.sendMessage(ChatUtil.color("&e/bw arena addteam <id> <team> &7- Enable team"));
        sender.sendMessage(ChatUtil.color("&e/bw arena enable <id> &7- Mark arena as ready"));
        sender.sendMessage(ChatUtil.color("&e/bw arena info <id> &7- Arena info"));
        sender.sendMessage(ChatUtil.color("&e/bw arena list &7- List arenas"));
        sender.sendMessage(ChatUtil.color("&e/bw setlobby &7- Set main lobby"));
        sender.sendMessage(ChatUtil.color("&e/bw npc create <id> <name> <type> &7- Create NPC"));
        sender.sendMessage(ChatUtil.color("&e/bw npc delete <id> &7- Delete NPC"));
        sender.sendMessage(ChatUtil.color("&e/bw npc setskin <id> <value> <signature> &7- Set skin"));
        sender.sendMessage(ChatUtil.color("&e/bw npc list &7- List NPCs"));
        sender.sendMessage(ChatUtil.color("&e/bw reload &7- Reload config"));
        sender.sendMessage(ChatUtil.color("&e/bw forcestart <arena> &7- Force start game"));
        sender.sendMessage(ChatUtil.color("&e/bw forcestop <arena> &7- Force stop game"));
        sender.sendMessage(ChatUtil.color("&e/bw stats [player] &7- View stats"));
    }

    private void handleArena(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bedwarsx.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatUtil.color("&cUsage: /bw arena <create|delete|list|info|...> [args]"));
            return;
        }

        String sub = args[1].toLowerCase();

        switch (sub) {
            case "create": {
                if (args.length < 3) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena create <id>")); return; }
                Arena arena = plugin.getArenaManager().createArena(args[2]);
                if (arena == null) {
                    sender.sendMessage(ChatUtil.color("&cArena &e" + args[2] + " &calready exists!"));
                } else {
                    sender.sendMessage(ChatUtil.color("&aArena &e" + args[2] + " &acreated! Now configure it with /bw arena set* commands."));
                }
                break;
            }
            case "delete": {
                if (args.length < 3) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena delete <id>")); return; }
                boolean deleted = plugin.getArenaManager().deleteArena(args[2]);
                sender.sendMessage(deleted
                        ? ChatUtil.color("&aArena &e" + args[2] + " &adeleted.")
                        : ChatUtil.color("&cArena not found."));
                break;
            }
            case "list": {
                sender.sendMessage(ChatUtil.color("&6&lArenas:"));
                for (Arena a : plugin.getArenaManager().getArenas()) {
                    sender.sendMessage(ChatUtil.color("  &e" + a.getId() + " &7- " + a.getState().getDisplayName()
                            + " &7- Setup: " + (a.isSetup() ? "&a✔" : "&c✗")));
                }
                break;
            }
            case "info": {
                if (args.length < 3) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena info <id>")); return; }
                Arena a = plugin.getArenaManager().getArena(args[2]);
                if (a == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
                sender.sendMessage(ChatUtil.color("&6&l━━━ Arena: " + a.getId() + " ━━━"));
                sender.sendMessage(ChatUtil.color("  &fDisplay Name: &e" + a.getDisplayName()));
                sender.sendMessage(ChatUtil.color("  &fStatus: " + a.getState().getDisplayName()));
                sender.sendMessage(ChatUtil.color("  &fSetup: " + (a.isSetup() ? "&a✔" : "&c✗")));
                sender.sendMessage(ChatUtil.color("  &fMode: &e" + a.getMode().getDisplayName()));
                sender.sendMessage(ChatUtil.color("  &fMax Players: &e" + a.getEffectiveMaxPlayers()));
                sender.sendMessage(ChatUtil.color("  &fMin Players: &e" + a.getMinPlayers()));
                sender.sendMessage(ChatUtil.color("  &fTeams: &e" + a.getEnabledTeams()));
                sender.sendMessage(ChatUtil.color("  &fIron Gens: &e" + a.getIronGenerators().size()));
                sender.sendMessage(ChatUtil.color("  &fGold Gens: &e" + a.getGoldGenerators().size()));
                sender.sendMessage(ChatUtil.color("  &fDiamond Gens: &e" + a.getDiamondGenerators().size()));
                sender.sendMessage(ChatUtil.color("  &fEmerald Gens: &e" + a.getEmeraldGenerators().size()));
                break;
            }
            case "setlobby": {
                if (!(sender instanceof Player)) { sender.sendMessage("In-game only."); return; }
                if (args.length < 3) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena setlobby <id>")); return; }
                Arena a = plugin.getArenaManager().getArena(args[2]);
                if (a == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
                a.setLobby(((Player)sender).getLocation());
                plugin.getArenaManager().saveArenas();
                sender.sendMessage(ChatUtil.color("&aLobby set for arena &e" + a.getId()));
                break;
            }
            case "setspawn": {
                if (!(sender instanceof Player)) { sender.sendMessage("In-game only."); return; }
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena setspawn <id> <team>")); return; }
                BedWarsTeam team = BedWarsTeam.fromString(args[3]);
                if (team == null) { sender.sendMessage(ChatUtil.color("&cInvalid team.")); return; }
                boolean set = plugin.getArenaManager().setTeamSpawn(args[2], team, ((Player)sender).getLocation());
                sender.sendMessage(set ? ChatUtil.color("&aSpawn set for team " + team.getColoredName())
                        : ChatUtil.color("&cArena not found."));
                break;
            }
            case "setbed": {
                if (!(sender instanceof Player)) { sender.sendMessage("In-game only."); return; }
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena setbed <id> <team>")); return; }
                BedWarsTeam team = BedWarsTeam.fromString(args[3]);
                if (team == null) { sender.sendMessage(ChatUtil.color("&cInvalid team.")); return; }
                boolean set = plugin.getArenaManager().setTeamBed(args[2], team, ((Player)sender).getLocation());
                sender.sendMessage(set ? ChatUtil.color("&aBed location set for team " + team.getColoredName())
                        : ChatUtil.color("&cArena not found."));
                break;
            }
            case "addgen": {
                if (!(sender instanceof Player)) { sender.sendMessage("In-game only."); return; }
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena addgen <id> <iron|gold|diamond|emerald>")); return; }
                boolean added = plugin.getArenaManager().addGenerator(args[2], args[3], ((Player)sender).getLocation());
                sender.sendMessage(added ? ChatUtil.color("&a" + args[3] + " generator added!")
                        : ChatUtil.color("&cFailed. Check arena ID and type."));
                break;
            }
            case "addteam": {
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena addteam <id> <team>")); return; }
                Arena a = plugin.getArenaManager().getArena(args[2]);
                if (a == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
                BedWarsTeam team = BedWarsTeam.fromString(args[3]);
                if (team == null) { sender.sendMessage(ChatUtil.color("&cInvalid team.")); return; }
                if (!a.getEnabledTeams().contains(team)) a.getEnabledTeams().add(team);
                plugin.getArenaManager().saveArenas();
                sender.sendMessage(ChatUtil.color("&aTeam " + team.getColoredName() + " &aadded to arena."));
                break;
            }
            case "enable": {
                if (args.length < 3) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena enable <id>")); return; }
                Arena a = plugin.getArenaManager().getArena(args[2]);
                if (a == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
                if (!plugin.getArenaManager().isArenaComplete(a)) {
                    sender.sendMessage(ChatUtil.color("&cArena is not fully configured!"));
                    sender.sendMessage(ChatUtil.color("&7Make sure: lobby, all team spawns, and all team beds are set."));
                    return;
                }
                a.setSetup(true);
                plugin.getArenaManager().saveArenas();
                sender.sendMessage(ChatUtil.color("&aArena &e" + a.getId() + " &ais now &aenabled!"));
                break;
            }
            case "setname": {
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena setname <id> <name>")); return; }
                Arena a = plugin.getArenaManager().getArena(args[2]);
                if (a == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
                a.setDisplayName(args[3]);
                plugin.getArenaManager().saveArenas();
                sender.sendMessage(ChatUtil.color("&aDisplay name set to &e" + args[3]));
                break;
            }
            case "setmaxplayers": {
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw arena setmaxplayers <id> <amount>")); return; }
                Arena a = plugin.getArenaManager().getArena(args[2]);
                if (a == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
                a.setMaxPlayers(Integer.parseInt(args[3]));
                plugin.getArenaManager().saveArenas();
                sender.sendMessage(ChatUtil.color("&aMax players set to &e" + args[3]));
                break;
            }
            default:
                sender.sendMessage(ChatUtil.color("&cUnknown arena subcommand."));
        }
    }

    private void handleSetLobby(CommandSender sender) {
        if (!(sender instanceof Player)) { sender.sendMessage("In-game only."); return; }
        if (!sender.hasPermission("bedwarsx.admin")) { sender.sendMessage(plugin.getConfigManager().getMessage("no-permission")); return; }
        Player player = (Player) sender;
        plugin.getConfigManager().setLobbyLocation(player.getLocation());
        ChatUtil.send(player, "&aMain lobby location set!");
    }

    private void handleNPC(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bedwarsx.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }
        if (!(sender instanceof Player) && !args[1].equalsIgnoreCase("list")) {
            sender.sendMessage("In-game only for NPC commands.");
            return;
        }

        if (args.length < 2) { sender.sendMessage(ChatUtil.color("&cUsage: /bw npc <create|delete|setskin|list> [args]")); return; }

        String sub = args[1].toLowerCase();
        Player player = sender instanceof Player ? (Player) sender : null;

        switch (sub) {
            case "create": {
                if (args.length < 5) { sender.sendMessage(ChatUtil.color("&cUsage: /bw npc create <id> <name> <type>")); return; }
                String id = args[2];
                String name = args[3].replace("_", " ");
                String type = args[4];

                BedWarsNPC npc = plugin.getNpcManager().createNPC(id, name, player.getLocation(), "", "", type);
                if (npc != null) {
                    plugin.getNpcManager().getNpcs().add(npc);
                    plugin.getNpcManager().spawnNPCForAll(npc);
                    plugin.getNpcManager().saveNPC(npc);
                    ChatUtil.send(player, "&aNPC &e" + id + " &acreated at your location.");
                    ChatUtil.send(player, "&7Type: &f" + type + " &7| Use &e/bw npc setskin &7to set skin.");
                } else {
                    ChatUtil.send(player, "&cFailed to create NPC.");
                }
                break;
            }
            case "delete": {
                if (args.length < 3) { sender.sendMessage(ChatUtil.color("&cUsage: /bw npc delete <id>")); return; }
                plugin.getNpcManager().deleteNPC(args[2]);
                sender.sendMessage(ChatUtil.color("&aNPC &e" + args[2] + " &adeleted."));
                break;
            }
            case "setskin": {
                if (args.length < 5) { sender.sendMessage(ChatUtil.color("&cUsage: /bw npc setskin <id> <value> <signature>")); return; }
                BedWarsNPC npc = plugin.getNpcManager().getNPCById(args[2]);
                if (npc == null) { sender.sendMessage(ChatUtil.color("&cNPC not found.")); return; }
                npc.setSkinValue(args[3]);
                npc.setSkinSignature(args[4]);
                plugin.getNpcManager().saveNPC(npc);
                // Re-spawn for all
                plugin.getNpcManager().despawnNPCForAll(npc);
                plugin.getNpcManager().spawnNPCForAll(npc);
                sender.sendMessage(ChatUtil.color("&aSkin updated for NPC &e" + args[2]));
                break;
            }
            case "list": {
                sender.sendMessage(ChatUtil.color("&6&lNPCs:"));
                for (BedWarsNPC npc : plugin.getNpcManager().getNpcs()) {
                    sender.sendMessage(ChatUtil.color("  &e" + npc.getId() + " &7- " + npc.getName()
                            + " &7[" + npc.getType() + "]"));
                }
                break;
            }
            case "link": {
                if (args.length < 4) { sender.sendMessage(ChatUtil.color("&cUsage: /bw npc link <npcId> <arenaId>")); return; }
                BedWarsNPC npc = plugin.getNpcManager().getNPCById(args[2]);
                if (npc == null) { sender.sendMessage(ChatUtil.color("&cNPC not found.")); return; }
                npc.setLinkedArena(args[3]);
                plugin.getNpcManager().saveNPC(npc);
                sender.sendMessage(ChatUtil.color("&aNPC linked to arena &e" + args[3]));
                break;
            }
            default:
                sender.sendMessage(ChatUtil.color("&cUnknown NPC subcommand."));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("bedwarsx.admin")) { sender.sendMessage(plugin.getConfigManager().getMessage("no-permission")); return; }
        plugin.reloadConfig();
        plugin.getConfigManager().reloadConfig("arenas");
        plugin.getConfigManager().reloadConfig("npcs");
        sender.sendMessage(ChatUtil.color("&aBedWarsX reloaded!"));
    }

    private void handleStats(CommandSender sender, String[] args) {
        String targetName = args.length > 1 ? args[1] : (sender instanceof Player ? sender.getName() : null);
        if (targetName == null) { sender.sendMessage(ChatUtil.color("&cUsage: /bw stats <player>")); return; }

        org.bukkit.entity.Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) { sender.sendMessage(ChatUtil.color("&cPlayer not found.")); return; }

        dev.bedwarsx.database.DatabaseManager.PlayerStats stats =
                plugin.getDatabaseManager().getStats(target.getUniqueId());

        sender.sendMessage(ChatUtil.color("&6&l━━━ Stats: " + target.getName() + " ━━━"));
        sender.sendMessage(ChatUtil.color("  &fWins: &a" + stats.getWins()));
        sender.sendMessage(ChatUtil.color("  &fLosses: &c" + stats.getLosses()));
        sender.sendMessage(ChatUtil.color("  &fKills: &e" + stats.getKills()));
        sender.sendMessage(ChatUtil.color("  &fDeaths: &e" + stats.getDeaths()));
        sender.sendMessage(ChatUtil.color("  &fFinal Kills: &e" + stats.getFinalKills()));
        sender.sendMessage(ChatUtil.color("  &fBeds Broken: &e" + stats.getBedsBroken()));
        sender.sendMessage(ChatUtil.color("  &fK/D: &e" + String.format("%.2f", stats.getKDR())));
        sender.sendMessage(ChatUtil.color("  &fWin Rate: &e" + String.format("%.1f", stats.getWinRate()) + "%"));
    }

    private void handleForceStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bedwarsx.admin")) { sender.sendMessage(plugin.getConfigManager().getMessage("no-permission")); return; }
        if (args.length < 2) { sender.sendMessage(ChatUtil.color("&cUsage: /bw forcestart <arenaId>")); return; }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
        dev.bedwarsx.game.Game game = plugin.getGameManager().getGame(arena);
        if (game == null) { sender.sendMessage(ChatUtil.color("&cNo active game for that arena.")); return; }
        game.startGame();
        sender.sendMessage(ChatUtil.color("&aForce-started arena &e" + args[1]));
    }

    private void handleForceStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bedwarsx.admin")) { sender.sendMessage(plugin.getConfigManager().getMessage("no-permission")); return; }
        if (args.length < 2) { sender.sendMessage(ChatUtil.color("&cUsage: /bw forcestop <arenaId>")); return; }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) { sender.sendMessage(ChatUtil.color("&cArena not found.")); return; }
        dev.bedwarsx.game.Game game = plugin.getGameManager().getGame(arena);
        if (game == null) { sender.sendMessage(ChatUtil.color("&cNo active game.")); return; }
        game.endGame();
        sender.sendMessage(ChatUtil.color("&aForce-stopped arena &e" + args[1]));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("bedwarsx.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return filterStart(Arrays.asList("help", "arena", "setlobby", "npc", "reload", "stats", "forcestart", "forcestop"), args[0]);
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "arena": return filterStart(Arrays.asList("create","delete","list","info","setlobby","setspawn","setbed","addgen","addteam","enable","setname","setmaxplayers"), args[1]);
                case "npc": return filterStart(Arrays.asList("create","delete","setskin","list","link"), args[1]);
                case "forcestart": case "forcestop": case "stats":
                    List<String> names = new ArrayList<>();
                    for (Arena a : plugin.getArenaManager().getArenas()) names.add(a.getId());
                    return filterStart(names, args[1]);
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("arena")) {
            switch (args[1].toLowerCase()) {
                case "delete": case "info": case "setlobby": case "enable": case "setname": case "setmaxplayers": case "addteam": case "addgen": case "setspawn": case "setbed":
                    List<String> ids = new ArrayList<>();
                    for (Arena a : plugin.getArenaManager().getArenas()) ids.add(a.getId());
                    return filterStart(ids, args[2]);
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("arena")) {
            switch (args[1].toLowerCase()) {
                case "addgen": return filterStart(Arrays.asList("iron","gold","diamond","emerald"), args[3]);
                case "addteam": case "setspawn": case "setbed":
                    List<String> teams = new ArrayList<>();
                    for (BedWarsTeam t : BedWarsTeam.values()) teams.add(t.name().toLowerCase());
                    return filterStart(teams, args[3]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> filterStart(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) result.add(s);
        }
        return result;
    }
}
