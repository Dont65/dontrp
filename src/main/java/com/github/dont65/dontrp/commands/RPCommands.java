package com.github.dont65.dontrp.commands;

import com.github.dont65.dontrp.DontRP;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class RPCommands implements CommandExecutor, TabCompleter {
    private final DontRP plugin;
    private final Map<UUID, Integer> playerRollMin = new HashMap<>();
    private final Map<UUID, Integer> playerRollMax = new HashMap<>();

    public RPCommands(DontRP plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();
        if (!(sender instanceof Player player)) {
            if (cmd.equals("rp") && args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                 plugin.reloadConfig(); plugin.reloadColorsConfig(); sender.sendMessage("Reloaded.");
            }
            return true;
        }

        switch (cmd) {
            case "rpname" -> handleRpName(player, args);
            case "me", "do", "try", "todo" -> handleChatAction(player, cmd, args);
            case "roll" -> handleRoll(player, args);
            case "description", "desc" -> handleDescription(player, args);
            case "rp" -> handleRP(player, args);
            case "adm" -> handleAdmin(player, args);
        }
        return true;
    }

    private void handleRpName(Player p, String[] args) {
        if (args.length == 0) {
            String name = plugin.getNameManager().getFullName(p.getUniqueId());
            p.sendMessage(DontRP.colorize(name != null ? "&aВаше имя: &e" + name : "&cИмя не установлено."));
        } else if (args.length >= 2) {
            plugin.getNameManager().setRpName(p.getUniqueId(), args[0], args[1]);
            p.sendMessage(DontRP.colorize("&aИмя установлено: &e" + args[0] + " " + args[1]));
        } else if (args[0].equalsIgnoreCase("remove")) {
            plugin.getNameManager().removeRpName(p.getUniqueId());
            p.sendMessage(DontRP.colorize("&aИмя удалено."));
        }
    }

    private void handleChatAction(Player p, String cmd, String[] args) {
        if (args.length == 0) return;
        String text = String.join(" ", args);
        String coloredName = plugin.getNameManager().getAdaptiveName(p);
        String format = plugin.getConfig().getString("formats." + cmd);
        String finalMsg = "";

        if (cmd.equals("todo")) {
            String sep = plugin.getConfig().getString("settings.todo-separator", "*");
            if (!text.contains(sep)) return;
            String[] parts = text.split(Pattern.quote(sep), 2);
            finalMsg = format.replace("{colored_name}", coloredName).replace("{action}", parts[0].trim()).replace("{speech}", parts[1].trim());
        } else if (cmd.equals("try")) {
            String res = ThreadLocalRandom.current().nextBoolean() ? plugin.getConfig().getString("formats.try-success") : plugin.getConfig().getString("formats.try-fail");
            finalMsg = format.replace("{colored_name}", coloredName).replace("{text}", text).replace("{result}", res);
        } else {
            finalMsg = format.replace("{colored_name}", coloredName).replace("{text}", text);
        }
        broadcast(p, DontRP.colorize(finalMsg));
    }

    private void handleRoll(Player p, String[] args) {
        if (args.length == 3 && args[0].equalsIgnoreCase("default")) {
            try {
                setPlayerRollMin(p.getUniqueId(), Integer.parseInt(args[1]));
                setPlayerRollMax(p.getUniqueId(), Integer.parseInt(args[2]));
                p.sendMessage(DontRP.colorize("&aНастройки Roll обновлены!"));
            } catch (Exception e) { p.sendMessage("&cОшибка числа."); }
            return;
        }
        int min = getPlayerRollMin(p.getUniqueId());
        int max = getPlayerRollMax(p.getUniqueId());
        int res = ThreadLocalRandom.current().nextInt(min, max + 1);
        String format = plugin.getConfig().getString("formats.roll");
        broadcast(p, DontRP.colorize(format.replace("{colored_name}", plugin.getNameManager().getAdaptiveName(p)).replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max)).replace("{result}", String.valueOf(res))));
    }

    private void handleRP(Player p, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("namecolor")) {
            if (!p.hasPermission("dontrp.rpname_color.admin")) {
                p.sendMessage(DontRP.colorize("&cУ вас нет разрешения dontrp.rpname_color.admin"));
                return;
            }
            plugin.getNameManager().setPlayerColor(p.getUniqueId(), args[1]);
            p.sendMessage(DontRP.colorize("&aЦвет изменен на " + args[1] + "Ваше Имя"));
        } else if (args.length > 0 && args[0].equalsIgnoreCase("settings")) {
            plugin.getInventoryListener().openMainMenu(p);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("reload") && p.hasPermission("dontrp.admin")) {
            plugin.reloadConfig(); plugin.reloadColorsConfig(); p.sendMessage("Reloaded.");
        }
    }

    private void handleDescription(Player p, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
            String d = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            plugin.getNameManager().setDescription(p.getUniqueId(), d);
            p.sendMessage(DontRP.colorize("&aОписание установлено."));
        } else {
            String d = plugin.getNameManager().getDescription(p.getUniqueId());
            p.sendMessage(DontRP.colorize(d != null ? "&aВаше описание: &e" + d : "&cОписание не установлено."));
        }
    }

    private void handleAdmin(Player p, String[] args) {
        if (!p.hasPermission("dontrp.admin") || args.length < 2) return;
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) return;
        if (args[0].equalsIgnoreCase("rpname") && args.length >= 3 && args[2].equalsIgnoreCase("remove")) {
            plugin.getNameManager().removeRpName(target.getUniqueId());
            p.sendMessage("Удалено.");
        }
    }

    public void setPlayerRollMin(UUID u, int v) { playerRollMin.put(u, v); }
    public void setPlayerRollMax(UUID u, int v) { playerRollMax.put(u, v); }
    public int getPlayerRollMin(UUID u) { return playerRollMin.getOrDefault(u, plugin.getConfig().getInt("settings.roll.default-min", 0)); }
    public int getPlayerRollMax(UUID u) { return playerRollMax.getOrDefault(u, plugin.getConfig().getInt("settings.roll.default-max", 100)); }
    public void resetPlayerRollSettings(UUID u) { playerRollMin.remove(u); playerRollMax.remove(u); }

    private void broadcast(Player s, String m) {
        double r = plugin.getConfig().getDouble("settings.local-chat.radius", 100.0);
        s.sendMessage(m);
        for (Player p : s.getWorld().getPlayers()) {
            if (!p.equals(s) && p.getLocation().distance(s.getLocation()) <= r) p.sendMessage(m);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) return Arrays.asList("settings", "reload", "namecolor");
        return null;
    }
}
