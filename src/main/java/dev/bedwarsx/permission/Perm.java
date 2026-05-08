package dev.bedwarsx.permission;

import org.bukkit.entity.Player;

/**
 * Sistema de permissões completo do BedWarsX.
 * Todas as permissões são verificadas aqui centralmente.
 */
public enum Perm {

    // ─── Admin Geral ──────────────────────────────────────────────────────────
    ADMIN                   ("bedwarsx.admin",               "Acesso total ao BedWarsX"),
    ADMIN_GUI               ("bedwarsx.admin.gui",           "Abrir painel administrativo"),
    ADMIN_RELOAD            ("bedwarsx.admin.reload",        "Recarregar o plugin"),

    // ─── Arena ────────────────────────────────────────────────────────────────
    ARENA_CRIAR             ("bedwarsx.arena.criar",         "Criar arenas"),
    ARENA_DELETAR           ("bedwarsx.arena.deletar",       "Deletar arenas"),
    ARENA_EDITAR            ("bedwarsx.arena.editar",        "Editar configurações de arenas"),
    ARENA_VER               ("bedwarsx.arena.ver",           "Ver informações de arenas"),
    ARENA_FORCAR_INICIO     ("bedwarsx.arena.forcar.inicio", "Forçar início de partida"),
    ARENA_FORCAR_FIM        ("bedwarsx.arena.forcar.fim",    "Forçar fim de partida"),

    // ─── NPC ──────────────────────────────────────────────────────────────────
    NPC_CRIAR               ("bedwarsx.npc.criar",           "Criar NPCs"),
    NPC_DELETAR             ("bedwarsx.npc.deletar",         "Deletar NPCs"),
    NPC_EDITAR              ("bedwarsx.npc.editar",          "Editar NPCs (skin, nome, tipo)"),

    // ─── Jogo ─────────────────────────────────────────────────────────────────
    JOGAR                   ("bedwarsx.jogar",               "Entrar em partidas de BedWars"),
    SPECTATOR               ("bedwarsx.spectator",           "Assistir partidas"),
    ENTRAR_EM_JOGO          ("bedwarsx.entrar",              "Usar /bw entrar"),
    SAIR_DO_JOGO            ("bedwarsx.sair",                "Usar /bw sair"),

    // ─── VIP ──────────────────────────────────────────────────────────────────
    VIP                     ("bedwarsx.vip",                 "Benefícios VIP"),
    VIP_ENTRAR_CHEIO        ("bedwarsx.vip.entrar.cheio",    "Entrar em arenas cheias"),
    VIP_SKIP_FILA           ("bedwarsx.vip.fila",            "Pular fila de espera"),
    VIP_COSMETICO           ("bedwarsx.vip.cosmetico",       "Usar cosméticos VIP"),

    // ─── MVP / Premium ────────────────────────────────────────────────────────
    MVP                     ("bedwarsx.mvp",                 "Benefícios MVP"),
    MVP_COSMETICO           ("bedwarsx.mvp.cosmetico",       "Usar cosméticos MVP"),
    MVP_TRAIL               ("bedwarsx.mvp.trail",           "Usar trails de partícula"),

    // ─── Stats ────────────────────────────────────────────────────────────────
    STATS_VER               ("bedwarsx.stats.ver",           "Ver próprias estatísticas"),
    STATS_VER_OUTROS        ("bedwarsx.stats.outros",        "Ver estatísticas de outros jogadores"),
    STATS_RESETAR           ("bedwarsx.stats.resetar",       "Resetar estatísticas"),

    // ─── Shop ─────────────────────────────────────────────────────────────────
    SHOP_EXPERIMENTAL       ("bedwarsx.shop.experimental",   "Acessar loja experimental"),
    SHOP_DESCONTO_VIP       ("bedwarsx.shop.desconto.vip",   "Desconto VIP na loja"),

    // ─── Bypass ───────────────────────────────────────────────────────────────
    BYPASS_COOLDOWN         ("bedwarsx.bypass.cooldown",     "Ignorar cooldowns"),
    BYPASS_PROTECAO         ("bedwarsx.bypass.protecao",     "Ignorar proteção do lobby");

    // ─────────────────────────────────────────────────────────────────────────

    private final String node;
    private final String descricao;

    Perm(String node, String descricao) {
        this.node = node;
        this.descricao = descricao;
    }

    public String getNode() {
        return node;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Verifica se o jogador tem essa permissão (ou bedwarsx.admin). */
    public boolean has(Player player) {
        return player.hasPermission(node) || player.hasPermission("bedwarsx.admin");
    }

    /** Verifica sem o fallback de admin. */
    public boolean hasExact(Player player) {
        return player.hasPermission(node);
    }

    @Override
    public String toString() {
        return node;
    }

    // ─── Grupos de permissão para plugin.yml ──────────────────────────────────

    public static final String[] ADMIN_NODES = {
        "bedwarsx.admin",
        "bedwarsx.admin.gui",
        "bedwarsx.admin.reload",
        "bedwarsx.arena.criar",
        "bedwarsx.arena.deletar",
        "bedwarsx.arena.editar",
        "bedwarsx.arena.ver",
        "bedwarsx.arena.forcar.inicio",
        "bedwarsx.arena.forcar.fim",
        "bedwarsx.npc.criar",
        "bedwarsx.npc.deletar",
        "bedwarsx.npc.editar",
        "bedwarsx.stats.resetar",
        "bedwarsx.stats.outros",
        "bedwarsx.bypass.cooldown",
        "bedwarsx.bypass.protecao"
    };

    public static final String[] VIP_NODES = {
        "bedwarsx.jogar",
        "bedwarsx.spectator",
        "bedwarsx.entrar",
        "bedwarsx.sair",
        "bedwarsx.stats.ver",
        "bedwarsx.vip",
        "bedwarsx.vip.entrar.cheio",
        "bedwarsx.vip.fila",
        "bedwarsx.vip.cosmetico",
        "bedwarsx.shop.experimental",
        "bedwarsx.shop.desconto.vip"
    };

    public static final String[] JOGADOR_NODES = {
        "bedwarsx.jogar",
        "bedwarsx.spectator",
        "bedwarsx.entrar",
        "bedwarsx.sair",
        "bedwarsx.stats.ver"
    };
}
