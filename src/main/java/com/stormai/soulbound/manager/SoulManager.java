package com.stormai.soulbound.manager;

import com.stormai.soulbound.SoulBoundSMP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SoulManager {
    private final SoulBoundSMP plugin;
    private final Map<UUID, SoulData> playerData;
    private final Map<UUID, Map<String, Long>> cooldowns;
    private final long killCooldownMillis;
    private final double weakenedHealth;
    private final double normalHealth;
    
    public SoulManager() {
        plugin = SoulBoundSMP.getInstance();
        playerData = new HashMap<>();
        cooldowns = new HashMap<>();
        
        killCooldownMillis = plugin.getConfig().getLong("cooldowns.kill", 60) * 1000L;
        weakenedHealth = plugin.getConfig().getDouble("weakened-health", 4.0);
        normalHealth = plugin.getConfig().getDouble("normal-health", 20.0);
        
        loadAllData();
    }
    
    private static class SoulData {
        int souls;
        long lastKillTime;
        boolean weakened;
        double originalMaxHealth; // Store original health when first loaded
    }
    
    public void addSoul(UUID uuid, int amount) {
        SoulData data = getOrLoadData(uuid);
        data.souls += amount;
        markDirty(uuid);
    }
    
    public void removeSouls(UUID uuid, int amount) {
        SoulData data = getOrLoadData(uuid);
        data.souls = Math.max(0, data.souls - amount);
        markDirty(uuid);
    }
    
    public int getSouls(UUID uuid) {
        return getOrLoadData(uuid).souls;
    }
    
    public void setLastKillTime(UUID uuid) {
        SoulData data = getOrLoadData(uuid);
        data.lastKillTime = System.currentTimeMillis();
        markDirty(uuid);
    }
    
    public boolean canGainSoul(UUID uuid) {
        SoulData data = getOrLoadData(uuid);
        return (System.currentTimeMillis() - data.lastKillTime) >= killCooldownMillis;
    }
    
    public void setWeakened(UUID uuid, boolean weakened) {
        SoulData data = getOrLoadData(uuid);
        data.weakened = weakened;
        markDirty(uuid);
    }
    
    public boolean isWeakened(UUID uuid) {
        return getOrLoadData(uuid).weakened;
    }
    
    public void updateMaxHealth(Player player) {
        SoulData data = getOrLoadData(player.getUniqueId());
        
        // Store original health if not stored
        if (data.originalMaxHealth == 0) {
            data.originalMaxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            markDirty(player.getUniqueId());
        }
        
        double newHealth = data.weakened ? weakenedHealth : data.originalMaxHealth;
        player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
    }
    
    public void setCooldown(UUID uuid, String ability, long seconds) {
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(ability, System.currentTimeMillis() + (seconds * 1000L));
    }
    
    public boolean isOnCooldown(UUID uuid, String ability) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return false;
        
        Long endTime = playerCooldowns.get(ability);
        if (endTime == null) return false;
        
        if (System.currentTimeMillis() >= endTime) {
            playerCooldowns.remove(ability);
            return false;
        }
        return true;
    }
    
    public long getCooldownRemaining(UUID uuid, String ability) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0;
        
        Long endTime = playerCooldowns.get(ability);
        if (endTime == null) return 0;
        
        long remaining = endTime - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000L : 0;
    }
    
    private SoulData getOrLoadData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> loadData(uuid));
    }
    
    private SoulData loadData(UUID uuid) {
        File playerFile = new File(plugin.getDataFolder() + "/players", uuid + ".yml");
        if (!playerFile.exists()) {
            SoulData data = new SoulData();
            data.souls = 0;
            data.lastKillTime = 0;
            data.weakened = false;
            data.originalMaxHealth = 20.0; // Default
            return data;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        SoulData data = new SoulData();
        data.souls = config.getInt("souls", 0);
        data.lastKillTime = config.getLong("lastKillTime", 0);
        data.weakened = config.getBoolean("weakened", false);
        data.originalMaxHealth = config.getDouble("originalMaxHealth", 20.0);
        return data;
    }
    
    private void saveData(UUID uuid, SoulData data) {
        File playerDir = new File(plugin.getDataFolder(), "players");
        if (!playerDir.exists()) playerDir.mkdirs();
        
        File playerFile = new File(playerDir, uuid + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("souls", data.souls);
        config.set("lastKillTime", data.lastKillTime);
        config.set("weakened", data.weakened);
        config.set("originalMaxHealth", data.originalMaxHealth);
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data for " + uuid + ": " + e.getMessage());
        }
    }
    
    public void saveAllData() {
        for (Map.Entry<UUID, SoulData> entry : playerData.entrySet()) {
            saveData(entry.getKey(), entry.getValue());
        }
    }
    
    private void markDirty(UUID uuid) {
        // This would be needed if we implement auto-save intervals
        // For now, we only save on disable or manually
    }
    
    public void loadAllData() {
        File playerDir = new File(plugin.getDataFolder(), "players");
        if (!playerDir.exists()) return;
        
        File[] files = playerDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;
        
        for (File file : files) {
            try {
                String filename = file.getName();
                UUID uuid = UUID.fromString(filename.substring(0, filename.length() - 4));
                SoulData data = loadData(uuid);
                playerData.put(uuid, data);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid player data file: " + file.getName());
            }
        }
    }
}