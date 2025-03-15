package it.quick.joinbackup.commands;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryBackupperCommand implements CommandExecutor, Listener {

    private static final Logger logger = Logger.getLogger("JoinBackup");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.valueOf(Component.text("§cSolo i giocatori possono usare questo comando.")));
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 2 && args[0].equalsIgnoreCase("loadinv")) {
            Player targetPlayer = player.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(String.valueOf(Component.text("§cIl giocatore specificato non è online.").color(TextColor.color(255, 0, 0))));
                return false;
            }
            try {
                openInventoryGui(player, targetPlayer);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Errore durante l'apertura della GUI per il giocatore " + targetPlayer.getName(), e);
                player.sendMessage(String.valueOf(Component.text("§cSi è verificato un errore durante il caricamento dell'inventario.").color(TextColor.color(255, 0, 0))));
            }
            return true;
        }

        player.sendMessage(String.valueOf(Component.text("§cUso del comando non valido. Usa /inventorybackupper loadinv <player>").color(TextColor.color(255, 0, 0))));
        return false;
    }

    private void openInventoryGui(Player player, Player targetPlayer) {
        Gui gui = Gui.gui()
                .title(Component.text("§aGUI di Inventario per " + targetPlayer.getName()))
                .rows(1)  // 1 riga di slot
                .create();

        gui.setItem(4, ItemBuilder.from(Material.GRASS_BLOCK)
                .setName("§aInventario di " + targetPlayer.getName())
                .asGuiItem());

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.open(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory != null && inventory.getHolder() instanceof Gui) {
            event.setCancelled(true);
        }
    }
}
