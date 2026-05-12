package me.blaze.blazeAnticheat.data;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DupeManager {
    private final BlazeAnticheat plugin;
    private final File dupesFile;
    private FileConfiguration dupesConfig;
    
    private final Map<UUID, Integer> itemUUIDs = new HashMap<>();
    private final Map<UUID, UUID> originalOwners = new HashMap<>();

    public DupeManager(BlazeAnticheat plugin) {
        this.plugin = plugin;
        this.dupesFile = new File(plugin.getDataFolder(), "dupes.yml");
        
        loadDupes();
    }

    private void loadDupes() {
        if (!dupesFile.exists()) {
            try {
                dupesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        dupesConfig = YamlConfiguration.loadConfiguration(dupesFile);
        
        if (dupesConfig.contains("itemUUIDs")) {
            for (String uuidStr : dupesConfig.getConfigurationSection("itemUUIDs").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                int count = dupesConfig.getInt("itemUUIDs." + uuidStr);
                itemUUIDs.put(uuid, count);
            }
        }
        
        if (dupesConfig.contains("originalOwners")) {
            for (String uuidStr : dupesConfig.getConfigurationSection("originalOwners").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                UUID owner = UUID.fromString(dupesConfig.getString("originalOwners." + uuidStr));
                originalOwners.put(uuid, owner);
            }
        }
    }

    public void saveDupes() {
        try {
            dupesConfig.save(dupesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UUID assignItemUUID(ItemStack item, UUID owner) {
        UUID itemUUID = UUID.randomUUID();
        itemUUIDs.put(itemUUID, 1);
        originalOwners.put(itemUUID, owner);
        
        dupesConfig.set("itemUUIDs." + itemUUID.toString(), 1);
        dupesConfig.set("originalOwners." + itemUUID.toString(), owner.toString());
        saveDupes();
        
        return itemUUID;
    }

    public boolean checkDupe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        
        for (String line : meta.getLore()) {
            if (line.startsWith("UUID: ")) {
                try {
                    UUID uuid = UUID.fromString(line.substring(5));
                    int count = itemUUIDs.getOrDefault(uuid, 0);
                    
                    if (count > 1) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        
        return false;
    }

    public void trackItem(ItemStack item, UUID owner) {
        if (item == null) return;
        
        UUID itemUUID = assignItemUUID(item, owner);
        
        ItemMeta meta = item.getItemMeta();
        java.util.List<String> lore = meta.hasLore() ? new java.util.ArrayList<>(meta.getLore()) : new java.util.ArrayList<>();
        lore.add("UUID: " + itemUUID.toString());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void incrementItemCount(UUID itemUUID) {
        int count = itemUUIDs.getOrDefault(itemUUID, 0) + 1;
        itemUUIDs.put(itemUUID, count);
        dupesConfig.set("itemUUIDs." + itemUUID.toString(), count);
        saveDupes();
    }
}
