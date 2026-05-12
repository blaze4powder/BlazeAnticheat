package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UndoStashCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public UndoStashCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.stash.enabled")) return true;

        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.player-only"));
            return true;
        }

        Player p = (Player) sender;
        String perm = plugin.getConfig().getString("permissions.stash");
        if (!p.hasPermission(perm)) {
            p.sendMessage(msg("messages.no-perm"));
            return true;
        }

        SpawnStashCommand.undoLastStash(p);
        p.sendMessage(msg("messages.stash-undone"));
        
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
