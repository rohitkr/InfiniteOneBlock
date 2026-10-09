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
         * CARRY-OVER RESOURCES
         *
         * Basic resources stay available in every later stage at a
         * low weight so the player never runs out of wood, stone or
         * iron. Stage 1 has its own hand-mineable pool and does not
         * use this method.
         *
         * Harvest requirements (vanilla rules, enforced on break):
         *   stone / cobblestone / coal ......... wooden pickaxe
         *   copper / iron / lapis .............. stone pickaxe
         *   gold / redstone / diamond / emerald  iron pickaxe
         *   obsidian / ancient debris .......... diamond pickaxe
         * --------------------------------------------------------
         */
        if (stage >= 2) {

            add(weights, Blocks.DIRT, 8);
            add(weights, Blocks.OAK_LOG, 6);

            add(weights, Blocks.STONE, 10);
            add(weights, Blocks.COBBLESTONE, 8);
            add(weights, Blocks.COAL_ORE, 6);

            /*
             * Iron is progression-critical. It is only offered once
             * the player has mined 15 cobblestone (stone pickaxe).
             */
            add(weights, Blocks.IRON_ORE, 6);
        }

        if (stage >= 3) {

            add(weights, Blocks.COPPER_ORE, 3);
            add(weights, Blocks.LAPIS_ORE, 2);
            add(weights, Blocks.GOLD_ORE, 2);
        }

        /*
         * Diamonds first appear in Stage 5 and then stay rare.
         */
        if (stage >= 5) {

            add(weights, Blocks.REDSTONE_ORE, 4);
            add(weights, Blocks.DIAMOND_ORE, 2);
        }

        if (stage >= 6) {

            add(weights, Blocks.NETHER_QUARTZ_ORE, 3);
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
        createPhase8();
    }

    /*
     * ============================================================
     * PHASE 1
     * PLAINS
     *
     * Everything here can be harvested by hand. Logs give the
     * player a crafting table and a wooden pickaxe for Stage 2.
     * ============================================================
     */
    private void createPhase1() {

        BlockPool pool = new BlockPool();

        pool.add(Blocks.GRASS_BLOCK, 40);
        pool.add(Blocks.DIRT, 30);
        pool.add(Blocks.COARSE_DIRT, 8);
        pool.add(Blocks.ROOTED_DIRT, 4);

        pool.add(Blocks.OAK_LOG, 24);
        pool.add(Blocks.BIRCH_LOG, 6);

        pool.add(Blocks.SAND, 6);
        pool.add(Blocks.GRAVEL, 5);
        pool.add(Blocks.CLAY, 2);

        pool.add(Blocks.PUMPKIN, 2);
        pool.add(Blocks.MELON, 2);

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
     *
     * Needs a wooden pickaxe. Stone tools, then iron.
     * ============================================================
     */
    private void createPhase2() {

        BlockPool pool = createPool(
                2,

                b(Blocks.STONE, 40),
                b(Blocks.COBBLESTONE, 30),
                b(Blocks.GRANITE, 8),
                b(Blocks.DIORITE, 8),
                b(Blocks.ANDESITE, 8),
                b(Blocks.TUFF, 4),
                b(Blocks.DEEPSLATE, 4),
                b(Blocks.GRAVEL, 8),

                b(Blocks.COAL_ORE, 10),
                b(Blocks.COPPER_ORE, 5),
                b(Blocks.IRON_ORE, 8)
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
     *
     * Iron gear stage. First gold and lapis.
     * ============================================================
     */
    private void createPhase3() {

        BlockPool pool = createPool(
                3,

                b(Blocks.SNOW_BLOCK, 15),
                b(Blocks.ICE, 8),
                b(Blocks.PACKED_ICE, 5),

                b(Blocks.SPRUCE_LOG, 18),

                b(Blocks.STONE, 12),
                b(Blocks.COBBLESTONE, 8),
                b(Blocks.COAL_ORE, 8),
                b(Blocks.IRON_ORE, 10),
                b(Blocks.COPPER_ORE, 4),
                b(Blocks.LAPIS_ORE, 4),
                b(Blocks.GOLD_ORE, 3)
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

                b(Blocks.SAND, 25),
                b(Blocks.CLAY, 15),
                b(Blocks.GRAVEL, 8),

                b(Blocks.PRISMARINE, 12),
                b(Blocks.PRISMARINE_BRICKS, 6),
                b(Blocks.DARK_PRISMARINE, 4),
                b(Blocks.SEA_LANTERN, 4),
                b(Blocks.WET_SPONGE, 2),
                b(Blocks.DRIED_KELP_BLOCK, 6),

                b(Blocks.COAL_ORE, 6),
                b(Blocks.IRON_ORE, 8),
                b(Blocks.LAPIS_ORE, 4),
                b(Blocks.GOLD_ORE, 5)
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
     * Diamonds unlock here (iron pickaxe required).
     * ============================================================
     */
    private void createPhase5() {

        BlockPool pool = createPool(
                5,

                b(Blocks.JUNGLE_LOG, 15),
                b(Blocks.MANGROVE_LOG, 10),

                b(Blocks.MUD, 12),
                b(Blocks.MOSS_BLOCK, 10),
                b(Blocks.CLAY, 8),

                b(Blocks.MELON, 6),
                b(Blocks.PUMPKIN, 6),

                b(Blocks.IRON_ORE, 8),
                b(Blocks.REDSTONE_ORE, 6),
                b(Blocks.GOLD_ORE, 5),
                b(Blocks.DIAMOND_ORE, 4),
                b(Blocks.EMERALD_ORE, 2)
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
     *
     * Blazes start spawning here (blaze rods for Eyes of Ender).
     * Ancient debris needs a diamond pickaxe and stays rare.
     * ============================================================
     */
    private void createPhase6() {

        BlockPool pool = createPool(
                6,

                b(Blocks.NETHERRACK, 30),
                b(Blocks.SOUL_SAND, 10),
                b(Blocks.SOUL_SOIL, 6),
                b(Blocks.MAGMA_BLOCK, 6),

                b(Blocks.BLACKSTONE, 8),
                b(Blocks.BASALT, 6),
                b(Blocks.NETHER_BRICKS, 6),

                b(Blocks.CRIMSON_STEM, 6),
                b(Blocks.WARPED_STEM, 6),

                b(Blocks.GLOWSTONE, 5),
                b(Blocks.NETHER_QUARTZ_ORE, 8),
                b(Blocks.NETHER_GOLD_ORE, 6),
                b(Blocks.GILDED_BLACKSTONE, 2),
                b(Blocks.OBSIDIAN, 2),
                b(Blocks.ANCIENT_DEBRIS, 2)
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
     * STRONGHOLD
     *
     * Endermen are common (ender pearls) and supply chests start
     * handing out End Portal Frames, one at a time, up to 12.
     * ============================================================
     */
    private void createPhase7() {

        BlockPool pool = createPool(
                7,

                b(Blocks.STONE_BRICKS, 20),
                b(Blocks.MOSSY_STONE_BRICKS, 8),
                b(Blocks.CRACKED_STONE_BRICKS, 8),
                b(Blocks.CHISELED_STONE_BRICKS, 3),
                b(Blocks.INFESTED_STONE_BRICKS, 3),

                b(Blocks.BOOKSHELF, 4),
                b(Blocks.COBWEB, 2),
                b(Blocks.OBSIDIAN, 3),

                b(Blocks.IRON_ORE, 8),
                b(Blocks.GOLD_ORE, 4),
                b(Blocks.REDSTONE_ORE, 4),
                b(Blocks.LAPIS_ORE, 4),
                b(Blocks.DIAMOND_ORE, 3)
        );

        phases.add(
                new Phase(
                        7,
                        "Stronghold",
                        pool
                )
        );
    }

    /*
     * ============================================================
     * PHASE 8
     * THE END
     *
     * Final stage. Finish the portal and fight the Ender Dragon.
     * ============================================================
     */
    private void createPhase8() {

        BlockPool pool = createPool(
                8,

                b(Blocks.END_STONE, 30),
                b(Blocks.END_STONE_BRICKS, 10),
                b(Blocks.PURPUR_BLOCK, 10),
                b(Blocks.PURPUR_PILLAR, 5),
                b(Blocks.END_ROD, 3),

                b(Blocks.OBSIDIAN, 6),
                b(Blocks.CRYING_OBSIDIAN, 2),

                b(Blocks.DIAMOND_ORE, 3),
                b(Blocks.ANCIENT_DEBRIS, 1)
        );

        phases.add(
                new Phase(
                        8,
                        "The End",
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
