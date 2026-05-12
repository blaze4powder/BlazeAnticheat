package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.data.MuteManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class VoiceChatListener {
    private final BlazeAnticheat plugin;
    private Object api;
    private final Map<UUID, Long> cd = new HashMap<>();
    private final Set<UUID> global = new HashSet<>();

    public VoiceChatListener(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    public String getPluginId() { return "blaze_anticheat"; }

    public void registerEvents(Object reg) {
        try {
            Class<?> voicechatServerStartedEventClass = Class.forName("de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent");
            Class<?> microphonePacketEventClass = Class.forName("de.maxhenkel.voicechat.api.events.MicrophonePacketEvent");
            
            reg.getClass().getMethod("registerEvent", Class.class, Object.class).invoke(reg, 
                voicechatServerStartedEventClass, 
                (java.util.function.Consumer<Object>) e -> {
                    try {
                        api = e.getClass().getMethod("getVoicechat").invoke(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            );
            reg.getClass().getMethod("registerEvent", Class.class, Object.class).invoke(reg, 
                microphonePacketEventClass, 
                (java.util.function.Consumer<Object>) this::onTalk
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onTalk(Object e) {
        try {
            Object senderConnection = e.getClass().getMethod("getSenderConnection").invoke(e);
            if (senderConnection == null) return;
            Object player = senderConnection.getClass().getMethod("getPlayer").invoke(senderConnection);
            UUID id = (UUID) player.getClass().getMethod("getUuid").invoke(player);
            
            if (plugin.getMuteManager().isVCMuted(id)) {
                e.getClass().getMethod("cancel").invoke(e);
                notifyMuted(id);
                return;
            }

            if (global.contains(id) && api != null) {
                e.getClass().getMethod("cancel").invoke(e);
                Object p = e.getClass().getMethod("getPacket").invoke(e);
                Object connections = api.getClass().getMethod("getConnections").invoke(api);
                ((Iterable<?>) connections).forEach(conn -> {
                    try {
                        Object connPlayer = conn.getClass().getMethod("getPlayer").invoke(conn);
                        UUID connId = (UUID) connPlayer.getClass().getMethod("getUuid").invoke(connPlayer);
                        if (!connId.equals(id)) {
                            Object staticSoundPacket = p.getClass().getMethod("toStaticSoundPacket").invoke(p);
                            api.getClass().getMethod("sendStaticSoundPacketTo", conn.getClass(), staticSoundPacket.getClass()).invoke(api, conn, staticSoundPacket);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void notifyMuted(UUID id) {
        long now = System.currentTimeMillis();
        if (now - cd.getOrDefault(id, 0L) < 2000) return;
        cd.put(id, now);
        Player p = Bukkit.getPlayer(id);
        if (p == null) return;
        MuteManager.MuteInfo info = plugin.getMuteManager().getVCMuteInfo(id);
        String m = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.mute-alert").replace("{time}", format(info != null ? info.expiry : -1)));
        Bukkit.getScheduler().runTask(plugin, () -> {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(m));
        });
    }

    public void toggleGlobal(UUID id) {
        if (global.contains(id)) global.remove(id);
        else global.add(id);
    }

    public boolean isGlobal(UUID id) { return global.contains(id); }

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
