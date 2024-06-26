/*
 * BedWars2023 - A bed wars mini-game.
 * Copyright (C) 2024 Tomas Keuper
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: contact@fyreblox.com
 */

package com.tomkeuper.bedwars.upgrades;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.upgrades.MenuContent;
import com.tomkeuper.bedwars.api.upgrades.UpgradesIndex;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.configuration.UpgradesConfig;
import com.tomkeuper.bedwars.upgrades.listeners.InventoryListener;
import com.tomkeuper.bedwars.upgrades.listeners.UpgradeOpenListener;
import com.tomkeuper.bedwars.upgrades.menu.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;

import static com.tomkeuper.bedwars.BedWars.nms;
import static com.tomkeuper.bedwars.BedWars.plugin;

public class UpgradesManager {

    private final LinkedList<UUID> upgradeViewers = new LinkedList<>();
    //store lower case names
    private final HashMap<String, MenuContent> menuContentByName = new HashMap<>();
    //store lower case names
    private final HashMap<String, UpgradesIndex> menuByName = new HashMap<>();

    private final HashMap<IArena, UpgradesIndex> customMenuForArena = new HashMap<>();

    private final UpgradesConfig upgrades;

    public UpgradesManager() {
        upgrades = new UpgradesConfig("upgrades", plugin.getDataFolder().getPath());
    }

    public void init(){
        String name;
        for (String index : upgrades.getYml().getConfigurationSection("").getKeys(false)) {
            name = index;
            if (index.startsWith("upgrade-")) {
                if (getMenuContent(name) == null && !loadUpgrade(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not load upgrade: " + name);
                }
            } else if (index.startsWith("separator-")) {
                if (getMenuContent(name) == null && !loadSeparator(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not load separator: " + name);
                }
            } else if (index.startsWith("category-")) {
                if (getMenuContent(name) == null && !loadCategory(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not load category: " + name);
                }
            } else if (index.startsWith("base-trap-")) {
                if (getMenuContent(name) == null && !loadBaseTrap(name)) {
                    Bukkit.getLogger().log(Level.WARNING, "Could not base trap: " + name);
                }
            } else if (index.endsWith("-upgrades-settings")) {
                name = index.replace("-upgrades-settings", "");
                if (!name.isEmpty()) {
                    if (!loadMenu(name)) {
                        Bukkit.getLogger().log(Level.WARNING, "Could not load menu: " + name);
                    }
                }
            }
        }

        BedWars.registerEvents(new InventoryListener(), new UpgradeOpenListener());
    }

    /**
     * @return true if has the upgrades GUI opened.
     */
    public boolean isWatchingUpgrades(UUID uuid) {
        return upgradeViewers.contains(uuid);
    }

    /**
     * Set watching upgrades GUI.
     */
    public void setWatchingUpgrades(UUID uuid) {
        if (!upgradeViewers.contains(uuid)) upgradeViewers.add(uuid);
    }

    /**
     * Remove from upgrades GUI.
     */
    public void removeWatchingUpgrades(UUID uuid) {
        upgradeViewers.remove(uuid);
    }

    /**
     * Load a menu for a group.
     *
     * @param groupName arena group name.
     * @return false if cannot be loaded.
     */
    public boolean loadMenu(String groupName) {
        if (!upgrades.getYml().isSet(groupName + "-upgrades-settings.menu-content")) return false;
        if (menuByName.containsKey(groupName.toLowerCase())) return false;
        InternalMenu um = new InternalMenu(groupName);
        for (String component : upgrades.getYml().getStringList(groupName + "-upgrades-settings.menu-content")) {
            String[] data = component.split(",");
            if (data.length <= 1) continue;

            MenuContent mc = getMenuContent(data[0]);
            if (data[0].startsWith("category-")) {
                if (mc == null && loadCategory(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("upgrade-")) {
                if (mc == null && loadUpgrade(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("trap-slot-")) {
                if (mc == null && loadTrapSlot(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("separator-")) {
                if (mc == null && loadSeparator(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("base-trap-")) {
                if (mc == null && loadBaseTrap(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            }
            if (mc == null) continue;
            for (int i = 1; i < data.length; i++) {
                if (Misc.isNumber(data[i])) um.addContent(mc, Integer.parseInt(data[i]));
            }
        }
        menuByName.put(groupName.toLowerCase(), um);
        BedWars.debug("Registering upgrade menu: " + groupName);
        return true;
    }

    /**
     * Load a category with given name from the shop file.
     *
     * @param name category name. Must start with "category-".
     * @return false if cannot be loaded.
     */
    private boolean loadCategory(String name) {
        if (name == null) return false;
        if (!name.startsWith("category-")) return false;
        if (upgrades.getYml().get(name) == null) return false;
        if (getMenuContent(name) != null) return false;
        MenuCategory uc = new MenuCategory(name, createDisplayItem(name));
        for (String component : upgrades.getYml().getStringList(name + ".category-content")) {
            String[] data = component.split(",");
            if (data.length <= 1) continue;

            MenuContent mc = null;
            if (data[0].startsWith("category-")) {
                mc = getMenuContent(data[0]);
                if (mc == null && loadCategory(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("upgrade-")) {
                mc = getMenuContent(data[0]);
                if (mc == null && loadUpgrade(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("trap-slot-")) {
                mc = getMenuContent(data[0]);
                if (mc == null && loadTrapSlot(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("separator-")) {
                mc = getMenuContent(data[0]);
                if (mc == null && loadSeparator(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            } else if (data[0].startsWith("base-trap-")) {
                mc = getMenuContent(data[0]);
                if (mc == null && loadBaseTrap(data[0])) {
                    mc = getMenuContent(data[0]);
                }
            }
            if (mc == null) continue;
            for (int i = 1; i < data.length; i++) {
                if (Misc.isNumber(data[i])) uc.addContent(mc, Integer.parseInt(data[i]));
            }
        }
        menuContentByName.put(name.toLowerCase(), uc);
        BedWars.debug("Registering upgrade: " + name);
        return true;
    }

    /**
     * Load an upgrade element with given name.
     *
     * @param name upgrade name. Must start with "upgrade-".
     * @return false if can't be loaded.
     */
    private boolean loadUpgrade(String name) {
        if (name == null) return false;
        if (!name.startsWith("upgrade-")) return false;
        if (upgrades.getYml().get(name) == null) return false;
        if (upgrades.getYml().get(name + ".tier-1") == null) return false;
        if (getMenuContent(name) != null) return false;
        MenuUpgrade mu = new MenuUpgrade(name);

        for (String s : upgrades.getYml().getConfigurationSection(name).getKeys(false)) {
            if (!s.startsWith("tier-")) continue;
            if (upgrades.getYml().get(name + "." + s + ".receive") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Receive not set.");
                continue;
            }
            if (upgrades.getYml().get(name + "." + s + ".display-item") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Display item not set.");
                continue;
            }
            if (upgrades.getYml().get(name + "." + s + ".cost") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Cost not set.");
                continue;
            }
            if (upgrades.getYml().get(name + "." + s + ".currency") == null) {
                BedWars.debug("Could not load Upgrade " + name + " tier: " + s + ". Currency not set.");
                continue;
            }
            UpgradeTier ut = new UpgradeTier(name, s, createDisplayItem(name + "." + s), upgrades.getYml().getInt(name + "." + s + ".cost"), getCurrency(upgrades.getYml().getString(name + "." + s + ".currency")));
            if (!mu.addTier(ut)) {
                Bukkit.getLogger().log(Level.WARNING, "Could not load tier: " + s + " at upgrade: " + name);
            }
        }
        BedWars.debug("Registering upgrade: " + name);
        menuContentByName.put(name.toLowerCase(), mu);
        return true;
    }

    /**
     * Load a separator with given name.
     *
     * @param name name. Must start with "separator-".
     * @return false if cannot be loaded.
     */
    private boolean loadSeparator(String name) {
        if (name == null) return false;
        if (!name.startsWith("separator-")) return false;
        if (upgrades.getYml().get(name) == null) return false;
        if (getMenuContent(name) != null) return false;
        MenuSeparator ms = new MenuSeparator(name, createDisplayItem(name));
        menuContentByName.put(name.toLowerCase(), ms);
        BedWars.debug("Registering upgrade: " + name);
        return true;
    }

    /**
     * Load a trap slot with given name.
     *
     * @param name name. Must start with "trap-slot-".
     * @return false if cannot be loaded.
     */
    private boolean loadTrapSlot(String name) {
        if (name == null) return false;
        if (!name.startsWith("trap-slot-")) return false;
        if (upgrades.getYml().get(name) == null) return false;
        if (getMenuContent(name) != null) return false;
        MenuTrapSlot mts = new MenuTrapSlot(name, createDisplayItem(name));
        menuContentByName.put(name.toLowerCase(), mts);
        BedWars.debug("Registering upgrade: " + name);
        return true;
    }

    private boolean loadBaseTrap(String name) {
        if (name == null) return false;
        if (!name.startsWith("base-trap-")) return false;
        if (upgrades.getYml().get(name) == null) return false;
        if (upgrades.getYml().get(name + ".receive") == null) {
            BedWars.debug("Could not load BaseTrap. Receive not set.");
            return false;
        }
        if (upgrades.getYml().get(name + ".display-item") == null) {
            BedWars.debug("Could not load BaseTrap. Display item not set.");
            return false;
        }

        MenuBaseTrap bt = new MenuBaseTrap(name, createDisplayItem(name), upgrades.getYml().getInt(name + ".cost"), getCurrency(upgrades.getYml().getString(name + ".currency")));
        BedWars.debug("Registering upgrade: " + name);
        menuContentByName.put(name.toLowerCase(), bt);
        return true;
    }

    /**
     * Check the money amount in a given currency of a player.
     *
     * @param player   target player.
     * @param currency {@link org.bukkit.Material#AIR} is used for vault, {@link org.bukkit.Material#IRON_INGOT} for iron, {@link org.bukkit.Material#GOLD_INGOT} for gold, {@link org.bukkit.Material#DIAMOND} for diamond, {@link org.bukkit.Material#EMERALD} for emerald.
     * @return the amount of money.
     */
    public int getMoney(Player player, Material currency) {
        if (currency == Material.AIR) {
            double amount = BedWars.getEconomy().getMoney(player);
            return amount % 2 == 0 ? (int) amount : (int) (amount - 1);
        }
        return BedWars.getAPI().getShopUtil().calculateMoney(player, currency);
    }

    /**
     * @param name the string to be converted.
     * @return null if not a currency. {@link org.bukkit.Material#AIR} is used for vault, {@link org.bukkit.Material#IRON_INGOT} for iron, {@link org.bukkit.Material#GOLD_INGOT} for gold, {@link org.bukkit.Material#DIAMOND} for diamond, {@link org.bukkit.Material#EMERALD} for emerald.
     */
    public Material getCurrency(String name) {
        if (name == null || name.isEmpty()) return null;
        return BedWars.getAPI().getShopUtil().getCurrency(name);
    }

    /**
     * Check if is upgradable item.
     * Used in inventory click.
     *
     * @param item item to be checked.
     * @return {@link MenuContent} NULL if isn't an element.
     */
    public MenuContent getMenuContent(ItemStack item) {
        if (item == null) return null;

        String identifier = nms.getCustomData(item);
        if (identifier == null) return null;
        if (identifier.equals("null")) return null;
        if (!identifier.startsWith("MCONT_")) return null;
        identifier = identifier.replaceFirst("MCONT_", "");
        if (identifier.isEmpty()) return null;
        return menuContentByName.getOrDefault(identifier.toLowerCase(), null);
    }

    /**
     * Get menu content by identifier.
     *
     * @return null if not found.
     */
    public MenuContent getMenuContent(String identifier) {
        return menuContentByName.getOrDefault(identifier.toLowerCase(), null);
    }

    /**
     * Set a custom menu that will override the group menu for an arena.
     *
     * @param arena target arena.
     * @param menu  custom menu.
     */
    public void setCustomMenuForArena(IArena arena, UpgradesIndex menu) {
        if (!customMenuForArena.containsKey(arena)) {
            customMenuForArena.put(arena, menu);
            BedWars.debug("Registering custom menu for arena: " + arena.getArenaName() + ". Using index: " + menu.getName());
        } else {
            BedWars.debug("Overriding custom menu for arena: " + arena.getArenaName() + ". Using index: " + menu.getName() + " Old index: " + customMenuForArena.get(arena).getName());
            customMenuForArena.replace(arena, menu);
        }
    }

    /**
     * Get upgrade menu index for an arena.
     *
     * @param arena target arena.
     * @return the default menu if the arena doesn't have an arena group menu set or a custom menu by an addon.
     */
    public UpgradesIndex getMenuForArena(IArena arena) {
        if (customMenuForArena.containsKey(arena)) return customMenuForArena.get(arena);
        return menuByName.getOrDefault(arena.getGroup().toLowerCase(), menuByName.get("default"));
    }

    /**
     * Create a display item.
     *
     * @param path config path.
     * @return bedrock if given material is null
     */
    private ItemStack createDisplayItem(String path) {
        Material m;
        try {
            m = Material.valueOf(upgrades.getYml().getString(path + ".display-item.material"));
        } catch (Exception e) {
            m = Material.BEDROCK;
        }
        ItemStack i = new ItemStack(m, Integer.parseInt(upgrades.getYml().getString(path + ".display-item.amount")), (short) upgrades.getYml().getInt(path + ".display-item.data"));
        if (upgrades.getYml().getBoolean(path + ".display-item.enchanted")) {
            i.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            ItemMeta im = i.getItemMeta();
            if (im != null) {
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                i.setItemMeta(im);
            }
        }
        return i;
    }

    /**
     * Get currency msg.
     */
    public String getCurrencyMsg(Player p, UpgradeTier ut) {
        String c = "";

        switch (ut.getCurrency()) {
            case IRON_INGOT:
                c = ut.getCost() == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
                break;
            case GOLD_INGOT:
                c = ut.getCost() == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
                break;
            case EMERALD:
                c = ut.getCost() == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
                break;
            case DIAMOND:
                c = ut.getCost() == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
                break;
            case AIR:
                c = ut.getCost() == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
                break;
        }

        return Language.getMsg(p, c);
    }

    public String getCurrencyMsg(Player p, int money, String currency) {
        String c;
        if (currency == null) {
            return Language.getMsg(p, money == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL);
        }

        switch (currency.toLowerCase()) {
            case "iron":
                c = money == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
                break;
            case "gold":
                c = money == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
                break;
            case "emerald":
                c = money == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
                break;
            case "diamond":
                c = money == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
                break;
            default:
                c = money == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
                break;
        }

        return Language.getMsg(p, c);
    }

    public String getCurrencyMsg(Player p, int money, Material currency) {
        String c;

        switch (currency) {
            case IRON_INGOT:
                c = money == 1 ? Messages.MEANING_IRON_SINGULAR : Messages.MEANING_IRON_PLURAL;
                break;
            case GOLD_INGOT:
                c = money == 1 ? Messages.MEANING_GOLD_SINGULAR : Messages.MEANING_GOLD_PLURAL;
                break;
            case EMERALD:
                c = money == 1 ? Messages.MEANING_EMERALD_SINGULAR : Messages.MEANING_EMERALD_PLURAL;
                break;
            case DIAMOND:
                c = money == 1 ? Messages.MEANING_DIAMOND_SINGULAR : Messages.MEANING_DIAMOND_PLURAL;
                break;
            default:
                c = money == 1 ? Messages.MEANING_VAULT_SINGULAR : Messages.MEANING_VAULT_PLURAL;
                break;
        }

        return Language.getMsg(p, c);
    }

    public ChatColor getCurrencyColor(Material currency) {
        switch (currency) {
            case DIAMOND:
                return ChatColor.AQUA;
            case GOLD_INGOT:
                return ChatColor.GOLD;
            case IRON_INGOT:
                return ChatColor.WHITE;
            case EMERALD:
                return ChatColor.GREEN;
            default:
                return ChatColor.DARK_GREEN;
        }
    }

    public UpgradesConfig getConfiguration() {
        return upgrades;
    }

    public int getMenuSize() {
        int size = getConfiguration().getInt("default-upgrades-settings.menu-size");
        if(size < 0 || size > 54) {
            size = 45;
        } else if(size % 9 != 0) {
            size = 45;
        }
        return size;
    }

    public HashMap<String, MenuContent> menuContentByName() {
        return menuContentByName;
    }
}
