package com.github.dont65.dontrp.listeners;

import com.github.dont65.dontrp.DontRP;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
    private final DontRP plugin;
    public InventoryListener(DontRP plugin) { this.plugin = plugin; }

    public void openMainMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "Настройки RP");
        inv.setItem(10, createItem(Material.NAME_TAG, "&eСменить Имя"));
        inv.setItem(12, createItem(Material.WRITABLE_BOOK, "&bСменить Описание"));
        inv.setItem(14, createItem(Material.BRUSH, "&6Сменить Цвет (HEX)"));
        inv.setItem(16, createItem(Material.PAPER, "&aНастройка Roll"));
        p.openInventory(inv);
    }

    public void openRollMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "Настройки Roll");
        inv.setItem(11, createItem(Material.PAPER, "&aМинимум (ЛКМ +1 / ПКМ -1)"));
        inv.setItem(15, createItem(Material.MAP, "&cМаксимум (ЛКМ +1 / ПКМ -1)"));
        inv.setItem(22, createItem(Material.ARROW, "&7Назад"));
        p.openInventory(inv);
    }

    private ItemStack createItem(Material m, String n) {
        ItemStack i = new ItemStack(m); ItemMeta mt = i.getItemMeta();
        mt.setDisplayName(DontRP.colorize(n)); i.setItemMeta(mt); return i;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        if (title.contains("Настройки")) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) return;
            String name = e.getCurrentItem().getItemMeta().getDisplayName();

            if (title.equals("Настройки RP")) {
                if (name.contains("Имя")) { p.closeInventory(); plugin.getChatListener().setAwaitingInput(p.getUniqueId(), "name"); p.sendMessage("Введите Имя Фамилия:"); }
                if (name.contains("Описание")) { p.closeInventory(); plugin.getChatListener().setAwaitingInput(p.getUniqueId(), "desc"); p.sendMessage("Введите описание:"); }
                if (name.contains("Цвет")) { p.closeInventory(); p.sendMessage("Используйте: /rp namecolor <#HEX>"); }
                if (name.contains("Roll")) openRollMenu(p);
            } else if (title.equals("Настройки Roll")) {
                var cmd = (com.github.dont65.dontrp.commands.RPCommands)plugin.getCommand("rp").getExecutor();
                if (name.contains("Минимум")) {
                    int cur = cmd.getPlayerRollMin(p.getUniqueId());
                    cmd.setPlayerRollMin(p.getUniqueId(), e.isLeftClick() ? cur + 1 : Math.max(0, cur - 1));
                    openRollMenu(p);
                }
                if (name.contains("Максимум")) {
                    int cur = cmd.getPlayerRollMax(p.getUniqueId());
                    cmd.setPlayerRollMax(p.getUniqueId(), e.isLeftClick() ? cur + 1 : Math.max(0, cur - 1));
                    openRollMenu(p);
                }
                if (name.contains("Назад")) openMainMenu(p);
            }
        }
    }
}
