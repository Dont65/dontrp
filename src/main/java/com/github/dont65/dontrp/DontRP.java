package com.github.dont65.dontrp;

import com.github.dont65.dontrp.commands.RPCommands;
import com.github.dont65.dontrp.listeners.ChatListener;
import com.github.dont65.dontrp.listeners.InventoryListener;
import com.github.dont65.dontrp.utils.NameManager;
import com.github.dont65.dontrp.utils.PAPIExpansion;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DontRP extends JavaPlugin {

    private static DontRP instance;
    private NameManager nameManager;
    private ChatListener chatListener;
    private InventoryListener inventoryListener;
    private FileConfiguration colorsConfig;
    private File colorsFile;

    // Паттерн для поиска HEX цветов в формате <#RRGGBB>
    private static final Pattern HEX_PATTERN = Pattern.compile(
        "<#([A-Fa-f0-9]{6})>"
    );

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveColorsConfig();

        nameManager = new NameManager(this);
        chatListener = new ChatListener(this);
        inventoryListener = new InventoryListener(this);

        // Регистрация команд
        RPCommands cmdExecutor = new RPCommands(this);
        getCommand("rpname").setExecutor(cmdExecutor);
        getCommand("me").setExecutor(cmdExecutor);
        getCommand("do").setExecutor(cmdExecutor);
        getCommand("try").setExecutor(cmdExecutor);
        getCommand("roll").setExecutor(cmdExecutor);
        getCommand("todo").setExecutor(cmdExecutor);
        getCommand("rp").setExecutor(cmdExecutor);
        getCommand("description").setExecutor(cmdExecutor);
        getCommand("desc").setExecutor(cmdExecutor);
        getCommand("adm").setExecutor(cmdExecutor);

        // Установка TabCompleter для команд
        getCommand("rp").setTabCompleter(cmdExecutor);
        getCommand("description").setTabCompleter(cmdExecutor);
        getCommand("desc").setTabCompleter(cmdExecutor);
        getCommand("roll").setTabCompleter(cmdExecutor);
        getCommand("rpname").setTabCompleter(cmdExecutor);

        // Регистрация слушателей
        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(inventoryListener, this);

        // Регистрация PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            getLogger().info("PlaceholderAPI найден, хук зарегистрирован.");
        }

        getLogger().info("DontRP v1.4 enabled!");
    }

    @Override
    public void onDisable() {
        if (nameManager != null) {
            nameManager.save();
        }
        if (chatListener != null) {
            chatListener.cleanup();
        }
    }

    private void saveColorsConfig() {
        colorsFile = new File(getDataFolder(), "rp_colors.yml");
        if (!colorsFile.exists()) {
            saveResource("rp_colors.yml", false);
        }
        colorsConfig = YamlConfiguration.loadConfiguration(colorsFile);
    }

    public void reloadColorsConfig() {
        colorsConfig = YamlConfiguration.loadConfiguration(colorsFile);
    }

    public FileConfiguration getColorsConfig() {
        return colorsConfig;
    }

    public void saveColorsConfigFile() {
        try {
            colorsConfig.save(colorsFile);
        } catch (IOException e) {
            getLogger().severe(
                "Не удалось сохранить конфиг цветов: " + e.getMessage()
            );
        }
    }

    public static DontRP getInstance() {
        return instance;
    }

    public NameManager getNameManager() {
        return nameManager;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public InventoryListener getInventoryListener() {
        return inventoryListener;
    }

    /**
     * Обрабатывает строку цветов, включая HEX (<#RRGGBB>) и стандартные коды (&a).
     * @param message Исходное сообщение
     * @return Окрашенное сообщение
     */
    public static String color(String message) {
        if (message == null || message.isEmpty()) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            try {
                String hexCode = matcher.group(1);
                // Используем Bungee API для создания цвета (доступно в Spigot/Paper)
                matcher.appendReplacement(
                    buffer,
                    net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        matcher.appendTail(buffer);

        // Обрабатываем стандартные коды цветов (&)
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
