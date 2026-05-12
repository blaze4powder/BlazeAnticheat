package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.commands.FreezeCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FreezeListener implements Listener {
    private final BlazeAnticheat plugin;
    private final FreezeCommand freezeCommand;

    public FreezeListener(BlazeAnticheat plugin, FreezeCommand freezeCommand) {
        this.plugin = plugin;
        this.freezeCommand = freezeCommand;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (freezeCommand.isFrozen(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player) {
            Player p = (Player) e.getPlayer();
            if (freezeCommand.isFrozen(p.getUniqueId())) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> freezeCommand.openFreezeMenu(p), 1L);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            if (freezeCommand.isFrozen(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
    }
}
