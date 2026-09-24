        package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.phase.PhaseManager;

import net.minecraft.world.level.block.Block;

public class BlockGenerator {

    private final PhaseManager phaseManager;

    public BlockGenerator(
            PhaseManager phaseManager
    ) {
        this.phaseManager =
                phaseManager;
    }

    public Block generateNextBlock(
            int stage,
            com.infiniteoneblock.island.Island island
    ) {

        Phase phase =
                phaseManager.getPhase(
                        stage
                );

        return phase
                .getBlockPool()
                .getRandomBlock(block -> {
                    if (block == net.minecraft.world.level.block.Blocks.IRON_ORE) {
                        return island.getCobblestoneCollected() >= 15;
                    }
                    return true;
                });
    }

    public Phase getCurrentPhase(
            int stage
    ) {

        return phaseManager.getPhase(
                stage
        );
    }
}
