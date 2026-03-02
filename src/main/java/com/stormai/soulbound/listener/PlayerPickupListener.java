package com.stormai.soulbound.listener;

import com.stormai.soulbound.SoulBoundSMP;
import com.stormai.soulbound.manager.SoulManager;
import com.stormai.soulbound.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerPickupListener implements Listener {
    private final SoulBoundSMP plugin;
    private final SoulManager soulManager;
    
    public PlayerPickupListener() {
        plugin = SoulBoundSMP.getInstance();
        soulManager = plugin.getSoulManager();
    }
    
    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        
        if (ItemUtil.isSoulItem(item)) {
            event.setCancelled(true);
            event.getItem().remove();
            
            int amount = ItemUtil.getSoulAmount(item);
            soulManager.addSoul(player.getUniqueId(), amount);
            
            // Check if should exit weakened state
            if (soulManager.isWeakened(player.getUniqueId()) && soulManager.getSouls(player.getUniqueId()) >= 1) {
                soulManager.setWeakened(player.getUniqueId(), false);
                soulManager.updateMaxHealth(player);
                player.sendMessage(plugin.getConfig().getString("messages.weakened-exit", 
                        "&aYou are no longer weakened!"));
            }
            
            player.sendMessage(plugin.getConfig().getString("messages.soul-pickup", 
                    "&aYou picked up &e{count} &aSoul(s)! Total: &e{total}")
                    .replace("{count}", String.valueOf(amount))
                    .replace("{total}", String.valueOf(soulManager.getSouls(player.getUniqueId()))));
        }
    }
}