package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

public class VulcanListener implements Listener {
    private final BlazeAnticheat plugin;
    private Method getPlayerMethod;
    private Method getCheckMethod;
    private Method getNameMethod;
    private Method getInfoMethod;
    private Method getVlMethod;

    public VulcanListener(BlazeAnticheat plugin) {
        this.plugin = plugin;
        setupReflection();
    }

    private void setupReflection() {
        try {
            Class<?> vulcanFlagEventClass = Class.forName("me.frep.vulcan.api.event.VulcanFlagEvent");
            getPlayerMethod = vulcanFlagEventClass.getMethod("getPlayer");
            getCheckMethod = vulcanFlagEventClass.getMethod("getCheck");
            
            Class<?> checkClass = Class.forName("me.frep.vulcan.api.check.Check");
            getNameMethod = checkClass.getMethod("getName");
            getInfoMethod = checkClass.getMethod("getInfo");
            getVlMethod = checkClass.getMethod("getVl");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onVulcanFlag(org.bukkit.event.Event event) {
        try {
            if (!event.getClass().getName().equals("me.frep.vulcan.api.event.VulcanFlagEvent")) {
                return;
            }

            Player player = (Player) getPlayerMethod.invoke(event);
            if (player == null) return;

            boolean ignoreHighPing = plugin.getConfig().getBoolean("alerts.ignore-high-ping");
            int pingThreshold = plugin.getConfig().getInt("alerts.ping-threshold");
            boolean ignoreFrozen = plugin.getConfig().getBoolean("alerts.ignore-frozen");

            if (ignoreHighPing) {
                try {
                    int ping = player.getPing();
                    if (ping >= pingThreshold) {
                        return;
                    }
                } catch (Exception ignored) {}
            }

            if (ignoreFrozen) {
                try {
                    if (player.getPing() > 1000) {
                        return;
                    }
                } catch (Exception ignored) {}
            }

            plugin.getFlagged().add(player.getUniqueId());
            var data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            data.violations++;
            data.totalAlerts++;
            data.lastAlertTime = System.currentTimeMillis();

            Object check = getCheckMethod.invoke(event);
            if (check != null) {
                String checkName = (String) getNameMethod.invoke(check);
                String info = (String) getInfoMethod.invoke(check);
                int violations = (Integer) getVlMethod.invoke(check);
                
                data.violationHistory.add(checkName + " (VL: " + violations + ")");

                var susAlerts = (me.blaze.blazeAnticheat.commands.SusAlertsCommand) plugin.getCommand("susalerts").getExecutor();
                if (susAlerts != null) {
                    susAlerts.sendAlert(player, checkName, violations, info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
