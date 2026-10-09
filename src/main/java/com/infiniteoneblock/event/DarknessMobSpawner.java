package com.infiniteoneblock.event;

import com.infiniteoneblock.InfiniteOneBlock;
import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;
import com.infiniteoneblock.oneblock.OneBlockManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

/**
 * Natural hostile spawning for the OneBlock dimension.
 *
 * The dimension uses the empty "the_void" biome, whose spawn list is empty, so vanilla
 * never spawns monsters there. This spawner fills that gap with the same rules vanilla
 * uses: a monster may only appear on a valid surface where the light is low enough
 * (night time on open ground, or dark spots under roofs), not in peaceful, and only
 * while the local monster count is below a cap. Mobs are spawned as NATURAL, so they
 * despawn like vanilla mobs when the player walks away.
 *
 * The mob type follows the island's current stage.
 */
public class DarknessMobSpawner {

    private static final int SPAWN_INTERVAL_TICKS = 40;
    private static final int ATTEMPTS_PER_PLAYER = 8;
    private static final int MIN_DISTANCE = 12;
    private static final int MAX_DISTANCE = 40;
    private static final int VERTICAL_RANGE = 16;
    private static final int LOCAL_MONSTER_CAP = 12;
    private static final int CAP_RADIUS = 64;

    private final IslandManager islandManager;
    private final OneBlockManager oneBlockManager;

    public DarknessMobSpawner(IslandManager islandManager, OneBlockManager oneBlockManager) {
        this.islandManager = islandManager;
        this.oneBlockManager = oneBlockManager;
    }

    public void register() {
        ServerTickEvents.END_LEVEL_TICK.register(this::onWorldTick);
    }

    private void onWorldTick(ServerLevel level) {
        if (!level.dimension().equals(InfiniteOneBlock.ONEBLOCK_WORLD_KEY)) {
            return;
        }
        if (level.getGameTime() % SPAWN_INTERVAL_TICKS != 0) {
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }

            Island island = islandManager.getIsland(player.getUUID());
            if (island == null) {
                continue;
            }

            if (countNearbyMonsters(level, player) >= LOCAL_MONSTER_CAP) {
                continue;
            }

            int stage = oneBlockManager.getCurrentStage(island);
            for (int attempt = 0; attempt < ATTEMPTS_PER_PLAYER; attempt++) {
                if (trySpawn(level, player, stage)) {
                    break;
                }
            }
        }
    }

    private int countNearbyMonsters(ServerLevel level, ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(CAP_RADIUS);
        return level.getEntitiesOfClass(Mob.class, area, mob -> mob instanceof Enemy).size();
    }

    private boolean trySpawn(ServerLevel level, ServerPlayer player, int stage) {
        RandomSource random = level.getRandom();

        double angle = random.nextDouble() * Math.PI * 2;
        int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE + 1);
        int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
        int startY = player.getBlockY() + random.nextInt(VERTICAL_RANGE * 2 + 1) - VERTICAL_RANGE;

        EntityType<?> type = oneBlockManager.getHostileMobForStage(stage);
        if (type == null || type == EntityTypes.GHAST) {
            // Ghast fireballs would wreck the island; keep them to the OneBlock spawns only
            return false;
        }

        // Walk down from a random height to the first surface below
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);
        for (int i = 0; i < VERTICAL_RANGE; i++, pos.move(0, -1, 0)) {
            if (pos.getY() <= level.getMinY()) {
                return false;
            }
            if (!level.getBlockState(pos).isAir() || level.getBlockState(pos.below()).isAir()) {
                continue;
            }

            BlockPos spawnPos = pos.immutable();
            if (!SpawnPlacements.isSpawnPositionOk(type, level, spawnPos)) {
                return false;
            }
            if (!Monster.isDarkEnoughToSpawn(level, spawnPos, random)) {
                return false;
            }
            if (!level.noCollision(type.getSpawnAABB(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5))) {
                return false;
            }

            return type.spawn(level, spawnPos, EntitySpawnReason.NATURAL) != null;
        }

        return false;
    }
}
