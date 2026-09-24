        package com.infiniteoneblock.phase;

import com.infiniteoneblock.oneblock.BlockPool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhaseManager {

    private final List<Phase> phases = new ArrayList<>();

    private boolean initialized = false;

    /*
     * ============================================================
     * PRIORITY / WEIGHT
     *
     * Higher number = more likely to spawn.
     *
     * 1  = extremely rare
     * 2  = very rare
     * 3  = rare
     * 5  = uncommon
     * 8  = normal
     * 10 = common
     * 20 = very common
     * ============================================================
     */
    private record BlockWeight(Block block, int weight) {
    }

    private static BlockWeight b(Block block, int weight) {
        return new BlockWeight(block, weight);
    }

    /*
     * ============================================================
     * POOL FACTORY
     *
     * Creates a phase pool and automatically adds resources that
     * should remain available from earlier stages.
     *
     * A later stage can override the weight of an inherited block
     * simply by adding it again in its own list.
     * ============================================================
     */
    private BlockPool createPool(
            int stage,
            BlockWeight... stageBlocks
    ) {

        Map<Block, Integer> weights =
                new LinkedHashMap<>();

        /*
         * --------------------------------------------------------
         * STAGE 1 RESOURCES
         *
         * These are safe/basic resources that remain useful later.
         *
         * IMPORTANT:
         * They are NOT added to Stage 1 through this method.
         * Stage 1 has its own pool.
         * --------------------------------------------------------
         */
        if (stage >= 2) {

            add(weights, Blocks.DIRT, 10);
            add(weights, Blocks.OAK_LOG, 6);
            add(weights, Blocks.OAK_PLANKS, 5);
        }

        /*
         * --------------------------------------------------------
         * STAGE 2 MINING RESOURCES
         *
         * These become available from Stage 2 onward.
         *
         * They are deliberately NOT available in Stage 1 because
         * the player is still hand-mining there.
         * --------------------------------------------------------
         */
        if (stage >= 2) {

            add(weights, Blocks.STONE, 12);
            add(weights, Blocks.COBBLESTONE, 10);
            add(weights, Blocks.COAL_ORE, 8);

            /*
             * Iron is a progression-critical resource.
             */
            add(weights, Blocks.IRON_ORE, 10);
        }

        /*
         * --------------------------------------------------------
         * STAGE 3
         * --------------------------------------------------------
         */
        if (stage >= 3) {

            add(weights, Blocks.LAPIS_ORE, 3);
        }

        /*
         * --------------------------------------------------------
         * STAGE 5
         *
         * Diamond becomes available here and remains available
         * forever after this stage.
         * --------------------------------------------------------
         */
        if (stage >= 5) {

            add(weights, Blocks.REDSTONE_ORE, 5);
            add(weights, Blocks.GOLD_ORE, 4);

            /*
             * Increased from 2 -> 5.
             *
             * Still rare compared with Iron, but actually
             * obtainable during normal progression.
             */
            add(weights, Blocks.DIAMOND_ORE, 5);
        }

        /*
         * --------------------------------------------------------
         * STAGE 6
         * --------------------------------------------------------
         */
        if (stage >= 6) {

            add(weights, Blocks.NETHER_QUARTZ_ORE, 5);
            add(weights, Blocks.NETHER_GOLD_ORE, 4);
            add(weights, Blocks.ANCIENT_DEBRIS, 7);
        }

        /*
         * --------------------------------------------------------
         * ADD CURRENT STAGE BLOCKS
         *
         * If a block already exists above, this overrides its
         * inherited weight.
         * --------------------------------------------------------
         */
        for (BlockWeight entry : stageBlocks) {
            weights.put(
                    entry.block(),
                    entry.weight()
            );
        }

        /*
         * --------------------------------------------------------
         * BUILD ACTUAL BLOCK POOL
         * --------------------------------------------------------
         */
        BlockPool pool = new BlockPool();

        for (Map.Entry<Block, Integer> entry : weights.entrySet()) {

            pool.add(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        return pool;
    }

    private void add(
            Map<Block, Integer> weights,
            Block block,
            int weight
    ) {
        weights.put(
                block,
                weight
        );
    }

    /*
     * ============================================================
     * INITIALIZATION
     * ============================================================
     */
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

        createPhase1();
        createPhase2();
        createPhase3();
        createPhase4();
        createPhase5();
        createPhase6();
        createPhase7();
    }

    /*
     * ============================================================
     * PHASE 1
     *
     * HAND-MINING
     *
     * No stone, cobblestone, coal or iron.
     * ============================================================
     */
    private void createPhase1() {

        BlockPool pool = new BlockPool();

        pool.add(Blocks.GRASS_BLOCK, 40);
        pool.add(Blocks.DIRT, 35);
        pool.add(Blocks.COARSE_DIRT, 10);
        pool.add(Blocks.ROOTED_DIRT, 5);

        pool.add(Blocks.OAK_LOG, 20);
        pool.add(Blocks.OAK_PLANKS, 10);

        pool.add(Blocks.SAND, 5);
        pool.add(Blocks.GRAVEL, 5);
        pool.add(Blocks.ANCIENT_DEBRIS, 5);

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
    private void createPhase2() {

        BlockPool pool = createPool(
                2,

                b(Blocks.STONE, 45),
                b(Blocks.COBBLESTONE, 30),
                b(Blocks.GRANITE, 10),
                b(Blocks.DIORITE, 10),
                b(Blocks.ANDESITE, 10),
                b(Blocks.DEEPSLATE, 8),
                b(Blocks.GRAVEL, 10),

                b(Blocks.COAL_ORE, 8),

                /*
                 * More iron than the original 5.
                 */
                b(Blocks.IRON_ORE, 10),

                b(Blocks.COPPER_ORE, 4),
                b(Blocks.GOLD_ORE, 2)
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
     * WINTER
     * ============================================================
     */
    private void createPhase3() {

        BlockPool pool = createPool(
                3,

                b(Blocks.SNOW_BLOCK, 30),
                b(Blocks.ICE, 25),
                b(Blocks.PACKED_ICE, 10),
                b(Blocks.BLUE_ICE, 5),

                b(Blocks.SPRUCE_LOG, 20),
                b(Blocks.SPRUCE_PLANKS, 10),

                b(Blocks.STONE, 15),
                b(Blocks.COBBLESTONE, 10),
                b(Blocks.COAL_ORE, 5),

                /*
                 * Increase iron from original 3.
                 */
                b(Blocks.IRON_ORE, 8),

                b(Blocks.LAPIS_ORE, 3)
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
    private void createPhase4() {

        BlockPool pool = createPool(
                4,

                b(Blocks.SAND, 30),
                b(Blocks.CLAY, 20),

                b(Blocks.PRISMARINE, 20),
                b(Blocks.PRISMARINE_BRICKS, 10),
                b(Blocks.DARK_PRISMARINE, 8),
                b(Blocks.SEA_LANTERN, 5),

                b(Blocks.GRAVEL, 10),
                b(Blocks.WET_SPONGE, 5),

                b(Blocks.STONE, 8),

                /*
                 * Increase iron from original 3.
                 */
                b(Blocks.IRON_ORE, 8),

                b(Blocks.GOLD_ORE, 4)
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
     *
     * Diamond is now available.
     * ============================================================
     */
    private void createPhase5() {

        BlockPool pool = createPool(
                5,

                b(Blocks.JUNGLE_LOG, 20),
                b(Blocks.JUNGLE_PLANKS, 10),

                b(Blocks.MANGROVE_LOG, 15),
                b(Blocks.MANGROVE_PLANKS, 8),

                b(Blocks.MUD, 20),
                b(Blocks.MOSS_BLOCK, 15),

                b(Blocks.MELON, 8),
                b(Blocks.PUMPKIN, 8),
                b(Blocks.CLAY, 10),

                b(Blocks.REDSTONE_ORE, 6),
                b(Blocks.GOLD_ORE, 4),

                /*
                 * Diamond increased from 2 -> 5.
                 *
                 * Iron remains inherited at 10.
                 */
                b(Blocks.DIAMOND_ORE, 10)
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
    private void createPhase6() {

        BlockPool pool = createPool(
                6,

                b(Blocks.NETHERRACK, 40),
                b(Blocks.SOUL_SAND, 15),
                b(Blocks.SOUL_SOIL, 10),
                b(Blocks.MAGMA_BLOCK, 10),

                b(Blocks.BLACKSTONE, 10),
                b(Blocks.BASALT, 10),

                b(Blocks.NETHER_BRICKS, 8),

                b(Blocks.CRIMSON_STEM, 8),
                b(Blocks.WARPED_STEM, 8),

                b(Blocks.GLOWSTONE, 8),
//                b(Blocks.OBSIDIAN, 6),

                b(Blocks.DIAMOND_ORE, 10)
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
    private void createPhase7() {

        BlockPool pool = createPool(
                7,

                b(Blocks.STONE_BRICKS, 20),
                b(Blocks.MOSSY_STONE_BRICKS, 8),
                b(Blocks.CRACKED_STONE_BRICKS, 8),

//                b(Blocks.OBSIDIAN, 10),

                b(Blocks.END_STONE, 25),
                b(Blocks.END_STONE_BRICKS, 10),

                b(Blocks.PURPUR_BLOCK, 15),
                b(Blocks.PURPUR_PILLAR, 10),

//                b(Blocks.CRYING_OBSIDIAN, 5),

//                b(Blocks.END_PORTAL_FRAME, 5),
                b(Blocks.END_ROD, 4),
                b(Blocks.RESPAWN_ANCHOR, 2)
        );

        phases.add(
                new Phase(
                        7,
                        "Stronghold / End",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * GET PHASE
     * ============================================================
     */
    public Phase getPhase(int stage) {

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
}
