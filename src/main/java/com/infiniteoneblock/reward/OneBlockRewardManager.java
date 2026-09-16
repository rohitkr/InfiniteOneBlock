package com.infiniteoneblock.reward;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.Random;

public class OneBlockRewardManager {

    /*
     * Chance of receiving a special supply chest
     * after successfully mining the OneBlock.
     *
     * 5% = approximately 1 chest every 20 breaks
     * on average.
     */
    private static final double SUPPLY_CHEST_CHANCE = 0.05;

    private final Random random =
            new Random();

    public void trySpawnSupplyChest(
            ServerPlayer player,
            ServerLevel world,
            BlockPos oneBlockPosition
    ) {

        if (random.nextDouble()
                >= SUPPLY_CHEST_CHANCE) {

            return;
        }

//        BlockPos chestPosition =
//                findChestPosition(
//                        world,
//                        oneBlockPosition
//                );
        BlockPos chestPosition = oneBlockPosition;

        if (chestPosition == null) {

            System.out.println(
                    "[InfiniteOneBlock] "
                            + "No free position found for supply chest."
            );

            return;
        }

        /*
         * Place the chest.
         */
        world.setBlock(
                chestPosition,
                Blocks.CHEST.defaultBlockState(),
                3
        );

        /*
         * Retrieve the newly-created chest
         * block entity.
         */
        if (!(world.getBlockEntity(
                chestPosition
        ) instanceof ChestBlockEntity chest)) {

            System.out.println(
                    "[InfiniteOneBlock] "
                            + "Failed to create supply chest."
            );

            return;
        }

        /*
         * Guaranteed water supply.
         */
        chest.setItem(
                0,
                new ItemStack(
                        Items.WATER_BUCKET
                )
        );

        /*
         * Guaranteed lava supply.
         */
        chest.setItem(
                1,
                new ItemStack(
                        Items.LAVA_BUCKET
                )
        );

        /*
         * A little additional useful material.
         */
        chest.setItem(
                2,
                new ItemStack(
                        Items.IRON_INGOT,
                        2
                )
        );

        chest.setItem(
                3,
                new ItemStack(
                        Items.FLINT
                )
        );

        chest.setChanged();

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "A supply chest has appeared!"
                )
        );

        System.out.println(
                "[InfiniteOneBlock] Supply chest spawned at "
                        + chestPosition
        );
    }

    /**
     * Finds a nearby free position for the chest.
     *
     * We deliberately keep the chest close to the
     * OneBlock so the player can see it.
     */
    private BlockPos findChestPosition(
            ServerLevel world,
            BlockPos oneBlockPosition
    ) {

        BlockPos[] candidates = {

                oneBlockPosition.east(),

                oneBlockPosition.west(),

                oneBlockPosition.north(),

                oneBlockPosition.south(),

                oneBlockPosition.above(),

                oneBlockPosition.east().above(),

                oneBlockPosition.west().above(),

                oneBlockPosition.north().above(),

                oneBlockPosition.south().above()
        };

        for (BlockPos candidate :
                candidates) {

            if (world.isEmptyBlock(candidate)) {

                return candidate;
            }
        }

        return null;
    }
}
