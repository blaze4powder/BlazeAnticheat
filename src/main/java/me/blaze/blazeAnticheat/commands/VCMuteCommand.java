package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VCMuteCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public VCMuteCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.vcmute.enabled")) return true;
        
        String perm = plugin.getConfig().getString("permissions.vcmute");
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-vcmute"));
            return true;
        }

        Player t = Bukkit.getPlayer(args[0]);
        if (t == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }

        long ms = -1;
        String r = "No reason specified";

        if (args.length >= 2) {
            ms = TimeUtil.parse(args[1]);
            if (ms == -2) {
                sender.sendMessage(msg("messages.invalid-time"));
                return true;
            }
            if (args.length >= 3) {
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) sb.append(args[i]).append(" ");
                r = sb.toString().trim();
            }
        }

        plugin.getMuteManager().vcMute(t.getUniqueId(), r, sender.getName(), ms);
        
        sender.sendMessage(msg("messages.vcmute-success")
                .replace("{player}", t.getName())
                .replace("{reason}", r)
                .replace("{duration}", TimeUtil.format(ms)));
        
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
