package com.github.dont65.dontrp.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PAPIUtils {

    public static String parse(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
