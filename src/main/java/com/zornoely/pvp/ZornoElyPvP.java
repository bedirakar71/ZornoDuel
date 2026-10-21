package com.zornoely.pvp;

import com.zornoely.pvp.commands.DuelCommand;
import com.zornoely.pvp.listeners.PvPListener;
import com.zornoely.pvp.managers.ArenaManager;
import com.zornoely.pvp.managers.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ZornoElyPvP extends JavaPlugin {

    private static ZornoElyPvP instance;
    private ArenaManager arenaManager;
    private QueueManager queueManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.arenaManager = new ArenaManager(this);
        this.queueManager = new QueueManager(this);

        this.arenaManager.loadArenas();

        DuelCommand duelCmd = new DuelCommand(this);
        getCommand("duel").setExecutor(duelCmd);
        getCommand("duel").setTabCompleter(duelCmd);

        getServer().getPluginManager().registerEvents(new PvPListener(this), this);

        getLogger().info("ZornoElyPvP 1.21.4 basariyla aktif edildi!");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) {
            arenaManager.saveArenas();
        }
    }

    public static ZornoElyPvP getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public QueueManager getQueueManager() { return queueManager; }
}
