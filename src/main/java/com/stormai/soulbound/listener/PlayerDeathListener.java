package com.stormai.soulbound.listener;

import com.stormai.soulbound.SoulBoundSMP;
import com.stormai.soulbound.manager.SoulManager;
import com.stormai.soulbound.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathListener implements Listener {
    private final SoulBoundSMP plugin;
    private final SoulManager soulManager;
    
    public PlayerDeathListener() {
        plugin = SoulBoundSMP.getInstance();
        soulManager = plugin.getSoulManager();
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        
        // Drop soul item
        ItemStack soulItem = ItemUtil.createSoulItem(1);
        loc.getWorld().dropItemNaturally(loc, soulItem);
        
        // Update weakened state
        int currentSouls = soulManager.getSouls(player.getUniqueId());
        if (currentSouls > 0) {
            soulManager.removeSouls(player.getUniqueId(), 1);
        }
        
        // Check if should be weakened
        if (soulManager.getSouls(player.getUniqueId()) == 0 && !soulManager.isWeakened(player.getUniqueId())) {
            soulManager.setWeakened(player.getUniqueId(), true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    soulManager.updateMaxHealth(player);
                    player.sendMessage(plugin.getConfig().getString("messages.weakened-enter", 
                            "&cYou have run out of souls! You are now weakened."));
                }
            }, 1L);
        }
    }
}