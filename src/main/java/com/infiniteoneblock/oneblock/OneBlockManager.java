package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.phase.PhaseManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class OneBlockManager {

    private final PhaseManager phaseManager;
    private final BlockGenerator blockGenerator;
    private final Random random = new Random();

    public OneBlockManager() {
        phaseManager = new PhaseManager();
        blockGenerator = new BlockGenerator(phaseManager);
    }

    public void initialize() {
        phaseManager.initialize();
    }

    public Block getNextBlock(Island island) {
        int stage = getCurrentStage(island);
        return blockGenerator.generateNextBlock(stage);
    }

    public void regenerate(Island island) {
        ServerLevel world = island.getWorld();
        BlockPos position = island.getOneBlockPosition();

        if (random.nextFloat() < 0.05f) {
            int stage = getCurrentStage(island);
            EntityType<?> selectedMobType = getMobTypeForStage(stage);

            if (selectedMobType != null) {
                BlockPos spawnPos = position.above();

                var entity = selectedMobType.spawn(world, spawnPos, EntitySpawnReason.EVENT);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                }

                world.setBlock(position, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                return;
            }
        }

        Block nextBlock = getNextBlock(island);
        BlockState nextState = nextBlock.defaultBlockState();

        if (nextState.getFluidState().isEmpty() && nextState.canSurvive(world, position)) {
            world.setBlock(position, nextState, 3);
            return;
        }

        System.out.println("[InfiniteOneBlock] Invalid OneBlock candidate: " + nextBlock);
        world.setBlock(position, Blocks.STONE.defaultBlockState(), 3);
    }

    /**
     * Unwraps the Optional Reference container natively required by Minecraft 26.2 registry maps.
     */
    private EntityType<?> getMobType(String path) {
        return BuiltInRegistries.ENTITY_TYPE.get(Identifier.fromNamespaceAndPath("minecraft", path))
                .map(Holder::value)
                .orElse(null);
    }

    private EntityType<?> getMobTypeForStage(int stage) {
        int roll = random.nextInt(100);
        // ============================================================
        // STAGE 1: Plains (100% Peaceful/Food Animals)
        // ============================================================
        if (stage == 1) {
            if (roll < 40) return getMobType("chicken");
            if (roll < 70) return getMobType("pig");
            return getMobType("sheep");
        }
        // ============================================================
        // STAGE 6: Nether (70% Nether Mobs, 30% Critical Food Animals)
        // ============================================================
        else if (stage == 6) {
            if (roll < 40) return getMobType("piglin");
            if (roll < 70) return getMobType("zombified_piglin");
            // Remaining 30% keeps food supplies spawning even in Hell
            if (roll < 85) return getMobType("cow");
            return getMobType("pig");
        }
        // ============================================================
        // ALL OTHER STAGES (Underground, Winter, Ocean, Jungle, End)
        // Dynamic Blend: 70% Monsters for challenge, 30% Food Animals for survival
        // ============================================================
        else {
            // 70% Chance for Hostile Monsters
            if (roll < 30) return getMobType("zombie");
            if (roll < 55) return getMobType("skeleton");
            if (roll < 70) return getMobType("creeper");
            // 30% Chance for Food & Utility Animals (Always available so players don't starve)
            if (roll < 80) return getMobType("cow");     // Leather & Beef
            if (roll < 90) return getMobType("pig");     // Porkchops
            return getMobType("sheep");                  // Wool & Mutton
        }
    }

    public Phase getCurrentPhase(Island island) {
        return blockGenerator.getCurrentPhase(getCurrentStage(island));
    }

    public int getCurrentStage(Island island) {
        int blocksMined = island.getBlocksMined();
        int stage = 7;

        if (blocksMined < 50) stage = 1;
        else if (blocksMined < 100) stage = 2;
        else if (blocksMined < 300) stage = 3;
        else if (blocksMined < 600) stage = 4;
        else if (blocksMined < 1000) stage = 5;
        else if (blocksMined < 1500) stage = 6;

        return stage;
    }
}
