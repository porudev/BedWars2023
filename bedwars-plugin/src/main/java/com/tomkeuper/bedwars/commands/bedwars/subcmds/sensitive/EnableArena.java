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

package com.tomkeuper.bedwars.commands.bedwars.subcmds.sensitive;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.command.ParentCommand;
import com.tomkeuper.bedwars.api.command.SubCommand;
import com.tomkeuper.bedwars.arena.Arena;
import com.tomkeuper.bedwars.arena.Misc;
import com.tomkeuper.bedwars.arena.SetupSession;
import com.tomkeuper.bedwars.commands.bedwars.MainCommand;
import com.tomkeuper.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnableArena extends SubCommand {

    public EnableArena(ParentCommand parent, String name) {
        super(parent, name);
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + getParent().getName() + " "+getSubCommandName()+" §6<worldName>","§fEnable an arena.",
                "/" + getParent().getName() + " "+getSubCommandName()+ " ", ClickEvent.Action.SUGGEST_COMMAND));
        showInList(true);
        setPriority(5);
        setPermission(Permissions.PERMISSION_ARENA_ENABLE);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!MainCommand.isLobbySet()) {
            s.sendMessage("§c▪ §7You have to set the lobby location first!");
            return true;
        }
        if (args.length != 1) {
            s.sendMessage("§c▪ §7Usage: §o/" + getParent().getName() + " enableRotation <mapName>");
            return true;
        }
        if (!BedWars.getAPI().getRestoreAdapter().isWorld(args[0])) {
            s.sendMessage("§c▪ §7" + args[0] + " doesn't exist!");
            return true;
        }

        for (IArena mm : Arena.getEnableQueue()){
            if (mm.getArenaName().equalsIgnoreCase(args[0])){
                s.sendMessage("§c▪ §7This arena is already in the enable queue!");
                return true;
            }
        }

        IArena aa = Arena.getArenaByName(args[0]);
        if (aa != null) {
            s.sendMessage("§c▪ §7This arena is already enabled!");
            return true;
        }
        s.sendMessage("§6 ▪ §7Enabling arena...");
        new Arena(args[0], s);
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        List<String> tab = new ArrayList<>();
        File dir = new File(BedWars.plugin.getDataFolder(), "/Arenas");
        if (dir.exists()) {
            File[] fls = dir.listFiles();
            for (File fl : Objects.requireNonNull(fls)) {
                if (fl.isFile()) {
                    if (fl.getName().contains(".yml")) {
                        tab.add(fl.getName().replace(".yml", ""));
                    }
                }
            }
        }
        return tab;
    }

    @Override
    public boolean canSee(CommandSender s, com.tomkeuper.bedwars.api.BedWars api) {
        if (s instanceof Player) {
            Player p = (Player) s;
            if (Arena.isInArena(p)) return false;
            if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        }
        return hasPermission(s);
    }
}
