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
            int stage
    ) {

        Phase phase =
                phaseManager.getPhase(
                        stage
                );

        return phase
                .getBlockPool()
                .getRandomBlock();
    }

    public Phase getCurrentPhase(
            int stage
    ) {

        return phaseManager.getPhase(
                stage
        );
    }
}
