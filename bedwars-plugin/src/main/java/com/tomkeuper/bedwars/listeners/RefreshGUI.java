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

package com.tomkeuper.bedwars.listeners;

import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaDisableEvent;
import com.tomkeuper.bedwars.api.events.server.ArenaEnableEvent;
import com.tomkeuper.bedwars.arena.ArenaGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RefreshGUI implements Listener {

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent e){
        if (e == null) return;
        int size = e.getArena().getPlayers().size();
        for (Player p : Bukkit.getOnlinePlayers()){
            ArenaGUI.refreshInv(p, e.getArena(), size);
        }
    }

    @EventHandler
    public void onPlayerJoinArena(PlayerJoinArenaEvent e){
        if (e == null) return;
        int size = e.getArena().getPlayers().size();
        if (!e.isSpectator()){
            size++;
        }
        for (Player p : Bukkit.getOnlinePlayers()){
            ArenaGUI.refreshInv(p, e.getArena(), size);
        }
    }

    @EventHandler
    public void onPlayerLeaveArena(PlayerLeaveArenaEvent e){
        if (e == null) return;
        int size = e.getArena().getPlayers().size();
        if (!e.isSpectator()){
            size--;
        }
        for (Player p : Bukkit.getOnlinePlayers()){
            ArenaGUI.refreshInv(p, e.getArena(), size);
        }
    }

    @EventHandler
    public void onArenaEnable(ArenaEnableEvent e){
        if (e == null) return;
        for (Player p : Bukkit.getOnlinePlayers()){
            ArenaGUI.refreshInv(p, e.getArena(), 0);
        }
    }

    @EventHandler
    public void onArenaDisable(ArenaDisableEvent e){
        for (Player p : Bukkit.getOnlinePlayers()){
            ArenaGUI.refreshInv(p, null, 0);
        }
    }
}
