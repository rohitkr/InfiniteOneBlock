package com.infiniteoneblock.oneblock;

import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class BlockPool {

    private final List<BlockEntry> blocks =
            new ArrayList<>();

    private int totalWeight = 0;

    private final Random random =
            new Random();

    public void add(
            Block block,
            int weight
    ) {

        if (weight <= 0) {
            throw new IllegalArgumentException(
                    "Block weight must be greater than zero."
            );
        }

        blocks.add(
                new BlockEntry(
                        block,
                        weight
                )
        );

        totalWeight += weight;
    }

    public void addAll(BlockPool other, double weightScale) {
        if (other == null || weightScale <= 0) {
            return;
        }

        for (BlockEntry entry : other.blocks) {
            int scaledWeight = Math.max(1, (int) Math.round(entry.weight * weightScale));
            add(entry.block, scaledWeight);
        }
    }

    /**
     * Returns a completely random block from the pool.
     */
    public Block getRandomBlock() {

        return getRandomBlock(
                block -> true
        );
    }

    /**
     * Returns a random block that satisfies
     * the supplied predicate.
     *
     * The weights are recalculated only among
     * eligible blocks.
     */
    public Block getRandomBlock(
            Predicate<Block> predicate
    ) {

        int eligibleWeight = 0;

        /*
         * First calculate the total weight of all
         * blocks that are actually eligible.
         */
        for (BlockEntry entry : blocks) {

            if (predicate.test(entry.block)) {

                eligibleWeight +=
                        entry.weight;
            }
        }

        if (eligibleWeight <= 0) {

            throw new IllegalStateException(
                    "No eligible blocks found in block pool."
            );
        }

        /*
         * Select using the original block weights,
         * but only among eligible blocks.
         */
        int randomValue =
                random.nextInt(
                        eligibleWeight
                );

        int currentWeight = 0;

        for (BlockEntry entry : blocks) {

            if (!predicate.test(entry.block)) {
                continue;
            }

            currentWeight +=
                    entry.weight;

            if (randomValue < currentWeight) {

                return entry.block;
            }
        }

        /*
         * This should never normally happen,
         * but provides a safe fallback.
         */
        for (BlockEntry entry : blocks) {

            if (predicate.test(entry.block)) {
                return entry.block;
            }
        }

        throw new IllegalStateException(
                "Failed to select an eligible block."
        );
    }

    private static class BlockEntry {

        private final Block block;
        private final int weight;

        private BlockEntry(
                Block block,
                int weight
        ) {
            this.block = block;
            this.weight = weight;
        }
    }
}