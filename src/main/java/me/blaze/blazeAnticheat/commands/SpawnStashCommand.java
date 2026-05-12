package me.blaze.blazeAnticheat.commands;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SpawnStashCommand implements CommandExecutor {
    private final BlazeAnticheat plugin;
    private static final Map<UUID, Map<Location, BlockState>> lastStates = new HashMap<>();

    public SpawnStashCommand(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("commands.stash.enabled")) return true;
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("messages.player-only"));
            return true;
        }

        Player p = (Player) sender;
        String perm = plugin.getConfig().getString("permissions.stash");
        if (!p.hasPermission(perm)) {
            p.sendMessage(msg("messages.no-perm"));
            return true;
        }

        generate(p.getLocation(), p);
        p.sendMessage(msg("messages.stash-created"));
        
        return true;
    }

    public static void undoLastStash(Player p) {
        Map<Location, BlockState> states = lastStates.remove(p.getUniqueId());
        if (states != null) {
            for (BlockState s : states.values()) s.update(true);
        }
    }

    private void generate(Location c, Player p) {
        Random r = new Random();
        
        int width = 5 + r.nextInt(3);
        int depth = 5 + r.nextInt(4);
        int height = 2 + r.nextInt(2);
        
        String[] floorMaterials = plugin.getConfig().getString("spawnstash.floor-materials", "COBBLESTONE,STONE_BRICKS,DIRT,GRASS_BLOCK,OAK_PLANKS").split(",");
        String[] wallMaterials = plugin.getConfig().getString("spawnstash.wall-materials", "COBBLESTONE,STONE,DIRT,MOSSY_COBBLESTONE").split(",");
        String[] chestMaterials = plugin.getConfig().getString("spawnstash.chest-materials", "CHEST,TRAPPED_CHEST,BARREL").split(",");
        String[] spawnerTypes = plugin.getConfig().getString("spawnstash.spawner-types", "ZOMBIE,SKELETON,SPIDER,CREEPER").split(",");
        
        Material[] floors = Arrays.stream(floorMaterials).map(String::trim).map(Material::valueOf).toArray(Material[]::new);
        Material[] walls = Arrays.stream(wallMaterials).map(String::trim).map(Material::valueOf).toArray(Material[]::new);
        Material[] chests = Arrays.stream(chestMaterials).map(String::trim).map(Material::valueOf).toArray(Material[]::new);
        
        World w = c.getWorld();
        Map<Location, BlockState> history = new HashMap<>();
        List<Location> wallLocations = new ArrayList<>();
        
        Material floorMat = floors[r.nextInt(floors.length)];
        Material wallMat = walls[r.nextInt(walls.length)];
        
        for (int x = -width/2; x <= width/2; x++) {
            for (int z = -depth/2; z <= depth/2; z++) {
                for (int y = -1; y <= height; y++) {
                    Location l = c.clone().add(x, y, z);
                    history.putIfAbsent(l, l.getBlock().getState());
                    
                    boolean isEdge = Math.abs(x) == width/2 || Math.abs(z) == depth/2;
                    boolean isCeiling = y == height;
                    boolean isFloor = y == -1;
                    
                    if (isFloor) {
                        l.getBlock().setType(floorMat);
                    } else if (isEdge || isCeiling) {
                        l.getBlock().setType(wallMat);
                        if (isEdge && !isCeiling) {
                            wallLocations.add(l);
                        }
                    } else {
                        l.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
        
        int chestWall = r.nextInt(4);
        int chestCount = 2 + r.nextInt(4);
        int chestsPlaced = 0;
        
        for (int i = 0; i < wallLocations.size() && chestsPlaced < chestCount; i++) {
            Location l = wallLocations.get(i);
            int x = l.getBlockX() - c.getBlockX();
            int z = l.getBlockZ() - c.getBlockZ();
            
            boolean correctWall = false;
            if (chestWall == 0 && x == -width/2) correctWall = true;
            if (chestWall == 1 && x == width/2) correctWall = true;
            if (chestWall == 2 && z == -depth/2) correctWall = true;
            if (chestWall == 3 && z == depth/2) correctWall = true;
            
            if (correctWall && l.getBlockY() == c.getBlockY()) {
                Location chestLoc = l.clone().add(0, 1, 0);
                if (chestLoc.getBlock().getType() == Material.AIR) {
                    chestLoc.getBlock().setType(chests[r.nextInt(chests.length)]);
                    if (chestLoc.getBlock().getState() instanceof Chest) {
                        fill(((Chest) chestLoc.getBlock().getState()).getInventory(), r);
                        chestsPlaced++;
                    }
                }
            }
        }
        
        if (r.nextDouble() > 0.5) {
            Location spawnerLoc = c.clone().add(0, 0, 0);
            spawnerLoc.getBlock().setType(Material.SPAWNER);
            try {
                org.bukkit.block.CreatureSpawner s = (org.bukkit.block.CreatureSpawner) spawnerLoc.getBlock().getState();
                s.setSpawnedType(EntityType.valueOf(spawnerTypes[r.nextInt(spawnerTypes.length)]));
            } catch (Exception ignored) {}
        }
        
        lastStates.put(p.getUniqueId(), history);
    }

    private void fill(Inventory i, Random r) {
        Material[] m = {Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI, Material.COAL, Material.ENDER_PEARL, Material.EXPERIENCE_BOTTLE};
        for (int j = 0; j < 8; j++) i.setItem(r.nextInt(i.getSize()), new ItemStack(m[r.nextInt(m.length)], 1 + r.nextInt(10)));
    }

    private String msg(String path) {
        String pre = plugin.getConfig().getString("settings.prefix");
        return ChatColor.translateAlternateColorCodes('&', pre + plugin.getConfig().getString(path));
    }
}
