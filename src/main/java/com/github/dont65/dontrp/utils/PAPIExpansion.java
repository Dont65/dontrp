package com.github.dont65.dontrp.utils;
import com.github.dont65.dontrp.DontRP;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
public class PAPIExpansion extends PlaceholderExpansion {
    private final DontRP plugin;
    public PAPIExpansion(DontRP plugin) { this.plugin = plugin; }
    @Override public @NotNull String getIdentifier() { return "dontrp"; }
    @Override public @NotNull String getAuthor() { return "Dont65"; }
    @Override public @NotNull String getVersion() { return "1.4"; }
    @Override public boolean persist() { return true; }
    @Override public String onRequest(OfflinePlayer p, @NotNull String params) {
        if (p == null || !p.isOnline()) return "";
        if (params.equals("rpname_full")) return plugin.getNameManager().getFullName(p.getUniqueId());
        if (params.equals("rpname_colored")) return plugin.getNameManager().getAdaptiveName(p.getPlayer());
        return null;
    }
}
