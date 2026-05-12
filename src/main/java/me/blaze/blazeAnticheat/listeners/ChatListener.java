package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.data.MuteManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final BlazeAnticheat plugin;

    public ChatListener(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        if (plugin.getMuteManager().isMuted(p.getUniqueId())) {
            event.setCancelled(true);
            MuteManager.MuteInfo info = plugin.getMuteManager().getMuteInfo(p.getUniqueId());
            
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            
            String t = format(info.expiry);
            String m = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.mute-alert").replace("{time}", t));
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(m));
        }
    }

    private String format(long e) {
        if (e == -1) return "Permanent";
        long r = e - System.currentTimeMillis();
        if (r <= 0) return "0s";
        long d = r / 86400000; r %= 86400000;
        long h = r / 3600000; r %= 3600000;
        long m = r / 60000; r %= 60000;
        long s = r / 1000;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.length() == 0) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
