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

package com.tomkeuper.bedwars.support.vipfeatures;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.server.ServerType;
import com.andrei1058.vipfeatures.api.IVipFeatures;
import com.andrei1058.vipfeatures.api.event.BlockChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

public class VipListeners implements Listener {

    private final IVipFeatures api;

    public VipListeners(IVipFeatures api) {
        this.api = api;
    }

    @EventHandler
    public void onServerJoin(PlayerJoinEvent e) {
        if (BedWars.getServerType() == ServerType.MULTIARENA) {
            Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> api.givePlayerItemStack(e.getPlayer()), 10L);
        }
    }

    @EventHandler
    public void onArenaJoin(PlayerJoinArenaEvent e) {
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> api.givePlayerItemStack(e.getPlayer()), 10L);
    }

    @EventHandler
    public void onBockChange(BlockChangeEvent e) {
        if (BedWars.getAPI().getArenaUtil().getArenaByName(e.getLocation().getWorld().getName()) != null) {
            IArena a = BedWars.getAPI().getArenaUtil().getArenaByName(e.getLocation().getWorld().getName());
            for (ITeam t : a.getTeams()) {
                for (int x = -1; x < 2; x++) {
                    for (int z = -1; z < 2; z++) {
                        if (e.getLocation().getBlockX() == t.getBed().getBlockX() &&
                                e.getLocation().getBlockY() == t.getBed().getBlockY() &&
                                e.getLocation().getBlockZ() == t.getBed().getBlockZ()) {
                            if (BedWars.nms.isBed(t.getBed().clone().add(x, 0, z).getBlock().getType())) e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            a.getPlaced().add(new Vector(e.getLocation().getBlockX(), e.getLocation().getBlockY(),e.getLocation().getBlockZ()));
        }
    }
}
