package com.zornoely.duel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUI {
    public static final Component QUEUE_TITLE  = Component.text("» PvP Sırası", NamedTextColor.DARK_GRAY);
    public static final Component INVITE_TITLE = Component.text("» Düello Modu Seç", NamedTextColor.DARK_GRAY);

    public static class DuelHolder implements InventoryHolder {
        private final String type;
        public DuelHolder(String type) { this.type = type; }
        public String getType() { return type; }
        @Override public Inventory getInventory() { return null; }
    }

    public static void openQueue(Player p, ZornoElyDuel plugin) {
        Inventory inv = Bukkit.createInventory(new DuelHolder("queue"), 27, QUEUE_TITLE);

        int activeMatches = 0;
        for (Arena a : plugin.getArenaManager().getAll()) if (a.isBusy()) activeMatches++;

        QueueManager qm = plugin.getQueueManager();
        boolean inElytra = qm.isInQueue(p.getUniqueId(), ArenaType.ELYTRA);
        boolean inTrap   = qm.isInQueue(p.getUniqueId(), ArenaType.TRAP);
        int elytraSize = qm.getQueue(ArenaType.ELYTRA).size();
        int trapSize   = qm.getQueue(ArenaType.TRAP).size();

        List<String> elytraLore = new ArrayList<>();
        elytraLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Elitra kullanımı: " + ChatColor.GREEN + "Açık");
        elytraLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Blok koyma: " + ChatColor.RED + "Kapalı");
        elytraLore.add("");
        elytraLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Sıradaki Oyuncu: " + ChatColor.AQUA + elytraSize);
        elytraLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Aktif Maç: " + ChatColor.AQUA + activeMatches);
        elytraLore.add("");
        elytraLore.add(inElytra ? ChatColor.RED + "» Çıkmak için tıkla!" : ChatColor.YELLOW + "» Sıraya girmek için tıkla!");

        inv.setItem(11, createItem(Material.ELYTRA, ChatColor.AQUA + "" + ChatColor.BOLD + "ELYTRA PVP", elytraLore));

        List<String> trapLore = new ArrayList<>();
        trapLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Ağ ve Blok: " + ChatColor.GREEN + "Açık");
        trapLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Elitra: " + ChatColor.RED + "Kapalı");
        trapLore.add("");
        trapLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Sıradaki Oyuncu: " + ChatColor.GOLD + trapSize);
        trapLore.add(ChatColor.DARK_GRAY + "▪ " + ChatColor.GRAY + "Aktif Maç: " + ChatColor.GOLD + activeMatches);
        trapLore.add("");
        trapLore.add(inTrap ? ChatColor.RED + "» Çıkmak için tıkla!" : ChatColor.YELLOW + "» Sıraya girmek için tıkla!");

        inv.setItem(15, createItem(Material.COBWEB, ChatColor.GOLD + "" + ChatColor.BOLD + "TRAP PVP", trapLore));

        p.openInventory(inv);
    }

    public static void openInvite(Player p, Player target) {
        Inventory inv = Bukkit.createInventory(new DuelHolder("invite"), 27, INVITE_TITLE);

        List<String> elytraLore = new ArrayList<>();
        elytraLore.add(ChatColor.GRAY + "- Blok: " + ChatColor.GREEN + "Açık");
        elytraLore.add(ChatColor.GRAY + "- Ağ: "   + ChatColor.RED + "Kapalı");
        elytraLore.add(ChatColor.GRAY + "- Elytra: " + ChatColor.GREEN + "Açık");
        elytraLore.add("");
        elytraLore.add(ChatColor.YELLOW + "Tıkla ve " + target.getName() + " oyuncusuna gönder!");

        inv.setItem(11, createItem(Material.ELYTRA, ChatColor.AQUA + "" + ChatColor.BOLD + "Elytra PvP Daveti", elytraLore));

        List<String> trapLore = new ArrayList<>();
        trapLore.add(ChatColor.GRAY + "- Blok: " + ChatColor.GREEN + "Açık");
        trapLore.add(ChatColor.GRAY + "- Ağ: "   + ChatColor.GREEN + "Açık");
        trapLore.add(ChatColor.GRAY + "- Elytra: " + ChatColor.RED + "Kapalı");
        trapLore.add("");
        trapLore.add(ChatColor.YELLOW + "Tıkla ve " + target.getName() + " oyuncusuna gönder!");

        inv.setItem(15, createItem(Material.COBWEB, ChatColor.GOLD + "" + ChatColor.BOLD + "Trap PvP Daveti", trapLore));

        p.openInventory(inv);
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
