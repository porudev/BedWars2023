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

package com.tomkeuper.bedwars.api.events.server;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaRestartEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private String arena, worldName;

    /**
     * Called when an arena is restarting.
     * After the world was unloaded.
     */
    public ArenaRestartEvent(String arena, String worldName) {
        this.arena = arena;
        this.worldName = worldName;
    }

    /**
     * Get the arena name.
     */
    public String getArenaName() {
        return arena;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getWorldName() {
        return worldName;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
