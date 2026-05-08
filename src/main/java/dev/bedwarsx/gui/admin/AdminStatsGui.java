package dev.bedwarsx.gui.admin;

import dev.bedwarsx.database.DatabaseManager;
import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.lang.Lang;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AdminStatsGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Inventory inventory;

    public AdminStatsGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &e📈 &lEstatísticas", 4);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(27 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 3; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.BOOK, 1,
                "&e📈 &lEstatísticas",
                "&7Consulte estatísticas de jogadores.",
                "&7Busque por nome de jogador."));

        // ── Stats do próprio admin
        DatabaseManager.PlayerStats selfStats = plugin.getDatabaseManager().getStats(player.getUniqueId());
        builder.set(11, buildStatsItem(player.getName(), selfStats));

        // ── Buscar jogador
        builder.set(15, ItemUtil.create(Material.COMPASS, 1,
                "&b🔍 &lBuscar Jogador",
                "&7Clique para buscar as stats",
                "&7de outro jogador pelo nome."));

        // ── Online players
        int slot = 19;
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (slot > 25) break;
            DatabaseManager.PlayerStats stats = plugin.getDatabaseManager().getStats(online.getUniqueId());
            builder.set(slot, buildStatsItem(online.getName(), stats));
            slot++;
        }

        builder.set(27, GuiBuilder.SETA_VOLTAR);
        this.inventory = builder.build();
    }

    private ItemStack buildStatsItem(String nome, DatabaseManager.PlayerStats stats) {
        return GuiBuilder.skull(nome,
                "&f" + nome,
                "&7Vitórias: &a" + stats.getWins(),
                "&7Derrotas: &c" + stats.getLosses(),
                "&7Abates: &e" + stats.getKills(),
                "&7Mortes: &e" + stats.getDeaths(),
                "&7Abates Finais: &e" + stats.getFinalKills(),
                "&7Camas Quebradas: &e" + stats.getBedsBroken(),
                "&7K/D: &e" + String.format("%.2f", stats.getKDR()),
                "&7Partidas: &e" + stats.getGamesPlayed());
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 27) {
            plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
            return;
        }
        if (slot == 15) {
            player.closeInventory();
            ChatUtil.send(player, Lang.prefix() + "&eBuscar stats de qual jogador? (ou &ccancel&e):");
            plugin.getChatInputManager().esperar(player, nome -> {
                if (nome.equalsIgnoreCase("cancel")) {
                    plugin.getGuiManager().abrir(player, new AdminStatsGui(plugin, player));
                    return;
                }
                Player target = plugin.getServer().getPlayer(nome);
                if (target == null) {
                    ChatUtil.send(player, Lang.p("jogador-nao-encontrado", "jogador", nome));
                    plugin.getGuiManager().abrir(player, new AdminStatsGui(plugin, player));
                    return;
                }
                DatabaseManager.PlayerStats stats = plugin.getDatabaseManager().getStats(target.getUniqueId());
                ChatUtil.send(player, Lang.p("stats-titulo", "jogador", target.getName()));
                ChatUtil.send(player, Lang.p("stats-vitorias", "valor", stats.getWins()));
                ChatUtil.send(player, Lang.p("stats-derrotas", "valor", stats.getLosses()));
                ChatUtil.send(player, Lang.p("stats-abates", "valor", stats.getKills()));
                ChatUtil.send(player, Lang.p("stats-mortes", "valor", stats.getDeaths()));
                ChatUtil.send(player, Lang.p("stats-abates-finais", "valor", stats.getFinalKills()));
                ChatUtil.send(player, Lang.p("stats-camas", "valor", stats.getBedsBroken()));
                ChatUtil.send(player, Lang.p("stats-kdr", "valor", String.format("%.2f", stats.getKDR())));
                ChatUtil.send(player, Lang.p("stats-winrate", "valor", String.format("%.1f", stats.getWinRate())));
                plugin.getGuiManager().abrir(player, new AdminStatsGui(plugin, player));
            });
        }
    }
}
