package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PopTotemCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public PopTotemCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.poptotem.enabled")) return true;
        
        String pPerm = plugin.getConfig().getString("permissions.poptotem");
        if (!sender.hasPermission(pPerm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-poptotem"));
            return true;
        }

        Player t = Bukkit.getPlayer(args[0]);
        if (t == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }

        ItemStack oh = t.getInventory().getItemInOffHand();
        ItemStack mh = t.getInventory().getItemInMainHand();

        boolean h = (oh != null && oh.getType() == Material.TOTEM_OF_UNDYING) ||
                     (mh != null && mh.getType() == Material.TOTEM_OF_UNDYING);

        if (h) {
            sender.sendMessage(msg("messages.totem-success").replace("{player}", t.getName()));
            t.setHealth(0);
        } else {
            sender.sendMessage(msg("messages.totem-fail").replace("{player}", t.getName()));
        }

        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
