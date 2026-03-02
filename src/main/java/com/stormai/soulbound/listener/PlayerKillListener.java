package com.stormai.soulbound.listener;

import com.stormai.soulbound.SoulBoundSMP;
import com.stormai.soulbound.manager.SoulManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerKillEvent;

public class PlayerKillListener implements Listener {
    private final SoulBoundSMP plugin;
    private final SoulManager soulManager;
    
    public PlayerKillListener() {
        plugin = SoulBoundSMP.getInstance();
        soulManager = plugin.getSoulManager();
    }
    
    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        Player killer = event.getPlayer();
        Player victim = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        
        if (victim == null || killer.equals(victim)) {
            return;
        }
        
        // Check cooldown
        if (!soulManager.canGainSoul(killer.getUniqueId())) {
            killer.sendMessage(plugin.getConfig().getString("messages.kill-cooldown", 
                    "&cYou are on cooldown for gaining souls!"));
            return;
        }
        
        // Grant soul
        soulManager.addSoul(killer.getUniqueId(), 1);
        soulManager.setLastKillTime(killer.getUniqueId());
        
        killer.sendMessage(plugin.getConfig().getString("messages.kill-soul", 
                "&aYou gained a soul! Total: &e{count}")
                .replace("{count}", String.valueOf(soulManager.getSouls(killer.getUniqueId()))));
    }
}