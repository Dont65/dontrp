package com.github.dont65.dontrp.listeners;
import com.github.dont65.dontrp.DontRP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.*;
public class ChatListener implements Listener {
    private final DontRP plugin;
    private final Map<UUID, String> awaiting = new HashMap<>();
    public ChatListener(DontRP plugin) { this.plugin = plugin; }
    public void setAwaitingInput(UUID u, String type) { awaiting.put(u, type); }
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        if (!awaiting.containsKey(u)) return;
        e.setCancelled(true);
        String type = awaiting.remove(u);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (type.equals("name")) e.getPlayer().performCommand("rpname " + e.getMessage());
            else if (type.equals("desc")) e.getPlayer().performCommand("description set " + e.getMessage());
        });
    }
}
