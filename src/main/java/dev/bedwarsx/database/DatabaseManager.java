package dev.bedwarsx.database;

import dev.bedwarsx.game.GamePlayer;
import dev.bedwarsx.main.BedWarsX;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final BedWarsX plugin;
    private Connection connection;
    private final String type;

    public DatabaseManager(BedWarsX plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        connect();
        createTables();
    }

    private void connect() {
        try {
            if (type.equals("MYSQL")) {
                String host = plugin.getConfig().getString("database.host", "localhost");
                int port = plugin.getConfig().getInt("database.port", 3306);
                String db = plugin.getConfig().getString("database.database", "bedwarsx");
                String user = plugin.getConfig().getString("database.username", "root");
                String pass = plugin.getConfig().getString("database.password", "");
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + db + "?autoReconnect=true&useSSL=false",
                        user, pass);
                plugin.getLogger().info("Connected to MySQL database.");
            } else {
                // SQLite
                File dbFile = new File(plugin.getDataFolder(), "data.db");
                dbFile.getParentFile().mkdirs();
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                plugin.getLogger().info("Connected to SQLite database.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS bwx_stats (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(16)," +
                    "wins INT DEFAULT 0," +
                    "losses INT DEFAULT 0," +
                    "kills INT DEFAULT 0," +
                    "deaths INT DEFAULT 0," +
                    "final_kills INT DEFAULT 0," +
                    "beds_broken INT DEFAULT 0," +
                    "games_played INT DEFAULT 0" +
                    ")");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create tables!", e);
        }
    }

    public PlayerStats getStats(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM bwx_stats WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PlayerStats stats = new PlayerStats(uuid);
                stats.setName(rs.getString("name"));
                stats.setWins(rs.getInt("wins"));
                stats.setLosses(rs.getInt("losses"));
                stats.setKills(rs.getInt("kills"));
                stats.setDeaths(rs.getInt("deaths"));
                stats.setFinalKills(rs.getInt("final_kills"));
                stats.setBedsBroken(rs.getInt("beds_broken"));
                stats.setGamesPlayed(rs.getInt("games_played"));
                return stats;
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get stats for " + uuid);
        }
        return new PlayerStats(uuid);
    }

    public void saveStats(GamePlayer gp) {
        try {
            // Upsert
            String sql;
            if (type.equals("MYSQL")) {
                sql = "INSERT INTO bwx_stats (uuid, name, kills, deaths, final_kills, beds_broken, games_played) " +
                        "VALUES (?, ?, ?, ?, ?, ?, 1) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "name=VALUES(name), kills=kills+VALUES(kills), deaths=deaths+VALUES(deaths), " +
                        "final_kills=final_kills+VALUES(final_kills), beds_broken=beds_broken+VALUES(beds_broken), " +
                        "games_played=games_played+1";
            } else {
                sql = "INSERT OR REPLACE INTO bwx_stats (uuid, name, kills, deaths, final_kills, beds_broken, games_played) " +
                        "VALUES (?, ?, " +
                        "COALESCE((SELECT kills FROM bwx_stats WHERE uuid=?),0)+?, " +
                        "COALESCE((SELECT deaths FROM bwx_stats WHERE uuid=?),0)+?, " +
                        "COALESCE((SELECT final_kills FROM bwx_stats WHERE uuid=?),0)+?, " +
                        "COALESCE((SELECT beds_broken FROM bwx_stats WHERE uuid=?),0)+?, " +
                        "COALESCE((SELECT games_played FROM bwx_stats WHERE uuid=?),0)+1)";
            }

            PreparedStatement ps = connection.prepareStatement(sql);
            String uuidStr = gp.getUuid().toString();

            if (type.equals("MYSQL")) {
                ps.setString(1, uuidStr);
                ps.setString(2, gp.getName());
                ps.setInt(3, gp.getKills());
                ps.setInt(4, gp.getDeaths());
                ps.setInt(5, gp.getFinalKills());
                ps.setInt(6, gp.getBedsBroken());
            } else {
                ps.setString(1, uuidStr);
                ps.setString(2, gp.getName());
                ps.setString(3, uuidStr);
                ps.setInt(4, gp.getKills());
                ps.setString(5, uuidStr);
                ps.setInt(6, gp.getDeaths());
                ps.setString(7, uuidStr);
                ps.setInt(8, gp.getFinalKills());
                ps.setString(9, uuidStr);
                ps.setInt(10, gp.getBedsBroken());
                ps.setString(11, uuidStr);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save stats for " + gp.getName() + ": " + e.getMessage());
        }
    }

    public void addWin(UUID uuid) {
        updateField(uuid, "wins", 1);
    }

    public void addLoss(UUID uuid) {
        updateField(uuid, "losses", 1);
    }

    private void updateField(UUID uuid, String field, int amount) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE bwx_stats SET " + field + " = " + field + " + ? WHERE uuid = ?");
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update " + field + " for " + uuid);
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to close database connection.");
        }
    }

    public static class PlayerStats {
        private final UUID uuid;
        private String name = "";
        private int wins, losses, kills, deaths, finalKills, bedsBroken, gamesPlayed;

        public PlayerStats(UUID uuid) { this.uuid = uuid; }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getWins() { return wins; }
        public void setWins(int wins) { this.wins = wins; }
        public int getLosses() { return losses; }
        public void setLosses(int losses) { this.losses = losses; }
        public int getKills() { return kills; }
        public void setKills(int kills) { this.kills = kills; }
        public int getDeaths() { return deaths; }
        public void setDeaths(int deaths) { this.deaths = deaths; }
        public int getFinalKills() { return finalKills; }
        public void setFinalKills(int finalKills) { this.finalKills = finalKills; }
        public int getBedsBroken() { return bedsBroken; }
        public void setBedsBroken(int bedsBroken) { this.bedsBroken = bedsBroken; }
        public int getGamesPlayed() { return gamesPlayed; }
        public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

        public double getKDR() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }

        public double getWinRate() {
            return gamesPlayed == 0 ? 0 : ((double) wins / gamesPlayed) * 100;
        }
    }
}
