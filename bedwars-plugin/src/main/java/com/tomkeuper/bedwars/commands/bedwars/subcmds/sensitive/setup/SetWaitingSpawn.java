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

package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive.setup;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.api.server.SetupType;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.configuration.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetWaitingSpawn extends SubCommand {

    public SetWaitingSpawn(ParentCommand parent, String name) {
        super(parent, name);
        setArenaSetupCommand(true);
        setPermission(Permissions.PERMISSION_SETUP_ARENA);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        SetupSession ss = SetupSession.getSession(p.getUniqueId());
        if (ss == null){
            s.sendMessage("§c ▪ §7You're not in a setup session!");
            return true;
        }
        p.sendMessage("§6 ▪ §7Waiting spawn set for §e"+ss.getWorldName()+"§7!");
        ss.getConfig().saveArenaLoc("waiting.Loc", p.getLocation());
        if (ss.getSetupType() == SetupType.ASSISTED){
            Bukkit.dispatchCommand(s, BedWars.mainCmd+" autocreateteams");
        } else {
            Bukkit.dispatchCommand(s, BedWars.mainCmd);
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, com.tomkeuper.bedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (!SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasPermission(s);
    }
}
