package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class OffendCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;
    private final Map<UUID, UUID> targets = new HashMap<>();
    private final Map<UUID, Integer> pages = new HashMap<>();
    private List<String> offenses;

    public OffendCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
        loadOffenses();
    }

    private void loadOffenses() {
        offenses = new ArrayList<>();
        if (plugin.getConfig().contains("punishments.offenses")) {
            offenses.addAll(plugin.getConfig().getConfigurationSection("punishments.offenses").getKeys(false));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.offend.enabled")) return true;
        
        String perm = plugin.getConfig().getString("permissions.offend");
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.player-only"));
            return true;
        }
        
        Player admin = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(msg("messages.usage-offend"));
            return true;
        }

        Player t = Bukkit.getPlayer(args[0]);
        if (t == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return true;
        }

        targets.put(admin.getUniqueId(), t.getUniqueId());
        pages.put(admin.getUniqueId(), 0);
        openMenu(admin, 0);
        return true;
    }

    private void openMenu(Player p, int page) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("offense-menu.title"));
        int perPage = plugin.getConfig().getInt("offense-menu.per-page", 28);
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        int startIndex = page * perPage;
        int endIndex = Math.min(startIndex + perPage, offenses.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String offense = offenses.get(i);
            String path = "punishments.offenses." + offense;
            String action = plugin.getConfig().getString(path + ".action", "kick");
            boolean wipe = plugin.getConfig().getBoolean(path + ".wipe", false);
            
            ItemStack item;
            if (action.equalsIgnoreCase("ban")) {
                if (wipe) {
                    item = new ItemStack(Material.valueOf(plugin.getConfig().getString("offense-menu.ban-wipe-dye", "RED_DYE")));
                } else {
                    item = new ItemStack(Material.valueOf(plugin.getConfig().getString("offense-menu.ban-no-wipe-dye", "ORANGE_DYE")));
                }
            } else if (action.equalsIgnoreCase("allmute")) {
                item = new ItemStack(Material.valueOf(plugin.getConfig().getString("offense-menu.mute-dye", "LIME_DYE")));
            } else if (action.equalsIgnoreCase("mute") || action.equalsIgnoreCase("vcmute")) {
                item = new ItemStack(Material.valueOf(plugin.getConfig().getString("offense-menu.mute-dye", "LIME_DYE")));
            } else {
                item = new ItemStack(Material.PAPER);
            }
            
            ItemMeta m = item.getItemMeta();
            m.setDisplayName(ChatColor.YELLOW + offense.toUpperCase().replace("_", " "));
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Action: " + ChatColor.WHITE + action.toUpperCase());
            lore.add(ChatColor.GRAY + "Wipe: " + (wipe ? ChatColor.RED + "Yes" : ChatColor.GREEN + "No"));
            String time = plugin.getConfig().getString(path + ".time", "perm");
            lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + time);
            m.setLore(lore);
            item.setItemMeta(m);
            inv.setItem(i - startIndex, item);
        }
        
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("offense-menu.next-page")));
        next.setItemMeta(nextMeta);
        inv.setItem(53, next);
        
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("offense-menu.previous-page")));
        prev.setItemMeta(prevMeta);
        inv.setItem(45, prev);
        
        p.openInventory(inv);
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }

    public static class OffendMenuListener implements Listener {
        private final OffendCommand cmd;
        
        public OffendMenuListener(OffendCommand cmd) {
            this.cmd = cmd;
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player) e.getWhoClicked();
            String title = ChatColor.translateAlternateColorCodes('&', cmd.plugin.getConfig().getString("offense-menu.title"));
            
            if (!e.getView().getTitle().equals(title)) return;
            e.setCancelled(true);
            
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            if (e.getCurrentItem().getType() == Material.ARROW) {
                int slot = e.getSlot();
                int currentPage = cmd.pages.getOrDefault(p.getUniqueId(), 0);
                if (slot == 53) {
                    cmd.openMenu(p, currentPage + 1);
                    cmd.pages.put(p.getUniqueId(), currentPage + 1);
                } else if (slot == 45) {
                    if (currentPage > 0) {
                        cmd.openMenu(p, currentPage - 1);
                        cmd.pages.put(p.getUniqueId(), currentPage - 1);
                    }
                }
                return;
            }

            UUID targetId = cmd.targets.get(p.getUniqueId());
            if (targetId == null) return;
            Player t = Bukkit.getPlayer(targetId);
            if (t == null) return;

            String o = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()).toLowerCase().replace(" ", "_");
            cmd.plugin.getPunishmentManager().punish(t, o, p.getName());
            p.closeInventory();
        }
    }
}
