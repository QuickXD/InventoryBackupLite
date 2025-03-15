package it.quick.joinbackup.database;

import it.quick.joinbackup.JoinBackup;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryManager {
    private static final Logger logger = Logger.getLogger("JoinBackup");

    public static void saveInventory(Player player) {
        try {
            String inventoryBase64 = serializeInventory(player.getInventory().getContents());
            String armorBase64 = serializeInventory(player.getInventory().getArmorContents());

            String playerUUID = player.getUniqueId().toString();
            long timestamp = System.currentTimeMillis();

            logger.info("Salvataggio inventario per: " + player.getName());

            int currentBackupCount = DatabaseManager.getInstance().getBackupCount(playerUUID);
            int maxBackups = JoinBackup.getMaxBackups();

            if (currentBackupCount >= maxBackups) {
                DatabaseManager.getInstance().deleteOldestBackup(playerUUID);
            }

            DatabaseManager.getInstance().saveInventoryToDatabase(playerUUID, inventoryBase64, armorBase64, timestamp);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Errore nel salvataggio inventario per " + player.getName(), e);
        }
    }



    public static String serializeInventory(ItemStack[] items) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeInt(items.length);
            for (ItemStack item : items) {
                objectOutputStream.writeObject(item != null ? itemStackToBase64(item) : null);
            }

            objectOutputStream.flush();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errore nella serializzazione dell'inventario.", e);
            return "";
        }
    }

    public static ItemStack[] deserializeInventory(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new ItemStack[36];
        }

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

            int length = objectStream.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                String base64Item = (String) objectStream.readObject();
                items[i] = (base64Item != null) ? itemStackFromBase64(base64Item) : null;
            }
            return items;

        } catch (EOFException e) {
            logger.log(Level.WARNING, "Inventario corrotto: dati incompleti.", e);
            return new ItemStack[36];
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Errore nel deserializzare l'inventario", e);
            return new ItemStack[36];
        }
    }

    private static String itemStackToBase64(ItemStack item) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private static ItemStack itemStackFromBase64(String data) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(data);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }
}
