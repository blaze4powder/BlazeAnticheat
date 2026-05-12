package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KbTestCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public KbTestCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.kbtest.enabled")) return true;
        
        String pPerm = plugin.getConfig().getString("permissions.kbtest");
        if (!sender.hasPermission(pPerm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-kbtest"));
            return true;
        }

        Player t = Bukkit.getPlayer(args[0]);
        if (t == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }

        Vector v = new Vector(0.5, 0.4, 0.5);
        t.setVelocity(v);

        sender.sendMessage(msg("messages.kb-start").replace("{player}", t.getName()));
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            double m = t.getVelocity().length();
            String status = m < 0.12 ? plugin.getConfig().getString("messages.kb-status-sus") : plugin.getConfig().getString("messages.kb-status-normal");
            
            sender.sendMessage(msg("messages.kb-result")
                    .replace("{player}", t.getName())
                    .replace("{m}", String.format("%.3f", m))
                    .replace("{status}", ChatColor.translateAlternateColorCodes('&', status)));
        }, 5L);

        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
