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

package com.tomkeuper.bedwars.shop.main;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.shop.ShopOpenEvent;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.shop.IPlayerQuickBuyCache;
import com.tomkeuper.bedwars.api.shop.IShopCategory;
import com.tomkeuper.bedwars.api.shop.IShopIndex;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.shop.ShopCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public class ShopIndex implements IShopIndex {

    private int invSize = 54;
    private String namePath, separatorNamePath, separatorLorePath;
    private List<IShopCategory> categoryList = new ArrayList<>();
    private QuickBuyButton quickBuyButton;
    public ItemStack separatorSelected, separatorStandard;

    public static List<UUID> indexViewers = new ArrayList<>();


    /**
     * Create a shop index
     *
     * @param namePath          Message path for the shop inventory name
     * @param quickBuyButton    Player quick buy preferences cache
     * @param separatorNamePath Message path for the shop separator item name
     * @param separatorLorePath Message path for the shop separator lore name
     * @param separatorSelected ItemStack for selected category indicator
     * @param separatorStandard ItemStack for standard separator
     */
    public ShopIndex(String namePath, QuickBuyButton quickBuyButton, String separatorNamePath, String separatorLorePath, ItemStack separatorSelected, ItemStack separatorStandard) {
        this.namePath = namePath;
        this.separatorLorePath = separatorLorePath;
        this.separatorNamePath = separatorNamePath;
        this.quickBuyButton = quickBuyButton;
        this.separatorStandard = separatorStandard;
        this.separatorSelected = separatorSelected;
    }

    /**
     * Open this shop to a player
     *
     * @param callEvent     true if you want to call the shop open event
     * @param quickBuyCache the player cache regarding his preferences
     * @param player        target player
     */
    @Override
    public void open(Player player, IPlayerQuickBuyCache quickBuyCache, boolean callEvent) {

        if (quickBuyCache == null) return;

        if (callEvent) {
            ShopOpenEvent event = new ShopOpenEvent(player, Arena.getArenaByPlayer(player));
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;
        }

        Inventory inv = Bukkit.createInventory(null, invSize, Language.getMsg(player, getNamePath()));

        inv.setItem(getQuickBuyButton().getSlot(), getQuickBuyButton().getItemStack(player));

        IArena arena = Arena.getArenaByPlayer(player);
        for (IShopCategory sc : getCategoryList()) {
            // Check if the shop name starts with "default" or matches the arena group name
            // If we don't check this, the shop will be displayed in all arenas
            if (sc.getName().toLowerCase().startsWith("default") || sc.getName().toLowerCase().startsWith(arena.getGroup().toLowerCase())) {
                inv.setItem(sc.getSlot(), sc.getItemStack(player));
            }
        }

        addSeparator(player, inv);

        inv.setItem(getQuickBuyButton().getSlot() + 9, getSelectedItem(player));

        ShopCache playerShopCache = ShopCache.getInstance().getShopCache(player.getUniqueId());
        playerShopCache.setSelectedCategory(getQuickBuyButton().getSlot());
        quickBuyCache.addInInventory(inv, playerShopCache);

        player.openInventory(inv);
        if (!indexViewers.contains(player.getUniqueId())) {
            indexViewers.add(player.getUniqueId());
        }
    }


    /**
     * Add shop separator between categories and items
     */
    @Override
    public void addSeparator(Player player, Inventory inv) {
        ItemStack i = separatorStandard.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath));
            im.setLore(Language.getList(player, separatorLorePath));
            i.setItemMeta(im);
        }

        for (int x = 9; x < 18; x++) {
            inv.setItem(x, i);
        }
    }

    /**
     * This is the item that indicates the selected category
     */
    @Override
    public ItemStack getSelectedItem(Player player) {
        ItemStack i = separatorSelected.clone();
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, separatorNamePath));
            im.setLore(Language.getList(player, separatorLorePath));
            i.setItemMeta(im);
        }
        return i;
    }

    /**
     * Add a shop category
     */
    @Override
    public void addShopCategory(IShopCategory sc) {
        categoryList.add(sc);
        BedWars.debug("Adding shop category: " + sc + " at slot " + sc.getSlot());
    }

    /**
     * Get the inventory name path
     */
    @Override
    public String getNamePath() {
        return namePath;
    }

    /**
     * Get the inventory size
     */
    @Override
    public int getInvSize() {
        return invSize;
    }

    /**
     * Get the shop's categories
     */
    public List<IShopCategory> getCategoryList() {
        return categoryList;
    }

    /**
     * Get the quick buy button
     */
    public QuickBuyButton getQuickBuyButton() {
        return quickBuyButton;
    }

    public static List<UUID> getIndexViewers() {
        return new ArrayList<>(indexViewers);
    }
}
