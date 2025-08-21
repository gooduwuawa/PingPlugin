package com.ping;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PingPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("ping").setExecutor(this);
        getLogger().info("PingPlugin Enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by the player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            int ping = getPing(player);
            String pingColor = getPingColor(ping);
            String message = "&8[&ePing&8] &7" + player.getName() + " " + pingColor + ping + " &8ms.";
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Player not found " + args[0]);
                return true;
            }

            int ping = getPing(target);
            String pingColor = getPingColor(ping);
            String message = "&8[&ePing&8] &7" + target.getName() + " " + pingColor + ping + " &8ms.";
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        return true;
    }

    private int getPing(Player player) {
        return player.getPing();
    }

    private String getPingColor(int ping) {
        if (ping <= 120) {
            return "&a";
        } else if (ping <= 180) {
            return "&e";
        } else {
            return "&c";
        }
    }
}
