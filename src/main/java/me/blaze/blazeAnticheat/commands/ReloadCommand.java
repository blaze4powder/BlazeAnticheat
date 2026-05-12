package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public ReloadCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.reload.enabled")) return true;
        
        String p = plugin.getConfig().getString("permissions.reload");
        if (!sender.hasPermission(p)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        plugin.reloadConfig();
        plugin.getPlayerDataManager().saveAll();
        
        plugin.getPunishmentManager().reloadPunishments();
        plugin.getBanManager().reloadConfig();
        plugin.getMuteManager().reloadConfig();
        
        sender.sendMessage(msg("messages.reload"));
        
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
