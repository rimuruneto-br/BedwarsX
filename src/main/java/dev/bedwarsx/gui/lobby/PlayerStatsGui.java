package dev.bedwarsx.gui.lobby;

import dev.bedwarsx.database.DatabaseManager;
import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI de estatísticas pessoais do jogador no lobby.
 */
public class PlayerStatsGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Inventory inventory;

    public PlayerStatsGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;
        DatabaseManager.PlayerStats stats = plugin.getDatabaseManager().getStats(player.getUniqueId());

        GuiBuilder builder = GuiBuilder.criar("&8✦ &e📊 &lSuas Estatísticas", 4);
        builder.borda(GuiBuilder.BORDER_BLACK);
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.glass((short)4, " "));
            builder.set(27 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 3; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        // ── Cabeçalho com skull do jogador ─────────────────────────────────────
        builder.set(4, GuiBuilder.skull(player.getName(),
                "&e&l" + player.getName() + " &7— Stats",
                "&7Suas estatísticas no BedWarsX."));

        // ── Cards de stat (linha 2) ────────────────────────────────────────────
        builder.set(10, buildCard(Material.EMERALD,
                "&a🏆 Vitórias", stats.getWins(),
                "&7Total de partidas vencidas."));

        builder.set(11, buildCard(Material.REDSTONE,
                "&c💀 Derrotas", stats.getLosses(),
                "&7Total de partidas perdidas."));

        builder.set(12, buildCard(Material.IRON_SWORD,
                "&f⚔ Abates", stats.getKills(),
                "&7Total de abates realizados."));

        builder.set(13, buildCard(Material.BONE,
                "&7☠ Mortes", stats.getDeaths(),
                "&7Total de mortes sofridas."));

        builder.set(14, buildCard(Material.DIAMOND_SWORD,
                "&b★ Abates Finais", stats.getFinalKills(),
                "&7Abates com cama destruída."));

        builder.set(15, buildCard(Material.BED,
                "&d🛏 Camas", stats.getBedsBroken(),
                "&7Camas destruídas."));

        builder.set(16, buildCard(Material.NETHER_STAR,
                "&e🎮 Partidas", stats.getGamesPlayed(),
                "&7Total de partidas jogadas."));

        // ── Cards de ratio (linha 3) ───────────────────────────────────────────
        String kdrColor = stats.getKDR() >= 2.0 ? "&a" : stats.getKDR() >= 1.0 ? "&e" : "&c";
        builder.set(20, ItemUtil.create(Material.BOOK, 1,
                "&fK/D Ratio",
                "&7Abates por morte:",
                kdrColor + String.format("%.2f", stats.getKDR()),
                "",
                getRating(stats.getKDR(), 3.0, 1.5)));

        String wrColor = stats.getWinRate() >= 50 ? "&a" : stats.getWinRate() >= 30 ? "&e" : "&c";
        builder.set(22, ItemUtil.create(Material.GOLD_INGOT, 1,
                "&fTaxa de Vitória",
                "&7Porcentagem de vitórias:",
                wrColor + String.format("%.1f", stats.getWinRate()) + "%",
                "",
                getRating(stats.getWinRate(), 60, 30)));

        // ── Progressão visual ──────────────────────────────────────────────────
        builder.set(24, ItemUtil.create(Material.EXP_BOTTLE, 1,
                "&aNível BedWars",
                "&7Baseado nas suas stats:",
                "&e" + calcNivel(stats),
                "",
                buildXPBar(stats)));

        builder.set(27, GuiBuilder.SETA_FECHAR);
        builder.preencher(GuiBuilder.BORDER_DARK);
        this.inventory = builder.build();
    }

    private ItemStack buildCard(Material mat, String nome, int valor, String desc) {
        return ItemUtil.create(mat, 1, nome,
                "&7" + desc,
                "",
                "&f" + valor);
    }

    private String getRating(double value, double high, double mid) {
        if (value >= high) return "&a&l★★★ EXCELENTE";
        if (value >= mid)  return "&e&l★★☆ BOM";
        return "&c&l★☆☆ INICIANTE";
    }

    private String calcNivel(DatabaseManager.PlayerStats s) {
        int pts = s.getWins() * 10 + s.getKills() * 2 + s.getFinalKills() * 3 + s.getBedsBroken() * 5;
        if (pts >= 5000) return "&6&lLendário";
        if (pts >= 2000) return "&5&lMestre";
        if (pts >= 800)  return "&b&lDiamante";
        if (pts >= 300)  return "&a&lOuro";
        if (pts >= 100)  return "&f&lPrata";
        return "&7&lBronze";
    }

    private String buildXPBar(DatabaseManager.PlayerStats s) {
        int pts = s.getWins() * 10 + s.getKills() * 2 + s.getFinalKills() * 3 + s.getBedsBroken() * 5;
        int[] thresholds = {0, 100, 300, 800, 2000, 5000};
        int curTier = 0;
        for (int i = 0; i < thresholds.length - 1; i++) {
            if (pts >= thresholds[i]) curTier = i;
        }
        if (curTier >= thresholds.length - 1) return "&6Nível máximo atingido!";
        int prev = thresholds[curTier], next = thresholds[curTier + 1];
        int progress = pts - prev, needed = next - prev;
        int filled = (int) Math.round((double) progress / needed * 10);
        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < 10; i++) bar.append(i < filled ? "&a■" : "&7■");
        bar.append("&8] &e").append(progress).append("&7/&e").append(needed);
        return bar.toString();
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 27) player.closeInventory();
    }
}
