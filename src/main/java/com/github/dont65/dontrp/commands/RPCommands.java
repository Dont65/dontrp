package com.github.dont65.dontrp.commands;

import com.github.dont65.dontrp.DontRP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final Map<UUID, Integer> playerRollMin;
    private final Map<UUID, Integer> playerRollMax;

    public RPCommands(DontRP plugin) {
        this.plugin = plugin;
        this.playerRollMin = new HashMap<>();
        this.playerRollMax = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "rpname":
                return handleRpName(sender, args);
            case "me":
            case "do":
            case "try":
            case "todo":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Только для игроков!");
                    return true;
                }
                return handleChatCommand((Player) sender, cmd, args);
            case "roll":
                return handleRollCommand(sender, args);
            case "rp":
                return handleRP(sender, args);
            case "description":
            case "desc":
                return handleDescription(sender, args);
            case "adm":
                return handleAdminCommand(sender, args);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("rp")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                completions.add("settings");
                completions.add("reload");
                completions.add("namecolor");
                completions.add("help");

                // Фильтруем по введенному тексту
                String input = args[0].toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String completion : completions) {
                    if (completion.toLowerCase().startsWith(input)) {
                        filtered.add(completion);
                    }
                }
                return filtered;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("namecolor")) {
                // Подсказки для цветов
                List<String> colorCompletions = new ArrayList<>();
                colorCompletions.add("default");
                colorCompletions.add("remove");

                // Добавляем цвета из конфига
                if (plugin.getColorsConfig().getConfigurationSection("colors") != null) {
                    for (String colorId : plugin.getColorsConfig().getConfigurationSection("colors").getKeys(false)) {
                        colorCompletions.add(colorId);
                    }
                }

                // Добавляем цветовые коды
                colorCompletions.add("&0");
                colorCompletions.add("&1");
                colorCompletions.add("&2");
                colorCompletions.add("&3");
                colorCompletions.add("&4");
                colorCompletions.add("&5");
                colorCompletions.add("&6");
                colorCompletions.add("&7");
                colorCompletions.add("&8");
                colorCompletions.add("&9");
                colorCompletions.add("&a");
                colorCompletions.add("&b");
                colorCompletions.add("&c");
                colorCompletions.add("&d");
                colorCompletions.add("&e");
                colorCompletions.add("&f");

                // Фильтруем
                String input = args[1].toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String completion : colorCompletions) {
                    if (completion.toLowerCase().startsWith(input)) {
                        filtered.add(completion);
                    }
                }
                return filtered;
            } else if (args.length == 3 && args[0].equalsIgnoreCase("namecolor")) {
                // Подсказки для имен игроков
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return playerNames;
            }
        } else if (cmd.equals("description") || cmd.equals("desc")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                completions.add("set");
                completions.add("help");

                // Добавляем имена игроков для просмотра
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }

                // Фильтруем
                String input = args[0].toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String completion : completions) {
                    if (completion.toLowerCase().startsWith(input)) {
                        filtered.add(completion);
                    }
                }
                return filtered;
            }
        } else if (cmd.equals("roll")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                completions.add("default");
                completions.add("help");
                return completions;
            }
        } else if (cmd.equals("rpname")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                completions.add("help");
                completions.add("remove");

                // Добавляем имена игроков для просмотра
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }

                // Фильтруем
                String input = args[0].toLowerCase();
                List<String> filtered = new ArrayList<>();
                for (String completion : completions) {
                    if (completion.toLowerCase().startsWith(input)) {
                        filtered.add(completion);
                    }
                }
                return filtered;
            }
        }

        return null;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String getMsg(String key) {
        return color(plugin.getConfig().getString("messages." + key, "&cСообщение не найдено: " + key));
    }

    private String getFormat(String key) {
        return plugin.getConfig().getString("formats." + key, "{name} {text}");
    }

    // Основной обработчик команд RPName
    private boolean handleRpName(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Показать свое RP имя
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только для игроков!");
                return true;
            }
            Player player = (Player) sender;
            if (plugin.getNameManager().hasRpName(player.getUniqueId())) {
                player.sendMessage(getMsg("your-rpname").replace("{name}",
                    plugin.getNameManager().getFullName(player.getUniqueId())));
            } else {
                player.sendMessage(getMsg("no-rpname"));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help")) {
            sender.sendMessage(color("&e--- Помощь по команде /rpname ---"));
            sender.sendMessage(color("&7/rpname &f- Показать свое RP имя"));
            sender.sendMessage(color("&7/rpname <игрок> &f- Показать RP имя игрока"));
            sender.sendMessage(color("&7/rpname <Имя> <Фамилия> &f- Установить RP имя"));
            sender.sendMessage(color("&7/rpname remove &f- Удалить свое RP имя"));
            return true;
        }

        // Удаление своего RP имени
        if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только для игроков!");
                return true;
            }
            Player player = (Player) sender;
            plugin.getNameManager().removeRpName(player.getUniqueId());
            plugin.getNameManager().removePlayerColor(player.getUniqueId());
            player.sendMessage(getMsg("name-removed"));
            return true;
        }

        // Просмотр RP имени другого игрока
        if (args.length == 1) {
            if (!sender.hasPermission("dontrp.rpname.view")) {
                sender.sendMessage(getMsg("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(getMsg("player-not-found"));
                return true;
            }

            if (plugin.getNameManager().hasRpName(target.getUniqueId())) {
                sender.sendMessage(getMsg("player-rpname")
                    .replace("{player}", target.getName())
                    .replace("{name}", plugin.getNameManager().getFullName(target.getUniqueId())));
            } else {
                sender.sendMessage(getMsg("player-no-rpname").replace("{player}", target.getName()));
            }
            return true;
        }

        // Админские команды RPName (удаление и установка для других)
        if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("dontrp.admin")) {
                sender.sendMessage(getMsg("no-permission"));
                return true;
            }

            String targetName = args[1];
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                plugin.getNameManager().removeRpName(target.getUniqueId());
                plugin.getNameManager().removePlayerColor(target.getUniqueId());
                sender.sendMessage(getMsg("name-removed-other").replace("{player}", target.getName()));
                target.sendMessage(getMsg("name-removed"));
            } else {
                sender.sendMessage(getMsg("player-not-found"));
            }
            return true;
        }

        // Установка своего RP имени
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        return handleRpNameSet(player, args);
    }

    // Список RP имен (только для админов через /adm rpname list)
    private boolean handleRpNameList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dontrp.admin")) {
            sender.sendMessage(getMsg("no-permission"));
            return true;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
            sender.sendMessage(color("&e--- Все RP имена (Names.yml) ---"));
            for (String uuidStr : plugin.getNameManager().getAllNames().keySet()) {
                String name = plugin.getNameManager().getFullName(UUID.fromString(uuidStr));
                String colorId = plugin.getNameManager().getPlayerColorId(UUID.fromString(uuidStr));
                String colorInfo = colorId != null ? " [Цвет: " + colorId + "]" : "";
                sender.sendMessage(color("&7" + uuidStr + ": &f" + name + colorInfo));
            }
        } else {
            sender.sendMessage(color("&e--- Онлайн игроки с RP ---"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getNameManager().hasRpName(p.getUniqueId())) {
                     sender.sendMessage(color("&7" + p.getName() + ": &f" + plugin.getNameManager().getFullName(p.getUniqueId())));
                }
            }
        }
        return true;
    }

    // Установка RP имени
    private boolean handleRpNameSet(Player player, String[] args) {
        boolean requireLast = plugin.getConfig().getBoolean("settings.require-lastname");
        if (args.length < (requireLast ? 2 : 1)) {
            player.sendMessage(getMsg("usage-rpname"));
            return true;
        }

        String first = args[0];
        String last = (args.length > 1) ? args[1] : "";

        Pattern pattern = Pattern.compile(plugin.getConfig().getString("settings.allowed-regex"));
        if (!pattern.matcher(first).matches() || (!last.isEmpty() && !pattern.matcher(last).matches())) {
            player.sendMessage(getMsg("name-invalid-regex"));
            return true;
        }

        int maxFirst = plugin.getConfig().getInt("settings.limits.first-name");
        int maxLast = plugin.getConfig().getInt("settings.limits.last-name");

        if (first.length() > maxFirst || last.length() > maxLast) {
            player.sendMessage(getMsg("name-length"));
            return true;
        }

        plugin.getNameManager().setRpName(player.getUniqueId(), first, last);
        player.sendMessage(getMsg("name-set").replace("{name}", first + " " + last));
        return true;
    }

    // Обработчик чат-команд (me, do, try, todo)
    private boolean handleChatCommand(Player player, String cmd, String[] args) {
        if (args.length == 0) {
            player.sendMessage(getMsg("usage-" + cmd));
            return true;
        }

        String text = String.join(" ", args);
        String coloredName = plugin.getNameManager().getAdaptiveName(player);

        switch (cmd) {
            case "me":
                String meMsg = color(getFormat("me")
                        .replace("{colored_name}", coloredName)
                        .replace("{name}", coloredName)
                        .replace("{text}", text));
                broadcastRadius(player, meMsg);
                break;

            case "do":
                String doMsg = color(getFormat("do")
                        .replace("{colored_name}", coloredName)
                        .replace("{name}", coloredName)
                        .replace("{text}", text));
                broadcastRadius(player, doMsg);
                break;

            case "try":
                boolean success = ThreadLocalRandom.current().nextBoolean();
                String result = success ? getFormat("try-success") : getFormat("try-fail");
                String tryMsg = color(getFormat("try")
                        .replace("{colored_name}", coloredName)
                        .replace("{name}", coloredName)
                        .replace("{text}", text)
                        .replace("{result}", result));
                broadcastRadius(player, tryMsg);
                break;

            case "todo":
                String fullArgs = String.join(" ", args);
                String separator = plugin.getConfig().getString("settings.todo-separator");

                if (!fullArgs.contains(separator)) {
                     player.sendMessage(getMsg("usage-todo"));
                     return true;
                }

                String[] parts = fullArgs.split(Pattern.quote(separator), 2);
                if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                     player.sendMessage(getMsg("usage-todo"));
                     return true;
                }

                String action = parts[0].trim();
                String speech = parts[1].trim();
                String todoMsg = color(getFormat("todo")
                        .replace("{colored_name}", coloredName)
                        .replace("{name}", coloredName)
                        .replace("{action}", action)
                        .replace("{speech}", speech));
                broadcastRadius(player, todoMsg);
                break;
        }
        return true;
    }

    // Обработчик команды Roll
    private boolean handleRollCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        // Помощь
        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            player.sendMessage(color("&e--- Помощь по команде /roll ---"));
            player.sendMessage(color("&7/roll &f- Бросить кубик с текущими настройками"));
            player.sendMessage(color("&7/roll <число> &f- Бросить кубик от 0 до указанного числа"));
            player.sendMessage(color("&7/roll <min> <max> &f- Бросить кубик в указанном диапазоне"));
            player.sendMessage(color("&7/roll default <min> <max> &f- Установить значения по умолчанию"));
            player.sendMessage(color("&7/rp settings &f- Настроить значения через меню"));
            return true;
        }

        // Команда /roll default <min> <max> - для себя, без прав
        if (args.length >= 1 && args[0].equalsIgnoreCase("default")) {
            if (args.length != 3) {
                player.sendMessage(getMsg("roll-default-usage"));
                return true;
            }

            try {
                int min = Integer.parseInt(args[1]);
                int max = Integer.parseInt(args[2]);

                if (min >= max) {
                    player.sendMessage(getMsg("roll-min-greater"));
                    return true;
                }

                // Сохраняем настройки для игрока
                setPlayerRollMin(player.getUniqueId(), min);
                setPlayerRollMax(player.getUniqueId(), max);

                player.sendMessage(getMsg("roll-default-set")
                        .replace("{min}", String.valueOf(min))
                        .replace("{max}", String.valueOf(max)));

            } catch (NumberFormatException e) {
                player.sendMessage(color("&cИспользуйте только числа."));
            }
            return true;
        }

        // Обычный бросок кубика
        return handleRoll(player, args);
    }

    // Обычный бросок кубика
    private boolean handleRoll(Player player, String[] args) {
        int min, max;

        // БЕЗОПАСНОЕ ПОЛУЧЕНИЕ ЗНАЧЕНИЙ - проверка на null
        Integer playerMin = playerRollMin.get(player.getUniqueId());
        Integer playerMax = playerRollMax.get(player.getUniqueId());

        if (playerMin != null && playerMax != null) {
            min = playerMin;
            max = playerMax;
        } else {
            min = plugin.getConfig().getInt("settings.roll.default-min");
            max = plugin.getConfig().getInt("settings.roll.default-max");
        }

        boolean allowOverride = plugin.getConfig().getBoolean("settings.roll.allow-override");

        if (args.length == 2 && allowOverride) {
            try {
                min = Integer.parseInt(args[0]);
                max = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(color("&cИспользуйте только числа."));
                return true;
            }
        } else if (args.length == 1 && allowOverride) {
            try {
                max = Integer.parseInt(args[0]);
                min = 0;
            } catch (NumberFormatException e) {
                player.sendMessage(color("&cИспользуйте только числа."));
                return true;
            }
        } else if (args.length > 0 && !allowOverride) {
            player.sendMessage(color("&cПереопределение отключено. Используйте /roll без аргументов или /rp settings"));
            return true;
        }

        if (min > max) { int temp = min; min = max; max = temp; }

        int result = ThreadLocalRandom.current().nextInt(min, max + 1);
        String coloredName = plugin.getNameManager().getAdaptiveName(player);

        String msg = color(getFormat("roll")
                .replace("{colored_name}", coloredName)
                .replace("{name}", coloredName)
                .replace("{min}", String.valueOf(min))
                .replace("{max}", String.valueOf(max))
                .replace("{result}", String.valueOf(result)));

        broadcastRadius(player, msg);
        return true;
    }

    private boolean handleRP(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getMsg("usage-rp"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "settings":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Только для игроков!");
                    return true;
                }
                plugin.getInventoryListener().openMainMenu((Player) sender);
                return true;
            case "reload":
                if (!sender.hasPermission("dontrp.admin")) {
                    sender.sendMessage(getMsg("no-permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.reloadColorsConfig();
                sender.sendMessage(getMsg("reload"));
                return true;
            case "namecolor":
                return handleNameColor(sender, args);
            case "help":
                sender.sendMessage(color("&e--- Помощь по команде /rp ---"));
                sender.sendMessage(color("&7/rp settings &f- Открыть меню настроек"));
                sender.sendMessage(color("&7/rp reload &f- Перезагрузить конфиг (админы)"));
                sender.sendMessage(color("&7/rp namecolor <цвет> [игрок] &f- Установить цвет имени (админы)"));
                sender.sendMessage(color("&7/rp help &f- Показать эту справку"));
                return true;
            default:
                sender.sendMessage(getMsg("usage-rp"));
                return true;
        }
    }

    // Обработчик команды NameColor
    private boolean handleNameColor(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(color("&cИспользование: /rp namecolor <цвет> [игрок]"));
            return true;
        }

        String colorValue = args[1];
        Player targetPlayer = null;
        boolean settingForOther = false;

        // Определяем цель
        if (args.length >= 3) {
            // Установка цвета для другого игрока
            if (!sender.hasPermission("dontrp.admin")) {
                sender.sendMessage(getMsg("no-permission"));
                return true;
            }

            targetPlayer = Bukkit.getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage(getMsg("player-not-found"));
                return true;
            }
            settingForOther = true;
        } else if (sender instanceof Player) {
            // Установка цвета для себя
            targetPlayer = (Player) sender;
        } else {
            sender.sendMessage("Консоль должна указать игрока: /rp namecolor <цвет> <игрок>");
            return true;
        }

        // Проверяем системный цвет
        if (!plugin.getConfig().getBoolean("settings.color-system-enabled", true)) {
            sender.sendMessage(getMsg("color-system-disabled"));
            return true;
        }

        // Для команды namecolor нужны права админа
        if (!sender.hasPermission("dontrp.admin") && !sender.hasPermission("dontrp.rpname_color.admin")) {
            sender.sendMessage(getMsg("no-permission"));
            return true;
        }

        // Проверяем валидность цвета
        if (colorValue.equalsIgnoreCase("default") || colorValue.equalsIgnoreCase("remove")) {
            // Сброс цвета
            plugin.getNameManager().removePlayerColor(targetPlayer.getUniqueId());

            if (settingForOther) {
                sender.sendMessage(getMsg("color-removed-other").replace("{player}", targetPlayer.getName()));
                targetPlayer.sendMessage(getMsg("color-removed"));
            } else {
                sender.sendMessage(getMsg("color-removed"));
            }
            return true;
        }

        // Проверяем, является ли это прямым цветовым кодом
        boolean isColorCode = colorValue.startsWith("&");

        if (isColorCode) {
            // Проверяем валидность цветового кода
            Pattern colorPattern = Pattern.compile("^&[0-9a-fk-or]$", Pattern.CASE_INSENSITIVE);
            if (!colorPattern.matcher(colorValue).matches()) {
                sender.sendMessage(color("&cНеверный цветовой код. Примеры: &6, &c, &a, &b"));
                return true;
            }

            // Устанавливаем прямой цветовой код
            plugin.getNameManager().setPlayerColor(targetPlayer.getUniqueId(), colorValue);

            String colorName = colorValue + "Цвет";
            String message = getMsg("color-set").replace("{color}", color(colorValue + colorName));

            if (settingForOther) {
                sender.sendMessage(getMsg("color-set-other")
                        .replace("{player}", targetPlayer.getName())
                        .replace("{color}", color(colorValue + colorName)));
                targetPlayer.sendMessage(message);
            } else {
                sender.sendMessage(message);
            }
            return true;
        }

        // Если это не цветовой код, ищем в конфиге
        String colorName = plugin.getColorsConfig().getString("colors." + colorValue + ".name");
        String colorCode = plugin.getColorsConfig().getString("colors." + colorValue + ".color-code", "&f");

        if (colorName == null) {
            // Собираем список доступных цветов
            StringBuilder colorsList = new StringBuilder();
            if (plugin.getColorsConfig().getConfigurationSection("colors") != null) {
                for (String key : plugin.getColorsConfig().getConfigurationSection("colors").getKeys(false)) {
                    colorsList.append(key).append(", ");
                }
                if (colorsList.length() > 2) {
                    colorsList.setLength(colorsList.length() - 2);
                }
            }
            sender.sendMessage(getMsg("color-invalid").replace("{colors}", colorsList.toString()));
            return true;
        }

        // Устанавливаем цвет из конфига
        plugin.getNameManager().setPlayerColor(targetPlayer.getUniqueId(), colorValue);

        String coloredName = color(colorCode + colorName);
        if (settingForOther) {
            sender.sendMessage(getMsg("color-set-other")
                    .replace("{player}", targetPlayer.getName())
                    .replace("{color}", coloredName));
            targetPlayer.sendMessage(getMsg("color-set").replace("{color}", coloredName));
        } else {
            sender.sendMessage(getMsg("color-set").replace("{color}", coloredName));
        }

        return true;
    }

    // Обработчик команды Description
    private boolean handleDescription(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Показать свое описание
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только для игроков!");
                return true;
            }
            Player player = (Player) sender;
            String description = plugin.getNameManager().getDescription(player.getUniqueId());
            if (description != null && !description.isEmpty()) {
                player.sendMessage(getMsg("your-description").replace("{description}", description));
            } else {
                player.sendMessage(getMsg("no-description"));
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help")) {
            sender.sendMessage(color("&e--- Помощь по команде /description ---"));
            sender.sendMessage(color("&7/description &f- Показать свое описание"));
            sender.sendMessage(color("&7/description set <текст> &f- Установить описание"));
            sender.sendMessage(color("&7/description <игрок> &f- Показать описание игрока"));
            sender.sendMessage(color("&7/desc &f- Алиас для /description"));
            return true;
        }

        // Установка описания через /desc set
        if (subCommand.equals("set")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только для игроков!");
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("dontrp.description.set")) {
                player.sendMessage(getMsg("no-permission"));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(color("&cИспользование: /description set <текст>"));
                return true;
            }

            String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            if (description.length() > 120) {
                player.sendMessage(getMsg("description-too-long"));
                return true;
            }

            plugin.getNameManager().setDescription(player.getUniqueId(), description);
            player.sendMessage(getMsg("description-set").replace("{description}", description));
            return true;
        }

        // Просмотр описания другого игрока
        if (args.length == 1) {
            if (!sender.hasPermission("dontrp.description.view")) {
                sender.sendMessage(getMsg("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(getMsg("player-not-found"));
                return true;
            }

            String description = plugin.getNameManager().getDescription(target.getUniqueId());
            if (description != null && !description.isEmpty()) {
                sender.sendMessage(getMsg("player-description")
                    .replace("{player}", target.getName())
                    .replace("{description}", description));
            } else {
                sender.sendMessage(getMsg("player-no-description").replace("{player}", target.getName()));
            }
            return true;
        }

        // Установка своего описания (старый способ для обратной совместимости)
        if (!(sender instanceof Player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("dontrp.description.set")) {
            player.sendMessage(getMsg("no-permission"));
            return true;
        }

        String description = String.join(" ", args);
        if (description.length() > 120) {
            player.sendMessage(getMsg("description-too-long"));
            return true;
        }

        plugin.getNameManager().setDescription(player.getUniqueId(), description);
        player.sendMessage(getMsg("description-set").replace("{description}", description));
        return true;
    }

    // Обработчик административных команд
    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMsg("usage-adm"));
            return true;
        }

        if (!sender.hasPermission("dontrp.admin")) {
            sender.sendMessage(getMsg("no-permission"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String targetName = args[1];

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(getMsg("player-not-found"));
            return true;
        }

        switch (subCommand) {
            case "rpname":
                return handleAdminRpName(sender, target, args);
            case "description":
                return handleAdminDescription(sender, target, args);
            default:
                sender.sendMessage(getMsg("usage-adm"));
                return true;
        }
    }

    // Административные команды RPName
    private boolean handleAdminRpName(CommandSender sender, Player target, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(getMsg("usage-adm-rpname"));
            return true;
        }

        String action = args[2].toLowerCase();

        if (action.equals("remove")) {
            plugin.getNameManager().removeRpName(target.getUniqueId());
            plugin.getNameManager().removePlayerColor(target.getUniqueId());
            sender.sendMessage(getMsg("name-removed-other").replace("{player}", target.getName()));
            target.sendMessage(getMsg("name-removed"));
            return true;
        } else if (action.equals("list")) {
            return handleRpNameList(sender, args);
        } else {
            // Установка RP имени
            if (args.length < 4) {
                sender.sendMessage(getMsg("usage-adm-rpname"));
                return true;
            }

            String first = args[2];
            String last = args[3];

            boolean requireLast = plugin.getConfig().getBoolean("settings.require-lastname");
            if (requireLast && last.isEmpty()) {
                sender.sendMessage(getMsg("usage-adm-rpname"));
                return true;
            }

            Pattern pattern = Pattern.compile(plugin.getConfig().getString("settings.allowed-regex"));
            if (!pattern.matcher(first).matches() || (!last.isEmpty() && !pattern.matcher(last).matches())) {
                sender.sendMessage(getMsg("name-invalid-regex"));
                return true;
            }

            int maxFirst = plugin.getConfig().getInt("settings.limits.first-name");
            int maxLast = plugin.getConfig().getInt("settings.limits.last-name");

            if (first.length() > maxFirst || last.length() > maxLast) {
                sender.sendMessage(getMsg("name-length"));
                return true;
            }

            plugin.getNameManager().setRpName(target.getUniqueId(), first, last);
            sender.sendMessage(getMsg("name-set-other")
                .replace("{player}", target.getName())
                .replace("{name}", first + " " + last));
            target.sendMessage(getMsg("name-set-admin").replace("{name}", first + " " + last));
            return true;
        }
    }

    // Административные команды Description
    private boolean handleAdminDescription(CommandSender sender, Player target, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(getMsg("usage-adm-description"));
            return true;
        }

        String action = args[2].toLowerCase();

        if (action.equals("remove")) {
            plugin.getNameManager().removeDescription(target.getUniqueId());
            sender.sendMessage(getMsg("description-removed-other").replace("{player}", target.getName()));
            target.sendMessage(getMsg("description-removed"));
            return true;
        } else {
            // Установка описания
            String description = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            if (description.length() > 120) {
                sender.sendMessage(getMsg("description-too-long"));
                return true;
            }

            plugin.getNameManager().setDescription(target.getUniqueId(), description);
            sender.sendMessage(getMsg("description-set-other")
                .replace("{player}", target.getName())
                .replace("{description}", description));
            target.sendMessage(getMsg("description-set-admin").replace("{description}", description));
            return true;
        }
    }

    public void setPlayerRollMin(UUID uuid, int min) {
        playerRollMin.put(uuid, min);
    }

    public void setPlayerRollMax(UUID uuid, int max) {
        playerRollMax.put(uuid, max);
    }

    public void resetPlayerRollSettings(UUID uuid) {
        playerRollMin.remove(uuid);
        playerRollMax.remove(uuid);
    }

    public int getPlayerRollMin(UUID uuid) {
        Integer value = playerRollMin.get(uuid);
        return value != null ? value : plugin.getConfig().getInt("settings.roll.default-min");
    }

    public int getPlayerRollMax(UUID uuid) {
        Integer value = playerRollMax.get(uuid);
        return value != null ? value : plugin.getConfig().getInt("settings.roll.default-max");
    }

    private void broadcastRadius(Player sender, String message) {
        boolean localEnabled = plugin.getConfig().getBoolean("settings.local-chat.enabled");

        if (!localEnabled) {
            Bukkit.broadcastMessage(message);
        } else {
            double radius = plugin.getConfig().getDouble("settings.local-chat.radius");
            int recipients = 0;

            sender.sendMessage(message);

            for (Player p : sender.getWorld().getPlayers()) {
                if (p.getUniqueId().equals(sender.getUniqueId())) continue;

                if (p.getLocation().distance(sender.getLocation()) <= radius) {
                    p.sendMessage(message);
                    recipients++;
                }
            }

            if (recipients == 0) {
                String nobodyMsg = plugin.getConfig().getString("settings.local-chat.nobody-heard");
                if (nobodyMsg != null && !nobodyMsg.isEmpty()) {
                    sender.sendMessage(color(nobodyMsg));
                }
            }
        }
    }
}
