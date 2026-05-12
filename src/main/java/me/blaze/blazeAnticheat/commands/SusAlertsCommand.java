package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SusAlertsCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;
    private final Set<UUID> subs = new HashSet<>();

    public SusAlertsCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.susalerts.enabled")) return true;
        
        String p = plugin.getConfig().getString("permissions.susalerts");
        if (!sender.hasPermission(p)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.player-only"));
            return true;
        }
        
        Player admin = (Player) sender;
        UUID id = admin.getUniqueId();

        if (subs.contains(id)) {
            subs.remove(id);
            sender.sendMessage(msg("messages.sus-alerts-toggle-disabled"));
        } else {
            subs.add(id);
            sender.sendMessage(msg("messages.sus-alerts-toggle-enabled"));
        }
        return true;
    }

    public void sendAlert(Player flaggedPlayer, String check, int vl, String details) {
        if (!plugin.getConfig().getBoolean("alerts.enabled")) return;
        
        String format = plugin.getConfig().getString("alerts.format");
        format = format.replace("{player}", flaggedPlayer.getName())
                .replace("{check}", check)
                .replace("{vl}", String.valueOf(vl))
                .replace("{details}", details);
        
        String message = ChatColor.translateAlternateColorCodes('&', format);
        
        boolean playSound = plugin.getConfig().getBoolean("alerts.play-sound");
        String sound = plugin.getConfig().getString("alerts.sound");
        
        for (UUID uuid : subs) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
                if (playSound) {
                    try {
                        p.playSound(p.getLocation(), org.bukkit.Sound.valueOf(sound), 1.0f, 1.0f);
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    public boolean isSubscribed(UUID id) {
        return subs.contains(id);
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }

    public static class SusAlertsListener implements Listener {
        private final SusAlertsCommand cmd;
        public SusAlertsListener(SusAlertsCommand cmd) { this.cmd = cmd; }
        
        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            String p = cmd.plugin.getConfig().getString("permissions.susalerts");
            if (e.getPlayer().hasPermission(p)) cmd.subs.add(e.getPlayer().getUniqueId());
        }
    }
}
