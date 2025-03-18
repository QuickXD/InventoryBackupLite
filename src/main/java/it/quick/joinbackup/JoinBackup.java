package it.quick.joinbackup;

import it.quick.joinbackup.database.DatabaseManager;
import it.quick.joinbackup.listener.PlayerJoinListener;
import it.quick.joinbackup.commands.LoadInvCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinBackup extends JavaPlugin {
    private static int maxBackups;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        boolean isJoinEnabled = getConfig().getBoolean("join.enabled", true);
        maxBackups = getConfig().getInt("join.max_backups", 1);

        getLogger().info("maxBackups configurato a: " + maxBackups);


        DatabaseManager.getInstance().initializeDatabase();
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        LoadInvCommand inventoryBackupperCommand = new LoadInvCommand();
        this.getCommand("inventorybackupper").setExecutor(inventoryBackupperCommand);
        getServer().getPluginManager().registerEvents(inventoryBackupperCommand, this);

        getLogger().info("JoinBackup abilitato con configurazioni: join.enabled=" + isJoinEnabled + ", join.max_backups=" + maxBackups);
    }


    @Override
    public void onDisable() {

        DatabaseManager.getInstance().closeDatabase();
        getLogger().info("JoinBackup disabilitato.");
    }

    public static int getMaxBackups() {
        return maxBackups;
    }
}