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
        // Create a small helper loop to determine how many item slots to populate (e.g., 1 to 3 items)
        int itemsToSpawn = 1 + random.nextInt(3);

        for (int i = 0; i < itemsToSpawn; i++) {
            int randomItem = random.nextInt(100);
            // Randomly scatter items across a standard chest's 27 inventory slots
            int randomSlot = random.nextInt(27);

            if (randomItem < 25) { // 25% Chance for baseline water/ice rules
                chest.setItem(randomSlot, new ItemStack(Items.WATER_BUCKET));
            } else if (randomItem < 45) { // 20% Chance for progressive build building blocks
                chest.setItem(randomSlot, new ItemStack(Items.DIRT, 4)); // Drop bundles instead of just 1
            } else if (randomItem < 60) { // 15% Chance for iron utility
                chest.setItem(randomSlot, new ItemStack(Items.IRON_INGOT, 2));
            } else if (randomItem < 75) { // 15% Chance for vital lava access
                chest.setItem(randomSlot, new ItemStack(Items.LAVA_BUCKET));
            } else if (randomItem < 85) { // 10% Chance for seeds/farming starters
                chest.setItem(randomSlot, new ItemStack(Items.OAK_SAPLING, 2));
            } else if (randomItem < 93) { // 8% Chance for gold economy tools
                chest.setItem(randomSlot, new ItemStack(Items.GOLD_INGOT, 2));
            } else if (randomItem < 98) { // 5% Chance for endgame diamond blocks
                chest.setItem(randomSlot, new ItemStack(Items.DIAMOND));
            } else { // 2% Chance for rare weapon sets
                chest.setItem(randomSlot, new ItemStack(Items.IRON_SWORD)); // Nerfed to Iron to preserve game tiers!
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
