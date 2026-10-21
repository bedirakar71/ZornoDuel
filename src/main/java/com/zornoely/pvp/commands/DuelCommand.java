package com.zornoely.pvp.commands;

import com.zornoely.pvp.ZornoElyPvP;
import com.zornoely.pvp.managers.ArenaManager;
import com.zornoely.pvp.listeners.PvPListener;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DuelCommand implements CommandExecutor, TabCompleter {

    private final ZornoElyPvP plugin;

    public DuelCommand(ZornoElyPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Bu komut sadece oyuncular icindir.");
            return true;
        }

        try {
            if (args.length == 0) {
                p.sendMessage("§8[§dZornoEly§8] §cKullanım: /duel <davet|kabul|reddet|menu|sandik|berabere|list>");
                return true;
            }

            String sub = args[0].toLowerCase();
            ArenaManager am = plugin.getArenaManager();

            switch (sub) {
                case "menu" -> PvPListener.openQueueMenu(p, plugin);
                case "berabere" -> {
                    if (am.isInMatch(p)) {
                        ArenaManager.ActiveMatch match = am.getMatch(p);
                        am.endMatchDraw(match, "Düello berabere sonlandırıldı! Spawna gönderiliyorsunuz.");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cAktif bir düelloda değilsiniz!");
                    }
                }
                case "sandik" -> {
                    if (am.getLoot(p.getUniqueId()) != null) {
                        PvPListener.openLootMenu(p, plugin);
                        p.sendMessage("§8[§dZornoEly§8] §aGanimet sandığı açıldı!");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cAktif bir ganimet sandığınız yok veya süreniz doldu!");
                    }
                }
                case "davet" -> {
                    if (args.length < 2) {
                        p.sendMessage("§8[§dZornoEly§8] §cOyuncu ismi girin: /duel davet <oyuncu>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null || !target.isOnline()) {
                        p.sendMessage("§8[§dZornoEly§8] §cOyuncu bulunamadı veya çevrimdışı!");
                        return true;
                    }
                    if (target.equals(p)) {
                        p.sendMessage("§8[§dZornoEly§8] §cKendine davet atamazsın!");
                        return true;
                    }
                    PvPListener.openInviteMenu(p, target);
                }
                case "kabul" -> {
                    if (args.length < 2) {
                        p.sendMessage("§8[§dZornoEly§8] §cKullanım: /duel kabul <oyuncu>");
                        return true;
                    }
                    Player senderPlayer = Bukkit.getPlayer(args[1]);
                    if (senderPlayer == null || !plugin.getQueueManager().acceptInvite(p, senderPlayer)) {
                        p.sendMessage("§8[§dZornoEly§8] §cAktif bir davet bulunamadı!");
                    }
                }
                case "reddet" -> {
                    if (args.length < 2) {
                        p.sendMessage("§8[§dZornoEly§8] §cKullanım: /duel reddet <oyuncu>");
                        return true;
                    }
                    Player senderPlayer = Bukkit.getPlayer(args[1]);
                    if (senderPlayer != null && plugin.getQueueManager().denyInvite(p, senderPlayer)) {
                        p.sendMessage("§8[§dZornoEly§8] §cDaveti reddettiniz.");
                        senderPlayer.sendMessage("§8[§dZornoEly§8] §c" + p.getName() + " düello davetinizi reddetti.");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cAktif bir davet bulunamadı!");
                    }
                }
                case "list" -> {
                    if (am.getArenas().isEmpty()) {
                        p.sendMessage("§8[§dZornoEly§8] §cBölgede kurulmuş arena bulunamadı!");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §e--- Arena Listesi ---");
                        for (ArenaManager.Arena a : am.getArenas()) {
                            String status = a.busy ? "§cDOLU" : "§aBOŞ";
                            p.sendMessage("§7- §f" + a.name + " §8[" + status + "§8] §7(Tip: §e" + a.type + "§7)");
                        }
                    }
                }
                case "create" -> {
                    if (!p.isOp()) { p.sendMessage("§8[§dZornoEly§8] §cBunu yapmaya yetkiniz yok!"); return true; }
                    if (args.length < 2) { p.sendMessage("§8[§dZornoEly§8] §cArena ismi girin!"); return true; }
                    am.createArena(args[1]);
                    p.sendMessage("§8[§dZornoEly§8] §a" + args[1] + " arenası oluşturuldu! Varsayılan tip: HEPSI");
                }
                case "setspawn1" -> {
                    if (!p.isOp()) { p.sendMessage("§8[§dZornoEly§8] §cBunu yapmaya yetkiniz yok!"); return true; }
                    if (args.length < 2) { p.sendMessage("§8[§dZornoEly§8] §cArena ismi girin!"); return true; }
                    ArenaManager.Arena a = am.getArena(args[1]);
                    if (a != null) {
                        a.spawn1 = p.getLocation();
                        am.saveArenas();
                        p.sendMessage("§8[§dZornoEly§8] §a" + a.name + " arenası Spawn 1 ayarlandı!");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cBöyle bir arena bulunamadı!");
                    }
                }
                case "setspawn2" -> {
                    if (!p.isOp()) { p.sendMessage("§8[§dZornoEly§8] §cBunu yapmaya yetkiniz yok!"); return true; }
                    if (args.length < 2) { p.sendMessage("§8[§dZornoEly§8] §cArena ismi girin!"); return true; }
                    ArenaManager.Arena a = am.getArena(args[1]);
                    if (a != null) {
                        a.spawn2 = p.getLocation();
                        am.saveArenas();
                        p.sendMessage("§8[§dZornoEly§8] §a" + a.name + " arenası Spawn 2 ayarlandı!");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cBöyle bir arena bulunamadı!");
                    }
                }
                case "settype" -> {
                    if (!p.isOp()) { p.sendMessage("§8[§dZornoEly§8] §cBunu yapmaya yetkiniz yok!"); return true; }
                    if (args.length < 3) { p.sendMessage("§8[§dZornoEly§8] §cKullanım: /duel settype <arena> <elytra|trap|hepsi>"); return true; }
                    ArenaManager.Arena a = am.getArena(args[1]);
                    if (a != null) {
                        a.type = args[2].toLowerCase();
                        am.saveArenas();
                        p.sendMessage("§8[§dZornoEly§8] §a" + a.name + " tipi " + args[2] + " olarak ayarlandı!");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cBöyle bir arena bulunamadı!");
                    }
                }
                case "tp" -> {
                    if (!p.isOp()) { p.sendMessage("§8[§dZornoEly§8] §cBunu yapmaya yetkiniz yok!"); return true; }
                    if (args.length < 2) { p.sendMessage("§8[§dZornoEly§8] §cArena ismi girin!"); return true; }
                    ArenaManager.Arena a = am.getArena(args[1]);
                    if (a != null && a.spawn1 != null) {
                        p.teleport(a.spawn1);
                        p.sendMessage("§8[§dZornoEly§8] §a" + a.name + " arenasına ışınlandınız!");
                    } else {
                        p.sendMessage("§8[§dZornoEly§8] §cArena bulunamadı veya Spawn 1 noktası ayarlanmamış!");
                    }
                }
                default -> p.sendMessage("§8[§dZornoEly§8] §cBilinmeyen komut! Kullanım: /duel <davet|kabul|reddet|menu|sandik|berabere|list>");
            }
        } catch (Exception ex) {
            p.sendMessage("§8[§dZornoEly§8] §cKomut çalıştırılırken bir hata oluştu!");
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player p)) return completions;

        try {
            if (args.length == 1) {
                List<String> allowed = new ArrayList<>(List.of("davet", "kabul", "reddet", "menu", "sandik", "berabere", "list"));
                if (p.isOp()) {
                    allowed.addAll(List.of("create", "setspawn1", "setspawn2", "settype", "tp"));
                }
                for (String s : allowed) {
                    if (s.startsWith(args[0].toLowerCase())) completions.add(s);
                }
            } else if (args.length == 2) {
                String sub = args[0].toLowerCase();
                if (sub.equals("davet") || sub.equals("kabul") || sub.equals("reddet")) {
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (!target.equals(p) && target.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(target.getName());
                        }
                    }
                } else if (p.isOp() && List.of("setspawn1", "setspawn2", "settype", "tp").contains(sub)) {
                    plugin.getArenaManager().getArenas().forEach(a -> {
                        if (a.name.toLowerCase().startsWith(args[1].toLowerCase())) completions.add(a.name);
                    });
                }
            } else if (args.length == 3 && p.isOp() && args[0].equalsIgnoreCase("settype")) {
                for (String type : List.of("elytra", "trap", "hepsi")) {
                    if (type.startsWith(args[2].toLowerCase())) completions.add(type);
                }
            }
        } catch (Exception ignored) {}
        return completions;
    }
            }
