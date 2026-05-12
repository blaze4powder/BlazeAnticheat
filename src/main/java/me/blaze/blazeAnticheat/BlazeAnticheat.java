package me.blaze.blazeAnticheat;

import me.blaze.blazeAnticheat.commands.*;
import me.blaze.blazeAnticheat.commands.AllmuteCommand;
import me.blaze.blazeAnticheat.commands.AllunmuteCommand;
import me.blaze.blazeAnticheat.commands.BanCommand;
import me.blaze.blazeAnticheat.commands.BanListCommand;
import me.blaze.blazeAnticheat.commands.FreezeCommand;
import me.blaze.blazeAnticheat.commands.KbTestCommand;
import me.blaze.blazeAnticheat.commands.MuteCommand;
import me.blaze.blazeAnticheat.commands.OffendCommand;
import me.blaze.blazeAnticheat.commands.PopTotemCommand;
import me.blaze.blazeAnticheat.commands.ReloadCommand;
import me.blaze.blazeAnticheat.commands.SpawnStashCommand;
import me.blaze.blazeAnticheat.commands.SusAlertsCommand;
import me.blaze.blazeAnticheat.commands.SusCommand;
import me.blaze.blazeAnticheat.commands.UndoStashCommand;
import me.blaze.blazeAnticheat.commands.UnbanCommand;
import me.blaze.blazeAnticheat.commands.UnfreezeCommand;
import me.blaze.blazeAnticheat.commands.UnmuteCommand;
import me.blaze.blazeAnticheat.commands.VCMuteCommand;
import me.blaze.blazeAnticheat.commands.VCUnmuteCommand;
import me.blaze.blazeAnticheat.commands.WipeCommand;
import me.blaze.blazeAnticheat.data.*;
import me.blaze.blazeAnticheat.listeners.*;
import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.GrimAPIProvider;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.event.GrimEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BlazeAnticheat extends JavaPlugin implements Listener {

    private static BlazeAnticheat instance;
    private BanManager banMgr;
    private MuteManager muteMgr;
    private PlayerDataManager dataMgr;
    private DupeManager dupeMgr;
    private PunishmentManager punishMgr;
    private GrimAbstractAPI grim;
    private Object lacInstance;
    private Object vcListener;
    private Object vulcanListener;
    private final Set<UUID> flagged = new HashSet<>();


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        banMgr = new BanManager(this);
        muteMgr = new MuteManager(this);
        dataMgr = new PlayerDataManager(this);
        dupeMgr = new DupeManager(this);
        punishMgr = new PunishmentManager(this);
        
        setupHooks();
        
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(this, this);
        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new QuitListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new DupeListener(this), this);
        
        regCmds();

        getLogger().info("BlazeAnticheat online.");
    }

    @Override
    public void onDisable() {
        if (dataMgr != null) dataMgr.saveAll();
    }

    public static BlazeAnticheat getInstance() {
        return instance;
    }

    private void setupHooks() {
        try {
            if (Bukkit.getPluginManager().getPlugin("GrimAC") != null) {
                try {
                    grim = GrimAPIProvider.get();
                    if (grim != null) {
                        grim.getEventBus().registerAnnotatedListeners(grim.getGrimPlugin(this), this);
                    }
                } catch (Exception ignored) {}
            }
            if (Bukkit.getPluginManager().getPlugin("LightAntiCheat") != null) {
                try {
                    Class<?> lacApiClass = Class.forName("me.vekster.lightanticheat.api.LACApi");
                    Object lacApi = lacApiClass.getMethod("getInstance").invoke(null);
                    if (lacApi != null) {
                        lacInstance = lacApi;
                        me.blaze.blazeAnticheat.listeners.LACListener lacListener = new me.blaze.blazeAnticheat.listeners.LACListener(this);
                        lacListener.registerLACEvents();
                    }
                } catch (Exception ignored) {}
            }
            if (Bukkit.getPluginManager().getPlugin("Vulcan") != null) {
                try {
                    Class<?> vulcanFlagEventClass = Class.forName("me.frep.vulcan.api.event.VulcanFlagEvent");
                    Class<?> vulcanListenerClass = Class.forName("me.blaze.blazeAnticheat.listeners.VulcanListener");
                    vulcanListener = vulcanListenerClass.getConstructor(BlazeAnticheat.class).newInstance(this);
                    Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) vulcanListener, this);
                } catch (Exception ignored) {}
            }
            try {
                Class<?> bukkitVoicechatServiceClass = Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
                Object vc = Bukkit.getServicesManager().load(bukkitVoicechatServiceClass);
                if (vc != null) {
                    Class<?> voiceChatListenerClass = Class.forName("me.blaze.blazeAnticheat.listeners.VoiceChatListener");
                    vcListener = voiceChatListenerClass.getConstructor(BlazeAnticheat.class).newInstance(this);
                    vc.getClass().getMethod("registerPlugin", Class.forName("de.maxhenkel.voicechat.api.VoicechatPlugin")).invoke(vc, vcListener);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            }
        } catch (Exception ignored) {}
    }

    @GrimEventHandler
    public void onFlag(FlagEvent e) {
        if (grim == null) return;
        Player player = Bukkit.getPlayer(e.getUser().getUniqueId());
        if (player != null) {
            boolean ignoreHighPing = getConfig().getBoolean("alerts.ignore-high-ping");
            int pingThreshold = getConfig().getInt("alerts.ping-threshold");
            boolean ignoreFrozen = getConfig().getBoolean("alerts.ignore-frozen");
            
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
                    if (e.getUser().getTransactionPing() > 1000) {
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        
        flagged.add(e.getUser().getUniqueId());
        PlayerDataManager.PlayerData data = dataMgr.getPlayerData(e.getUser().getUniqueId());
        data.violations++;
        data.totalAlerts++;
        data.lastAlertTime = System.currentTimeMillis();
        
        if (player != null) {
            String checkName = e.getCheck().getCheckName();
            String details = "";
            int violations = (int) e.getViolations();
            data.violationHistory.add(checkName + " (VL: " + violations + ")");
        }
        
        SusAlertsCommand susAlerts = (SusAlertsCommand) getCommand("susalerts").getExecutor();
        if (susAlerts != null) {
            if (player != null) {
                String checkName = e.getCheck().getCheckName();
                String details = "";
                int violations = (int) e.getViolations();
                susAlerts.sendAlert(player, checkName, violations, details);
            }
        }
    }

    private void regCmds() {
        getCommand("sus").setExecutor(new SusCommand(this));
        getCommand("susalerts").setExecutor(new SusAlertsCommand(this));
        getCommand("offend").setExecutor(new OffendCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("banlist").setExecutor(new BanListCommand(this));
        getCommand("spawnstash").setExecutor(new SpawnStashCommand(this));
        getCommand("undostash").setExecutor(new UndoStashCommand(this));
        getCommand("blazereload").setExecutor(new ReloadCommand(this));
        getCommand("poptotem").setExecutor(new PopTotemCommand(this));
        getCommand("kbtest").setExecutor(new KbTestCommand(this));
        getCommand("wipe").setExecutor(new WipeCommand(this));
        getCommand("vcmute").setExecutor(new VCMuteCommand(this));
        getCommand("vcunmute").setExecutor(new VCUnmuteCommand(this));
        getCommand("allmute").setExecutor(new AllmuteCommand(this));
        getCommand("allunmute").setExecutor(new AllunmuteCommand(this));
        getCommand("freeze").setExecutor(new FreezeCommand(this));
        getCommand("unfreeze").setExecutor(new UnfreezeCommand(this));
        
        SusCommand sc = (SusCommand) getCommand("sus").getExecutor();
        Bukkit.getPluginManager().registerEvents(new SusCommand.SusMenuListener(sc), this);
        
        OffendCommand oc = (OffendCommand) getCommand("offend").getExecutor();
        Bukkit.getPluginManager().registerEvents(new OffendCommand.OffendMenuListener(oc), this);
        
        SusAlertsCommand sac = (SusAlertsCommand) getCommand("susalerts").getExecutor();
        Bukkit.getPluginManager().registerEvents(new SusAlertsCommand.SusAlertsListener(sac), this);
        
        FreezeCommand fc = (FreezeCommand) getCommand("freeze").getExecutor();
        Bukkit.getPluginManager().registerEvents(new me.blaze.blazeAnticheat.listeners.FreezeListener(this, fc), this);
    }

    public BanManager getBanManager() { return banMgr; }
    public MuteManager getMuteManager() { return muteMgr; }
    public PlayerDataManager getPlayerDataManager() { return dataMgr; }
    public DupeManager getDupeManager() { return dupeMgr; }
    public PunishmentManager getPunishmentManager() { return punishMgr; }
    public Object getVCListener() { return vcListener; }
    public Set<UUID> getFlagged() { return flagged; }
    public GrimAbstractAPI getGrim() { return grim; }
    public Object getLacInstance() { return lacInstance; }
    public Object getVulcanListener() { return vulcanListener; }
}
