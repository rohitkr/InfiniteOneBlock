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

    public boolean trySpawnSupplyChest(
            ServerPlayer player,
            ServerLevel world,
            BlockPos oneBlockPosition,
            int stage
    ) {

        if (random.nextDouble() >= SUPPLY_CHEST_CHANCE) {
            return false;
        }

        if (oneBlockPosition == null) {
            System.out.println(
                    "[InfiniteOneBlock] "
                            + "No free position found for supply chest."
            );
            return false;
        }

        /*
         * Place the chest.
         */
        world.setBlock(
                oneBlockPosition,
                Blocks.CHEST.defaultBlockState(),
                3
        );

        /*
         * Retrieve the newly-created chest
         * block entity.
         */
        if (!(world.getBlockEntity(
                oneBlockPosition
        ) instanceof ChestBlockEntity chest)) {

            System.out.println(
                    "[InfiniteOneBlock] "
                            + "Failed to create supply chest."
            );

            return false;
        }

        java.util.Random random = new java.util.Random();
        int itemsToSpawn = 1 + random.nextInt(3); // Populates between 1 to 3 items per chest

        for (int i = 0; i < itemsToSpawn; i++) {
            int randomItem = random.nextInt(100);
            int randomSlot = random.nextInt(27); // Standard chest inventory slots (0 - 26)

            switch (stage) {
                case 1:
                    if (randomItem < 30) {
                        chest.setItem(randomSlot, new ItemStack(Items.WATER_BUCKET));
                    } else if (randomItem < 55) {
                        chest.setItem(randomSlot, new ItemStack(Items.OAK_SAPLING, 2));
                    } else if (randomItem < 80) {
                        chest.setItem(randomSlot, new ItemStack(Items.WHEAT_SEEDS, 4));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.BREAD, 3));
                    }
                    break;

                case 2:
                    if (randomItem < 25) {
                        chest.setItem(randomSlot, new ItemStack(Items.LAVA_BUCKET));
                    } else if (randomItem < 55) {
                        chest.setItem(randomSlot, new ItemStack(Items.IRON_INGOT, 3));
                    } else if (randomItem < 80) {
                        chest.setItem(randomSlot, new ItemStack(Items.COAL, 8));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.FLINT_AND_STEEL));
                    }
                    break;

                case 3:
                    if (randomItem < 40) {
                        chest.setItem(randomSlot, new ItemStack(Items.TORCH, 8));
                    } else if (randomItem < 70) {
                        chest.setItem(randomSlot, new ItemStack(Items.SPRUCE_SAPLING, 2));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.IRON_PICKAXE));
                    }
                    break;

                case 4:
                    if (randomItem < 40) {
                        chest.setItem(randomSlot, new ItemStack(Items.PRISMARINE_SHARD, 4));
                    } else if (randomItem < 70) {
                        chest.setItem(randomSlot, new ItemStack(Items.COOKED_COD, 4));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.HEART_OF_THE_SEA));
                    }
                    break;

                case 5:
                    if (randomItem < 35) {
                        chest.setItem(randomSlot, new ItemStack(Items.GOLD_INGOT, 3));
                    } else if (randomItem < 70) {
                        chest.setItem(randomSlot, new ItemStack(Items.MELON_SEEDS, 2));
                    } else if (randomItem < 90) {
                        chest.setItem(randomSlot, new ItemStack(Items.LAPIS_LAZULI, 4));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.DIAMOND));
                    }
                    break;

                case 6:
                    if (randomItem < 35) {
                        chest.setItem(randomSlot, new ItemStack(Items.NETHER_WART, 2));
                    } else if (randomItem < 60) {
                        chest.setItem(randomSlot, new ItemStack(Items.BLAZE_ROD, 2));
                    } else if (randomItem < 80) {
                        chest.setItem(randomSlot, new ItemStack(Items.ENDER_PEARL, 2));
                    } else if (randomItem < 95) {
                        chest.setItem(randomSlot, new ItemStack(Items.GLOWSTONE_DUST, 4));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.NETHERITE_SCRAP));
                    }
                    break;

                case 7:
                    if (randomItem < 25) {
                        chest.setItem(randomSlot, new ItemStack(Items.ENDER_EYE, 2));
                    } else if (randomItem < 50) {
                        chest.setItem(randomSlot, new ItemStack(Items.ENDER_PEARL, 4));
                    } else if (randomItem < 70) {
                        chest.setItem(randomSlot, new ItemStack(Items.DIAMOND, 2));
                    } else if (randomItem < 85) {
                        chest.setItem(randomSlot, new ItemStack(Items.CHORUS_FRUIT, 8));
                    } else if (randomItem < 95) {
                        chest.setItem(randomSlot, new ItemStack(Items.SHULKER_SHELL, 2));
                    } else {
                        chest.setItem(randomSlot, new ItemStack(Items.ELYTRA));
                    }
                    break;

                default:
                    chest.setItem(randomSlot, new ItemStack(Items.COBBLESTONE, 16));
                    break;
            }
        }
        chest.setChanged();

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "A supply chest has appeared!"
                )
        );

        System.out.println(
                "[InfiniteOneBlock] Supply chest spawned at "
                        + oneBlockPosition
        );

        return true;
    }
}
