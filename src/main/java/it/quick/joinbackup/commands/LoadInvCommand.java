package it.quick.joinbackup.commands;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import it.quick.joinbackup.JoinBackup;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import it.quick.joinbackup.database.DatabaseManager;
import it.quick.joinbackup.database.InventoryManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadInvCommand implements CommandExecutor, Listener {

    private static final Logger logger = Logger.getLogger("JoinBackup");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo i giocatori possono usare questo comando.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length >= 1 && args[0].equalsIgnoreCase("loadinv")) {
            if (args.length < 2) {
                player.sendMessage("§cUso del comando non valido. Usa: /inventorybackupper loadinv <player>");
                return false;
            }

            Player targetPlayer = Bukkit.getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage("§cIl giocatore specificato non è online o non esiste.");
                return false;
            }

            try {
                openInventoryGui(player, targetPlayer);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Errore durante l'apertura della GUI per il giocatore " + targetPlayer.getName(), e);
                player.sendMessage("§cSi è verificato un errore durante il caricamento dell'inventario.");
            }
            return true;
        }

        player.sendMessage("§cUso del comando non valido. Usa: /inventorybackupper loadinv <player>");
        return false;
    }

    private void openInventoryGui(Player player, Player targetPlayer) {
        Gui gui = Gui.gui()
                .title(Component.text("§aJOIN BACKUP's " + targetPlayer.getName()))
                .rows(1)
                .create();

        gui.setItem(4, ItemBuilder.from(Material.CHEST)
                .name(Component.text("§aInventario di " + targetPlayer.getName()))
                .asGuiItem(event -> openBackupListGui(player, targetPlayer)));

        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.open(player);
    }

    private void openBackupListGui(Player player, Player targetPlayer) {
        Gui backupGui = Gui.gui()
                .title(Component.text("§aBackup di " + targetPlayer.getName()))
                .rows(3)
                .create();

        int backupCount = DatabaseManager.getInstance().getBackupCount(targetPlayer.getUniqueId().toString());
        int maxBackups = JoinBackup.getMaxBackups();

        if (backupCount == 0) {
            player.sendMessage("§cNon ci sono backup disponibili per questo giocatore.");
            return;
        }

        for (int i = 0; i < Math.min(backupCount, maxBackups); i++) {
            int finalI = i;
            backupGui.setItem(i, ItemBuilder.from(Material.PAPER)
                    .name(Component.text("§aBackup #" + (i + 1)))
                    .asGuiItem(event -> openBackupInventoryGui(player, targetPlayer, finalI)));
        }

        backupGui.setDefaultClickAction(event -> event.setCancelled(true));
        backupGui.open(player);
    }


    private void openBackupInventoryGui(Player player, Player targetPlayer, int backupIndex) {
        Gui inventoryGui = Gui.gui()
                .title(Component.text("§aInventario Backup #" + (backupIndex + 1)))
                .rows(6)
                .create();

        String playerUUID = targetPlayer.getUniqueId().toString();
        String inventoryBase64 = DatabaseManager.getInstance().getBackupInventory(playerUUID, backupIndex);
        String armorBase64 = DatabaseManager.getInstance().getBackupArmor(playerUUID, backupIndex);

        ItemStack[] inventoryItems = InventoryManager.deserializeInventory(inventoryBase64);
        ItemStack[] armorItems = InventoryManager.deserializeInventory(armorBase64);

        for (int i = 0; i < 36; i++) {
            if (i < inventoryItems.length && inventoryItems[i] != null && inventoryItems[i].getType() != Material.AIR) {
                inventoryGui.setItem(i, ItemBuilder.from(inventoryItems[i]).asGuiItem());
            }
        }

        for (int i = 36; i < 45; i++) {
            inventoryGui.setItem(i, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .name(Component.text(" "))
                    .asGuiItem());
        }

        int[] armorSlots = {45, 46, 47, 48};
        for (int i = 0; i < 4; i++) {
            if (i < armorItems.length && armorItems[i] != null && armorItems[i].getType() != Material.AIR) {
                inventoryGui.setItem(armorSlots[i], ItemBuilder.from(armorItems[i]).asGuiItem());
            }
        }

        inventoryGui.setDefaultClickAction(event -> event.setCancelled(true));
        inventoryGui.open(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Gui) {
            event.setCancelled(true);
        }
    }
}
