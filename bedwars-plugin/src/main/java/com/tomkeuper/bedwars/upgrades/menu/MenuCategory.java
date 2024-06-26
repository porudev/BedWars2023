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

package com.tomkeuper.bedwars.upgrades.menu;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.api.upgrades.MenuContent;
import com.tomkeuper.bedwars.configuration.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuCategory implements MenuContent {

    private ItemStack displayItem;
    private String name;

    private HashMap<Integer, MenuContent> menuContentBySlot = new HashMap<>();

    public MenuCategory(String name, ItemStack displayItem) {
        this.name = name;
        this.displayItem = BedWars.nms.addCustomData(displayItem, "MCONT_" + name);
        Language.saveIfNotExists(Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + name.replace("category-", ""), "&8" + name);
        Language.saveIfNotExists(Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + name.replace("category-", ""), "&cName not set");
        Language.saveIfNotExists(Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + name.replace("category-", ""), Collections.singletonList("&cLore not set"));
    }

    /**
     * Add content to a menu.
     *
     * @param content content instance.
     * @param slot    where to put the content in the menu.
     * @return false if te given slot is in use.
     */
    public boolean addContent(MenuContent content, int slot) {
        if (menuContentBySlot.get(slot) != null) return false;
        menuContentBySlot.put(slot, content);
        return true;
    }

    @Override
    public ItemStack getDisplayItem(Player player, ITeam team) {
        ItemStack i = new ItemStack(displayItem);
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Language.getMsg(player, Messages.UPGRADES_CATEGORY_ITEM_NAME_PATH + name.replace("category-", "")));
            List<String> lore = Language.getList(player, Messages.UPGRADES_CATEGORY_ITEM_LORE_PATH + name.replace("category-", ""));

            if (name.equalsIgnoreCase("traps")) {
                int queueLimit = BedWars.getUpgradeManager().getConfiguration().getInt(team.getArena().getGroup().toLowerCase() + "-upgrades-settings.trap-queue-limit");
                if (queueLimit == 0) {
                    queueLimit = BedWars.getUpgradeManager().getConfiguration().getInt("default-upgrades-settings.trap-queue-limit");
                }
                if (queueLimit == team.getActiveTraps().size()) {
                    lore.add("");
                    lore.add(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT));
                }
            }
            im.setLore(lore);
            i.setItemMeta(im);
        }
        return i;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean onClick(Player player, ClickType clickType, ITeam team, boolean forFree, boolean announcePurchase, boolean announceAlreadyUnlocked, boolean openInv) {
        if (name.equalsIgnoreCase("category-traps")){
            int queueLimit = BedWars.getUpgradeManager().getConfiguration().getInt(team.getArena().getGroup().toLowerCase()+"-upgrades-settings.trap-queue-limit");
            if (queueLimit == 0){
                queueLimit = BedWars.getUpgradeManager().getConfiguration().getInt("default-upgrades-settings.trap-queue-limit");
            }
            if (queueLimit <= team.getActiveTraps().size()){
                if (announceAlreadyUnlocked){
                    Sounds.playSound(ConfigPath.SOUNDS_INSUFF_MONEY, player);
                    player.sendMessage(Language.getMsg(player, Messages.UPGRADES_TRAP_QUEUE_LIMIT));
                }
                return false;
            }
        }
        Inventory inv = Bukkit.createInventory(null, 45, Language.getMsg(player, Messages.UPGRADES_CATEGORY_GUI_NAME_PATH + name.replace("category-", "")));
        for (Map.Entry<Integer, MenuContent> entry : menuContentBySlot.entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().getDisplayItem(player, team));
        }
        player.openInventory(inv);
        BedWars.getUpgradeManager().setWatchingUpgrades(player.getUniqueId());
        return true;
    }

}
