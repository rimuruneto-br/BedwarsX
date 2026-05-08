package dev.bedwarsx.gui.lobby;

import dev.bedwarsx.gui.BedWarsGui;
import dev.bedwarsx.gui.GuiBuilder;
import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI de configurações pessoais do jogador (chat, efeitos, etc.)
 */
public class PlayerSettingsGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Player player;
    private final Inventory inventory;
    private final FileConfiguration prefs;

    public PlayerSettingsGui(BedWarsX plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.prefs = plugin.getConfigManager().getConfig("stats");

        String base = "players." + player.getUniqueId() + ".";
        boolean chatOn    = prefs.getBoolean(base + "chat", true);
        boolean popupsOn  = prefs.getBoolean(base + "popups", true);
        boolean soundsOn  = prefs.getBoolean(base + "sounds", true);
        boolean particlesOn = prefs.getBoolean(base + "particles", true);

        GuiBuilder builder = GuiBuilder.criar("&8✦ &7⚙ &lConfigurações do Jogador", 4);
        builder.borda(GuiBuilder.BORDER_BLACK);
        for (int col = 1; col < 8; col++) {
            builder.set(col, GuiBuilder.glass((short)7, " "));
            builder.set(27 + col, GuiBuilder.BORDER_DARK);
        }
        for (int l = 1; l < 3; l++) {
            builder.set(l * 9 + 1, GuiBuilder.BORDER_DARK);
            builder.set(l * 9 + 7, GuiBuilder.BORDER_DARK);
        }

        builder.set(4, ItemUtil.create(Material.REDSTONE_COMPARATOR, 1,
                "&7⚙ &lConfigurações",
                "&7Personalize sua experiência.",
                "&7As configurações são salvas automaticamente."));

        builder.set(10, buildToggle(chatOn, "&aMensagens de Arena",
                "&7Ativa/desativa mensagens de kill,",
                "&7cama destruída, etc."));

        builder.set(12, buildToggle(popupsOn, "&aTítulos de Tela",
                "&7Ativa/desativa títulos da tela",
                "&7(renascer, vitória, etc.)"));

        builder.set(14, buildToggle(soundsOn, "&aSons do Jogo",
                "&7Ativa/desativa sons especiais",
                "&7de BedWars."));

        builder.set(16, buildToggle(particlesOn, "&aPartículas",
                "&7Ativa/desativa efeitos de",
                "&7partículas no jogo."));

        builder.set(27, GuiBuilder.SETA_FECHAR);
        builder.preencher(GuiBuilder.BORDER_DARK);
        this.inventory = builder.build();
    }

    private ItemStack buildToggle(boolean on, String nome, String... lore) {
        String[] fullLore = new String[lore.length + 2];
        System.arraycopy(lore, 0, fullLore, 0, lore.length);
        fullLore[lore.length] = "";
        fullLore[lore.length + 1] = on ? "&aAtivado &8— clique para desativar" : "&cDesativado &8— clique para ativar";
        return ItemUtil.create(
                on ? Material.WOOL : Material.STAINED_GLASS_PANE,
                1, (short)(on ? 13 : 14), nome, fullLore);
    }

    private void toggle(String key) {
        String path = "players." + player.getUniqueId() + "." + key;
        prefs.set(path, !prefs.getBoolean(path, true));
        plugin.getConfigManager().saveConfig("stats");
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        switch (slot) {
            case 10: toggle("chat");      break;
            case 12: toggle("popups");    break;
            case 14: toggle("sounds");    break;
            case 16: toggle("particles"); break;
            case 27: player.closeInventory(); return;
        }
        plugin.getGuiManager().abrir(player, new PlayerSettingsGui(plugin, player));
    }
}
