package com.zornoely.pvp.managers;

import com.zornoely.pvp.ZornoElyPvP;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueManager {

    private final ZornoElyPvP plugin;
    private final List<UUID> elytraQueue = new ArrayList<>();
    private final List<UUID> trapQueue = new ArrayList<>();
    private final Map<UUID, UUID> invites = new HashMap<>();
    private final Map<UUID, String> inviteModes = new HashMap<>();

    public QueueManager(ZornoElyPvP plugin) {
        this.plugin = plugin;
    }

    public void toggleQueue(Player p, String mode) {
        UUID u = p.getUniqueId();
        if (mode.equalsIgnoreCase("elytra")) {
            trapQueue.remove(u);
            if (elytraQueue.contains(u)) {
                elytraQueue.remove(u);
                p.sendMessage("§8[§dZornoEly§8] §cElytra PvP sırasından çıktın!");
            } else {
                elytraQueue.add(u);
                p.sendMessage("§8[§dZornoEly§8] §aElytra PvP sırasına girdin!");
                checkQueue("elytra");
            }
        } else if (mode.equalsIgnoreCase("trap")) {
            elytraQueue.remove(u);
            if (trapQueue.contains(u)) {
                trapQueue.remove(u);
                p.sendMessage("§8[§dZornoEly§8] §cTrap PvP sırasından çıktın!");
            } else {
                trapQueue.add(u);
                p.sendMessage("§8[§dZornoEly§8] §aTrap PvP sırasına girdin!");
                checkQueue("trap");
            }
        }
    }

    public void removeFromQueue(Player p) {
        elytraQueue.remove(p.getUniqueId());
        trapQueue.remove(p.getUniqueId());
    }

    private void checkQueue(String mode) {
        List<UUID> queue = mode.equalsIgnoreCase("elytra") ? elytraQueue : trapQueue;
        if (queue.size() >= 2) {
            UUID id1 = queue.remove(0);
            UUID id2 = queue.remove(0);

            Player p1 = plugin.getServer().getPlayer(id1);
            Player p2 = plugin.getServer().getPlayer(id2);

            if (p1 != null && p2 != null) {
                plugin.getArenaManager().startMatch(p1, p2, mode);
            }
        }
    }

    public void sendInvite(Player sender, Player target, String mode) {
        invites.put(sender.getUniqueId(), target.getUniqueId());
        inviteModes.put(sender.getUniqueId(), mode);

        sender.sendMessage("§8[§dZornoEly§8] §a" + target.getName() + " oyuncusuna " + (mode.equalsIgnoreCase("elytra") ? "§bElytra PvP" : "§6Trap PvP") + " §adaveti gönderildi!");
        target.sendMessage("§8[§dZornoEly§8] §e" + sender.getName() + " sana " + (mode.equalsIgnoreCase("elytra") ? "§bElytra PvP" : "§6Trap PvP") + " §edüello daveti yolladı! Kabul etmek için: §a/duel kabul " + sender.getName() + " §e, reddetmek için: §c/duel reddet " + sender.getName());
    }

    public boolean acceptInvite(Player target, Player sender) {
        UUID senderId = sender.getUniqueId();
        if (invites.containsKey(senderId) && invites.get(senderId).equals(target.getUniqueId())) {
            String mode = inviteModes.get(senderId);
            invites.remove(senderId);
            inviteModes.remove(senderId);

            plugin.getArenaManager().startMatch(sender, target, mode);
            return true;
        }
        return false;
    }

    public boolean denyInvite(Player target, Player sender) {
        UUID senderId = sender.getUniqueId();
        if (invites.containsKey(senderId) && invites.get(senderId).equals(target.getUniqueId())) {
            invites.remove(senderId);
            inviteModes.remove(senderId);
            return true;
        }
        return false;
    }

    public int getQueueSize(String mode) {
        return mode.equalsIgnoreCase("elytra") ? elytraQueue.size() : trapQueue.size();
    }

    public boolean isInQueue(Player p, String mode) {
        return mode.equalsIgnoreCase("elytra") ? elytraQueue.contains(p.getUniqueId()) : trapQueue.contains(p.getUniqueId());
    }
}
