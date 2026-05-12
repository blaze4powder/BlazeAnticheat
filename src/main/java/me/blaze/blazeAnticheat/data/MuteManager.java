package me.blaze.blazeAnticheat.data;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.utils.TimeUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager {
    private final BlazeAnticheat plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<UUID, MuteInfo> mutes = new HashMap<>();
    private final Map<UUID, MuteInfo> vcmutes = new HashMap<>();

    public MuteManager(BlazeAnticheat plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mutes.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        loadSec("mutes", mutes);
        loadSec("vcmutes", vcmutes);
    }

    private void loadSec(String s, Map<UUID, MuteInfo> m) {
        if (!cfg.contains(s)) return;
        for (String id : cfg.getConfigurationSection(s).getKeys(false)) {
            UUID u = UUID.fromString(id);
            long e = cfg.getLong(s + "." + id + ".expiry");
            if (e != -1 && System.currentTimeMillis() > e) {
                cfg.set(s + "." + id, null);
                continue;
            }
            m.put(u, new MuteInfo(cfg.getString(s + "." + id + ".reason"), e, cfg.getString(s + "." + id + ".by")));
        }
        save();
    }

    private void save() {
        try { cfg.save(file); } catch (IOException ignored) {}
    }

    public boolean isMuted(UUID u) { return check(u, mutes, "mutes"); }
    public boolean isVCMuted(UUID u) { return check(u, vcmutes, "vcmutes"); }

    private boolean check(UUID u, Map<UUID, MuteInfo> m, String s) {
        MuteInfo i = m.get(u);
        if (i == null) return false;
        if (i.expiry != -1 && System.currentTimeMillis() > i.expiry) {
            m.remove(u);
            cfg.set(s + "." + u, null);
            save();
            return false;
        }
        return true;
    }

    public MuteInfo getMuteInfo(UUID u) { return mutes.get(u); }
    public MuteInfo getVCMuteInfo(UUID u) { return vcmutes.get(u); }

    public void mute(UUID u, String r, String b, long ms) { add(u, r, b, ms, mutes, "mutes"); }
    public void vcMute(UUID u, String r, String b, long ms) { add(u, r, b, ms, vcmutes, "vcmutes"); }

    private void add(UUID u, String r, String b, long ms, Map<UUID, MuteInfo> m, String s) {
        long e = ms == -1 ? -1 : System.currentTimeMillis() + ms;
        MuteInfo i = new MuteInfo(r, e, b);
        m.put(u, i);
        cfg.set(s + "." + u + ".reason", r);
        cfg.set(s + "." + u + ".expiry", e);
        cfg.set(s + "." + u + ".by", b);
        save();
    }

    public void unmute(UUID u) {
        mutes.remove(u);
        cfg.set("mutes." + u, null);
        save();
    }

    public void vcUnmute(UUID u) {
        vcmutes.remove(u);
        cfg.set("vcmutes." + u, null);
        save();
    }

    public Map<UUID, MuteInfo> getMutes() { return mutes; }
    public Map<UUID, MuteInfo> getVCMutes() { return vcmutes; }
    
    public void reloadConfig() {
        mutes.clear();
        vcmutes.clear();
        load();
    }


    public static class MuteInfo {
        public final String reason;
        public final long expiry;
        public final String by;
        public MuteInfo(String r, long e, String b) { this.reason = r; this.expiry = e; this.by = b; }
    }
}
