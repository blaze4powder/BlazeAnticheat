package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.util.UUID;

public class WipeCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;

    public WipeCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.wipe.enabled")) return true;

        String p = plugin.getConfig().getString("permissions.wipe");
        if (!sender.hasPermission(p)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-wipe"));
            return true;
        }

        Player t = Bukkit.getPlayer(args[0]);
        OfflinePlayer offlineT = Bukkit.getOfflinePlayer(args[0]);
        
        if (t == null && !offlineT.hasPlayedBefore()) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }
        
        UUID targetUuid = offlineT.getUniqueId();
        String targetName = offlineT.getName();

        boolean wipeInventory = plugin.getConfig().getBoolean("commands.wipe.inventory", true);
        boolean wipeEnderChest = plugin.getConfig().getBoolean("commands.wipe.ender-chest", true);
        boolean wipeMoney = plugin.getConfig().getBoolean("commands.wipe.money", false);
        boolean wipePlayerData = plugin.getConfig().getBoolean("commands.wipe.player-data", false);
        boolean wipePosition = plugin.getConfig().getBoolean("commands.wipe.position", false);
        boolean wipeStats = plugin.getConfig().getBoolean("commands.wipe.stats", false);
        boolean resetHealth = plugin.getConfig().getBoolean("commands.wipe.health", true);
        boolean resetHunger = plugin.getConfig().getBoolean("commands.wipe.hunger", true);
        boolean resetExperience = plugin.getConfig().getBoolean("commands.wipe.experience", true);
        boolean clearBans = plugin.getConfig().getBoolean("commands.wipe.clear-bans", true);
        boolean clearMutes = plugin.getConfig().getBoolean("commands.wipe.clear-mutes", true);
        boolean kickPlayer = plugin.getConfig().getBoolean("commands.wipe.kick-player", true);

        if (t != null) {
            if (wipeInventory) t.getInventory().clear();
            if (wipeEnderChest) t.getEnderChest().clear();
            if (resetHealth) {
                t.setHealth(20);
                t.setFireTicks(0);
            }
            if (resetHunger) t.setFoodLevel(20);
            if (resetExperience) {
                t.setExp(0);
                t.setLevel(0);
                t.setTotalExperience(0);
            }
            if (wipePosition) {
                Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
                t.teleport(spawn);
            }
        }

        if (clearBans) plugin.getBanManager().unban(targetUuid);
        if (clearMutes) {
            plugin.getMuteManager().unmute(targetUuid);
            plugin.getMuteManager().vcUnmute(targetUuid);
        }

        if (wipeMoney) {
            try {
                if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
                    Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
                    Object econ = Bukkit.getServicesManager().getRegistration(economyClass).getProvider();
                    if (econ != null) {
                        Object balance = econ.getClass().getMethod("getBalance", OfflinePlayer.class).invoke(econ, offlineT);
                        econ.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class).invoke(econ, offlineT, balance);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (wipePlayerData) {
            try {
                plugin.getPlayerDataManager().wipePlayerData(targetUuid);
            } catch (Exception ignored) {}
        }

        if (wipeStats) {
            try {
                plugin.getPlayerDataManager().resetStats(targetUuid);
            } catch (Exception ignored) {}
        }

        if (kickPlayer && t != null) {
            String kickMessage = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("commands.wipe.kick-message", "&cYour data has been wiped. Please rejoin."));
            t.kickPlayer(kickMessage);
        }

        sender.sendMessage(msg("messages.wipe-success").replace("{player}", targetName));
        return true;
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
