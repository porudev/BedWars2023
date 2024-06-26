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

package com.tomkeuper.bedwars.arena.upgrades;

import com.tomkeuper.bedwars.BedWars;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.arena.team.ITeam;
import com.tomkeuper.bedwars.api.configuration.ConfigPath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HealPoolTask extends BukkitRunnable {

    private ITeam bwt;
    private int maxX, minX, maxY, minY, maxZ, minZ;
    private IArena arena;
    private Random r = new Random();
    private Location l;

    private static List<HealPoolTask> healPoolTasks = new ArrayList<>();

    public HealPoolTask(ITeam bwt){
        this.bwt = bwt;
        if (bwt == null || bwt.getSpawn() == null){
            removeForTeam(this.bwt);
            cancel();
            return;
        }
        int radius = bwt.getArena().getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
        Location teamspawn = bwt.getSpawn();
        this.maxX = (teamspawn.getBlockX() + radius);
        this.minX = (teamspawn.getBlockX() - radius);
        this.maxY = (teamspawn.getBlockY() + radius);
        this.minY = (teamspawn.getBlockY() - radius);
        this.maxZ = (teamspawn.getBlockZ() + radius);
        this.minZ = (teamspawn.getBlockZ() - radius);
        this.arena = bwt.getArena();
        this.runTaskTimerAsynchronously(BedWars.plugin, 0, 30L);
        healPoolTasks.add(this);
    }

    @Override
    public void run(){
        //null checks
        if ((bwt == null) || (bwt.getSpawn() == null) || (arena == null)){
            healPoolTasks.remove(this);
            return;
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    l = new Location(arena.getWorld(), x + .5, y + .5, z +.5);
                    if (l.getBlock().getType() != Material.AIR) continue;
                    int chance = r.nextInt(250);
                    if (chance == 0) {
                        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_HEAL_POOL_SEEN_TEAM_ONLY)) {
                            for (Player p : bwt.getMembers()) {
                                BedWars.nms.playVillagerEffect(p, l);
                            }
                        }
                        else
                        {
                            for (Player p : arena.getPlayers()) {
                                BedWars.nms.playVillagerEffect(p, l);
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean exists(IArena arena, ITeam bwt){
        if (healPoolTasks.isEmpty()) return false;
        for (HealPoolTask hpt : healPoolTasks) {
            if (hpt.getArena() == arena && hpt.getBwt() == bwt) return true;
        }
        return false;
    }

    public static void removeForArena(IArena a) {
        if (healPoolTasks.isEmpty() || a == null) return;

        Iterator<HealPoolTask> iterator = healPoolTasks.iterator();
        while (iterator.hasNext()) {
            HealPoolTask hpt = iterator.next();
            if (hpt == null) continue;
            if (hpt.getArena().equals(a)) {
                hpt.cancel();
                iterator.remove();
            }
        }
    }

    public static void removeForArena(String a) {
        if (healPoolTasks == null || healPoolTasks.isEmpty() || a == null) return;

        Iterator<HealPoolTask> iterator = healPoolTasks.iterator();
        while (iterator.hasNext()) {
            HealPoolTask hpt = iterator.next();
            if (hpt == null) continue;
            if (hpt.getArena().getWorldName().equals(a)) {
                hpt.cancel();
                iterator.remove();
            }
        }
    }

    public static void removeForTeam(ITeam team) {
        if (healPoolTasks == null || healPoolTasks.isEmpty() || team == null) return;

        Iterator<HealPoolTask> iterator = healPoolTasks.iterator();
        while (iterator.hasNext()) {
            HealPoolTask hpt = iterator.next();
            if (hpt == null) continue;
            if (hpt.getBwt().equals(team)) {
                hpt.cancel();
                iterator.remove();
            }
        }
    }

    public ITeam getBwt() {return bwt;}

    public IArena getArena() {return arena;}
}
