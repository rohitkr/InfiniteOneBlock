        package com.infiniteoneblock.phase;

import com.infiniteoneblock.oneblock.BlockPool;

import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class PhaseManager {

    private final List<Phase> phases =
            new ArrayList<>();

    private boolean initialized = false;

    public PhaseManager() {
    }

    public void initialize() {

        if (initialized) {
            return;
        }

        System.out.println(
                "[InfiniteOneBlock] Initializing OneBlock phases..."
        );

        createPhases();

        initialized = true;

        System.out.println(
                "[InfiniteOneBlock] OneBlock phases initialized."
        );
    }

    private void createPhases() {

        createPhase1Plains();
        createPhase2Underground();
        createPhase3Winter();
        createPhase4Ocean();
        createPhase5JungleSwamp();
        createPhase6Nether();
        createPhase7StrongholdEnd();
    }

    /*
     * ============================================================
     * PHASE 1
     * PLAINS
     * ============================================================
     */
    private void createPhase1Plains() {

        BlockPool pool =
                new BlockPool();

        pool.add(
                Blocks.GRASS_BLOCK,
                40
        );

        pool.add(
                Blocks.DIRT,
                35
        );

        pool.add(
                Blocks.COARSE_DIRT,
                10
        );

        pool.add(
                Blocks.ROOTED_DIRT,
                5
        );

        pool.add(
                Blocks.OAK_LOG,
                20
        );

        pool.add(
                Blocks.OAK_PLANKS,
                10
        );

        pool.add(
                Blocks.SAND,
                5
        );

        pool.add(
                Blocks.GRAVEL,
                5
        );

        phases.add(
                new Phase(
                        1,
                        "Plains",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 2
     * UNDERGROUND
     * ============================================================
     */
    private void createPhase2Underground() {

        BlockPool pool =
                new BlockPool();

        /*
         * Basic underground blocks.
         */
        pool.add(
                Blocks.STONE,
                45
        );

        pool.add(
                Blocks.COBBLESTONE,
                30
        );

        pool.add(
                Blocks.GRANITE,
                10
        );

        pool.add(
                Blocks.DIORITE,
                10
        );

        pool.add(
                Blocks.ANDESITE,
                10
        );

        pool.add(
                Blocks.DEEPSLATE,
                8
        );

        pool.add(
                Blocks.GRAVEL,
                10
        );

        /*
         * Coal is common.
         */
        pool.add(
                Blocks.COAL_ORE,
                8
        );

        /*
         * Iron is less common.
         */
        pool.add(
                Blocks.IRON_ORE,
                5
        );

        pool.add(
                Blocks.COPPER_ORE,
                4
        );

        pool.add(
                Blocks.GOLD_ORE,
                2
        );

        /*
         * Keep some Phase 1 materials alive after
         * progression.
         */
        pool.add(
                Blocks.DIRT,
                10
        );

        pool.add(
                Blocks.OAK_LOG,
                5
        );

        phases.add(
                new Phase(
                        2,
                        "Underground",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 3
     * WINTER / SNOW
     * ============================================================
     */
    private void createPhase3Winter() {

        BlockPool pool =
                new BlockPool();

        pool.add(
                Blocks.SNOW_BLOCK,
                30
        );

        pool.add(
                Blocks.ICE,
                25
        );

        pool.add(
                Blocks.PACKED_ICE,
                10
        );

        pool.add(
                Blocks.BLUE_ICE,
                5
        );

        pool.add(
                Blocks.SPRUCE_LOG,
                20
        );

        pool.add(
                Blocks.SPRUCE_PLANKS,
                10
        );

        pool.add(
                Blocks.STONE,
                15
        );

        pool.add(
                Blocks.COBBLESTONE,
                10
        );

        pool.add(
                Blocks.COAL_ORE,
                5
        );

        pool.add(
                Blocks.IRON_ORE,
                3
        );

        pool.add(
                Blocks.LAPIS_ORE,
                3
        );

        phases.add(
                new Phase(
                        3,
                        "Winter",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 4
     * OCEAN
     * ============================================================
     */
    private void createPhase4Ocean() {

        BlockPool pool =
                new BlockPool();

        pool.add(
                Blocks.SAND,
                30
        );

        pool.add(
                Blocks.CLAY,
                20
        );

        pool.add(
                Blocks.PRISMARINE,
                20
        );

        pool.add(
                Blocks.PRISMARINE_BRICKS,
                10
        );

        pool.add(
                Blocks.DARK_PRISMARINE,
                8
        );

        pool.add(
                Blocks.SEA_LANTERN,
                5
        );

        pool.add(
                Blocks.GRAVEL,
                10
        );

        pool.add(
                Blocks.WET_SPONGE,
                5
        );

        /*
         * Keep some previous materials available.
         */
        pool.add(
                Blocks.STONE,
                8
        );

        pool.add(
                Blocks.IRON_ORE,
                3
        );

        pool.add(
                Blocks.GOLD_ORE,
                4
        );

        phases.add(
                new Phase(
                        4,
                        "Ocean",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 5
     * JUNGLE / SWAMP
     * ============================================================
     */
    private void createPhase5JungleSwamp() {

        BlockPool pool =
                new BlockPool();

        pool.add(
                Blocks.JUNGLE_LOG,
                20
        );

        pool.add(
                Blocks.JUNGLE_PLANKS,
                10
        );

        pool.add(
                Blocks.MANGROVE_LOG,
                15
        );

        pool.add(
                Blocks.MANGROVE_PLANKS,
                8
        );

        pool.add(
                Blocks.MUD,
                20
        );

        pool.add(
                Blocks.MOSS_BLOCK,
                15
        );

        pool.add(
                Blocks.MELON,
                8
        );

        pool.add(
                Blocks.PUMPKIN,
                8
        );

        pool.add(
                Blocks.CLAY,
                10
        );

        pool.add(
                Blocks.DIRT,
                10
        );

        pool.add(
                Blocks.GRASS_BLOCK,
                8
        );

        pool.add(
                Blocks.REDSTONE_ORE,
                6
        );

        pool.add(
                Blocks.GOLD_ORE,
                4
        );

        pool.add(
                Blocks.DIAMOND_ORE,
                2
        );

        phases.add(
                new Phase(
                        5,
                        "Jungle / Swamp",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 6
     * NETHER
     * ============================================================
     */
    private void createPhase6Nether() {

        BlockPool pool =
                new BlockPool();

        pool.add(
                Blocks.NETHERRACK,
                40
        );

        pool.add(
                Blocks.SOUL_SAND,
                15
        );

        pool.add(
                Blocks.SOUL_SOIL,
                10
        );

        pool.add(
                Blocks.MAGMA_BLOCK,
                10
        );

        pool.add(
                Blocks.BLACKSTONE,
                10
        );

        pool.add(
                Blocks.BASALT,
                10
        );

        pool.add(
                Blocks.NETHER_BRICKS,
                8
        );

        pool.add(
                Blocks.CRIMSON_STEM,
                8
        );

        pool.add(
                Blocks.WARPED_STEM,
                8
        );

        pool.add(
                Blocks.NETHER_QUARTZ_ORE,
                5
        );

        pool.add(
                Blocks.NETHER_GOLD_ORE,
                4
        );

        pool.add(
                Blocks.ANCIENT_DEBRIS,
                1
        );

        pool.add(
                Blocks.GLOWSTONE,
                8
        );

        pool.add(
                Blocks.OBSIDIAN,
                6
        );

        phases.add(
                new Phase(
                        6,
                        "Nether",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 7
     * STRONGHOLD / END
     * ============================================================
     */
    private void createPhase7StrongholdEnd() {

        BlockPool pool =
                new BlockPool();

        pool.add(
                Blocks.STONE_BRICKS,
                20
        );

        pool.add(
                Blocks.MOSSY_STONE_BRICKS,
                8
        );

        pool.add(
                Blocks.CRACKED_STONE_BRICKS,
                8
        );

        pool.add(
                Blocks.OBSIDIAN,
                10
        );

        pool.add(
                Blocks.END_STONE,
                25
        );

        pool.add(
                Blocks.END_STONE_BRICKS,
                10
        );

        pool.add(
                Blocks.PURPUR_BLOCK,
                15
        );

        pool.add(
                Blocks.PURPUR_PILLAR,
                10
        );

        pool.add(
                Blocks.CRYING_OBSIDIAN,
                5
        );

        pool.add(
                Blocks.DIAMOND_ORE,
                6
        );

        pool.add(
                Blocks.END_PORTAL_FRAME,
                5
        );

        pool.add(
                Blocks.END_ROD,
                4
        );

        pool.add(
                Blocks.RESPAWN_ANCHOR,
                2
        );

        phases.add(
                new Phase(
                        7,
                        "Stronghold / End",
                        pool
                )
        );
    }

    public Phase getPhase(
            int stage
    ) {

        if (!initialized) {

            throw new IllegalStateException(
                    "PhaseManager has not been initialized yet."
            );
        }

        if (stage <= 1) {
            return phases.get(0);
        }

        if (stage >= phases.size()) {
            return phases.get(
                    phases.size() - 1
            );
        }

        return phases.get(
                stage - 1
        );
    }

    public int getPhaseCount() {
        return phases.size();
    }
}
