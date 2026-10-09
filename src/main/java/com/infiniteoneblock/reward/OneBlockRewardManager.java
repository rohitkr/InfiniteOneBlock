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
        double SUPPLY_CHEST_CHANCE = stage >= 2 ? 0.12 : 0.08;
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
        addEndPortalFrame(loot, island, stage, player);

        loot.add(randomStageLoot(stage));
        loot.add(randomStageLoot(stage));

        if (random.nextBoolean()) {
            loot.add(randomStageLoot(stage));
        }

        if (random.nextDouble() < EXTRA_BASIC_CHANCE) {
            loot.add(randomBasicLoot());
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

    /*
     * End Portal Frames cannot be mined from the OneBlock. From Stage 7 each supply
     * chest has a chance to contain a single frame until the player has the 12 needed
     * for a portal. That spreads the portal over roughly the second half of Stage 7.
     */
    private static final int END_PORTAL_FRAMES_NEEDED = 12;
    private static final double END_PORTAL_FRAME_CHANCE = 0.35;

    private void addEndPortalFrame(List<ItemStack> loot, Island island, int stage, ServerPlayer player) {
        if (stage < 7 || island.getEndPortalFramesGranted() >= END_PORTAL_FRAMES_NEEDED) {
            return;
        }
        if (random.nextDouble() >= END_PORTAL_FRAME_CHANCE) {
            return;
        }

        loot.add(new ItemStack(Items.END_PORTAL_FRAME));
        island.setEndPortalFramesGranted(island.getEndPortalFramesGranted() + 1);
        player.sendSystemMessage(Component.literal(
                "End Portal Frame found! (" + island.getEndPortalFramesGranted() + "/" + END_PORTAL_FRAMES_NEEDED + ")"));
    }

    private ItemStack randomBasicLoot() {
        return switch (random.nextInt(5)) {
            case 0 -> new ItemStack(Items.OAK_SAPLING, 2);
            case 1 -> new ItemStack(Items.BREAD, 3);
            case 2 -> new ItemStack(Items.TORCH, 8);
            case 3 -> new ItemStack(Items.APPLE, 2);
            default -> new ItemStack(Items.BONE_MEAL, 4);
        };
    }

    private int generateItems(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    /*
     * ============================================================
     * STAGE LOOT TABLE
     *
     * minStage .. maxStage, weight, item, min..max count.
     *
     * Design rules:
     *  - Chests top up what the OneBlock and mobs already give; they never
     *    replace the main progression path.
     *  - Iron ingots from Stage 3, diamonds only as single rare drops from
     *    Stage 4, never in bulk.
     *  - Blaze rods and ender pearls only appear from Stage 6 as rare drops.
     *    Most of them must come from Blazes and Endermen.
     *  - Eyes of Ender are never given directly: craft them.
     * ============================================================
     */
    private record LootEntry(int minStage, int maxStage, int weight, net.minecraft.world.item.Item item, int min, int max) {
    }

    private static final int ANY = Integer.MAX_VALUE;

    private static final List<LootEntry> STAGE_LOOT = List.of(
            // Stage 1 — Plains: food and farming
            new LootEntry(1, ANY, 6, Items.OAK_SAPLING, 1, 2),
            new LootEntry(1, ANY, 6, Items.WHEAT_SEEDS, 2, 5),
            new LootEntry(1, ANY, 5, Items.BREAD, 1, 3),
            new LootEntry(1, 4, 4, Items.APPLE, 1, 3),
            new LootEntry(1, 3, 3, Items.SUGAR_CANE, 1, 3),
            new LootEntry(1, 2, 3, Items.BONE_MEAL, 2, 4),

            // Stage 2 — Underground: light and basic tools materials
            new LootEntry(2, ANY, 6, Items.COAL, 2, 5),
            new LootEntry(2, ANY, 5, Items.TORCH, 4, 8),
            new LootEntry(2, 5, 3, Items.LEATHER, 1, 3),
            new LootEntry(2, 5, 3, Items.STRING, 1, 3),
            new LootEntry(2, 3, 3, Items.COBBLESTONE, 4, 12),

            // Stage 3 — Winter: iron age
            new LootEntry(3, ANY, 5, Items.IRON_INGOT, 1, 3),
            new LootEntry(3, ANY, 4, Items.ARROW, 4, 10),
            new LootEntry(3, ANY, 3, Items.GUNPOWDER, 1, 3),
            new LootEntry(3, 6, 3, Items.PAPER, 2, 4),
            new LootEntry(3, 6, 2, Items.BOOK, 1, 2),
            new LootEntry(3, 4, 3, Items.SPRUCE_SAPLING, 1, 2),
            new LootEntry(3, 5, 2, Items.PUMPKIN_SEEDS, 1, 2),
            new LootEntry(3, 5, 2, Items.MELON_SEEDS, 1, 2),

            // Stage 4 — Ocean: gold and the first rare diamond
            new LootEntry(4, ANY, 4, Items.COOKED_COD, 2, 4),
            new LootEntry(4, ANY, 3, Items.GOLD_INGOT, 1, 3),
            new LootEntry(4, ANY, 1, Items.DIAMOND, 1, 1),

            // Stage 5 — Jungle / Swamp: enchanting materials
            new LootEntry(5, ANY, 4, Items.LAPIS_LAZULI, 3, 6),
            new LootEntry(5, ANY, 2, Items.EXPERIENCE_BOTTLE, 2, 5),
            new LootEntry(5, ANY, 2, Items.OBSIDIAN, 1, 2),
            new LootEntry(5, ANY, 2, Items.COCOA_BEANS, 1, 3),

            // Stage 6 — Nether: brewing, bartering gold, rare rods and pearls
            new LootEntry(6, ANY, 4, Items.NETHER_WART, 1, 3),
            new LootEntry(6, ANY, 3, Items.GLOWSTONE_DUST, 2, 5),
            new LootEntry(6, ANY, 4, Items.GOLD_INGOT, 3, 6),
            new LootEntry(6, ANY, 2, Items.BLAZE_ROD, 1, 1),
            new LootEntry(6, ANY, 2, Items.ENDER_PEARL, 1, 1),
            new LootEntry(6, ANY, 1, Items.NETHERITE_SCRAP, 1, 1),

            // Stage 7 — Stronghold: preparing for the End
            new LootEntry(7, ANY, 3, Items.ENDER_PEARL, 1, 2),
            new LootEntry(7, ANY, 2, Items.BLAZE_ROD, 1, 2),
            new LootEntry(7, ANY, 3, Items.ARROW, 8, 16),
            new LootEntry(7, ANY, 2, Items.GOLDEN_APPLE, 1, 1),
            new LootEntry(7, ANY, 2, Items.IRON_INGOT, 2, 5),

            // Stage 8 — The End: dragon fight supplies
            new LootEntry(8, ANY, 3, Items.GOLDEN_APPLE, 1, 2),
            new LootEntry(8, ANY, 2, Items.DIAMOND, 1, 2),
            new LootEntry(8, ANY, 2, Items.OBSIDIAN, 2, 4),
            new LootEntry(8, ANY, 1, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 1)
    );

    private ItemStack randomStageLoot(int stage) {
        int clampedStage = Math.max(1, stage);

        int totalWeight = 0;
        for (LootEntry entry : STAGE_LOOT) {
            if (clampedStage >= entry.minStage() && clampedStage <= entry.maxStage()) {
                totalWeight += entry.weight();
            }
        }

        int roll = random.nextInt(totalWeight);
        for (LootEntry entry : STAGE_LOOT) {
            if (clampedStage < entry.minStage() || clampedStage > entry.maxStage()) {
                continue;
            }
            roll -= entry.weight();
            if (roll < 0) {
                return new ItemStack(entry.item(), generateItems(entry.min(), entry.max()));
            }
        }

        return new ItemStack(Items.BREAD, 2);
    }
}
