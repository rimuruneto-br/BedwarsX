package dev.bedwarsx.gui.admin;

import dev.bedwarsx.arena.Arena;
import dev.bedwarsx.game.Game;
import dev.bedwarsx.game.GameState;
import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.team.GameTeam;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminStatusGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Inventory inventory;

    public AdminStatusGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &b📊 &lStatus do Servidor", 6);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(45 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 5; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Título
        builder.set(4, ItemUtil.create(Material.REDSTONE_COMPARATOR, 1,
                "&b📊 &lStatus em Tempo Real",
                "&7Informações do servidor."));

        // ── Estatísticas gerais
        int online = plugin.getServer().getOnlinePlayers().size();
        long emPartida = plugin.getGameManager().getGames().stream()
                .mapToLong(Game::getPlayerCount).sum();
        long partidas = plugin.getGameManager().getGames().stream()
                .filter(g -> g.getState() == GameState.IN_GAME).count();

        builder.set(20, ItemUtil.create(Material.SKULL_ITEM, (int)Math.max(1, online),
                "&e👥 &lJogadores Online",
                "&7Total online: &e" + online,
                "&7Em partidas: &a" + emPartida,
                "&7No lobby: &7" + (online - emPartida)));

        builder.set(22, ItemUtil.create(Material.FIREBALL, (int)Math.max(1, partidas),
                "&c🎮 &lPartidas Ativas",
                "&7Partidas em jogo: &c" + partidas,
                "&7Partidas aguardando: &e" +
                        plugin.getGameManager().getGames().stream()
                                .filter(g -> g.getState() == GameState.WAITING
                                        || g.getState() == GameState.STARTING).count()));

        builder.set(24, ItemUtil.create(Material.NETHER_STAR, 1,
                "&a🏟 &lArenas",
                "&7Total de arenas: &e" + plugin.getArenaManager().getArenas().size(),
                "&7Configuradas: &a" + plugin.getArenaManager().getArenas().stream().filter(Arena::isSetup).count(),
                "&7Não configuradas: &c" + plugin.getArenaManager().getArenas().stream().filter(a -> !a.isSetup()).count()));

        // ── Partidas em andamento
        int slotBase = 28;
        for (Game game : plugin.getGameManager().getGames()) {
            if (slotBase > 43) break;
            if (game.getState() != GameState.IN_GAME && game.getState() != GameState.STARTING) continue;
            builder.set(slotBase, buildGameItem(game));
            slotBase++;
        }

        builder.set(45, GuiBuilder.SETA_VOLTAR);

        this.inventory = builder.build();
    }

    private ItemStack buildGameItem(Game game) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("  &fStatus: " + game.getState().name()));
        lore.add(ChatUtil.color("  &fJogadores: &e" + game.getPlayerCount()));
        lore.add(ChatUtil.color("  &fTimes ativos: &e" +
                game.getTeams().values().stream().filter(t -> !t.isEliminated()).count()));
        lore.add(ChatUtil.color(""));
        for (GameTeam team : game.getTeams().values()) {
            String status = team.isEliminated() ? "&c✗" : (team.isBedAlive() ? "&a✔" : "&e⚠");
            lore.add(ChatUtil.color("  " + status + " " + team.getColoredName()));
        }
        lore.add(ChatUtil.color(""));
        lore.add(ChatUtil.color("&eClique para gerenciar"));

        return ItemUtil.create(Material.MAP, 1,
                "&f&l" + game.getArena().getDisplayName(), lore);
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 45) {
            plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
        }
    }
}
