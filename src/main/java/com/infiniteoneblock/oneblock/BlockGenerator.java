package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.phase.PhaseManager;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BlockGenerator {

    private static final double PREVIOUS_PHASE_SCALE = 0.3;

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
        BlockPool combined = new BlockPool();
        addEssentials(combined);

        int maxStage = Math.min(Math.max(stage, 1), phaseManager.getPhaseCount());
        for (int i = 1; i <= maxStage; i++) {
            double scale = (i == maxStage) ? 1.0 : PREVIOUS_PHASE_SCALE;
            combined.addAll(phaseManager.getPhase(i).getBlockPool(), scale);
        }

        return combined.getRandomBlock();
    }

    public Phase getCurrentPhase(
            int stage
    ) {

        return phaseManager.getPhase(
                stage
        );
    }

    private void addEssentials(BlockPool pool) {
        pool.add(Blocks.DIRT, 24);
        pool.add(Blocks.GRASS_BLOCK, 12);
        pool.add(Blocks.OAK_LOG, 16);
        pool.add(Blocks.COBBLESTONE, 18);
        pool.add(Blocks.STONE, 14);
        pool.add(Blocks.COAL_ORE, 8);
        pool.add(Blocks.IRON_ORE, 6);
    }
}
