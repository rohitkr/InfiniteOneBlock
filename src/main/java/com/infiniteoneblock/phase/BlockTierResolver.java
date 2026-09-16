        package com.infiniteoneblock.phase;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTierResolver {

    public MiningTier getTier(Block block) {

        BlockState state =
                block.defaultBlockState();

        /*
         * No correct tool required.
         *
         * This is the HAND tier.
         */
        if (!state.requiresCorrectToolForDrops()) {
            return MiningTier.HAND;
        }

        /*
         * Diamond-level blocks.
         */
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return MiningTier.DIAMOND;
        }

        /*
         * Iron-level blocks.
         */
        if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return MiningTier.IRON;
        }

        /*
         * Stone-level blocks.
         */
        if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return MiningTier.STONE;
        }

        /*
         * Correct tool required, but no higher mining
         * level requirement.
         *
         * This is the wooden-tool tier.
         */
        return MiningTier.WOOD;
    }

    public boolean isValidBlock(Block block) {

        /*
         * Never generate air.
         */
        if (block == Blocks.AIR
                || block == Blocks.CAVE_AIR
                || block == Blocks.VOID_AIR) {

            return false;
        }

        /*
         * Only use vanilla Minecraft blocks.
         */
        if (!BuiltInRegistries.BLOCK
                .getKey(block)
                .getNamespace()
                .equals("minecraft")) {

            return false;
        }

        /*
         * A block that has no corresponding BlockItem
         * generally isn't appropriate as a OneBlock result.
         *
         * This filters out things such as:
         *
         * - water
         * - lava
         * - fire
         * - technical blocks
         * - other non-placeable registry blocks
         */
        return BuiltInRegistries.BLOCK
                .getKey(block) != null;
    }
}
