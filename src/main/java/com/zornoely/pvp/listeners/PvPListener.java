package com.zornoely.pvp.listeners;

import com.zornoely.pvp.ZornoElyPvP;
import com.zornoely.pvp.managers.ArenaManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PvPListener implements Listener {

    private final ZornoElyPvP plugin;
    private static final Map<UUID, Player> inviteTargets = new HashMap<>();

    public PvPListener(ZornoElyPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        Entity target = e.getRightClicked();
        if (target instanceof Player) return;

        String name = target.getCustomName();
        if (name != null) {
            String stripped = name.toLowerCase();
            if (stripped.contains("duel") || stripped.contains("pvp") || stripped.contains("zornoely")) {
                e.setCancelled(true);
                openQueueMenu(e.getPlayer(), plugin);
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (plugin.getArenaManager().isInMatch(p)) {
            String msg = e.getMessage().toLowerCase();
            if (!msg.startsWith("/duel berabere")) {
                e.setCancelled(true);
                p.sendMessage("§8[§dZornoEly§8] §cDüellodayken başka komut kullanamazsınız! (Savaşı bitirmek için: §e/duel berabere§c)");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ArenaManager am = plugin.getArenaManager();
        if (am.isInMatch(p)) {
            ArenaManager.ActiveMatch match = am.getMatch(p);
            if (match.mode.equalsIgnoreCase("elytra") && e.getBlock().getType() == Material.COBWEB) {
                e.setCancelled(true);
                p.sendMessage("§8[§dZornoEly§8] §cBu düelloda örümcek ağı kapalı!");
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        ArenaManager am = plugin.getArenaManager();

        if (am.isInMatch(victim)) {
            ArenaManager.ActiveMatch match = am.getMatch(victim);
            UUID winnerId = match.p1.equals(victim.getUniqueId()) ? match.p2 : match.p1;
            Player winner = Bukkit.getPlayer(winnerId);

            List<ItemStack> drops = new ArrayList<>(e.getDrops());
            e.getDrops().clear();

            if (winner != null && winner.isOnline()) {
                am.setLoot(winner.getUniqueId(), drops);
                winner.sendMessage("§8[§dZornoEly§8] §aTebrikler! Ölen oyuncunun eşyalarını almak için 40 saniyen var.");
                winner.sendMessage("§8[§dZornoEly§8] §eTekrar açmak için: §f/duel sandik");
                openLootMenu(winner, plugin);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> victim.performCommand("hub"), 5L);
            am.endMatchWithWinner(match, winnerId);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        plugin.getQueueManager().removeFromQueue(p);

        ArenaManager am = plugin.getArenaManager();
        if (am.isInMatch(p)) {
            ArenaManager.ActiveMatch match = am.getMatch(p);
            UUID winnerId = match.p1.equals(p.getUniqueId()) ? match.p2 : match.p1;
            Player winner = Bukkit.getPlayer(winnerId);

            List<ItemStack> inventoryItems = new ArrayList<>();
            for (ItemStack item : p.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) inventoryItems.add(item);
            }
            p.getInventory().clear();

            if (winner != null && winner.isOnline()) {
                am.setLoot(winner.getUniqueId(), inventoryItems);
                winner.sendMessage("§8[§dZornoEly§8] §aRakibiniz oyundan kaçtığı için kazandınız!");
                openLootMenu(winner, plugin);
            }

            am.endMatchWithWinner(match, winnerId);
        }
    }

    public static void openQueueMenu(Player p, ZornoElyPvP plugin) {
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("§5ZornoEly PvP Sırası"));

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta1 = elytra.getItemMeta();
        meta1.displayName(Component.text("§b§lELYTRA PVP SIRA"));
        meta1.lore(List.of(
                Component.text("§7Elitra kullanımı açık."),
                Component.text("§8------------------------"),
                Component.text("§eSıradaki Oyuncular: §f" + plugin.getQueueManager().getQueueSize("elytra")),
                Component.text("§8------------------------"),
                Component.text(plugin.getQueueManager().isInQueue(p, "elytra") ? "§a§l[SIRADASIN - Çıkmak için Tıkla]" : "§eSıraya girmek için tıkla!")
        ));
        elytra.setItemMeta(meta1);

        ItemStack cobweb = new ItemStack(Material.COBWEB);
        ItemMeta meta2 = cobweb.getItemMeta();
        meta2.displayName(Component.text("§6§lTRAP PVP SIRA"));
        meta2.lore(List.of(
                Component.text("§7Ağlar ve bloklar açık."),
                Component.text("§8------------------------"),
                Component.text("§eSıradaki Oyuncular: §f" + plugin.getQueueManager().getQueueSize("trap")),
                Component.text("§8------------------------"),
                Component.text(plugin.getQueueManager().isInQueue(p, "trap") ? "§a§l[SIRADASIN - Çıkmak için Tıkla]" : "§eSıraya girmek için tıkla!")
        ));
        cobweb.setItemMeta(meta2);

        gui.setItem(11, elytra);
        gui.setItem(15, cobweb);
        p.openInventory(gui);
    }

    public static void openInviteMenu(Player p, Player target) {
        inviteTargets.put(p.getUniqueId(), target);
        Inventory gui = Bukkit.createInventory(null, 27, Component.text("§8Düello Modu Seç"));

        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta m1 = elytra.getItemMeta();
        m1.displayName(Component.text("§bElytra PvP Daveti"));
        elytra.setItemMeta(m1);

        ItemStack cobweb = new ItemStack(Material.COBWEB);
        ItemMeta m2 = cobweb.getItemMeta();
        m2.displayName(Component.text("§6Trap PvP Daveti"));
        cobweb.setItemMeta(m2);

        gui.setItem(11, elytra);
        gui.setItem(15, cobweb);
        p.openInventory(gui);
    }

    public static void openLootMenu(Player p, ZornoElyPvP plugin) {
        List<ItemStack> items = plugin.getArenaManager().getLoot(p.getUniqueId());
        if (items == null) return;

        Inventory gui = Bukkit.createInventory(null, 54, Component.text("§4Ganimet Sandığı"));
        for (int i = 0; i < Math.min(items.size(), 54); i++) {
            gui.setItem(i, items.get(i));
        }
        p.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        if (title.contains("ZornoEly PvP Sırası")) {
            e.setCancelled(true);
            if (e.getRawSlot() == 11) {
                p.closeInventory();
                plugin.getQueueManager().toggleQueue(p, "elytra");
            } else if (e.getRawSlot() == 15) {
                p.closeInventory();
                plugin.getQueueManager().toggleQueue(p, "trap");
            }
        } else if (title.contains("Düello Modu Seç")) {
            e.setCancelled(true);
            Player target = inviteTargets.get(p.getUniqueId());
            if (target != null) {
                if (e.getRawSlot() == 11) {
                    p.closeInventory();
                    plugin.getQueueManager().sendInvite(p, target, "elytra");
                } else if (e.getRawSlot() == 15) {
                    p.closeInventory();
                    plugin.getQueueManager().sendInvite(p, target, "trap");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().contains("Ganimet Sandığı") && e.getPlayer() instanceof Player p) {
            List<ItemStack> remaining = new ArrayList<>();
            for (ItemStack is : e.getInventory().getContents()) {
                if (is != null && is.getType() != Material.AIR) remaining.add(is);
            }
            plugin.getArenaManager().setLoot(p.getUniqueId(), remaining);
        }
    }
}
