package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.data.BanManager;
import me.blaze.blazeAnticheat.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class BanListCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public BanListCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.banlist.enabled")) return true;
        
        String p = plugin.getConfig().getString("permissions.banlist");
        if (!sender.hasPermission(p)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        Map<UUID, BanManager.BanInfo> bans = plugin.getBanManager().getBans();
        if (bans.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No active bans found.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "--- Active Ban List ---");
        for (Map.Entry<UUID, BanManager.BanInfo> entry : bans.entrySet()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(entry.getKey());
            BanManager.BanInfo info = entry.getValue();
            long r = info.expiry == -1 ? -1 : info.expiry - System.currentTimeMillis();
            
            sender.sendMessage(ChatColor.YELLOW + (op.getName() != null ? op.getName() : entry.getKey().toString()));
            sender.sendMessage(ChatColor.GRAY + " Reason: " + ChatColor.WHITE + info.reason);
            sender.sendMessage(ChatColor.GRAY + " By: " + ChatColor.WHITE + info.by);
            sender.sendMessage(ChatColor.GRAY + " Expires: " + ChatColor.WHITE + TimeUtil.format(r));
            sender.sendMessage("");
        }
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
