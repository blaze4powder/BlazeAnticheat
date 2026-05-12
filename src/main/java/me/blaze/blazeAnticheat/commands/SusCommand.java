package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import me.blaze.blazeAnticheat.data.BanManager;
import me.blaze.blazeAnticheat.data.MuteManager;
import me.blaze.blazeAnticheat.data.PlayerDataManager;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class SusCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;
    private final Map<UUID, UUID> selectedTargets = new HashMap<>();
    private final Map<UUID, Integer> pages = new HashMap<>();

    public SusCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.sus.enabled")) return true;
        
        String perm = plugin.getConfig().getString("permissions.sus");
        if (!sender.hasPermission(perm)) {
            sender.sendMessage(msg("messages.no-perm"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.player-only"));
            return true;
        }
        
        pages.put(((Player) sender).getUniqueId(), 0);
        openMenu((Player) sender, 0);
        return true;
    }

    private void openMenu(Player p, int page) {
        List<Player> flagged = new ArrayList<>();
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (plugin.getFlagged().contains(t.getUniqueId())) {
                flagged.add(t);
            }
        }
        
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        int itemsPerPage = 45;
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, flagged.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Player t = flagged.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(t);
            meta.setDisplayName(ChatColor.YELLOW + t.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: View Actions");
            lore.add(ChatColor.GRAY + "Right-click: View Violations");
            
            BanManager.BanInfo ban = plugin.getBanManager().getBans().get(t.getUniqueId());
            if (ban != null) {
                lore.add(ChatColor.RED + "BANNED: " + ban.reason);
            }
            
            MuteManager.MuteInfo mute = plugin.getMuteManager().getMutes().get(t.getUniqueId());
            if (mute != null) {
                lore.add(ChatColor.RED + "MUTED: " + mute.reason);
            }
            
            PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(t.getUniqueId());
            if (data.violations > 0) {
                lore.add(ChatColor.YELLOW + "Violations: " + data.violations);
            }
            
            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.setItem(i - startIndex, head);
        }
        
        ItemStack anticheatInfo = new ItemStack(Material.NETHER_STAR);
        ItemMeta anticheatMeta = anticheatInfo.getItemMeta();
        anticheatMeta.setDisplayName(ChatColor.AQUA + "Anticheat Status");
        List<String> anticheatLore = new ArrayList<>();
        anticheatLore.add(ChatColor.GRAY + "Available anticheats:");
        
        if (plugin.getGrim() != null) {
            anticheatLore.add(ChatColor.GREEN + "✓ GrimAC");
        } else {
            anticheatLore.add(ChatColor.RED + "✗ GrimAC");
        }
        
        if (plugin.getLacInstance() != null) {
            anticheatLore.add(ChatColor.GREEN + "✓ LightAntiCheat");
        } else {
            anticheatLore.add(ChatColor.RED + "✗ LightAntiCheat");
        }
        
        if (Bukkit.getPluginManager().getPlugin("Vulcan") != null) {
            anticheatLore.add(ChatColor.GREEN + "✓ Vulcan");
        } else {
            anticheatLore.add(ChatColor.RED + "✗ Vulcan");
        }
        
        if (plugin.getGrim() == null && plugin.getLacInstance() == null && Bukkit.getPluginManager().getPlugin("Vulcan") == null) {
            anticheatLore.add(ChatColor.YELLOW + "No anticheats detected");
        }
        
        anticheatMeta.setLore(anticheatLore);
        anticheatInfo.setItemMeta(anticheatMeta);
        inv.setItem(48, anticheatInfo);
        
        ItemStack refresh = new ItemStack(Material.COMPASS);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.refresh-display")));
        refresh.setItemMeta(refreshMeta);
        inv.setItem(49, refresh);
        
        ItemStack more = new ItemStack(Material.BOOK);
        ItemMeta moreMeta = more.getItemMeta();
        moreMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.more-display")));
        more.setItemMeta(moreMeta);
        inv.setItem(50, more);
        
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.next-page")));
        next.setItemMeta(nextMeta);
        inv.setItem(53, next);
        
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.previous-page")));
        prev.setItemMeta(prevMeta);
        inv.setItem(45, prev);
        
        p.openInventory(inv);
    }

    private void openViolationsMenu(Player p, Player target) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.violations-title"));
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Violations: " + ChatColor.YELLOW + data.violations);
        lore.add(ChatColor.GRAY + "Total Alerts: " + ChatColor.YELLOW + data.totalAlerts);
        lore.add("");
        lore.add(ChatColor.GRAY + "Last Alert: " + (data.lastAlertTime > 0 ? new Date(data.lastAlertTime).toString() : "Never"));
        
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + target.getName() + "'s Violations");
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(13, info);
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        inv.setItem(22, back);
        
        p.openInventory(inv);
    }

    private void openDetailedViolationsMenu(Player p, Player target) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.violations-title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + target.getName() + "'s Detailed Violations");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Total Violations: " + ChatColor.YELLOW + data.violations);
        lore.add(ChatColor.GRAY + "Total Alerts: " + ChatColor.YELLOW + data.totalAlerts);
        lore.add("");
        lore.add(ChatColor.GRAY + "Last Alert: " + (data.lastAlertTime > 0 ? new Date(data.lastAlertTime).toString() : "Never"));
        lore.add("");
        
        if (data.violationHistory != null && !data.violationHistory.isEmpty()) {
            lore.add(ChatColor.YELLOW + "Recent Violations:");
            for (String violation : data.violationHistory) {
                lore.add(ChatColor.GRAY + "- " + violation);
            }
        } else {
            lore.add(ChatColor.GRAY + "No detailed violation history available.");
        }
        
        meta.setLore(lore);
        info.setItemMeta(meta);
        inv.setItem(13, info);
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);
        
        p.openInventory(inv);
    }

    private void openActionMenu(Player p, Player target) {
        selectedTargets.put(p.getUniqueId(), target.getUniqueId());
        plugin.getPlayerDataManager().getPlayerData(p.getUniqueId()).lastTarget = target.getUniqueId();
        
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.action-menu-title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        ItemStack teleport = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleport.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.teleport-display")));
        teleport.setItemMeta(teleportMeta);
        inv.setItem(11, teleport);
        
        ItemStack violations = new ItemStack(Material.BOOK);
        ItemMeta violationsMeta = violations.getItemMeta();
        violationsMeta.setDisplayName(ChatColor.YELLOW + "View Violations");
        List<String> violationsLore = new ArrayList<>();
        violationsLore.add(ChatColor.GRAY + "Click to view");
        violationsLore.add(ChatColor.GRAY + "violation details");
        violationsMeta.setLore(violationsLore);
        violations.setItemMeta(violationsMeta);
        inv.setItem(15, violations);
        
        ItemStack offend = new ItemStack(Material.RED_DYE);
        ItemMeta offendMeta = offend.getItemMeta();
        offendMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.offend-display")));
        offend.setItemMeta(offendMeta);
        inv.setItem(20, offend);
        
        ItemStack delete = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.delete-checks-display")));
        delete.setItemMeta(deleteMeta);
        inv.setItem(24, delete);
        
        ItemStack returnItem = new ItemStack(Material.FEATHER);
        ItemMeta returnMeta = returnItem.getItemMeta();
        returnMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.return-display")));
        List<String> returnLore = new ArrayList<>();
        returnLore.add(ChatColor.GRAY + "Return to your location");
        returnLore.add(ChatColor.GRAY + "and restore inventory");
        returnMeta.setLore(returnLore);
        returnItem.setItemMeta(returnMeta);
        inv.setItem(40, returnItem);
        
        p.openInventory(inv);
    }

    private void giveMenuBook(Player p, Player target) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(ChatColor.YELLOW + target.getName() + " Menu");
        meta.setAuthor("BlazeAnticheat");
        meta.setPages(ChatColor.YELLOW + "Click to open menu for " + target.getName());
        book.setItemMeta(meta);
        
        p.getInventory().setItem(0, book);
    }

    private void openAllPlayersMenu(Player p) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("sus-menu.all-players-title"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        
        List<Player> flaggedPlayers = new ArrayList<>();
        for (Player t : Bukkit.getOnlinePlayers()) {
            if (plugin.getFlagged().contains(t.getUniqueId())) {
                flaggedPlayers.add(t);
            }
        }
        
        int itemsPerPage = 45;
        int startIndex = 0;
        int endIndex = Math.min(itemsPerPage, flaggedPlayers.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Player t = flaggedPlayers.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(t);
            meta.setDisplayName(ChatColor.YELLOW + t.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: View Actions");
            lore.add(ChatColor.GRAY + "Right-click: View Violations");
            lore.add(ChatColor.RED + "FLAGGED");
            
            BanManager.BanInfo ban = plugin.getBanManager().getBans().get(t.getUniqueId());
            if (ban != null) {
                lore.add(ChatColor.RED + "BANNED: " + ban.reason);
            }
            
            MuteManager.MuteInfo mute = plugin.getMuteManager().getMutes().get(t.getUniqueId());
            if (mute != null) {
                lore.add(ChatColor.RED + "MUTED: " + mute.reason);
            }
            
            PlayerDataManager.PlayerData data = plugin.getPlayerDataManager().getPlayerData(t.getUniqueId());
            if (data.violations > 0) {
                lore.add(ChatColor.YELLOW + "Violations: " + data.violations);
            }
            
            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.setItem(i - startIndex, head);
        }
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Sus Menu");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);
        
        p.openInventory(inv);
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }

    public static class SusMenuListener implements Listener {
        private final SusCommand cmd;
        
        public SusMenuListener(SusCommand cmd) {
            this.cmd = cmd;
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            Player p = (Player) e.getWhoClicked();
            String susTitle = ChatColor.translateAlternateColorCodes('&', cmd.plugin.getConfig().getString("sus-menu.title"));
            String allPlayersTitle = ChatColor.translateAlternateColorCodes('&', cmd.plugin.getConfig().getString("sus-menu.all-players-title"));
            String violationsTitle = ChatColor.translateAlternateColorCodes('&', cmd.plugin.getConfig().getString("sus-menu.violations-title"));
            String actionTitle = ChatColor.translateAlternateColorCodes('&', cmd.plugin.getConfig().getString("sus-menu.action-menu-title"));
            
            String viewTitle = e.getView().getTitle();
            
            if (viewTitle.equals(allPlayersTitle)) {
                e.setCancelled(true);
                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
                
                if (e.getCurrentItem().getType() == Material.ARROW) {
                    int currentPage = cmd.pages.getOrDefault(p.getUniqueId(), 0);
                    cmd.openMenu(p, currentPage);
                    return;
                }
                
                if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        org.bukkit.OfflinePlayer offlineTarget = meta.getOwningPlayer();
                        Player target = offlineTarget.getPlayer();
                        if (target != null) {
                            if (e.isRightClick()) {
                                cmd.openViolationsMenu(p, target);
                            } else {
                                cmd.openActionMenu(p, target);
                            }
                        }
                    }
                }
            } else if (viewTitle.equals(susTitle)) {
                e.setCancelled(true);
                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
                
                if (e.getCurrentItem().getType() == Material.COMPASS) {
                    int currentPage = cmd.pages.getOrDefault(p.getUniqueId(), 0);
                    cmd.openMenu(p, currentPage);
                    return;
                }
                
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
                
                if (e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                    SkullMeta meta = (SkullMeta) e.getCurrentItem().getItemMeta();
                    if (meta != null && meta.getOwningPlayer() != null) {
                        org.bukkit.OfflinePlayer offlineTarget = meta.getOwningPlayer();
                        Player target = offlineTarget.getPlayer();
                        if (target != null) {
                            if (e.isRightClick()) {
                                cmd.openViolationsMenu(p, target);
                            } else {
                                cmd.openActionMenu(p, target);
                            }
                        }
                    }
                }
                
                if (e.getCurrentItem().getType() == Material.BOOK && e.getSlot() == 50) {
                    cmd.openAllPlayersMenu(p);
                }
                
                if (e.getCurrentItem().getType() == Material.NETHER_STAR && e.getSlot() == 48) {
                    p.sendMessage(ChatColor.AQUA + "=== Anticheat Status ===");
                    if (cmd.plugin.getGrim() != null) {
                        p.sendMessage(ChatColor.GREEN + "✓ GrimAC - Connected");
                    } else {
                        p.sendMessage(ChatColor.RED + "✗ GrimAC - Not found");
                    }
                    
                    if (cmd.plugin.getLacInstance() != null) {
                        p.sendMessage(ChatColor.GREEN + "✓ LightAntiCheat - Connected");
                    } else {
                        p.sendMessage(ChatColor.RED + "✗ LightAntiCheat - Not found");
                    }
                    
                    if (Bukkit.getPluginManager().getPlugin("Vulcan") != null) {
                        p.sendMessage(ChatColor.GREEN + "✓ Vulcan - Connected");
                    } else {
                        p.sendMessage(ChatColor.RED + "✗ Vulcan - Not found");
                    }
                    
                    if (cmd.plugin.getGrim() == null && cmd.plugin.getLacInstance() == null && Bukkit.getPluginManager().getPlugin("Vulcan") == null) {
                        p.sendMessage(ChatColor.YELLOW + "No anticheats detected");
                    }
                }
            } else if (viewTitle.equals(violationsTitle)) {
                e.setCancelled(true);
                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
                
                if (e.getCurrentItem().getType() == Material.ARROW) {
                    UUID targetId = cmd.selectedTargets.get(p.getUniqueId());
                    if (targetId != null) {
                        Player target = Bukkit.getPlayer(targetId);
                        if (target != null) {
                            cmd.openActionMenu(p, target);
                        } else {
                            int currentPage = cmd.pages.getOrDefault(p.getUniqueId(), 0);
                            cmd.openMenu(p, currentPage);
                        }
                    } else {
                        int currentPage = cmd.pages.getOrDefault(p.getUniqueId(), 0);
                        cmd.openMenu(p, currentPage);
                    }
                }
                
                if (e.getCurrentItem().getType() == Material.BOOK && e.isRightClick()) {
                    UUID targetId = cmd.selectedTargets.get(p.getUniqueId());
                    if (targetId != null) {
                        Player target = Bukkit.getPlayer(targetId);
                        if (target != null) {
                            cmd.openDetailedViolationsMenu(p, target);
                        }
                    }
                }
                
                if (e.getCurrentItem().getType() == Material.ARROW && e.getSlot() == 49) {
                    UUID targetId = cmd.selectedTargets.get(p.getUniqueId());
                    if (targetId != null) {
                        Player target = Bukkit.getPlayer(targetId);
                        if (target != null) {
                            cmd.openViolationsMenu(p, target);
                        }
                    }
                }
            } else if (viewTitle.equals(actionTitle)) {
                e.setCancelled(true);
                if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
                
                UUID targetId = cmd.selectedTargets.get(p.getUniqueId());
                if (targetId == null) return;
                Player target = Bukkit.getPlayer(targetId);
                if (target == null) return;
                
                if (e.getCurrentItem().getType() == Material.ENDER_PEARL) {
                    cmd.plugin.getPlayerDataManager().saveInventory(p);
                    p.teleport(target.getLocation());
                    p.setGameMode(org.bukkit.GameMode.CREATIVE);
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                    p.hidePlayer(cmd.plugin, target);
                    cmd.giveMenuBook(p, target);
                    p.sendMessage(cmd.msg("messages.teleported-to-player").replace("{player}", target.getName()));
                    p.closeInventory();
                } else if (e.getCurrentItem().getType() == Material.BOOK && e.getSlot() == 15) {
                    cmd.openViolationsMenu(p, target);
                } else if (e.getCurrentItem().getType() == Material.RED_DYE) {
                    p.closeInventory();
                    Bukkit.getScheduler().runTask(cmd.plugin, () -> p.performCommand("offend " + target.getName()));
                } else if (e.getCurrentItem().getType() == Material.BARRIER) {
                    PlayerDataManager.PlayerData data = cmd.plugin.getPlayerDataManager().getPlayerData(targetId);
                    data.violations = 0;
                    data.totalAlerts = 0;
                    p.sendMessage(cmd.msg("messages.checks-deleted").replace("{player}", target.getName()));
                    p.closeInventory();
                } else if (e.getCurrentItem().getType() == Material.FEATHER) {
                    cmd.plugin.getPlayerDataManager().restoreInventory(p);
                    p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                    p.getInventory().setItem(0, null);
                    p.sendMessage(ChatColor.GREEN + "Returned to original location.");
                    p.closeInventory();
                }
            }
        }
        
        @EventHandler
        public void onBookClick(org.bukkit.event.player.PlayerInteractEvent e) {
            if (e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                if (e.getItem() != null && e.getItem().getType() == Material.WRITTEN_BOOK) {
                    Player p = e.getPlayer();
                    PlayerDataManager.PlayerData data = cmd.plugin.getPlayerDataManager().getPlayerData(p.getUniqueId());
                    if (data.lastTarget != null) {
                        Player target = Bukkit.getPlayer(data.lastTarget);
                        if (target != null) {
                            cmd.openActionMenu(p, target);
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
