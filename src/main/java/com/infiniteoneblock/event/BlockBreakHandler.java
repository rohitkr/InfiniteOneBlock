package com.infiniteoneblock.event;

import com.infiniteoneblock.InfiniteOneBlock;
import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;
import com.infiniteoneblock.oneblock.OneBlockManager;
import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.reward.OneBlockRewardManager;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.stats.Stats;

import java.util.List;

public class BlockBreakHandler {

    private final IslandManager islandManager;
    private final OneBlockManager oneBlockManager;
    private final OneBlockRewardManager rewardManager;

    public BlockBreakHandler(
            IslandManager islandManager,
            OneBlockManager oneBlockManager
    ) {
        this.islandManager = islandManager;
        this.oneBlockManager = oneBlockManager;
        this.rewardManager = new OneBlockRewardManager();
    }

    public void register() {
        PlayerBlockBreakEvents.BEFORE.register(
                (world, player, pos, state, blockEntity) -> {

                    if (!(player instanceof ServerPlayer serverPlayer)) {
                        return true;
                    }

                    Island island = islandManager.getIsland(serverPlayer.getUUID());
                    if (island == null) {
                        return true;
                    }

                    if (!pos.equals(island.getOneBlockPosition())) {
                        return true;
                    }

                    handleOneBlockBreak(serverPlayer, island, state, pos);
                    return false; // Cancels vanilla code, requiring manual damage math below
                }
        );
    }

    private void handleOneBlockBreak(
            ServerPlayer player,
            Island island,
            BlockState brokenState,
            BlockPos pos
    ) {
        ServerLevel world = island.getWorld();

        // 1. Drops follow vanilla harvest rules
        ItemStack tool = player.getMainHandItem();
        boolean creative = player.isCreative();

        /*
         * Same check vanilla uses in ServerPlayerGameMode: blocks such as stone, ores,
         * obsidian or snow only drop when mined with a tool of the right type and tier.
         * Dirt, logs, sand etc. still drop by hand.
         */
        boolean canHarvest = !creative && player.hasCorrectToolForDrops(brokenState);

        if (canHarvest) {
            // Vanilla loot table with the held tool, so Fortune and Silk Touch apply too
            List<ItemStack> drops = Block.getDrops(brokenState, world, pos, world.getBlockEntity(pos), player, tool);
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    Block.popResource(world, pos.above(), drop);
                }
            }

            // Ore experience and silverfish from infested blocks
            brokenState.spawnAfterBreak(world, pos, tool, true);
        }

        if (!creative) {
            // Tool durability, stats and hunger exactly as a vanilla block break
            tool.mineBlock(world, brokenState, pos, player);
            player.awardStat(Stats.BLOCK_MINED.get(brokenState.getBlock()));
            player.causeFoodExhaustion(0.005F);
        }

        int stageBefore = oneBlockManager.getCurrentStage(island);
        int blocksMined = island.incrementBlocksMined();
        int stageAfter = oneBlockManager.getCurrentStage(island);

        if (brokenState.is(Blocks.OAK_LOG)) {
            int oakLogs = island.incrementOakLogsCollected();
            player.sendSystemMessage(Component.literal("Oak Logs: " + oakLogs));
        }

        // Only counts when cobblestone actually dropped (needs a pickaxe); this gates iron ore
        if (canHarvest && (brokenState.is(Blocks.COBBLESTONE) || brokenState.is(Blocks.STONE))) {
            int cobblestone = island.incrementCobblestoneCollected();
            player.sendSystemMessage(Component.literal("Cobblestone: " + cobblestone));
        }

        if (stageAfter > stageBefore) {
            Phase unlocked = oneBlockManager.getCurrentPhase(island);
            player.sendSystemMessage(Component.literal("Stage " + unlocked.getId() + " unlocked: " + unlocked.getName()));
            if (unlocked.getId() == 7) {
                player.sendSystemMessage(Component.literal("Supply chests now hold End Portal Frames. Collect 12, and craft Eyes of Ender from Blaze Powder and Ender Pearls."));
            }
            if (unlocked.getId() == 8) {
                player.sendSystemMessage(Component.literal("Final stage! Build the End Portal and defeat the Ender Dragon."));
            }
        }

        Phase currentPhase = oneBlockManager.getCurrentPhase(island);

        // Nudge player and freeze gravity momentum briefly before block transition
        boolean isStandingOnBlock = player.blockPosition().below().equals(pos) || player.blockPosition().equals(pos);
        if (isStandingOnBlock) {
            player.fallDistance = 0.0F;
            player.setNoGravity(true);
            player.teleportTo(player.getX(), pos.getY() + 1.05, player.getZ());
        }

        int stage = oneBlockManager.getCurrentStage(island);

        // Evaluate the chest spawn sequence directly
        boolean spawnedChest = rewardManager.trySpawnSupplyChest(
                player,
                world,
                pos,
                stage,
                island
        );

        if (!spawnedChest) {
            oneBlockManager.regenerate(island);
        }

        if (world.getBlockState(pos).isAir()) {
            world.setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState());
        }

        if (isStandingOnBlock) {
            world.getServer().execute(() -> {
                player.setNoGravity(false);
            });
        }

        if (blocksMined % 10 == 0) {
            player.sendSystemMessage(Component.literal("One Block: " + blocksMined + " blocks mined."));
            player.sendSystemMessage(Component.literal("Stage " + currentPhase.getId() + ": " + currentPhase.getName()));
        }

        // Marks the saved data as changed. Minecraft will now auto-save these stats to the HDD periodically.
        com.infiniteoneblock.event.ModStateSaver.save(world.getServer());
    }
}
