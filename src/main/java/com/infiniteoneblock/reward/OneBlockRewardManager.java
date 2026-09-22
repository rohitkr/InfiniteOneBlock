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

    private static final double SUPPLY_CHEST_CHANCE = 0.05;
    private static final double EXTRA_BASIC_CHANCE = 0.35;

    private final Random random = new Random();

    public boolean trySpawnSupplyChest(
            ServerPlayer player,
            ServerLevel world,
            BlockPos oneBlockPosition,
            int stage,
            Island island
    ) {
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
        int roll = random.nextInt(100);

        return switch (stage) {
            case 1 -> {
                if (roll < 40) yield new ItemStack(Items.OAK_SAPLING, 2);
                if (roll < 70) yield new ItemStack(Items.WHEAT_SEEDS, 4);
                yield new ItemStack(Items.BREAD, 3);
            }
            case 2 -> {
                if (roll < 35) yield new ItemStack(Items.IRON_INGOT, 3);
                if (roll < 65) yield new ItemStack(Items.COAL, 8);
                if (roll < 85) yield new ItemStack(Items.FLINT_AND_STEEL);
                yield new ItemStack(Items.STONE_PICKAXE);
            }
            case 3 -> {
                if (roll < 40) yield new ItemStack(Items.TORCH, 8);
                if (roll < 70) yield new ItemStack(Items.SPRUCE_SAPLING, 2);
                yield new ItemStack(Items.IRON_PICKAXE);
            }
            case 4 -> {
                if (roll < 40) yield new ItemStack(Items.PRISMARINE_SHARD, 4);
                if (roll < 70) yield new ItemStack(Items.COOKED_COD, 4);
                yield new ItemStack(Items.HEART_OF_THE_SEA);
            }
            case 5 -> {
                if (roll < 35) yield new ItemStack(Items.GOLD_INGOT, 3);
                if (roll < 70) yield new ItemStack(Items.MELON_SEEDS, 2);
                if (roll < 90) yield new ItemStack(Items.LAPIS_LAZULI, 4);
                yield new ItemStack(Items.DIAMOND);
            }
            case 6 -> {
                if (roll < 35) yield new ItemStack(Items.NETHER_WART, 2);
                if (roll < 60) yield new ItemStack(Items.BLAZE_ROD, 2);
                if (roll < 80) yield new ItemStack(Items.ENDER_PEARL, 2);
                if (roll < 95) yield new ItemStack(Items.GLOWSTONE_DUST, 4);
                yield new ItemStack(Items.NETHERITE_SCRAP);
            }
            case 7 -> {
                if (roll < 25) yield new ItemStack(Items.ENDER_EYE, 2);
                if (roll < 50) yield new ItemStack(Items.ENDER_PEARL, 4);
                if (roll < 70) yield new ItemStack(Items.DIAMOND, 2);
                if (roll < 85) yield new ItemStack(Items.CHORUS_FRUIT, 8);
                if (roll < 95) yield new ItemStack(Items.SHULKER_SHELL, 2);
                yield new ItemStack(Items.ELYTRA);
            }
            default -> new ItemStack(Items.COBBLESTONE, 16);
        };
    }
}
