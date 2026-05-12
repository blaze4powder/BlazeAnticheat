package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnmuteCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public UnmuteCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.unmute.enabled")) return true;
        
        String p = plugin.getConfig().getString("permissions.unmute");
        if (!sender.hasPermission(p)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-unmute"));
            return true;
        }

        Player t = Bukkit.getPlayer(args[0]);
        if (t == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }

        if (!plugin.getMuteManager().isMuted(t.getUniqueId())) {
            sender.sendMessage(msg("messages.not-muted"));
            return true;
        }

        plugin.getMuteManager().unmute(t.getUniqueId());
        sender.sendMessage(msg("messages.unmute-success").replace("{player}", t.getName()));
        
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
