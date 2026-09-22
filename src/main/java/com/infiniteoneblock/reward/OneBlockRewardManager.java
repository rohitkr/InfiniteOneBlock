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
import java.util.function.Supplier;

public class OneBlockRewardManager {

    private static final double SUPPLY_CHEST_CHANCE = 0.05;
    private static final double EXTRA_LOOT_CHANCE = 0.5;

    private static final List<LootEntry> LOOT_TABLE = List.of(
            new LootEntry(1, 28, () -> new ItemStack(Items.OAK_LOG, 4)),
            new LootEntry(1, 24, () -> new ItemStack(Items.COBBLESTONE, 8)),
            new LootEntry(1, 16, () -> new ItemStack(Items.WHEAT_SEEDS, 4)),
            new LootEntry(1, 14, () -> new ItemStack(Items.BREAD, 4)),
            new LootEntry(1, 14, () -> new ItemStack(Items.TORCH, 8)),
            new LootEntry(1, 12, () -> new ItemStack(Items.OAK_SAPLING, 2)),
            new LootEntry(2, 16, () -> new ItemStack(Items.COAL, 8)),
            new LootEntry(2, 14, () -> new ItemStack(Items.IRON_INGOT, 3)),
            new LootEntry(2, 6, () -> new ItemStack(Items.FLINT_AND_STEEL)),
            new LootEntry(2, 6, () -> new ItemStack(Items.STONE_PICKAXE)),
            new LootEntry(3, 8, () -> new ItemStack(Items.SPRUCE_SAPLING, 2)),
            new LootEntry(3, 6, () -> new ItemStack(Items.IRON_PICKAXE)),
            new LootEntry(4, 10, () -> new ItemStack(Items.PRISMARINE_SHARD, 4)),
            new LootEntry(4, 8, () -> new ItemStack(Items.COOKED_COD, 4)),
            new LootEntry(4, 2, () -> new ItemStack(Items.HEART_OF_THE_SEA)),
            new LootEntry(5, 8, () -> new ItemStack(Items.GOLD_INGOT, 3)),
            new LootEntry(5, 8, () -> new ItemStack(Items.MELON_SEEDS, 2)),
            new LootEntry(5, 8, () -> new ItemStack(Items.LAPIS_LAZULI, 4)),
            new LootEntry(5, 3, () -> new ItemStack(Items.DIAMOND)),
            new LootEntry(6, 8, () -> new ItemStack(Items.NETHER_WART, 2)),
            new LootEntry(6, 6, () -> new ItemStack(Items.BLAZE_ROD, 2)),
            new LootEntry(6, 6, () -> new ItemStack(Items.ENDER_PEARL, 2)),
            new LootEntry(6, 6, () -> new ItemStack(Items.GLOWSTONE_DUST, 4)),
            new LootEntry(6, 2, () -> new ItemStack(Items.NETHERITE_SCRAP)),
            new LootEntry(7, 6, () -> new ItemStack(Items.ENDER_EYE, 2)),
            new LootEntry(7, 6, () -> new ItemStack(Items.ENDER_PEARL, 4)),
            new LootEntry(7, 4, () -> new ItemStack(Items.DIAMOND, 2)),
            new LootEntry(7, 6, () -> new ItemStack(Items.CHORUS_FRUIT, 8)),
            new LootEntry(7, 3, () -> new ItemStack(Items.SHULKER_SHELL, 2)),
            new LootEntry(7, 1, () -> new ItemStack(Items.ELYTRA))
    );

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
        loot.add(randomBasicLoot());
        loot.add(randomCumulativeLoot(stage));

        if (random.nextDouble() < EXTRA_LOOT_CHANCE) {
            loot.add(randomBasicLoot());
        }

        if (random.nextBoolean()) {
            loot.add(randomCumulativeLoot(stage));
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
            case 2 -> new ItemStack(Items.OAK_LOG, random.nextInt(15) + 1);
            case 3 -> new ItemStack(Items.BREAD, 4);
            case 4 -> new ItemStack(Items.TORCH, 8);
            default -> new ItemStack(Items.COBBLESTONE, random.nextInt(15) + 1);
        };
    }

    private ItemStack randomCumulativeLoot(int stage) {
        int totalWeight = 0;
        for (LootEntry entry : LOOT_TABLE) {
            if (stage >= entry.minStage()) {
                totalWeight += entry.weight();
            }
        }

        if (totalWeight <= 0) {
            return new ItemStack(Items.COBBLESTONE, 16);
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (LootEntry entry : LOOT_TABLE) {
            if (stage < entry.minStage()) {
                continue;
            }
            current += entry.weight();
            if (roll < current) {
                return entry.stack().get();
            }
        }

        return new ItemStack(Items.COBBLESTONE, 16);
    }

    private record LootEntry(int minStage, int weight, Supplier<ItemStack> stack) {
    }
}
