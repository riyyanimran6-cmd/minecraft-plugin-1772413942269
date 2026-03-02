package com.stormai.soulbound.gui;

import com.stormai.soulbound.SoulBoundSMP;
import com.stormai.soulbound.manager.SoulManager;
import com.stormai.soulbound.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class SoulsGUI {
    private final SoulBoundSMP plugin;
    private final SoulManager soulManager;
    
    public SoulsGUI() {
        plugin = SoulBoundSMP.getInstance();
        soulManager = plugin.getSoulManager();
    }
    
    public void openSoulsGUI(Player player) {
        int souls = soulManager.getSouls(player.getUniqueId());
        String title = plugin.getConfig().getString("gui.title", "&6Souls: {souls}")
                .replace("{souls}", String.valueOf(souls));
        
        Inventory inv = Bukkit.createInventory(null, 27, 
                org.bukkit.ChatColor.translateAlternateColorCodes('&', title));
        
        // Fill with glass panes
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
        
        // Souls display
        ItemStack soulItem = ItemUtil.createSoulItem(souls);
        inv.setItem(10, soulItem);
        
        // Ability items
        int slot = 13;
        for (String abilityKey : plugin.getConfig().getConfigurationSection("abilities").getKeys(false)) {
            String path = "abilities." + abilityKey;
            String name = plugin.getConfig().getString(path + ".name", abilityKey);
            int cost = plugin.getConfig().getInt(path + ".cost", 1);
            int duration = plugin.getConfig().getInt(path + ".duration", 30);
            String effect = plugin.getConfig().getString(path + ".potion-effect", "strength");
            int amplifier = plugin.getConfig().getInt(path + ".potion-amplifier", 0);
            
            ItemStack abilityItem = new ItemStack(Material.PAPER);
            ItemMeta meta = abilityItem.getItemMeta();
            meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfig().getString("gui.ability-name", "&e{ability}").replace("{ability}", name)));
            
            List<String> lore = Arrays.asList(
                    plugin.getConfig().getString("gui.cost", "&7Cost: &e{cost} Soul(s)").replace("{cost}", String.valueOf(cost)),
                    plugin.getConfig().getString("gui.duration", "&7Duration: &b{duration}s").replace("{duration}", String.valueOf(duration)),
                    plugin.getConfig().getString("gui.effect", "&7Effect: &a{effect} {amp}").replace("{effect}", effect).replace("{amp}", amplifier > 0 ? "II" : ""),
                    "",
                    plugin.getConfig().getString("gui.click", "&eClick to activate!")
            );
            meta.setLore(lore);
            
            // Check if can afford
            if (souls >= cost && !soulManager.isOnCooldown(player.getUniqueId(), abilityKey)) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else if (souls < cost) {
                lore.add(plugin.getConfig().getString("gui.cannot-afford", "&cNot enough souls!"));
                meta.setLore(lore);
            } else {
                long remaining = soulManager.getCooldownRemaining(player.getUniqueId(), abilityKey);
                lore.add(plugin.getConfig().getString("gui.cooldown", "&cCooldown: &e{time}s").replace("{time}", String.valueOf(remaining)));
                meta.setLore(lore);
            }
            
            abilityItem.setItemMeta(meta);
            inv.setItem(slot++, abilityItem);
        }
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfig().getString("gui.close", "&cClose")));
        close.setItemMeta(closeMeta);
        inv.setItem(22, close);
        
        player.openInventory(inv);
    }
    
    public void handleInventoryClick(Player player, int slot) {
        int souls = soulManager.getSouls(player.getUniqueId());
        int abilityIndex = slot - 13;
        
        if (abilityIndex >= 0 && abilityIndex < plugin.getConfig().getConfigurationSection("abilities").getKeys(false).size()) {
            String abilityKey = (String) plugin.getConfig().getConfigurationSection("abilities").getKeys(false).toArray()[abilityIndex];
            String path = "abilities." + abilityKey;
            int cost = plugin.getConfig().getInt(path + ".cost", 1);
            int duration = plugin.getConfig().getInt(path + ".duration", 30);
            String effect = plugin.getConfig().getString(path + ".potion-effect", "strength");
            int amplifier = plugin.getConfig().getInt(path + ".potion-amplifier", 0);
            
            if (souls >= cost) {
                if (soulManager.isOnCooldown(player.getUniqueId(), abilityKey)) {
                    player.sendMessage(plugin.getConfig().getString("messages.ability-on-cooldown", 
                            "&c{ability} is on cooldown for {time} seconds!")
                            .replace("{ability}", plugin.getConfig().getString(path + ".name", abilityKey))
                            .replace("{time}", String.valueOf(soulManager.getCooldownRemaining(player.getUniqueId(), abilityKey))));
                    return;
                }
                
                soulManager.removeSouls(player.getUniqueId(), cost);
                soulManager.setCooldown(player.getUniqueId(), abilityKey, 
                        plugin.getConfig().getInt("cooldowns." + abilityKey, duration));
                
                // Apply potion effect
                org.bukkit.potion.PotionEffect potion = new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.getByName(effect.toUpperCase()),
                        duration * 20,
                        amplifier,
                        false,
                        true
                );
                player.addPotionEffect(potion);
                
                String msg = plugin.getConfig().getString("messages.ability-activated", 
                        "&aYou activated {ability} for {duration} seconds!")
                        .replace("{ability}", plugin.getConfig().getString(path + ".name", abilityKey))
                        .replace("{duration}", String.valueOf(duration));
                player.sendMessage(msg);
                
                // Refresh GUI
                openSoulsGUI(player);
            } else {
                player.sendMessage(plugin.getConfig().getString("messages.ability-no-souls", 
                        "&cYou need {cost} souls to activate {ability}!")
                        .replace("{cost}", String.valueOf(cost))
                        .replace("{ability}", plugin.getConfig().getString(path + ".name", abilityKey)));
            }
        }
    }
}