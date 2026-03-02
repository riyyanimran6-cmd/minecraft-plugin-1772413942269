package com.stormai.soulbound.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemUtil {
    private static final String SOUL_ITEM_NAME = "§bSoul";
    private static final List<String> SOUL_ITEM_LORE = Arrays.asList(
            "§7Drop of a player's essence.",
            "§7Right-click to consume (not implemented)"
    );
    
    public static ItemStack createSoulItem(int amount) {
        ItemStack item = new ItemStack(Material.ENDER_PEARL, amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(SOUL_ITEM_NAME);
        meta.setLore(SOUL_ITEM_LORE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }
    
    public static boolean isSoulItem(ItemStack item) {
        if (item == null || item.getType() != Material.ENDER_PEARL) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return meta.getDisplayName().equals(SOUL_ITEM_NAME);
    }
    
    public static int getSoulAmount(ItemStack item) {
        return item != null ? item.getAmount() : 0;
    }
}