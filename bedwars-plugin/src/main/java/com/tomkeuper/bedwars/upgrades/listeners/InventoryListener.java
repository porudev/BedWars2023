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

package com.tomkeuper.bedwars.upgrades.listeners;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.upgrades.MenuContent;
import com.tomkeuper.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        IArena a = Arena.getArenaByPlayer((Player) e.getWhoClicked());
        if (a == null) return;
        if (a.isSpectator((Player) e.getWhoClicked())) return;
        if (!BedWars.getUpgradeManager().isWatchingUpgrades(e.getWhoClicked().getUniqueId())) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getType() == Material.AIR) return;

        MenuContent mc = BedWars.getUpgradeManager().getMenuContent(e.getCurrentItem());
        if (mc == null) return;
        mc.onClick((Player) e.getWhoClicked(), e.getClick(), a.getTeam((Player) e.getWhoClicked()), false, true, true, true);
    }

    @EventHandler
    public void onUpgradesClose(InventoryCloseEvent e) {
        BedWars.getUpgradeManager().removeWatchingUpgrades(e.getPlayer().getUniqueId());
    }
}
