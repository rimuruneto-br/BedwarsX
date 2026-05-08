package dev.bedwarsx.gui.admin;

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
 * GUI de confirmação reutilizável.
 */
public class ConfirmGui implements BedWarsGui {

    private final BedWarsX plugin;
    private final Inventory inventory;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public ConfirmGui(BedWarsX plugin, Player player, String titulo, Runnable onConfirm, Runnable onCancel) {
        this.plugin = plugin;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        GuiBuilder builder = GuiBuilder.criar("&8⚠ &cConfirmação", 3);
        builder.borda(GuiBuilder.BORDER_BLACK);

        // ── Pergunta central
        builder.set(13, ItemUtil.create(Material.BARRIER, 1,
                "&c⚠ &lConfirmar Ação",
                titulo,
                "",
                "&7Essa ação não pode ser desfeita!"));

        // ── Confirmar
        builder.set(11, ItemUtil.create(Material.WOOL, 1, (short)13,
                "&a✔ &lConfirmar",
                "&7Clique para confirmar a ação."));

        // ── Cancelar
        builder.set(15, ItemUtil.create(Material.WOOL, 1, (short)14,
                "&c✖ &lCancelar",
                "&7Clique para cancelar."));

        // Preencher resto
        builder.preencher(GuiBuilder.BORDER_DARK);

        this.inventory = builder.build();
    }

    @Override
    public Inventory getInventory() { return inventory; }

    @Override
    public void onClick(Player player, int slot, ItemStack item, InventoryClickEvent event) {
        if (slot == 11 && onConfirm != null) {
            player.closeInventory();
            onConfirm.run();
        } else if (slot == 15 && onCancel != null) {
            player.closeInventory();
            onCancel.run();
        }
    }
}
