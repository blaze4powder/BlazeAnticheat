package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.commands.FreezeCommand;
import me.blaze.blazeAnticheat.data.BanManager;
import me.blaze.blazeAnticheat.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final BlazeAnticheat plugin;

    public JoinListener(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        if (plugin.getBanManager().isBanned(e.getUniqueId())) {
            BanManager.BanInfo info = plugin.getBanManager().getBanInfo(e.getUniqueId());
            long r = info.expiry == -1 ? -1 : info.expiry - System.currentTimeMillis();
            
            String m = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("ban.kick-screen")
                    .replace("{reason}", info.reason)
                    .replace("{admin}", info.by)
                    .replace("{expires}", TimeUtil.format(r)));
            
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, m);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        FreezeCommand fc = (FreezeCommand) plugin.getCommand("freeze").getExecutor();
        if (fc != null && fc.isFrozen(e.getPlayer().getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                e.getPlayer().setWalkSpeed(0f);
                e.getPlayer().setFlySpeed(0f);
                fc.openFreezeMenu(e.getPlayer());
                String message = plugin.getConfig().getString("freeze-menu.message");
                String discord = plugin.getConfig().getString("freeze-menu.discord");
                message = message.replace("{discord}", discord);
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }, 1L);
        }
    }
}
