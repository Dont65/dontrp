package com.github.dont65.dontrp.listeners;

import com.github.dont65.dontrp.DontRP;
import com.github.dont65.dontrp.commands.RPCommands;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {

    private final DontRP plugin;
    private final Map<UUID, String> currentMenuType;
    private final Map<UUID, Inventory> openMenus;

    public InventoryListener(DontRP plugin) {
        this.plugin = plugin;
        this.currentMenuType = new HashMap<>();
        this.openMenus = new HashMap<>();
    }

    // Основное меню с выбором вкладок
    public void openMainMenu(Player player) {
        int size = plugin.getConfig().getInt("settings.main-menu.size", 36);
        String title = color(
            plugin
                .getConfig()
                .getString("settings.main-menu.title", "&8Настройки RP")
        );

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Заполняем бордер
        ItemStack borderItem = createMenuItem(
            "border",
            "settings.main-menu.border-item"
        );
        for (int i = 0; i < size; i++) {
            inv.setItem(i, borderItem.clone());
        }

        // Кнопка "Настройка RP имени"
        ItemStack rpnameItem = createMenuItemWithSlot(
            "rpname",
            "settings.main-menu.rpname-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.main-menu.rpname-item.slot", 10),
            rpnameItem
        );

        // Кнопка "Описание персонажа"
        ItemStack descriptionItem = createMenuItemWithSlot(
            "description",
            "settings.main-menu.description-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.main-menu.description-item.slot", 12),
            descriptionItem
        );

        // Кнопка "Цвет имени"
        if (
            plugin.getConfig().getBoolean("settings.color-system-enabled", true)
        ) {
            ItemStack colorsItem = createMenuItemWithSlot(
                "colors",
                "settings.main-menu.colors-item"
            );
            inv.setItem(
                getSlotFromConfig("settings.main-menu.colors-item.slot", 14),
                colorsItem
            );
        }

        // Кнопка "Настройки Roll"
        ItemStack rollItem = createMenuItemWithSlot(
            "roll",
            "settings.main-menu.roll-settings-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.main-menu.roll-settings-item.slot", 16),
            rollItem
        );

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), inv);
        currentMenuType.put(player.getUniqueId(), "main");
    }

    // Меню настроек RP имени
    public void openRpNameMenu(Player player) {
        int size = plugin.getConfig().getInt("settings.rpname-menu.size", 27);
        String title = color(
            plugin
                .getConfig()
                .getString("settings.rpname-menu.title", "&8Настройка RP имени")
        );

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Заполняем бордер
        ItemStack borderItem = createMenuItem(
            "border",
            "settings.rpname-menu.border-item"
        );
        for (int i = 0; i < size; i++) {
            inv.setItem(i, borderItem.clone());
        }

        // Кнопка "Изменить RP имя"
        ItemStack changeNameItem = createMenuItemWithSlot(
            "change-name",
            "settings.rpname-menu.change-name"
        );
        inv.setItem(
            getSlotFromConfig("settings.rpname-menu.change-name.slot", 11),
            changeNameItem
        );

        // Кнопка "Удалить RP имя"
        ItemStack removeNameItem = createMenuItemWithSlot(
            "remove-name",
            "settings.rpname-menu.remove-name"
        );
        inv.setItem(
            getSlotFromConfig("settings.rpname-menu.remove-name.slot", 13),
            removeNameItem
        );

        // Кнопка "Назад"
        ItemStack backItem = createMenuItemWithSlot(
            "back",
            "settings.rpname-menu.back-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.rpname-menu.back-item.slot", 15),
            backItem
        );

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), inv);
        currentMenuType.put(player.getUniqueId(), "rpname");
    }

    // Меню настроек описания
    public void openDescriptionMenu(Player player) {
        int size = plugin.getConfig().getInt("settings.description-menu.size", 27);
        String title = color(
            plugin
                .getConfig()
                .getString("settings.description-menu.title", "&8Описание персонажа")
        );

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Заполняем бордер
        ItemStack borderItem = createMenuItem(
            "border",
            "settings.description-menu.border-item"
        );
        for (int i = 0; i < size; i++) {
            inv.setItem(i, borderItem.clone());
        }

        // Кнопка "Изменить описание"
        ItemStack changeDescItem = createMenuItemWithSlot(
            "change-description",
            "settings.description-menu.change-description"
        );

        // Добавляем текущее описание в лор
        String currentDesc = plugin.getNameManager().getDescription(player.getUniqueId());
        ItemMeta meta = changeDescItem.getItemMeta();
        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add(color("&7Текущее описание:"));
            if (currentDesc != null && !currentDesc.isEmpty()) {
                lore.add(color("&f" + currentDesc));
            } else {
                lore.add(color("&cНе установлено"));
            }
            lore.add("");
            for (String line : plugin.getConfig().getStringList("settings.description-menu.change-description.lore")) {
                lore.add(color(line));
            }
            meta.setLore(lore);
            changeDescItem.setItemMeta(meta);
        }

        inv.setItem(
            getSlotFromConfig("settings.description-menu.change-description.slot", 11),
            changeDescItem
        );

        // Кнопка "Удалить описание"
        ItemStack removeDescItem = createMenuItemWithSlot(
            "remove-description",
            "settings.description-menu.remove-description"
        );
        inv.setItem(
            getSlotFromConfig("settings.description-menu.remove-description.slot", 13),
            removeDescItem
        );

        // Кнопка "Назад"
        ItemStack backItem = createMenuItemWithSlot(
            "back",
            "settings.description-menu.back-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.description-menu.back-item.slot", 15),
            backItem
        );

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), inv);
        currentMenuType.put(player.getUniqueId(), "description");
    }

    // Меню выбора цвета
    public void openColorMenu(Player player) {
        if (
            !plugin
                .getConfig()
                .getBoolean("settings.color-system-enabled", true)
        ) {
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.color-system-disabled",
                            "&cСистема цветов отключена"
                        )
                )
            );
            return;
        }

        int size = plugin.getConfig().getInt("settings.color-menu.size", 36);
        String title = color(
            plugin
                .getConfig()
                .getString("settings.color-menu.title", "&8Выбор цвета имени")
        );

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Заполняем бордер
        ItemStack borderItem = createMenuItem(
            "border",
            "settings.color-menu.border-item"
        );
        for (int i = 0; i < size; i++) {
            inv.setItem(i, borderItem.clone());
        }

        // Кнопка "Цвет по умолчанию"
        ItemStack defaultColorItem = createMenuItemWithSlot(
            "default",
            "settings.color-menu.default-color-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.color-menu.default-color-item.slot", 4),
            defaultColorItem
        );

        // Добавляем доступные цвета из конфига
        ConfigurationSection colors = plugin
            .getColorsConfig()
            .getConfigurationSection("colors");
        if (colors != null) {
            for (String colorId : colors.getKeys(false)) {
                int slot = colors.getInt(colorId + ".slot", -1);
                if (slot < 0 || slot >= size) {
                    continue; // Пропускаем цвета без слота или с неверным слотом
                }

                String name = colors.getString(
                    colorId + ".name",
                    "&fНеизвестный цвет"
                );
                Material material = Material.getMaterial(
                    colors.getString(colorId + ".material", "WHITE_DYE")
                );
                if (material == null) material = Material.WHITE_DYE;

                String permission = colors.getString(
                    colorId + ".permission",
                    ""
                );
                String colorCode = colors.getString(
                    colorId + ".color-code",
                    "&f"
                );

                // Проверяем права игрока
                boolean hasPermission =
                    permission.isEmpty() ||
                    player.hasPermission(permission) ||
                    player.hasPermission("dontrp.rpname_color.*") ||
                    player.hasPermission("dontrp.admin");

                ItemStack colorItem = new ItemStack(material);
                ItemMeta meta = colorItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(color(name));

                    List<String> lore = new ArrayList<>();
                    if (hasPermission) {
                        lore.add(color("&aДоступно"));
                        lore.add(color("&eНажмите, чтобы выбрать"));
                    } else {
                        lore.add(color("&cТребуется право:"));
                        lore.add(color("&7" + permission));
                    }

                    meta.setLore(lore);
                    colorItem.setItemMeta(meta);
                }

                inv.setItem(slot, colorItem);
            }
        }

        // Кнопка "Назад"
        ItemStack backItem = createMenuItemWithSlot(
            "back",
            "settings.color-menu.back-item"
        );
        inv.setItem(
            getSlotFromConfig("settings.color-menu.back-item.slot", 31),
            backItem
        );

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), inv);
        currentMenuType.put(player.getUniqueId(), "colors");
    }

    // Меню настроек Roll
    public void openRollMenu(Player player) {
        RPCommands commands = (RPCommands) plugin
            .getCommand("rp")
            .getExecutor();
        int currentMin = commands.getPlayerRollMin(player.getUniqueId());
        int currentMax = commands.getPlayerRollMax(player.getUniqueId());

        int size = plugin.getConfig().getInt("settings.roll-menu.size", 27);
        String title = color(
            plugin
                .getConfig()
                .getString("settings.roll-menu.title", "&8Настройки Roll")
        );

        Inventory inv = Bukkit.createInventory(null, size, title);

        // Заполняем бордер
        ItemStack borderItem = createMenuItem(
            "border",
            "settings.roll-menu.border-item"
        );
        for (int i = 0; i < size; i++) {
            inv.setItem(i, borderItem.clone());
        }

        // Кнопка Min
        ItemStack minItem = createMenuItemWithSlot(
            "roll-min",
            "settings.roll-menu.roll-min"
        );
        ItemMeta minMeta = minItem.getItemMeta();
        if (minMeta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : plugin
                .getConfig()
                .getStringList("settings.roll-menu.roll-min.lore")) {
                lore.add(
                    color(line.replace("{min}", String.valueOf(currentMin)))
                );
            }
            minMeta.setLore(lore);
            minItem.setItemMeta(minMeta);
        }

        // Кнопка Max
        ItemStack maxItem = createMenuItemWithSlot(
            "roll-max",
            "settings.roll-menu.roll-max"
        );
        ItemMeta maxMeta = maxItem.getItemMeta();
        if (maxMeta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : plugin
                .getConfig()
                .getStringList("settings.roll-menu.roll-max.lore")) {
                lore.add(
                    color(line.replace("{max}", String.valueOf(currentMax)))
                );
            }
            maxMeta.setLore(lore);
            maxItem.setItemMeta(maxMeta);
        }

        // Кнопка Info
        ItemStack infoItem = createMenuItemWithSlot(
            "info",
            "settings.roll-menu.info-item"
        );
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            List<String> lore = new ArrayList<>();
            for (String line : plugin
                .getConfig()
                .getStringList("settings.roll-menu.info-item.lore")) {
                lore.add(
                    color(
                        line
                            .replace("{min}", String.valueOf(currentMin))
                            .replace("{max}", String.valueOf(currentMax))
                    )
                );
            }
            infoMeta.setLore(lore);
            infoItem.setItemMeta(infoMeta);
        }

        // Кнопка Reset
        ItemStack resetItem = createMenuItemWithSlot(
            "reset",
            "settings.roll-menu.reset-item"
        );

        // Кнопка Save
        ItemStack saveItem = createMenuItemWithSlot(
            "save",
            "settings.roll-menu.save-item"
        );

        // Кнопка "Назад"
        ItemStack backItem = createMenuItemWithSlot(
            "back",
            "settings.roll-menu.back-item"
        );

        // Размещаем предметы по слотам из конфига
        inv.setItem(
            getSlotFromConfig("settings.roll-menu.roll-min.slot", 11),
            minItem
        );
        inv.setItem(
            getSlotFromConfig("settings.roll-menu.info-item.slot", 13),
            infoItem
        );
        inv.setItem(
            getSlotFromConfig("settings.roll-menu.roll-max.slot", 15),
            maxItem
        );
        inv.setItem(
            getSlotFromConfig("settings.roll-menu.reset-item.slot", 22),
            resetItem
        );
        inv.setItem(
            getSlotFromConfig("settings.roll-menu.save-item.slot", 24),
            saveItem
        );
        inv.setItem(
            getSlotFromConfig("settings.roll-menu.back-item.slot", 26),
            backItem
        );

        player.openInventory(inv);
        openMenus.put(player.getUniqueId(), inv);
        currentMenuType.put(player.getUniqueId(), "roll");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();

        if (
            !openMenus.containsKey(player.getUniqueId()) ||
            !inv.equals(openMenus.get(player.getUniqueId()))
        ) {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String menuType = currentMenuType.get(player.getUniqueId());

        switch (menuType) {
            case "main":
                handleMainMenuClick(event, player, clickedItem, meta);
                break;
            case "rpname":
                handleRpNameMenuClick(event, player, clickedItem, meta);
                break;
            case "description":
                handleDescriptionMenuClick(event, player, clickedItem, meta);
                break;
            case "colors":
                handleColorMenuClick(event, player, clickedItem, meta);
                break;
            case "roll":
                handleRollMenuClick(event, player, clickedItem, meta);
                break;
        }
    }

    private void handleMainMenuClick(
        InventoryClickEvent event,
        Player player,
        ItemStack item,
        ItemMeta meta
    ) {
        int slot = event.getSlot();

        // Проверяем, какая кнопка нажата по слоту
        int rpnameSlot = getSlotFromConfig(
            "settings.main-menu.rpname-item.slot",
            10
        );
        int descriptionSlot = getSlotFromConfig(
            "settings.main-menu.description-item.slot",
            12
        );
        int colorsSlot = getSlotFromConfig(
            "settings.main-menu.colors-item.slot",
            14
        );
        int rollSlot = getSlotFromConfig(
            "settings.main-menu.roll-settings-item.slot",
            16
        );

        if (slot == rpnameSlot) {
            openRpNameMenu(player);
        } else if (slot == descriptionSlot) {
            openDescriptionMenu(player);
        } else if (slot == colorsSlot) {
            openColorMenu(player);
        } else if (slot == rollSlot) {
            openRollMenu(player);
        }
    }

    private void handleRpNameMenuClick(
        InventoryClickEvent event,
        Player player,
        ItemStack item,
        ItemMeta meta
    ) {
        int slot = event.getSlot();

        // Проверяем слоты кнопок
        int changeSlot = getSlotFromConfig(
            "settings.rpname-menu.change-name.slot",
            11
        );
        int removeSlot = getSlotFromConfig(
            "settings.rpname-menu.remove-name.slot",
            13
        );
        int backSlot = getSlotFromConfig(
            "settings.rpname-menu.back-item.slot",
            15
        );

        if (slot == changeSlot) {
            // Закрываем меню и запрашиваем ввод нового имени
            player.closeInventory();
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.rpname-change-enter",
                            "&aВведите новое RP имя в формате: Имя Фамилия"
                        )
                )
            );
            // Устанавливаем ожидание ввода
            plugin.getChatListener().setAwaitingInput(player, "rpname_change");
        } else if (slot == removeSlot) {
            // Удаляем RP имя
            plugin.getNameManager().removeRpName(player.getUniqueId());
            plugin.getNameManager().removePlayerColor(player.getUniqueId());
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.name-removed",
                            "&aВаше RP имя удалено."
                        )
                )
            );
            player.closeInventory();
        } else if (slot == backSlot) {
            openMainMenu(player);
        }
    }

    private void handleDescriptionMenuClick(
        InventoryClickEvent event,
        Player player,
        ItemStack item,
        ItemMeta meta
    ) {
        int slot = event.getSlot();

        // Проверяем слоты кнопок
        int changeSlot = getSlotFromConfig(
            "settings.description-menu.change-description.slot",
            11
        );
        int removeSlot = getSlotFromConfig(
            "settings.description-menu.remove-description.slot",
            13
        );
        int backSlot = getSlotFromConfig(
            "settings.description-menu.back-item.slot",
            15
        );

        if (slot == changeSlot) {
            // Закрываем меню и запрашиваем ввод нового описания
            player.closeInventory();
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.description-change-enter",
                            "&aВведите новое описание персонажа (максимум 120 символов):"
                        )
                )
            );
            // Устанавливаем ожидание ввода
            plugin.getChatListener().setAwaitingInput(player, "description_change");
        } else if (slot == removeSlot) {
            // Удаляем описание
            plugin.getNameManager().removeDescription(player.getUniqueId());
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.description-removed",
                            "&aВаше описание удалено."
                        )
                )
            );
            player.closeInventory();
        } else if (slot == backSlot) {
            openMainMenu(player);
        }
    }

    private void handleColorMenuClick(
        InventoryClickEvent event,
        Player player,
        ItemStack item,
        ItemMeta meta
    ) {
        int slot = event.getSlot();

        // Проверяем слоты специальных кнопок
        int defaultColorSlot = getSlotFromConfig(
            "settings.color-menu.default-color-item.slot",
            4
        );
        int backSlot = getSlotFromConfig(
            "settings.color-menu.back-item.slot",
            31
        );

        if (slot == defaultColorSlot) {
            // Выбор цвета по умолчанию
            plugin.getNameManager().removePlayerColor(player.getUniqueId());
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.color-removed",
                            "&aЦвет имени сброшен на значение по умолчанию"
                        )
                )
            );
            player.closeInventory();
        } else if (slot == backSlot) {
            openMainMenu(player);
        } else {
            // Ищем цвет по слоту в конфиге
            ConfigurationSection colors = plugin
                .getColorsConfig()
                .getConfigurationSection("colors");
            String foundColorId = null;
            if (colors != null) {
                for (String colorId : colors.getKeys(false)) {
                    int colorSlot = colors.getInt(colorId + ".slot", -1);
                    if (colorSlot == slot) {
                        foundColorId = colorId;
                        break;
                    }
                }
            }

            if (foundColorId != null) {
                String permission = plugin
                    .getColorsConfig()
                    .getString("colors." + foundColorId + ".permission", "");
                String colorCode = plugin
                    .getColorsConfig()
                    .getString("colors." + foundColorId + ".color-code", "&f");
                String colorName = plugin
                    .getColorsConfig()
                    .getString(
                        "colors." + foundColorId + ".name",
                        "&fНеизвестный цвет"
                    );

                // Проверяем права
                if (
                    !permission.isEmpty() &&
                    !player.hasPermission(permission) &&
                    !player.hasPermission("dontrp.rpname_color.*") &&
                    !player.hasPermission("dontrp.admin")
                ) {
                    player.sendMessage(
                        color(
                            plugin
                                .getConfig()
                                .getString(
                                    "messages.color-no-permission",
                                    "&cУ вас нет прав на этот цвет!"
                                )
                        )
                    );
                    return;
                }

                // Устанавливаем цвет
                plugin
                    .getNameManager()
                    .setPlayerColor(player.getUniqueId(), foundColorId);
                player.sendMessage(
                    color(
                        plugin
                            .getConfig()
                            .getString(
                                "messages.color-set",
                                "&aЦвет имени установлен: {color}"
                            )
                            .replace("{color}", color(colorName))
                    )
                );
                player.closeInventory();
            }
        }
    }

    private void handleRollMenuClick(
        InventoryClickEvent event,
        Player player,
        ItemStack item,
        ItemMeta meta
    ) {
        int slot = event.getSlot();

        // Проверяем слоты кнопок
        int minSlot = getSlotFromConfig("settings.roll-menu.roll-min.slot", 11);
        int maxSlot = getSlotFromConfig("settings.roll-menu.roll-max.slot", 15);
        int resetSlot = getSlotFromConfig(
            "settings.roll-menu.reset-item.slot",
            22
        );
        int saveSlot = getSlotFromConfig(
            "settings.roll-menu.save-item.slot",
            24
        );
        int backSlot = getSlotFromConfig(
            "settings.roll-menu.back-item.slot",
            26
        );

        RPCommands commands = (RPCommands) plugin
            .getCommand("rp")
            .getExecutor();
        int currentMin = commands.getPlayerRollMin(player.getUniqueId());
        int currentMax = commands.getPlayerRollMax(player.getUniqueId());

        if (slot == minSlot) {
            if (event.isLeftClick()) {
                if (event.isShiftClick()) {
                    // Shift+ЛКМ - ввод вручную
                    player.closeInventory();
                    player.sendMessage(
                        color(
                            plugin
                                .getConfig()
                                .getString(
                                    "messages.roll-enter-min",
                                    "&aВведите минимальное значение:"
                                )
                        )
                    );
                    plugin
                        .getChatListener()
                        .setAwaitingInput(player, "roll_min");
                } else {
                    int newMin = currentMin + 1;
                    if (newMin >= currentMax) {
                        player.sendMessage(
                            color(
                                plugin
                                    .getConfig()
                                    .getString(
                                        "messages.roll-min-greater",
                                        "&cМинимальное значение не может быть больше максимального!"
                                    )
                            )
                        );
                    } else {
                        commands.setPlayerRollMin(player.getUniqueId(), newMin);
                        player.sendMessage(
                            color(
                                plugin
                                    .getConfig()
                                    .getString(
                                        "messages.roll-min-changed",
                                        "&aМинимальное значение установлено: &e{min}"
                                    )
                                    .replace("{min}", String.valueOf(newMin))
                            )
                        );
                        openRollMenu(player);
                    }
                }
            } else if (event.isRightClick()) {
                int newMin = Math.max(0, currentMin - 1);
                commands.setPlayerRollMin(player.getUniqueId(), newMin);
                player.sendMessage(
                    color(
                        plugin
                            .getConfig()
                            .getString(
                                "messages.roll-min-changed",
                                "&aМинимальное значение установлено: &e{min}"
                            )
                            .replace("{min}", String.valueOf(newMin))
                    )
                );
                openRollMenu(player);
            }
        } else if (slot == maxSlot) {
            if (event.isLeftClick()) {
                if (event.isShiftClick()) {
                    // Shift+ЛКМ - ввод вручную
                    player.closeInventory();
                    player.sendMessage(
                        color(
                            plugin
                                .getConfig()
                                .getString(
                                    "messages.roll-enter-max",
                                    "&aВведите максимальное значение:"
                                )
                        )
                    );
                    plugin
                        .getChatListener()
                        .setAwaitingInput(player, "roll_max");
                } else {
                    int newMax = currentMax + 1;
                    if (newMax < currentMin) {
                        player.sendMessage(
                            color(
                                plugin
                                    .getConfig()
                                    .getString(
                                        "messages.roll-min-greater",
                                        "&cМаксимальное значение не может быть меньше минимального!"
                                    )
                            )
                        );
                    } else {
                        commands.setPlayerRollMax(player.getUniqueId(), newMax);
                        player.sendMessage(
                            color(
                                plugin
                                    .getConfig()
                                    .getString(
                                        "messages.roll-max-changed",
                                        "&aМаксимальное значение установлено: &e{max}"
                                    )
                                    .replace("{max}", String.valueOf(newMax))
                            )
                        );
                        openRollMenu(player);
                    }
                }
            } else if (event.isRightClick()) {
                int newMax = Math.max(currentMin + 1, currentMax - 1);
                commands.setPlayerRollMax(player.getUniqueId(), newMax);
                player.sendMessage(
                    color(
                        plugin
                            .getConfig()
                            .getString(
                                "messages.roll-max-changed",
                                "&aМаксимальное значение установлено: &e{max}"
                            )
                            .replace("{max}", String.valueOf(newMax))
                    )
                );
                openRollMenu(player);
            }
        } else if (slot == resetSlot) {
            commands.resetPlayerRollSettings(player.getUniqueId());
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.roll-reset",
                            "&aНастройки Roll сброшены"
                        )
                )
            );
            openRollMenu(player);
        } else if (slot == saveSlot) {
            player.sendMessage(
                color(
                    plugin
                        .getConfig()
                        .getString(
                            "messages.roll-saved",
                            "&aНастройки сохранены!"
                        )
                )
            );
            player.closeInventory();
        } else if (slot == backSlot) {
            openMainMenu(player);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        openMenus.remove(player.getUniqueId());
        currentMenuType.remove(player.getUniqueId());
    }

    // Вспомогательные методы
    private ItemStack createMenuItem(String type, String configPath) {
        Material material = Material.getMaterial(
            plugin.getConfig().getString(configPath + ".material", "STONE")
        );
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                color(
                    plugin
                        .getConfig()
                        .getString(configPath + ".name", "Предмет")
                )
            );

            List<String> lore = new ArrayList<>();
            for (String line : plugin
                .getConfig()
                .getStringList(configPath + ".lore")) {
                lore.add(color(line));
            }
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createMenuItemWithSlot(String type, String configPath) {
        // Создаем предмет без учета слота (слот используется только для размещения)
        return createMenuItem(type, configPath);
    }

    private int getSlotFromConfig(String configPath, int defaultValue) {
        return plugin.getConfig().getInt(configPath, defaultValue);
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
