package dev.bedwarsx.gui.admin;

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

public class AdminConfigGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Inventory inventory;

    public AdminConfigGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        GuiBuilder builder = GuiBuilder.criar("&8✦ &6⚙ &lConfigurações do Plugin", 5);
        builder.bordaPadrao();
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.BORDER_DARK);
            builder.set(36 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 4; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.COMMAND, 1,
                "&6⚙ &lConfigurações",
                "&7Altere as configurações do plugin.",
                "&7As mudanças são salvas automaticamente."));

        boolean scoreboardOn = plugin.getConfig().getBoolean("scoreboard.enabled", true);
        boolean nametag      = plugin.getConfig().getBoolean("nametag.enabled", true);
        int countWait        = plugin.getConfig().getInt("countdown.waiting", 30);
        int countStart       = plugin.getConfig().getInt("countdown.starting", 10);
        int respawnTime      = plugin.getConfig().getInt("game.respawn-time", 5);
        int minPlayers       = plugin.getConfig().getInt("game.min-players", 2);
        boolean experimental = plugin.getConfig().getBoolean("shop.experimental.enabled", true);

        // ── Linha 2
        builder.set(10, GuiBuilder.criarBotaoToggle(scoreboardOn,
                "&f📊 Scoreboard"));
        builder.set(11, GuiBuilder.criarBotaoToggle(nametag,
                "&f🏷 Nametags de Time"));
        builder.set(12, GuiBuilder.criarBotaoToggle(experimental,
                "&f🔮 Loja Experimental"));

        // ── Linha 3 - Valores numéricos
        builder.set(19, buildNumBtn(Material.WATCH,
                "&e⏱ Contagem de Espera",
                countWait + " segundos", "countdown.waiting", countWait, 5, 120));
        builder.set(20, buildNumBtn(Material.WATCH,
                "&e⏱ Contagem de Início",
                countStart + " segundos", "countdown.starting", countStart, 5, 30));
        builder.set(21, buildNumBtn(Material.BED,
                "&c💀 Tempo de Respawn",
                respawnTime + " segundos", "game.respawn-time", respawnTime, 3, 15));
        builder.set(22, buildNumBtn(Material.SKULL_ITEM,
                "&7👥 Mín. Jogadores",
                minPlayers + " jogadores", "game.min-players", minPlayers, 2, 8));

        builder.set(36, GuiBuilder.SETA_VOLTAR);

        this.inventory = builder.build();
    }

    private ItemStack buildNumBtn(Material mat, String nome, String atual, String path, int valor, int min, int max) {
        return ItemUtil.create(mat, 1,
                nome,
                "&7Atual: &e" + atual,
                "",
                "&eClique Esquerdo: &7+1",
                "&eClique Direito: &7-1",
                "&eShift+Esq: &7+5",
                "&eShift+Dir: &7-5",
                "&8[" + min + " - " + max + "]");
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        switch (slot) {
            case 36:
                plugin.getGuiManager().abrir(player, new AdminMainGui(plugin));
                return;
            case 10:
                toggleBool("scoreboard.enabled");
                break;
            case 11:
                toggleBool("nametag.enabled");
                break;
            case 12:
                toggleBool("shop.experimental.enabled");
                break;
            case 19:
                adjustInt("countdown.waiting", event, 5, 120);
                break;
            case 20:
                adjustInt("countdown.starting", event, 5, 30);
                break;
            case 21:
                adjustInt("game.respawn-time", event, 3, 15);
                break;
            case 22:
                adjustInt("game.min-players", event, 2, 8);
                break;
        }
        plugin.saveConfig();
        plugin.getGuiManager().abrir(player, new AdminConfigGui(plugin, player));
    }

    private void toggleBool(String path) {
        plugin.getConfig().set(path, !plugin.getConfig().getBoolean(path));
    }

    private void adjustInt(String path, InventoryClickEvent event, int min, int max) {
        int current = plugin.getConfig().getInt(path, 0);
        int delta = event.isShiftClick() ? 5 : 1;
        if (event.isRightClick()) delta = -delta;
        int newVal = Math.max(min, Math.min(max, current + delta));
        plugin.getConfig().set(path, newVal);
        ChatUtil.send(player, Lang.prefix() + "&f" + path + " &7→ &e" + newVal);
    }
}
