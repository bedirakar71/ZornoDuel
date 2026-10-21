package com.zornoely.pvp.managers;

import com.zornoely.pvp.ZornoElyPvP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class ArenaManager {

    private final ZornoElyPvP plugin;

    public static class Arena {
        public String name;
        public Location spawn1;
        public Location spawn2;
        public String type = "hepsi";
        public boolean busy = false;

        public Arena(String name) {
            this.name = name;
        }
    }

    public static class ActiveMatch {
        public Arena arena;
        public UUID p1;
        public UUID p2;
        public String mode;
        public BukkitTask timerTask;
    }

    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, ActiveMatch> activeMatches = new HashMap<>();
    private final Map<UUID, List<ItemStack>> lootStorage = new HashMap<>();
    private final Map<UUID, BukkitTask> lootTimers = new HashMap<>();

    public ArenaManager(ZornoElyPvP plugin) {
        this.plugin = plugin;
    }

    public void createArena(String name) {
        if (!arenas.containsKey(name.toLowerCase())) {
            arenas.put(name.toLowerCase(), new Arena(name));
            saveArenas();
        }
    }

    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public boolean isInMatch(Player p) {
        return activeMatches.containsKey(p.getUniqueId());
    }

    public ActiveMatch getMatch(Player p) {
        return activeMatches.get(p.getUniqueId());
    }

    public void startMatch(Player p1, Player p2, String mode) {
        Arena freeArena = null;
        for (Arena a : arenas.values()) {
            if (!a.busy && a.spawn1 != null && a.spawn2 != null) {
                if (a.type.equalsIgnoreCase(mode) || a.type.equalsIgnoreCase("hepsi")) {
                    freeArena = a;
                    break;
                }
            }
        }

        if (freeArena == null) {
            p1.sendMessage("§8[§dZornoEly§8] §cBoş veya spawn noktaları ayarlanmış arena bulunamadı!");
            p2.sendMessage("§8[§dZornoEly§8] §cBoş veya spawn noktaları ayarlanmış arena bulunamadı!");
            return;
        }

        freeArena.busy = true;

        ActiveMatch match = new ActiveMatch();
        match.arena = freeArena;
        match.p1 = p1.getUniqueId();
        match.p2 = p2.getUniqueId();
        match.mode = mode;

        activeMatches.put(p1.getUniqueId(), match);
        activeMatches.put(p2.getUniqueId(), match);

        p1.teleport(freeArena.spawn1);
        p2.teleport(freeArena.spawn2);

        String msg = "§8[§dZornoEly§8] §aDüello başladı! Arena: §e" + freeArena.name + "§a. Süreniz 20 dakikadır.";
        p1.sendMessage(msg);
        p2.sendMessage(msg);

        match.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                endMatchDraw(match, "20 dakika boyunca kimse ölmediği için düello berabere bitti!");
            }
        }.runTaskLater(plugin, 20 * 60 * 20L);
    }

    public void endMatchDraw(ActiveMatch match, String reason) {
        if (match.timerTask != null) match.timerTask.cancel();

        Player p1 = Bukkit.getPlayer(match.p1);
        Player p2 = Bukkit.getPlayer(match.p2);

        activeMatches.remove(match.p1);
        activeMatches.remove(match.p2);

        match.arena.busy = false;

        if (p1 != null && p1.isOnline()) {
            p1.sendMessage("§8[§dZornoEly§8] §e" + reason);
            p1.performCommand("hub");
        }
        if (p2 != null && p2.isOnline()) {
            p2.sendMessage("§8[§dZornoEly§8] §e" + reason);
            p2.performCommand("hub");
        }
    }

    public void endMatchWithWinner(ActiveMatch match, UUID winnerId) {
        if (match.timerTask != null) match.timerTask.cancel();

        activeMatches.remove(match.p1);
        activeMatches.remove(match.p2);

        Player winner = Bukkit.getPlayer(winnerId);
        if (winner != null && winner.isOnline()) {
            startLootTimer(winner, match.arena);
        } else {
            match.arena.busy = false;
        }
    }

    public void setLoot(UUID winner, List<ItemStack> items) {
        lootStorage.put(winner, items);
    }

    public List<ItemStack> getLoot(UUID winner) {
        return lootStorage.get(winner);
    }

    public void startLootTimer(Player winner, Arena arena) {
        if (lootTimers.containsKey(winner.getUniqueId())) {
            lootTimers.get(winner.getUniqueId()).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            int time = 40;

            @Override
            public void run() {
                if (time <= 0) {
                    lootStorage.remove(winner.getUniqueId());
                    lootTimers.remove(winner.getUniqueId());

                    arena.busy = false;

                    if (winner.isOnline()) {
                        winner.sendMessage("§8[§dZornoEly§8] §eGanimet süren doldu, Hub'a gönderildin!");
                        winner.performCommand("hub");
                    }
                    cancel();
                    return;
                }

                if (winner.isOnline()) {
                    winner.sendActionBar(net.kyori.adventure.text.Component.text("§e§lGANİMET SÜRESİ: §f" + time + " §esaniye"));
                }
                time--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        lootTimers.put(winner.getUniqueId(), task);
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("arenas")) return;

        for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
            Arena a = new Arena(key);
            a.type = config.getString("arenas." + key + ".type", "hepsi");
            a.spawn1 = config.getLocation("arenas." + key + ".spawn1");
            a.spawn2 = config.getLocation("arenas." + key + ".spawn2");
            arenas.put(key.toLowerCase(), a);
        }
    }

    public void saveArenas() {
        FileConfiguration config = plugin.getConfig();
        config.set("arenas", null);
        for (Arena a : arenas.values()) {
            config.set("arenas." + a.name + ".type", a.type);
            config.set("arenas." + a.name + ".spawn1", a.spawn1);
            config.set("arenas." + a.name + ".spawn2", a.spawn2);
        }
        plugin.saveConfig();
    }
}
