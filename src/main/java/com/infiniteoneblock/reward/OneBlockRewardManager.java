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

    private int generateItems(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    private ItemStack randomStageLoot(int stage) {
        // Safety check for invalid stages
        if (stage < 1) return new ItemStack(Items.COBBLESTONE, generateItems(2, 8));

        List<ItemStack> possibleLoot = new ArrayList<>();

        // Stage 1 Items (Always available from Stage 1 onwards)
        possibleLoot.add(new ItemStack(Items.OAK_SAPLING, generateItems(1, 4)));
        possibleLoot.add(new ItemStack(Items.DIRT, generateItems(2, 6)));
        possibleLoot.add(new ItemStack(Items.WHEAT_SEEDS, generateItems(4, 8)));
        possibleLoot.add(new ItemStack(Items.BREAD, generateItems(2, 5)));

        // Stage 2 Items
        if (stage >= 2) {
            possibleLoot.add(new ItemStack(Items.COAL, generateItems(3, 6)));
            possibleLoot.add(new ItemStack(Items.LEATHER, generateItems(4, 8)));
        }

        // Stage 3 Items
        if (stage >= 3) {
            possibleLoot.add(new ItemStack(Items.TORCH, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.SPRUCE_SAPLING, generateItems(1, 4)));
            possibleLoot.add(new ItemStack(Items.IRON_INGOT, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.STRING, generateItems(2, 4)));
            possibleLoot.add(new ItemStack(Items.ARROW, generateItems(4, 12)));
            possibleLoot.add(new ItemStack(Items.GUNPOWDER, generateItems(2, 5)));
            possibleLoot.add(new ItemStack(Items.PAPER, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.BOOK, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.BOOKSHELF, generateItems(2, 6)));
        }

        // Stage 4 Items
        if (stage >= 4) {
//            possibleLoot.add(new ItemStack(Items.PRISMARINE_SHARD, 4));
            possibleLoot.add(new ItemStack(Items.COOKED_COD, generateItems(2, 4)));
            possibleLoot.add(new ItemStack(Items.DIAMOND, generateItems(2, 6)));
        }

        // Stage 5 Items
        if (stage >= 5) {
            possibleLoot.add(new ItemStack(Items.GOLD_INGOT, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.LAPIS_LAZULI, generateItems(4, 8)));
        }

        // Stage 6 Items
        if (stage >= 6) {
            possibleLoot.add(new ItemStack(Items.NETHER_WART, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.BLAZE_ROD, generateItems(2, 4)));
            possibleLoot.add(new ItemStack(Items.ENDER_PEARL, generateItems(2, 4)));
            possibleLoot.add(new ItemStack(Items.GLOWSTONE_DUST, generateItems(4, 6)));
            possibleLoot.add(new ItemStack(Items.NETHERITE_SCRAP, generateItems(4, 8)));
        }

        // Stage 7 Items
        if (stage >= 7) {
            possibleLoot.add(new ItemStack(Items.ENDER_EYE, generateItems(2, 6)));
            // possibleLoot.add(new ItemStack(Items.SHULKER_SHELL, 2));
            // possibleLoot.add(new ItemStack(Items.ELYTRA));
            possibleLoot.add(new ItemStack(Items.END_PORTAL_FRAME, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.OBSIDIAN, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.GUNPOWDER, generateItems(2, 6)));
            possibleLoot.add(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, generateItems(2, 4)));
        }

        // Pick a random item out of the entire accumulated pool
        int randomIndex = random.nextInt(possibleLoot.size());

        // Return a copy to ensure safe item handling
        return possibleLoot.get(randomIndex).copy();
    }
}
