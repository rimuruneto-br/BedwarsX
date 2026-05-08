package dev.bedwarsx.gui.lobby;

import dev.bedwarsx.main.BedWarsX;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Gerencia os itens da hotbar do lobby para cada jogador.
 */
public class LobbyItemManager {

    private final BedWarsX plugin;

    // ── Slots e itens do lobby ─────────────────────────────────────────────────
    public static final int SLOT_JOGAR    = 0;
    public static final int SLOT_STATS    = 4;
    public static final int SLOT_CONFIGS  = 8;

    public LobbyItemManager(BedWarsX plugin) {
        this.plugin = plugin;
    }

    public void darItensLobby(Player player) {
        player.getInventory().clear();

        // ── Bússola de jogo (slot 0) ───────────────────────────────────────────
        player.getInventory().setItem(SLOT_JOGAR,
                ItemUtil.create(Material.COMPASS, 1,
                        "&6&l» &eJogar BedWars &6«",
                        "&7Clique para selecionar uma arena",
                        "&7e entrar em uma partida!",
                        "",
                        "&eClique direito &7para abrir o menu."));

        // ── Livro de stats (slot 4) ────────────────────────────────────────────
        player.getInventory().setItem(SLOT_STATS,
                ItemUtil.create(Material.BOOK, 1,
                        "&e📊 &lEstatísticas",
                        "&7Veja suas estatísticas de BedWars.",
                        "",
                        "&eClique para abrir."));

        // ── Configurações do jogador (slot 8) ─────────────────────────────────
        player.getInventory().setItem(SLOT_CONFIGS,
                ItemUtil.create(Material.REDSTONE_COMPARATOR, 1,
                        "&7⚙ &lConfigurações",
                        "&7Personalize sua experiência.",
                        "",
                        "&eClique para abrir."));
    }

    public boolean isLobbyItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasDisplayName()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name.contains("Jogar BedWars")
                || name.contains("Estatísticas")
                || name.contains("Configurações");
    }
}
