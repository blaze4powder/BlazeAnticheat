package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllunmuteCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public AllunmuteCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.allunmute.enabled")) return true;
        
        String perm = plugin.getConfig().getString("permissions.allunmute");
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        int unmutedCount = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
                plugin.getMuteManager().unmute(player.getUniqueId());
                unmutedCount++;
            }
            if (plugin.getMuteManager().isVCMuted(player.getUniqueId())) {
                plugin.getMuteManager().vcUnmute(player.getUniqueId());
                unmutedCount++;
            }
        }
        
        sender.sendMessage(msg("messages.allunmute-success").replace("{count}", String.valueOf(unmutedCount)));
        Bukkit.broadcastMessage(msg("messages.allunmute-broadcast"));
        
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
