package com.github.dont65.dontrp.utils;

import com.github.dont65.dontrp.DontRP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NameManager {
    private final DontRP plugin;
    private File file;
    private FileConfiguration config;

    public NameManager(DontRP plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "names.yml");
        if (!file.exists()) try { file.createNewFile(); } catch (IOException ignored) {}
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() { try { config.save(file); } catch (IOException ignored) {} }

    public void setRpName(UUID u, String f, String l) {
        config.set(u + ".full", f + " " + l);
        config.set(u + ".first", f); config.set(u + ".last", l);
        save();
    }
    public void removeRpName(UUID u) { config.set(u.toString(), null); save(); }
    public boolean hasRpName(UUID u) { return config.contains(u + ".full"); }
    public String getFullName(UUID u) { return config.getString(u + ".full"); }
    public void setDescription(UUID u, String d) { config.set(u + ".description", d); save(); }
    public String getDescription(UUID u) { return config.getString(u + ".description"); }
    public void setPlayerColor(UUID u, String c) { config.set(u + ".color", c); save(); }

    public String getAdaptiveName(Player p) {
        String name = hasRpName(p.getUniqueId()) ? getFullName(p.getUniqueId()) : p.getName();
        String color = config.getString(p.getUniqueId() + ".color", plugin.getColorsConfig().getString("default-color", "&f"));
        if (!color.startsWith("&") && !color.startsWith("<#")) {
            color = plugin.getColorsConfig().getString("colors." + color + ".color-code", "&f");
        }
        return color + name;
    }
}
