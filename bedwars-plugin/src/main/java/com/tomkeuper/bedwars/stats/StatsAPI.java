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

package com.tomkeuper.bedwars.stats;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.api.stats.IPlayerStats;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@SuppressWarnings("unused")
public class StatsAPI implements BedWars.IStats {

    private static StatsAPI instance;

    public static StatsAPI getInstance() {
        if (instance == null) {
            instance = new StatsAPI();
        }
        return instance;
    }

    private StatsAPI() {
    }

    private IPlayerStats getData(UUID uuid) {
        IPlayerStats stats = com.tomkeuper.bedwars.BedWars.getStatsManager().getUnsafe(uuid);
        if (stats == null) {
            stats = com.tomkeuper.bedwars.BedWars.getRemoteDatabase().fetchStats(uuid);
        }
        return stats;
    }

    @Override
    public Timestamp getPlayerFirstPlay(UUID p) {
        Instant firstPlay = getData(p).getFirstPlay();
        if (firstPlay == null) {
            return null;
        }
        return Timestamp.from(firstPlay);
    }

    @Override
    public Timestamp getPlayerLastPlay(UUID p) {
        Instant lastPlay = getData(p).getLastPlay();
        if (lastPlay == null) {
            return null;
        }
        return Timestamp.from(lastPlay);
    }

    @Override
    public int getPlayerWins(UUID p) {
        return getData(p).getWins();
    }

    @Override
    public int getPlayerKills(UUID p) {
        return getData(p).getKills();
    }

    @Override
    public int getPlayerTotalKills(UUID p) {
        return getData(p).getTotalKills();
    }

    @Override
    public int getPlayerFinalKills(UUID p) {
        return getData(p).getFinalKills();
    }

    @Override
    public int getPlayerLoses(UUID p) {
        return getData(p).getLosses();
    }

    @Override
    public int getPlayerDeaths(UUID p) {
        return getData(p).getDeaths();
    }

    @Override
    public int getPlayerFinalDeaths(UUID p) {
        return getData(p).getFinalDeaths();
    }

    @Override
    public int getPlayerBedsDestroyed(UUID p) {
        return getData(p).getBedsDestroyed();
    }

    @Override
    public int getPlayerGamesPlayed(UUID p) {
        return getData(p).getGamesPlayed();
    }
}
