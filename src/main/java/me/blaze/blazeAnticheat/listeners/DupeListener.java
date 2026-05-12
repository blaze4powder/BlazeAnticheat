package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class DupeListener implements Listener {
    private final BlazeAnticheat plugin;

    public DupeListener(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (player.getGameMode() == GameMode.CREATIVE) return;
        
        ItemStack item = event.getCurrentItem();
        if (plugin.getDupeManager().checkDupe(item)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("general.prefix", "&8[&cBlazeAnticheat&8] ") + plugin.getConfig().getString("messages.dupe-detected", "&cDupe detected! This item is locked.")));
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        return;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        
        if (plugin.getDupeManager().checkDupe(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("general.prefix", "&8[&cBlazeAnticheat&8] ") + plugin.getConfig().getString("messages.dupe-drop-prevented", "&cYou cannot drop duped items!")));
        }
    }
}
