package dev.bedwarsx.gui;

import dev.bedwarsx.util.ChatUtil;
import dev.bedwarsx.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Utilitário para construção de GUIs bonitas e padronizadas.
 */
public class GuiBuilder {

    private final Inventory inventory;

    // ─── Bordas e decorações ──────────────────────────────────────────────────
    public static final ItemStack BORDER_DARK  = glass((short) 7,  " ");
    public static final ItemStack BORDER_BLACK = glass((short) 15, " ");
    public static final ItemStack BORDER_CINZA = glass((short) 8,  " ");
    public static final ItemStack BORDER_AZUL  = glass((short) 11, " ");
    public static final ItemStack BORDER_VERDE = glass((short) 13, " ");
    public static final ItemStack BORDER_ROXO  = glass((short) 10, " ");
    public static final ItemStack BORDER_OURO  = glass((short) 4,  " ");
    public static final ItemStack BORDER_VERM  = glass((short) 14, " ");

    public static final ItemStack SETA_VOLTAR = ItemUtil.create(
            Material.ARROW, 1, "&7← &fVoltar", "&7Clique para voltar.");
    public static final ItemStack SETA_FECHAR = ItemUtil.create(
            Material.BARRIER, 1, "&c✖ &fFechar", "&7Fecha este menu.");
    public static final ItemStack PAGINA_ANTERIOR = ItemUtil.create(
            Material.ARROW, 1, "&e« Anterior", "&7Página anterior.");
    public static final ItemStack PROXIMA_PAGINA = ItemUtil.create(
            Material.ARROW, 1, "&eProxima »", "&7Próxima página.");
    public static final ItemStack DIVISOR = glass((short) 7, " ");

    // ─── Construtores ─────────────────────────────────────────────────────────

    public GuiBuilder(String titulo, int linhas) {
        this.inventory = Bukkit.createInventory(null, linhas * 9, ChatUtil.color(titulo));
    }

    public static GuiBuilder criar(String titulo, int linhas) {
        return new GuiBuilder(titulo, linhas);
    }

    // ─── Métodos de preenchimento ──────────────────────────────────────────────

    /** Preenche todos os slots vazios com o item especificado. */
    public GuiBuilder preencher(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, item);
            }
        }
        return this;
    }

    /** Borda completa (primeira e última linha + colunas 0 e 8). */
    public GuiBuilder borda(ItemStack item) {
        int size = inventory.getSize();
        int linhas = size / 9;
        for (int col = 0; col < 9; col++) {
            inventory.setItem(col, item);              // primeira linha
            inventory.setItem(size - 9 + col, item);  // última linha
        }
        for (int linha = 1; linha < linhas - 1; linha++) {
            inventory.setItem(linha * 9, item);        // coluna esquerda
            inventory.setItem(linha * 9 + 8, item);    // coluna direita
        }
        return this;
    }

    /** Borda colorida padrão do BedWarsX (padrão de cores). */
    public GuiBuilder bordaPadrao() {
        return borda(BORDER_DARK);
    }

    /** Define um item em um slot específico. */
    public GuiBuilder set(int slot, ItemStack item) {
        inventory.setItem(slot, item);
        return this;
    }

    /** Define múltiplos slots com o mesmo item. */
    public GuiBuilder set(ItemStack item, int... slots) {
        for (int slot : slots) {
            inventory.setItem(slot, item);
        }
        return this;
    }

    /** Limpa um slot (coloca air). */
    public GuiBuilder limpar(int slot) {
        inventory.setItem(slot, null);
        return this;
    }

    /** Linha de separador horizontal. */
    public GuiBuilder separador(int linha, ItemStack item) {
        for (int col = 0; col < 9; col++) {
            inventory.setItem(linha * 9 + col, item);
        }
        return this;
    }

    /** Botão de voltar no slot 45 (última linha esquerda). */
    public GuiBuilder botaoVoltar() {
        return set(inventory.getSize() - 9, SETA_VOLTAR);
    }

    /** Botão de fechar no slot mais à direita da última linha. */
    public GuiBuilder botaoFechar() {
        return set(inventory.getSize() - 1, SETA_FECHAR);
    }

    /** Botões de paginação. */
    public GuiBuilder paginacao(boolean temAnterior, boolean temProxima) {
        if (temAnterior) set(inventory.getSize() - 9, PAGINA_ANTERIOR);
        if (temProxima)  set(inventory.getSize() - 1, PROXIMA_PAGINA);
        return this;
    }

    /** Cabeçalho decorativo com título no centro. */
    public GuiBuilder cabecalho(String titulo, ItemStack icone) {
        // Linha 0: borda
        for (int col = 0; col < 9; col++) {
            inventory.setItem(col, BORDER_DARK);
        }
        // Centro com ícone
        ItemStack iconeNomeado = icone.clone();
        ItemMeta meta = iconeNomeado.getItemMeta();
        meta.setDisplayName(ChatUtil.color(titulo));
        iconeNomeado.setItemMeta(meta);
        inventory.setItem(4, iconeNomeado);
        return this;
    }

    public Inventory build() {
        return inventory;
    }

    // ─── Helpers estáticos ────────────────────────────────────────────────────

    public static ItemStack glass(short data, String nome) {
        return ItemUtil.create(Material.STAINED_GLASS_PANE, 1, data, ChatUtil.color(nome), null);
    }

    public static ItemStack criarBotao(Material mat, String nome, String... lore) {
        return ItemUtil.create(mat, 1, nome, lore);
    }

    public static ItemStack criarBotaoToggle(boolean ativo, String nome) {
        Material mat = ativo ? Material.WOOL : Material.STAINED_GLASS_PANE;
        short data = ativo ? (short) 13 : (short) 14;
        String status = ativo ? "&a● ATIVADO" : "&c● DESATIVADO";
        return ItemUtil.create(mat, 1, data, nome, Arrays.asList(ChatUtil.color(status)));
    }

    public static ItemStack criarInfo(String titulo, String... linhas) {
        return ItemUtil.create(Material.PAPER, 1, titulo, linhas);
    }

    /** Item de cabeça de jogador (skull) com nome. */
    public static ItemStack skull(String dono, String nome, String... lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) skull.getItemMeta();
        meta.setOwner(dono);
        meta.setDisplayName(ChatUtil.color(nome));
        if (lore.length > 0) {
            java.util.List<String> loreList = new java.util.ArrayList<>();
            for (String l : lore) loreList.add(ChatUtil.color(l));
            meta.setLore(loreList);
        }
        skull.setItemMeta(meta);
        return skull;
    }
}
