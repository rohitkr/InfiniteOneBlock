package com.infiniteoneblock.island;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandManager {

    private static final int ISLAND_Y = 128;

    private final Map<UUID, Island> islands = new HashMap<>();

    public Island createIsland(UUID playerId, ServerLevel world) {

        if (islands.containsKey(playerId)) {
            return islands.get(playerId);
        }

        BlockPos center = new BlockPos(0, ISLAND_Y, 0);

        BlockPos oneBlockPosition = center;

        BlockPos spawnPosition = center.above();

        Island island = new Island(
                playerId,
                world,
                center,
                oneBlockPosition,
                spawnPosition
        );

        islands.put(playerId, island);

        return island;
    }

    public Island getIsland(UUID playerId) {
        return islands.get(playerId);
    }

    public boolean hasIsland(UUID playerId) {
        return islands.containsKey(playerId);
    }

    public void removeIsland(UUID playerId) {
        islands.remove(playerId);
    }
}
