package it.quick.joinbackup.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private static final Logger logger = Logger.getLogger("JoinBackup");
    private static final DatabaseManager instance = new DatabaseManager();
    private HikariDataSource dataSource;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        return instance;
    }

    public void initializeDatabase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:plugins/JoinBackup/database.db");
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);

        logger.info("Database inizializzato.");
        createInventoryTable();
    }

    private void createInventoryTable() {
        String query = "CREATE TABLE IF NOT EXISTS inventories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_uuid CHAR(36) UNIQUE, " +
                "inventory_base TEXT, " +
                "armor_base TEXT, " +
                "timestamp INTEGER)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
            logger.info("Tabella 'inventories' creata o già esistente.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nella creazione della tabella del database", e);
        }
    }

    public void saveInventoryToDatabase(String playerUUID, String inventoryBase, String armorBase, long timestamp) {
        String query = "INSERT INTO inventories (player_uuid, inventory_base, armor_base, timestamp) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid) DO UPDATE SET inventory_base = excluded.inventory_base, " +
                "armor_base = excluded.armor_base, timestamp = excluded.timestamp";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            statement.setString(2, inventoryBase);
            statement.setString(3, armorBase);
            statement.setLong(4, timestamp);
            statement.executeUpdate();
            logger.info("Inventario salvato nel database per: " + playerUUID);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel salvataggio DB per: " + playerUUID, e);
        }
    }

    public int getBackupCount(String playerUUID) {
        String query = "SELECT COUNT(*) FROM inventories WHERE player_uuid = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel recupero del numero di backup", e);
        }
        return 0;
    }



    public String getBackupInventory(String playerUUID, int backupIndex) {
        String query = "SELECT inventory_base FROM inventories WHERE player_uuid = ? LIMIT 1 OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            statement.setInt(2, backupIndex);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("inventory_base");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel recupero dell'inventario di backup", e);
        }
        return null;
    }

    public String getBackupArmor(String playerUUID, int backupIndex) {
        String query = "SELECT armor_base FROM inventories WHERE player_uuid = ? LIMIT 1 OFFSET ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            statement.setInt(2, backupIndex);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getString("armor_base");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nel recupero dell'armatura di backup", e);
        }
        return null;
    }

    public void deleteOldestBackup(String playerUUID) {
        String query = "DELETE FROM inventories WHERE id = (SELECT MIN(id) FROM inventories WHERE player_uuid = ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerUUID);
            statement.executeUpdate();
            logger.info("Backup più vecchio eliminato per: " + playerUUID);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Errore nell'eliminazione del backup più vecchio", e);
        }
    }


    public void closeDatabase() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Connessione al database chiusa.");
        }
    }
}
