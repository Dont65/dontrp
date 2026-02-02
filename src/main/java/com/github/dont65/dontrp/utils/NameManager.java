package com.github.dont65.dontrp.utils;

import com.github.dont65.dontrp.DontRP;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class NameManager {

    private final DontRP plugin;
    private File file;
    private FileConfiguration config;

    public NameManager(DontRP plugin) {
        this.plugin = plugin;
        loadFile();
    }

    private void loadFile() {
        file = new File(plugin.getDataFolder(), "names.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRpName(UUID uuid, String firstName, String lastName) {
        String fullName = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");
        config.set(uuid.toString() + ".full", fullName);
        config.set(uuid.toString() + ".first", firstName);
        config.set(uuid.toString() + ".last", lastName);
        save();
    }

    public void removeRpName(UUID uuid) {
        config.set(uuid.toString() + ".full", null);
        config.set(uuid.toString() + ".first", null);
        config.set(uuid.toString() + ".last", null);
        save();
    }

    public boolean hasRpName(UUID uuid) {
        return config.contains(uuid.toString() + ".full");
    }

    public String getFullName(UUID uuid) {
        return config.getString(uuid.toString() + ".full");
    }

    public String getFirstName(UUID uuid) {
        return config.getString(uuid.toString() + ".first");
    }

    public String getLastName(UUID uuid) {
        return config.getString(uuid.toString() + ".last");
    }

    // Методы для работы с описаниями
    public void setDescription(UUID uuid, String description) {
        config.set(uuid.toString() + ".description", description);
        save();
    }

    public String getDescription(UUID uuid) {
        return config.getString(uuid.toString() + ".description");
    }

    public void removeDescription(UUID uuid) {
        config.set(uuid.toString() + ".description", null);
        save();
    }

    public boolean hasDescription(UUID uuid) {
        return config.contains(uuid.toString() + ".description");
    }

    // Получить описание игрока по имени
    public String getDescriptionByPlayerName(String playerName) {
        for (String uuidStr : config.getKeys(false)) {
            Player player = plugin.getServer().getPlayer(UUID.fromString(uuidStr));
            if (player != null && player.getName().equalsIgnoreCase(playerName)) {
                return config.getString(uuidStr + ".description");
            }
        }
        return null;
    }

    // Получить имя с цветом (если установлен)
    public String getColoredName(Player player) {
        String nameToColor;

        // Если есть RP имя - используем его, иначе используем никнейм
        if (hasRpName(player.getUniqueId())) {
            nameToColor = getFullName(player.getUniqueId());
        } else {
            nameToColor = player.getName();
        }

        if (nameToColor == null) {
            nameToColor = player.getName();
        }

        // Получаем цвет игрока
        String colorCode = getPlayerColorCode(player.getUniqueId());

        return ChatColor.translateAlternateColorCodes('&', colorCode + nameToColor);
    }

    // Получить адаптивное имя (с цветом, если система цветов включена)
    public String getAdaptiveName(Player player) {
        // Если система цветов отключена, возвращаем имя без цвета
        if (!plugin.getConfig().getBoolean("settings.color-system-enabled", true)) {
            if (hasRpName(player.getUniqueId())) {
                return getFullName(player.getUniqueId());
            } else {
                return player.getName();
            }
        }

        // Система цветов включена - возвращаем окрашенное имя
        return getColoredName(player);
    }

    // Цветовые настройки игрока
    public void setPlayerColor(UUID uuid, String colorValue) {
        config.set(uuid.toString() + ".color", colorValue);
        save();
    }

    public void removePlayerColor(UUID uuid) {
        config.set(uuid.toString() + ".color", null);
        save();
    }

    // Получить цветовой код игрока
    public String getPlayerColorCode(UUID uuid) {
        String colorValue = config.getString(uuid.toString() + ".color");

        // Если цвет не установлен, возвращаем дефолтный
        if (colorValue == null || colorValue.isEmpty()) {
            return plugin.getColorsConfig().getString("default-color", "&f");
        }

        // Проверяем, является ли значение прямым цветовым кодом (начинается с &)
        if (colorValue.startsWith("&")) {
            // Проверяем, валидный ли это цветовой код
            Pattern colorPattern = Pattern.compile("^&[0-9a-fk-or]$", Pattern.CASE_INSENSITIVE);
            if (colorPattern.matcher(colorValue).matches()) {
                return colorValue;
            } else {
                // Невалидный цветовой код - возвращаем дефолтный
                return plugin.getColorsConfig().getString("default-color", "&f");
            }
        }

        // Если это ID цвета из конфига
        String colorCode = plugin.getColorsConfig().getString("colors." + colorValue + ".color-code");

        // Если цвет не найден в конфиге, возвращаем дефолтный
        return colorCode != null ? colorCode : plugin.getColorsConfig().getString("default-color", "&f");
    }

    public String getPlayerColorId(UUID uuid) {
        return config.getString(uuid.toString() + ".color");
    }

    public Map<String, Object> getAllNames() {
        return config.getValues(false);
    }
}
