package me.blaze.blazeAnticheat.data;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BanManager {
    private final BlazeAnticheat plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<UUID, BanInfo> bans = new HashMap<>();

    public BanManager(BlazeAnticheat plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bans.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.contains("bans")) {
            for (String id : cfg.getConfigurationSection("bans").getKeys(false)) {
                UUID u = UUID.fromString(id);
                long e = cfg.getLong("bans." + id + ".expiry");
                if (e != -1 && System.currentTimeMillis() > e) {
                    cfg.set("bans." + id, null);
                    continue;
                }
                bans.put(u, new BanInfo(cfg.getString("bans." + id + ".reason"), e, cfg.getString("bans." + id + ".by")));
            }
        }
        save();
    }

    private void save() {
        try { cfg.save(file); } catch (IOException ignored) {}
    }

    public boolean isBanned(UUID u) {
        BanInfo i = bans.get(u);
        if (i == null) return false;
        if (i.expiry != -1 && System.currentTimeMillis() > i.expiry) {
            bans.remove(u);
            cfg.set("bans." + u, null);
            save();
            return false;
        }
        return true;
    }

    public BanInfo getBanInfo(UUID u) { return bans.get(u); }

    public void ban(UUID u, String r, String b, long ms) {
        long e = ms == -1 ? -1 : System.currentTimeMillis() + ms;
        BanInfo i = new BanInfo(r, e, b);
        bans.put(u, i);
        cfg.set("bans." + u + ".reason", r);
        cfg.set("bans." + u + ".expiry", e);
        cfg.set("bans." + u + ".by", b);
        save();
        
        org.bukkit.entity.Player p = Bukkit.getPlayer(u);
        if (p != null) {
            String m = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ban.kick-screen")
                    .replace("{reason}", r)
                    .replace("{admin}", b)
                    .replace("{expires}", TimeUtil.format(ms)));
            Bukkit.getScheduler().runTask(plugin, () -> p.kickPlayer(m));
        }
    }

    public void unban(UUID u) {
        bans.remove(u);
        cfg.set("bans." + u, null);
        save();
    }

    public Map<UUID, BanInfo> getBans() { return bans; }
    
    public void reloadConfig() {
        bans.clear();
        load();
    }


    public static class BanInfo {
        public final String reason;
        public final long expiry;
        public final String by;
        public BanInfo(String r, long e, String b) { this.reason = r; this.expiry = e; this.by = b; }
    }
}
