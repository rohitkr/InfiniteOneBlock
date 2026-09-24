package com.infiniteoneblock.reward;

import com.infiniteoneblock.island.Island;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OneBlockRewardManager {

//    private static final double SUPPLY_CHEST_CHANCE = 0.05;
//    private static final double EXTRA_BASIC_CHANCE = 0.35;

    private final Random random = new Random();

    public boolean trySpawnSupplyChest(
            ServerPlayer player,
            ServerLevel world,
            BlockPos oneBlockPosition,
            int stage,
            Island island
    ) {
        double SUPPLY_CHEST_CHANCE = stage >= 2 ? 0.2 : 0.08;
        double EXTRA_BASIC_CHANCE = 0.35;

        if (random.nextDouble() >= SUPPLY_CHEST_CHANCE) {
            return false;
        }

        if (oneBlockPosition == null) {
            return false;
        }

        world.setBlock(oneBlockPosition, Blocks.CHEST.defaultBlockState(), 3);

        if (!(world.getBlockEntity(oneBlockPosition) instanceof ChestBlockEntity chest)) {
            System.out.println("[InfiniteOneBlock] Failed to create supply chest.");
            return false;
        }

        List<ItemStack> loot = new ArrayList<>();
        addMissingEssentials(loot, island, stage);
        loot.add(randomStageLoot(stage));

        if (random.nextDouble() < EXTRA_BASIC_CHANCE) {
            loot.add(randomBasicLoot());
        }

        if (random.nextBoolean()) {
            loot.add(randomStageLoot(stage));
        }

        loot.add(randomStageLoot(stage));
        loot.add(randomStageLoot(stage));

        int slot = 0;
        for (ItemStack stack : loot) {
            if (slot >= 27) {
                break;
            }
            chest.setItem(slot++, stack);
        }
        chest.setChanged();

        player.sendSystemMessage(Component.literal("A supply chest has appeared!"));
        return true;
    }

    private void addMissingEssentials(List<ItemStack> loot, Island island, int stage) {
        for (EssentialReward essential : EssentialReward.values()) {
            if (stage < essential.minStage()) {
                continue;
            }
            if (island.hasGrantedEssential(essential.bitMask())) {
                continue;
            }
            loot.add(essential.createStack());
            island.grantEssential(essential.bitMask());
        }
    }

    private ItemStack randomBasicLoot() {
        return switch (random.nextInt(6)) {
            case 0 -> new ItemStack(Items.WATER_BUCKET);
            case 1 -> new ItemStack(Items.LAVA_BUCKET);
            case 2 -> new ItemStack(Items.OAK_SAPLING, 2);
            case 3 -> new ItemStack(Items.BREAD, 4);
            case 4 -> new ItemStack(Items.TORCH, 8);
            default -> new ItemStack(Items.COBBLESTONE, 16);
        };
    }

    private ItemStack randomStageLoot(int stage) {
        // Safety check for invalid stages
        if (stage < 1) return new ItemStack(Items.COBBLESTONE, 16);

        List<ItemStack> possibleLoot = new ArrayList<>();

        // Stage 1 Items (Always available from Stage 1 onwards)
        possibleLoot.add(new ItemStack(Items.OAK_SAPLING, 2));
        possibleLoot.add(new ItemStack(Items.WHEAT_SEEDS, 4));
        possibleLoot.add(new ItemStack(Items.BREAD, 3));
        possibleLoot.add(new ItemStack(Items.LEATHER, random.nextInt(9) + 3));

        // Stage 2 Items
        if (stage >= 2) {
            possibleLoot.add(new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(4)));
            possibleLoot.add(new ItemStack(Items.COAL, 8));
            possibleLoot.add(new ItemStack(Items.FLINT_AND_STEEL));
            possibleLoot.add(new ItemStack(Items.STONE_PICKAXE));
            possibleLoot.add(new ItemStack(Items.PAPER, random.nextInt(9) + 3));
            possibleLoot.add(new ItemStack(Items.BOOK, random.nextInt(9) + 3));
            possibleLoot.add(new ItemStack(Items.BOOKSHELF, random.nextInt(9) + 3));
        }

        // Stage 3 Items
        if (stage >= 3) {
            possibleLoot.add(new ItemStack(Items.TORCH, 8));
            possibleLoot.add(new ItemStack(Items.SPRUCE_SAPLING, 2));
            possibleLoot.add(new ItemStack(Items.IRON_PICKAXE));
            possibleLoot.add(new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(8)));
            possibleLoot.add(new ItemStack(Items.GUNPOWDER, random.nextInt(9) + 3));
            possibleLoot.add(new ItemStack(Items.ARROW, random.nextInt(50) + 14));
            possibleLoot.add(new ItemStack(Items.STRING, random.nextInt(12) + 4));
        }

        // Stage 4 Items
        if (stage >= 4) {
//            possibleLoot.add(new ItemStack(Items.PRISMARINE_SHARD, 4));
            possibleLoot.add(new ItemStack(Items.COOKED_COD, 4));
            possibleLoot.add(new ItemStack(Items.HEART_OF_THE_SEA));
            possibleLoot.add(new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(10)));
            possibleLoot.add(new ItemStack(Items.DIAMOND, 1 + random.nextInt(2))); // Added diamond here as well
        }

        // Stage 5 Items
        if (stage >= 5) {
            possibleLoot.add(new ItemStack(Items.GOLD_INGOT, 3));
            possibleLoot.add(new ItemStack(Items.MELON_SEEDS, 2));
            possibleLoot.add(new ItemStack(Items.LAPIS_LAZULI, 4));
            possibleLoot.add(new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(15)));
            possibleLoot.add(new ItemStack(Items.DIAMOND, 1 + random.nextInt(3)));
        }

        // Stage 6 Items
        if (stage >= 6) {
            possibleLoot.add(new ItemStack(Items.NETHER_WART, 2));
            possibleLoot.add(new ItemStack(Items.BLAZE_ROD, 2));
            possibleLoot.add(new ItemStack(Items.ENDER_PEARL, 2));
            possibleLoot.add(new ItemStack(Items.GLOWSTONE_DUST, 4));
            possibleLoot.add(new ItemStack(Items.NETHERITE_SCRAP, 4 + random.nextInt(15)));
            possibleLoot.add(new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(20)));
            possibleLoot.add(new ItemStack(Items.DIAMOND, 1 + random.nextInt(8)));
        }

        // Stage 7 Items
        if (stage >= 7) {
            possibleLoot.add(new ItemStack(Items.ENDER_EYE, 2));
            possibleLoot.add(new ItemStack(Items.ENDER_PEARL, 4));
            possibleLoot.add(new ItemStack(Items.CHORUS_FRUIT, 8));
            possibleLoot.add(new ItemStack(Items.SHULKER_SHELL, 2));
//            possibleLoot.add(new ItemStack(Items.ELYTRA));
            possibleLoot.add(new ItemStack(Items.END_PORTAL_FRAME, 1 + random.nextInt(8)));
            possibleLoot.add(new ItemStack(Items.OBSIDIAN, 1 + random.nextInt(8)));
            possibleLoot.add(new ItemStack(Items.GUNPOWDER, random.nextInt(9) + 3));
            possibleLoot.add(new ItemStack(Items.DIAMOND, 1 + random.nextInt(8)));
            possibleLoot.add(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1 + random.nextInt(4)));
            possibleLoot.add(new ItemStack(Items.NETHERITE_SCRAP, 4 + random.nextInt(15)));
        }

        // Pick a random item out of the entire accumulated pool
        int randomIndex = random.nextInt(possibleLoot.size());

        // Return a copy to ensure safe item handling
        return possibleLoot.get(randomIndex).copy();
    }
}
