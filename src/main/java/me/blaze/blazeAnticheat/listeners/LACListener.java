package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.commands.SusAlertsCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class LACListener {
    private final BlazeAnticheat plugin;

    public LACListener(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    public void registerLACEvents() {
        try {
            Class<?> lacViolationEventClass = Class.forName("me.vekster.lightanticheat.api.events.LACViolationEvent");
            Class<?> lacPunishmentEventClass = Class.forName("me.vekster.lightanticheat.api.events.LACPunishmentEvent");
            
            Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
                @org.bukkit.event.EventHandler
                public void onLACViolation(org.bukkit.event.Event event) {
                    if (!lacViolationEventClass.isInstance(event)) return;
                    
                    try {
                        Method getPlayerMethod = event.getClass().getMethod("getPlayer");
                        Player player = (Player) getPlayerMethod.invoke(event);
                        
                        Method getCheckNameMethod = event.getClass().getMethod("getCheckName");
                        String checkName = (String) getCheckNameMethod.invoke(event);
                        
                        Method getViolationsMethod = event.getClass().getMethod("getViolations");
                        int violations = (int) getViolationsMethod.invoke(event);
                        
                        if (player != null) {
                            boolean ignoreHighPing = plugin.getConfig().getBoolean("alerts.ignore-high-ping");
                            int pingThreshold = plugin.getConfig().getInt("alerts.ping-threshold");
                            
                            if (ignoreHighPing) {
                                try {
                                    int ping = player.getPing();
                                    if (ping >= pingThreshold) {
                                        return;
                                    }
                                } catch (Exception ignored) {}
                            }
                            
                            plugin.getFlagged().add(player.getUniqueId());
                            plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).violations++;
                            plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).totalAlerts++;
                            plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).lastAlertTime = System.currentTimeMillis();
                            plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).violationHistory.add(checkName + " (VL: " + violations + ")");
                            
                            SusAlertsCommand susAlerts = (SusAlertsCommand) plugin.getCommand("susalerts").getExecutor();
                            if (susAlerts != null) {
                                susAlerts.sendAlert(player, checkName, violations, "");
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }, plugin);
            
        } catch (Exception ignored) {}
    }

    public static void disableDetection(Player player, long durationMils) {
        try {
            Class<?> lacApiClass = Class.forName("me.vekster.lightanticheat.api.LACApi");
            Object lacApi = lacApiClass.getMethod("getInstance").invoke(null);
            if (lacApi != null) {
                Class<?> checkTypeClass = Class.forName("me.vekster.lightanticheat.api.enums.CheckType");
                Object checkTypeAll = checkTypeClass.getField("ALL").get(null);
                
                Method getCheckNamesMethod = lacApiClass.getMethod("getCheckNames", checkTypeClass);
                String[] checkNames = (String[]) getCheckNamesMethod.invoke(lacApi, checkTypeAll);
                
                Method disableDetectionMethod = lacApiClass.getMethod("disableDetection", Player.class, String.class, long.class);
                for (String checkName : checkNames) {
                    disableDetectionMethod.invoke(lacApi, player, checkName, durationMils);
                }
            }
        } catch (Exception ignored) {}
    }
}
