package com.stormai.soulbound;

import com.stormai.soulbound.command.SoulsCommand;
import com.stormai.soulbound.gui.SoulsGUI;
import com.stormai.soulbound.listener.PlayerDeathListener;
import com.stormai.soulbound.listener.PlayerKillListener;
import com.stormai.soulbound.listener.PlayerPickupListener;
import com.stormai.soulbound.manager.SoulManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoulBoundSMP extends JavaPlugin {
    private static SoulBoundSMP instance;
    private SoulManager soulManager;
    private SoulsGUI soulsGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        soulManager = new SoulManager();
        soulsGUI = new SoulsGUI();
        
        getCommand("souls").setExecutor(new SoulsCommand());
        getCommand("soulbound").setExecutor(new SoulsCommand()); // alias
        
        getServer().getPluginManager().registerEvents(new PlayerKillListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPickupListener(), this);
        
        getLogger().info("SoulBoundSMP has been enabled!");
    }

    @Override
    public void onDisable() {
        soulManager.saveAllData();
        getLogger().info("SoulBoundSMP has been disabled!");
    }
    
    public static SoulBoundSMP getInstance() {
        return instance;
    }
    
    public SoulManager getSoulManager() {
        return soulManager;
    }
    
    public SoulsGUI getSoulsGUI() {
        return soulsGUI;
    }
}