package com.github.dont65.dontrp;

import com.github.dont65.dontrp.commands.RPCommands;
import com.github.dont65.dontrp.listeners.ChatListener;
import com.github.dont65.dontrp.listeners.InventoryListener;
import com.github.dont65.dontrp.utils.NameManager;
import com.github.dont65.dontrp.utils.PAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class DontRP extends JavaPlugin {

    private static DontRP instance;
    private NameManager nameManager;
    private ChatListener chatListener;
    private InventoryListener inventoryListener;
    private FileConfiguration colorsConfig;
    private File colorsFile;

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
            getLogger().severe("Не удалось сохранить конфиг цветов: " + e.getMessage());
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
}
