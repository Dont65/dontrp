package com.github.dont65.dontrp.listeners;

import com.github.dont65.dontrp.DontRP;
import com.github.dont65.dontrp.commands.RPCommands;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final DontRP plugin;
    private final Map<UUID, String> awaitingInput;

    public ChatListener(DontRP plugin) {
        this.plugin = plugin;
        this.awaitingInput = new HashMap<>();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingInput.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        String inputType = awaitingInput.remove(uuid);
        String message = event.getMessage().trim();

        if (message.equalsIgnoreCase("отмена") || message.equalsIgnoreCase("cancel") ||
            message.equalsIgnoreCase("выход") || message.equalsIgnoreCase("exit")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.chat-enter-cancel", "&cВвод отменен")));
            return;
        }

        switch (inputType) {
            case "roll_min":
            case "roll_max":
                handleRollInput(player, inputType, message);
                break;
            case "rpname_change":
                handleRpNameChangeInput(player, message);
                break;
            case "description_change":
                handleDescriptionChangeInput(player, message);
                break;
        }
    }

    private void handleRollInput(Player player, String inputType, String message) {
        try {
            int value = Integer.parseInt(message);
            RPCommands commands = (RPCommands) plugin.getCommand("rp").getExecutor();

            switch (inputType) {
                case "roll_min":
                    int currentMax = commands.getPlayerRollMax(player.getUniqueId());
                    if (value >= currentMax) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("messages.roll-min-greater",
                                        "&cМинимальное значение не может быть больше максимального!")));
                        return;
                    }
                    commands.setPlayerRollMin(player.getUniqueId(), value);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.roll-min-changed",
                                    "&aМинимальное значение установлено: &e{min}").replace("{min}", String.valueOf(value))));
                    break;

                case "roll_max":
                    int currentMin = commands.getPlayerRollMin(player.getUniqueId());
                    if (value <= currentMin) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                plugin.getConfig().getString("messages.roll-min-greater",
                                        "&cМаксимальное значение не может быть меньше минимального!")));
                        return;
                    }
                    commands.setPlayerRollMax(player.getUniqueId(), value);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("messages.roll-max-changed",
                                    "&aМаксимальное значение установлено: &e{max}").replace("{max}", String.valueOf(value))));
                    break;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getInventoryListener().openRollMenu(player);
            });

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.roll-invalid-number",
                            "&cПожалуйста, введите корректное число!")));
        }
    }

    private void handleRpNameChangeInput(Player player, String message) {
        String[] args = message.split(" ");

        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.rpname-change-cancel",
                            "&cВвод отменен. Используйте /rpname <Имя> <Фамилия>")));
            return;
        }

        // Проверяем регулярное выражение
        Pattern pattern = Pattern.compile(plugin.getConfig().getString("settings.allowed-regex"));
        String firstName = args[0];
        String lastName = args.length > 1 ? args[1] : "";

        boolean requireLast = plugin.getConfig().getBoolean("settings.require-lastname");

        if (requireLast && lastName.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.usage-rpname",
                            "&cИспользование: /rpname <Имя> <Фамилия>")));
            return;
        }

        if (!pattern.matcher(firstName).matches() || (!lastName.isEmpty() && !pattern.matcher(lastName).matches())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.name-invalid-regex",
                            "&cИмя содержит запрещенные символы (разрешена только кириллица).")));
            return;
        }

        // Проверяем длину
        int maxFirst = plugin.getConfig().getInt("settings.limits.first-name");
        int maxLast = plugin.getConfig().getInt("settings.limits.last-name");

        if (firstName.length() > maxFirst || (!lastName.isEmpty() && lastName.length() > maxLast)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.name-length",
                            "&cИмя или фамилия слишком длинные.")));
            return;
        }

        // Устанавливаем новое имя
        plugin.getNameManager().setRpName(player.getUniqueId(), firstName, lastName);
        String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.rpname-change-complete",
                        "&aRP имя успешно изменено: &e{name}").replace("{name}", fullName)));
    }

    private void handleDescriptionChangeInput(Player player, String message) {
        if (message.length() > 120) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.description-too-long",
                            "&cОписание слишком длинное (максимум 120 символов).")));
            return;
        }

        plugin.getNameManager().setDescription(player.getUniqueId(), message);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.description-change-complete",
                        "&aОписание успешно изменено!")));
    }

    public void setAwaitingInput(Player player, String inputType) {
        awaitingInput.put(player.getUniqueId(), inputType);
    }

    public void cleanup() {
        for (UUID uuid : awaitingInput.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("messages.chat-enter-cancel",
                                "&cВвод отменен из-за перезагрузки плагина")));
            }
        }
        awaitingInput.clear();
    }
}
