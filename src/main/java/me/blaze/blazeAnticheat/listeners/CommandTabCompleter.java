package me.blaze.blazeAnticheat.listeners;

import me.blaze.blazeAnticheat.BlazeAnticheat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTabCompleter implements TabCompleter {
    private final BlazeAnticheat plugin;

    public CommandTabCompleter(BlazeAnticheat plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> s = new ArrayList<>();
        if (args.length == 1) {
            String q = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(q))
                    .collect(Collectors.toList());
        }
        
        String n = command.getName().toLowerCase();
        if ((n.equals("ban") || n.equals("mute") || n.equals("vcmute")) && args.length == 2) {
            return Arrays.asList("10m", "1h", "1d", "1w", "perm");
        }
        
        return s;
    }
}
