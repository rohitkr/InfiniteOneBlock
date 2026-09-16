        package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.phase.PhaseManager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class OneBlockManager {

    private final PhaseManager phaseManager;
    private final BlockGenerator blockGenerator;

    public OneBlockManager() {

        phaseManager =
                new PhaseManager();

        blockGenerator =
                new BlockGenerator(
                        phaseManager
                );
    }

    public void initialize() {

        phaseManager.initialize();
    }

    public Block getNextBlock(
            Island island
    ) {

        int stage =
                getCurrentStage(island);

        return blockGenerator.generateNextBlock(
                stage
        );
    }

    public void regenerate(
            Island island
    ) {

        ServerLevel world =
                island.getWorld();

        BlockPos position =
                island.getOneBlockPosition();

        Block nextBlock =
                getNextBlock(island);

        /*
         * Final safety check.
         *
         * No phase should ever contain a fluid block,
         * but we keep this protection here so a future
         * phase cannot accidentally introduce water/lava.
         */
        BlockState nextState =
                nextBlock.defaultBlockState();

        if (nextState.getFluidState().isEmpty()
                && nextState.canSurvive(
                world,
                position
        )) {

            world.setBlock(
                    position,
                    nextState,
                    3
            );

            return;
        }

        /*
         * If a future phase accidentally contains an
         * invalid block, use a guaranteed-safe fallback.
         */
        System.out.println(
                "[InfiniteOneBlock] Invalid OneBlock candidate: "
                        + nextBlock
        );

        world.setBlock(
                position,
                Blocks.STONE.defaultBlockState(),
                3
        );
    }

    public Phase getCurrentPhase(
            Island island
    ) {

        return blockGenerator.getCurrentPhase(
                getCurrentStage(island)
        );
    }

    public int getCurrentStage(
            Island island
    ) {

        /*
         * Phase 1:
         *
         * Until the player has collected
         * six Oak Logs.
         */
        if (!island.hasUnlockedStage2()) {
            return 1;
        }

        /*
         * Phase 2:
         *
         * Six or more Oak Logs.
         *
         * Phase 3-7 unlock rules will be added
         * separately instead of inventing arbitrary
         * progression requirements here.
         */
        return 2;
    }
}
