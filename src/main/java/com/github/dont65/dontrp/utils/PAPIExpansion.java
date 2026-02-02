package com.github.dont65.dontrp.utils;

import com.github.dont65.dontrp.DontRP;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIExpansion extends PlaceholderExpansion {

    private final DontRP plugin;

    public PAPIExpansion(DontRP plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "dontrp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Dont65";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        NameManager nm = plugin.getNameManager();
        boolean hasName = nm.hasRpName(player.getUniqueId());

        if (params.equalsIgnoreCase("rpname_full")) {
            return hasName ? nm.getFullName(player.getUniqueId()) : "None";
        }

        if (params.equalsIgnoreCase("rpname_first")) {
            return hasName ? nm.getFirstName(player.getUniqueId()) : "None";
        }

        if (params.equalsIgnoreCase("rpname_last")) {
            return hasName ? nm.getLastName(player.getUniqueId()) : "None";
        }

        if (params.equalsIgnoreCase("rpname_adaptive")) {
            if (!hasName) return player.getName();

            if (player.isOnline()) {
                return ChatColor.stripColor(nm.getAdaptiveName(player.getPlayer()));
            } else {
                return nm.getFullName(player.getUniqueId());
            }
        }

        if (params.equalsIgnoreCase("rpname_colored")) {
            if (!hasName) return player.getName();

            if (player.isOnline()) {
                return nm.getAdaptiveName(player.getPlayer());
            } else {
                return nm.getFullName(player.getUniqueId());
            }
        }

        if (params.equalsIgnoreCase("rpname_color")) {
            String colorCode = nm.getPlayerColorCode(player.getUniqueId());
            return colorCode != null ? colorCode : plugin.getColorsConfig().getString("default-color", "&f");
        }

        if (params.equalsIgnoreCase("description")) {
            String description = nm.getDescription(player.getUniqueId());
            return description != null ? description : "";
        }

        // Плейсхолдер для описания по имени игрока
        if (params.startsWith("description_")) {
            String playerName = params.substring("description_".length());
            if (playerName.isEmpty()) return "";

            // Ищем игрока онлайн
            Player target = plugin.getServer().getPlayer(playerName);
            if (target != null) {
                return nm.getDescription(target.getUniqueId()) != null ? nm.getDescription(target.getUniqueId()) : "";
            }

            // Ищем в конфиге
            return nm.getDescriptionByPlayerName(playerName) != null ? nm.getDescriptionByPlayerName(playerName) : "";
        }

        return null;
    }
}
