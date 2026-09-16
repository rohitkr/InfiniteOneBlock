
        package com.infiniteoneblock.island;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class Island {

    private final UUID ownerId;
    private final ServerLevel world;
    private final BlockPos center;
    private final BlockPos oneBlockPosition;
    private final BlockPos spawnPosition;

    private int blocksMined;
    private int oakLogsCollected;

    public Island(
            UUID ownerId,
            ServerLevel world,
            BlockPos center,
            BlockPos oneBlockPosition,
            BlockPos spawnPosition
    ) {
        this.ownerId = ownerId;
        this.world = world;
        this.center = center;
        this.oneBlockPosition = oneBlockPosition;
        this.spawnPosition = spawnPosition;

        this.blocksMined = 0;
        this.oakLogsCollected = 0;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public ServerLevel getWorld() {
        return world;
    }

    public BlockPos getCenter() {
        return center;
    }

    public BlockPos getOneBlockPosition() {
        return oneBlockPosition;
    }

    public BlockPos getSpawnPosition() {
        return spawnPosition;
    }

    public int getBlocksMined() {
        return blocksMined;
    }

    public int incrementBlocksMined() {
        blocksMined++;
        return blocksMined;
    }

    public int getOakLogsCollected() {
        return oakLogsCollected;
    }

    public int incrementOakLogsCollected() {
        oakLogsCollected++;
        return oakLogsCollected;
    }

    public boolean hasUnlockedStage2() {
        return oakLogsCollected >= 6;
    }
}
