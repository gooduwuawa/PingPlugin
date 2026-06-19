package com.ping;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PingPlugin extends JavaPlugin implements CommandExecutor {

    private static class PingRange {
        int min, max;
        PingRange(int min, int max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
        }
    }

    private final Map<UUID, PingRange> customPingRanges = new HashMap<>();
    private final Map<UUID, Integer> currentFakePings = new HashMap<>();

    @Override
    public void onEnable() {
        getCommand("ping").setExecutor(this);
        getCommand("setping").setExecutor(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PingExpansion(this).register();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PingRange range = customPingRanges.get(player.getUniqueId());
                    if (range != null) {
                        int nextPing = ThreadLocalRandom.current().nextInt(range.min, range.max + 1);
                        currentFakePings.put(player.getUniqueId(), nextPing);
                    } else {
                        currentFakePings.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        getLogger().info("PingPlugin Enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setping")) {
            if (!sender.hasPermission("ping.admin")) {
                sender.sendMessage(ChatColor.RED + "No Permission");
                return true;
            }
            if (args.length < 2) return false;

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;

            if (args[1].equalsIgnoreCase("reset")) {
                customPingRanges.remove(target.getUniqueId());
                currentFakePings.remove(target.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Reset " + target.getName());
                return true;
            }

            if (args.length == 3) {
                try {
                    int min = Integer.parseInt(args[1]);
                    int max = Integer.parseInt(args[2]);
                    customPingRanges.put(target.getUniqueId(), new PingRange(min, max));
                    sender.sendMessage(ChatColor.AQUA + "Setting successful: " + min + " ~ " + max + "ms");
                } catch (NumberFormatException e) {
                    sender.sendMessage("Enter a number.");
                }
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("ping")) {
            Player target = (args.length == 0 && sender instanceof Player) ? (Player) sender : Bukkit.getPlayer(args[0]);
            if (target != null) {
                int ping = getStoredPing(target);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    "&8[&ePing&8] &7" + target.getName() + " " + getPingColor(ping) + ping + " &8ms."));
            }
            return true;
        }
        return false;
    }

    public int getStoredPing(Player player) {
        return currentFakePings.getOrDefault(player.getUniqueId(), player.getPing());
    }

    public String getPingColor(int ping) {
        if (ping <= 120) return "&a";
        if (ping <= 180) return "&e";
        return "&c";
    }

    public class PingExpansion extends PlaceholderExpansion {
        private final PingPlugin plugin;
        public PingExpansion(PingPlugin plugin) { this.plugin = plugin; }
        @Override public @NotNull String getIdentifier() { return "customping"; }
        @Override public @NotNull String getAuthor() { return "YourName"; }
        @Override public @NotNull String getVersion() { return "1.0"; }
        @Override public boolean persist() { return true; }

        @Override
        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return "0";
            if (params.equalsIgnoreCase("value")) return String.valueOf(plugin.getStoredPing(player));
            if (params.equalsIgnoreCase("color")) return ChatColor.translateAlternateColorCodes('&', plugin.getPingColor(plugin.getStoredPing(player)));
            return null;
        }
    }
}