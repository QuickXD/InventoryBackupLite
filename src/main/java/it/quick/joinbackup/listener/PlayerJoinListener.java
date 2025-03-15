package it.quick.joinbackup.listener;

import it.quick.joinbackup.database.InventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerJoinListener implements Listener {
    private static final Logger logger = Logger.getLogger("JoinBackup");
    private static final ExecutorService executor = Executors.newFixedThreadPool(2); // max 2 thread per non sovraccaricare il server

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        executor.execute(() -> {
            try {
                logger.info("Salvataggio inventario per: " + player.getName());
                InventoryManager.saveInventory(player);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Errore salvataggio inventario per " + player.getName(), ex);
            }
        });
    }
}
