package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FreezeCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;
    private final Set<UUID> frozen = new java.util.HashSet<>();

    public FreezeCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.freeze.enabled")) return true;

        String perm = plugin.getConfig().getString("permissions.freeze");
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-freeze"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }

        if (frozen.contains(target.getUniqueId())) {
            frozen.remove(target.getUniqueId());
            target.setWalkSpeed(0.2f);
            target.setFlySpeed(0.1f);
            target.closeInventory();
            sender.sendMessage(msg("messages.unfreeze-success").replace("{player}", target.getName()));
            target.sendMessage(msg("messages.unfrozen"));
        } else {
            frozen.add(target.getUniqueId());
            target.setWalkSpeed(0f);
            target.setFlySpeed(0f);
            openFreezeMenu(target);
            String message = plugin.getConfig().getString("freeze-menu.message");
            String discord = plugin.getConfig().getString("freeze-menu.discord");
            message = message.replace("{discord}", discord);
            target.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            sender.sendMessage(msg("messages.freeze-success").replace("{player}", target.getName()));
        }

        return true;
    }

    public void openFreezeMenu(Player p) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("freeze-menu.title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("freeze-menu.barrier-display")));
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("freeze-menu.barrier-lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(lore);
        barrier.setItemMeta(meta);

        for (int i = 0; i < 54; i++) {
            inv.setItem(i, barrier);
        }

        p.openInventory(inv);
    }

    public boolean isFrozen(UUID id) {
        return frozen.contains(id);
    }

    public void unfreeze(UUID id) {
        frozen.remove(id);
        Player p = Bukkit.getPlayer(id);
        if (p != null) {
            p.setWalkSpeed(0.2f);
            p.setFlySpeed(0.1f);
            p.closeInventory();
        }
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
