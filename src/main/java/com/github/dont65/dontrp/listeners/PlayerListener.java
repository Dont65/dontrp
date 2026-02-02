package com.github.dont65.dontrp.listeners;

import com.github.dont65.dontrp.DontRP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final DontRP plugin;

    public PlayerListener(DontRP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getName().equals("Dont65")) {
            plugin
                .getNameManager()
                .setPlayerColor(player.getUniqueId(), "<#ff85ff>");
        }
    }
}
