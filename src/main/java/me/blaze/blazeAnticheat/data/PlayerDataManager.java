package me.blaze.blazeAnticheat.data;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final BlazeAnticheat plugin;
    private final File playerDataFile;
    private FileConfiguration playerDataConfig;
    
    private final Map<UUID, PlayerData> playerDataMap;
    private final Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();
    private final Map<UUID, org.bukkit.Location> savedLocations = new HashMap<>();
    private final Map<UUID, org.bukkit.GameMode> savedGameModes = new HashMap<>();

    public PlayerDataManager(BlazeAnticheat plugin) {
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.playerDataMap = new HashMap<>();
        
        loadPlayerData();
    }

    private void loadPlayerData() {
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        
        if (playerDataConfig.contains("players")) {
            for (String uuidStr : playerDataConfig.getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                int violations = playerDataConfig.getInt("players." + uuidStr + ".violations", 0);
                long lastAlertTime = playerDataConfig.getLong("players." + uuidStr + ".lastAlertTime", 0);
                int totalAlerts = playerDataConfig.getInt("players." + uuidStr + ".totalAlerts", 0);
                List<String> violationHistory = playerDataConfig.getStringList("players." + uuidStr + ".violationHistory");
                
                playerDataMap.put(uuid, new PlayerData(violations, lastAlertTime, totalAlerts, violationHistory));
            }
        }
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;
        
        playerDataConfig.set("players." + uuid.toString() + ".violations", data.violations);
        playerDataConfig.set("players." + uuid.toString() + ".lastAlertTime", data.lastAlertTime);
        playerDataConfig.set("players." + uuid.toString() + ".totalAlerts", data.totalAlerts);
        playerDataConfig.set("players." + uuid.toString() + ".violationHistory", data.violationHistory);
    }

    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            savePlayerData(uuid);
        }
        
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(0, 0, 0, new ArrayList<>()));
    }

    public void removePlayerData(UUID uuid) {
        savePlayerData(uuid);
        playerDataMap.remove(uuid);
    }
    
    public void wipePlayerData(UUID uuid) {
        playerDataMap.put(uuid, new PlayerData(0, 0, 0, new ArrayList<>()));
        playerDataConfig.set("players." + uuid.toString(), null);
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void resetStats(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        data.violations = 0;
        data.totalAlerts = 0;
        data.lastAlertTime = 0;
        data.violationHistory.clear();
        data.lastTarget = null;
        savePlayerData(uuid);
    }
    
    public void saveInventory(Player p) {
        savedInventories.put(p.getUniqueId(), p.getInventory().getContents());
        savedArmor.put(p.getUniqueId(), p.getInventory().getArmorContents());
        savedLocations.put(p.getUniqueId(), p.getLocation());
        savedGameModes.put(p.getUniqueId(), p.getGameMode());
        p.getInventory().clear();
    }
    
    public void restoreInventory(Player p) {
        if (savedInventories.containsKey(p.getUniqueId())) {
            p.getInventory().setContents(savedInventories.get(p.getUniqueId()));
            savedInventories.remove(p.getUniqueId());
        }
        if (savedArmor.containsKey(p.getUniqueId())) {
            p.getInventory().setArmorContents(savedArmor.get(p.getUniqueId()));
            savedArmor.remove(p.getUniqueId());
        }
        if (savedLocations.containsKey(p.getUniqueId())) {
            p.teleport(savedLocations.get(p.getUniqueId()));
            savedLocations.remove(p.getUniqueId());
        }
        if (savedGameModes.containsKey(p.getUniqueId())) {
            p.setGameMode(savedGameModes.get(p.getUniqueId()));
            savedGameModes.remove(p.getUniqueId());
        }
    }

    public static class PlayerData {
        public int violations;
        public long lastAlertTime;
        public int totalAlerts;
        public UUID lastTarget;
        public List<String> violationHistory;
        
        public PlayerData(int violations, long lastAlertTime, int totalAlerts, List<String> violationHistory) {
            this.violations = violations;
            this.lastAlertTime = lastAlertTime;
            this.totalAlerts = totalAlerts;
            this.violationHistory = violationHistory;
        }
    }
}
