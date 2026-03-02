package com.stormai.soulbound.command;

import com.stormai.soulbound.SoulBoundSMP;
import com.stormai.soulbound.gui.SoulsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoulsCommand implements CommandExecutor {
    private final SoulBoundSMP plugin = SoulBoundSMP.getInstance();
    private final SoulsGUI gui;
    
    public SoulsCommand() {
        this.gui = plugin.getSoulsGUI();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }
        
        if (args.length == 1 && args[0].equalsIgnoreCase("revive")) {
            handleRevive(player);
            return true;
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("revive")) {
            // Admin revive another player
            if (!player.hasPermission("soulbound.revive.other")) {
                player.sendMessage(plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission!"));
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target != null) {
                handleReviveOthers(player, target);
            } else {
                player.sendMessage(plugin.getConfig().getString("messages.revive-no-target", "&cPlayer not found or not weakened!"));
            }
            return true;
        }
        
        // Open GUI
        gui.openSoulsGUI(player);
        return true;
    }
    
    private void handleRevive(Player player) {
        if (!player.hasPermission("soulbound.revive")) {
            player.sendMessage(plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission!"));
            return;
        }
        
        var soulManager = plugin.getSoulManager();
        if (!soulManager.isWeakened(player.getUniqueId())) {
            player.sendMessage(plugin.getConfig().getString("messages.revive-not-weakened", "&cYou are not in a weakened state!"));
            return;
        }
        
        if (soulManager.getSouls(player.getUniqueId()) < 1) {
            player.sendMessage(plugin.getConfig().getString("messages.revive-no-soul", "&cYou need at least 1 soul to revive!"));
            return;
        }
        
        soulManager.removeSouls(player.getUniqueId(), 1);
        soulManager.setWeakened(player.getUniqueId(), false);
        soulManager.updateMaxHealth(player);
        player.sendMessage(plugin.getConfig().getString("messages.revive-self", "&aYou have revived yourself from the weakened state!"));
    }
    
    private void handleReviveOthers(Player reviver, Player target) {
        var soulManager = plugin.getSoulManager();
        if (!soulManager.isWeakened(target.getUniqueId())) {
            reviver.sendMessage(plugin.getConfig().getString("messages.revive-target-not-weakened", "&cThat player is not weakened!"));
            return;
        }
        
        if (soulManager.getSouls(reviver.getUniqueId()) < 1) {
            reviver.sendMessage(plugin.getConfig().getString("messages.revive-no-soul", "&cYou need at least 1 soul to revive someone!"));
            return;
        }
        
        soulManager.removeSouls(reviver.getUniqueId(), 1);
        soulManager.setWeakened(target.getUniqueId(), false);
        soulManager.updateMaxHealth(target);
        
        String msg = plugin.getConfig().getString("messages.revive-success", "&a{target} has been revived from weakened state by {player}!")
                .replace("{target}", target.getName())
                .replace("{player}", reviver.getName());
        reviver.sendMessage(msg);
        target.sendMessage(plugin.getConfig().getString("messages.revive-target", "&aYou have been revived from the weakened state!"));
    }
}