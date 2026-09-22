
        package com.infiniteoneblock.island;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class Island {

    private final UUID ownerId;
    private final ServerLevel world;
    private final BlockPos center;
    private final BlockPos oneBlockPosition;
    private final BlockPos spawnPosition;

    private int blocksMined;
    private int oakLogsCollected;


    private boolean spawnedGuardian = false;
    private boolean spawnedWitherSkeleton = false;
    private boolean spawnedWarden = false;

    public boolean hasSpawnedGuardian() { return this.spawnedGuardian; }
    public void setSpawnedGuardian(boolean val) { this.spawnedGuardian = val; }

    public boolean hasSpawnedWitherSkeleton() { return this.spawnedWitherSkeleton; }
    public void setSpawnedWitherSkeleton(boolean val) { this.spawnedWitherSkeleton = val; }

    public boolean hasSpawnedWarden() { return this.spawnedWarden; }
    public void setSpawnedWarden(boolean val) { this.spawnedWarden = val; }


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

    public BlockPos getSupportPosition() {
        return oneBlockPosition.below();
    }

    public void updateGravitySupport(BlockState oneBlockState) {
        BlockPos supportPos = getSupportPosition();
        BlockState below = world.getBlockState(supportPos);

        if (oneBlockState.getBlock() instanceof FallingBlock) {
            if (below.isAir()) {
                world.setBlockAndUpdate(supportPos, Blocks.BARRIER.defaultBlockState());
            }
            return;
        }

        if (below.is(Blocks.BARRIER)) {
            world.setBlockAndUpdate(supportPos, Blocks.AIR.defaultBlockState());
        }
    }

    public BlockPos getSpawnPosition() {
        return spawnPosition;
    }

    public int getBlocksMined() {
        return blocksMined;
    }

    public void setBlocksMined(int blocksMined) {
        this.blocksMined = Math.max(0, blocksMined);
    }

    public void setOakLogsCollected(int oakLogsCollected) {
        this.oakLogsCollected = Math.max(0, oakLogsCollected);
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
