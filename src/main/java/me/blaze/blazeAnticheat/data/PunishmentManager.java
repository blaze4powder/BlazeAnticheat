package me.blaze.blazeAnticheat.data;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PunishmentManager {
    private final BlazeAnticheat plugin;

    public PunishmentManager(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    public void punish(Player t, String r, String b) {
        String path = "punishments.offenses." + r.toLowerCase().replace(" ", "_");
        if (!plugin.getConfig().contains(path)) path = "punishments.offenses.cheating";

        String a = plugin.getConfig().getString(path + ".action", "kick");
        String dur = plugin.getConfig().getString(path + ".time", "perm");
        boolean w = plugin.getConfig().getBoolean(path + ".wipe", false);

        if (w) wipe(t);

        long ms = TimeUtil.parse(dur);

        if (a.equalsIgnoreCase("ban")) {
            plugin.getBanManager().ban(t.getUniqueId(), r, b, ms);
            if (plugin.getConfig().getBoolean("broadcast.ban")) {
                broadcast("messages.ban-success", t.getName(), r, TimeUtil.format(ms));
            }
        } else if (a.equalsIgnoreCase("mute")) {
            plugin.getMuteManager().mute(t.getUniqueId(), r, b, ms);
            if (plugin.getConfig().getBoolean("broadcast.mute")) {
                broadcast("messages.mute-success", t.getName(), r, TimeUtil.format(ms));
            }
        } else if (a.equalsIgnoreCase("vcmute")) {
            plugin.getMuteManager().vcMute(t.getUniqueId(), r, b, ms);
            if (plugin.getConfig().getBoolean("broadcast.vcmute")) {
                broadcast("messages.vcmute-success", t.getName(), r, TimeUtil.format(ms));
            }
        } else if (a.equalsIgnoreCase("allmute")) {
            plugin.getMuteManager().mute(t.getUniqueId(), r, b, ms);
            plugin.getMuteManager().vcMute(t.getUniqueId(), r, b, ms);
            if (plugin.getConfig().getBoolean("broadcast.allmute")) {
                broadcast("messages.mute-success", t.getName(), r, TimeUtil.format(ms));
            }
        } else {
            t.kickPlayer(ChatColor.RED + "Kicked: " + r);
        }
    }

    private void wipe(Player t) {
        t.getInventory().clear();
        t.getEnderChest().clear();
        t.setExp(0);
        t.setLevel(0);
        t.teleport(t.getWorld().getSpawnLocation());
    }

    private void broadcast(String path, String p, String r, String d) {
        String pre = plugin.getConfig().getString("settings.prefix");
        String m = plugin.getConfig().getString(path)
                .replace("{player}", p)
                .replace("{reason}", r)
                .replace("{duration}", d);
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', pre + m));
    }
    
    public void reloadPunishments() {
    }
}
