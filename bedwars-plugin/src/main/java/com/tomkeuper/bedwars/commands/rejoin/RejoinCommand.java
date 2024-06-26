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

package com.tomkeuper.bedwars.commands.rejoin;

import com.tomkeuper.bedwars.api.language.Language;
import com.tomkeuper.bedwars.api.language.Messages;
import com.tomkeuper.bedwars.arena.ReJoin;
import com.tomkeuper.bedwars.configuration.Permissions;
import com.tomkeuper.bedwars.configuration.Sounds;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class RejoinCommand extends BukkitCommand {

    public RejoinCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender s, String st, String[] args) {
        if (s instanceof ConsoleCommandSender) {
            s.sendMessage("This command is for players!");
            return true;
        }

        Player p = (Player) s;

        if (!p.hasPermission(Permissions.PERMISSION_REJOIN)) {
            p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
            return true;
        }

        ReJoin rj = ReJoin.getPlayer(p);

        if (rj == null) {
            p.sendMessage(Language.getMsg(p, Messages.REJOIN_NO_ARENA));
            Sounds.playSound("rejoin-denied", p);
            return true;
        }

        if (!rj.canReJoin()) {
            p.sendMessage(Language.getMsg(p, Messages.REJOIN_DENIED));
            Sounds.playSound("rejoin-denied", p);
            return true;
        }

        p.sendMessage(Language.getMsg(p, Messages.REJOIN_ALLOWED).replace("%bw_arena%", rj.getArena().getDisplayName()));
        Sounds.playSound("rejoin-allowed", p);
        rj.reJoin(p);
        return true;
    }
}
