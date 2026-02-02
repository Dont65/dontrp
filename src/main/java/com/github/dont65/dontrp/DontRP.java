package com.github.dont65.dontrp;

import com.github.dont65.dontrp.commands.RPCommands;
import com.github.dont65.dontrp.listeners.ChatListener;
import com.github.dont65.dontrp.listeners.InventoryListener;
import com.github.dont65.dontrp.utils.NameManager;
import com.github.dont65.dontrp.utils.PAPIExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DontRP extends JavaPlugin {
    private static DontRP instance;
    private NameManager nameManager;
    private ChatListener chatListener;
    private InventoryListener inventoryListener;
    private FileConfiguration colorsConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("rp_colors.yml", false);
        colorsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "rp_colors.yml"));

        nameManager = new NameManager(this);
        chatListener = new ChatListener(this);
        inventoryListener = new InventoryListener(this);

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

        getCommand("rp").setTabCompleter(cmdExecutor);
        getCommand("description").setTabCompleter(cmdExecutor);
        getCommand("roll").setTabCompleter(cmdExecutor);
        getCommand("rpname").setTabCompleter(cmdExecutor);

        Bukkit.getPluginManager().registerEvents(chatListener, this);
        Bukkit.getPluginManager().registerEvents(inventoryListener, this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
        }
        getLogger().info("DontRP v1.4-HEX Enabled!");
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) return message;
        Pattern pattern = Pattern.compile("<#([A-Fa-f0-9]{6})>");
        Matcher matcher = pattern.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String color = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : color.toCharArray()) replacement.append('§').append(c);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static DontRP getInstance() { return instance; }
    public NameManager getNameManager() { return nameManager; }
    public ChatListener getChatListener() { return chatListener; }
    public InventoryListener getInventoryListener() { return inventoryListener; }
    public FileConfiguration getColorsConfig() { return colorsConfig; }
    public void reloadColorsConfig() {
        colorsConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "rp_colors.yml"));
    }
}
