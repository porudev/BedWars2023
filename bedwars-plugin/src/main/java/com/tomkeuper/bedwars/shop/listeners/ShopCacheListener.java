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

package com.tomkeuper.bedwars.shop.listeners;

import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.shop.ShopCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ShopCacheListener implements Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaJoin(PlayerJoinArenaEvent e) {
        if (e.isSpectator()) return;
        ShopCache sc = ShopCache.getInstance().getShopCache(e.getPlayer().getUniqueId());
        if (sc != null) {
            sc.destroy();
        }
        new ShopCache(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaLeave(PlayerLeaveArenaEvent e) {
        ShopCache sc = ShopCache.getInstance().getShopCache(e.getPlayer().getUniqueId());
        if (sc != null) {
            sc.destroy();
        }
    }

    @EventHandler
    public void onServerLeave(PlayerQuitEvent e) {
        //if (Main.getServerType() == ServerType.BUNGEE) return;
        //don't remove immediately in case of /rejoin
        ShopCache sc = ShopCache.getInstance().getShopCache(e.getPlayer().getUniqueId());
        if (sc != null) {
            sc.destroy();
        }
    }

}
