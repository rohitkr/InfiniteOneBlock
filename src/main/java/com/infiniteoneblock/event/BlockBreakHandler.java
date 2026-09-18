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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

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
                    return false;
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

        // 1. Force Item Drop Capture into Inventory
        LootParams.Builder lootBuilder = new LootParams.Builder(world)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, world.getBlockEntity(pos));

        List<ItemStack> drops = brokenState.getDrops(lootBuilder);
        for (ItemStack drop : drops) {
            if (!player.getInventory().add(drop)) {
                Block.popResource(world, player.blockPosition().above(), drop);
            }
        }

        int blocksMined = island.incrementBlocksMined();

        if (brokenState.is(Blocks.OAK_LOG)) {
            int oakLogs = island.incrementOakLogsCollected();
            player.sendSystemMessage(Component.literal("Oak Logs: " + oakLogs + "/6"));

            if (oakLogs == 6) {
                player.sendSystemMessage(Component.literal("Stage 2 unlocked!"));
                player.sendSystemMessage(Component.literal("Underground blocks are now available."));
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

        /*
         * FIX BUG: SPONDING CHEST RESTORATION
         * Evaluate the chest spawn sequence directly on the brokenState BEFORE clearing it to AIR.
         */
        boolean spawnedChest = rewardManager.trySpawnSupplyChest(
                player,
                world,
                pos
        );

        if (!spawnedChest) {
            /*
             * FIX BUG: SAND/GRAVEL GRAVITY COLLAPSE
             * To completely stop sand and gravel from falling into the void, we place a
             * permanent invisible structural block (like a Barrier) or Bedrock directly underneath
             * the OneBlock coordinate space before regeneration fires.
             */
            BlockPos supportPos = pos.below();
            if (world.getBlockState(supportPos).isAir()) {
                // Places a solid barrier block underneath so sand/gravel has structural friction
                world.setBlockAndUpdate(supportPos, Blocks.BARRIER.defaultBlockState());
            }

            // Regenerate the tile safely now that the floor is secured
            oneBlockManager.regenerate(island);
        }

        // Fallback safety valve
        if (world.getBlockState(pos).isAir()) {
            world.setBlockAndUpdate(pos, Blocks.BEDROCK.defaultBlockState());
        }

        // Cleanly restore player gravity settings on the next processing loop frame
        if (isStandingOnBlock) {
            world.getServer().execute(() -> {
                player.setNoGravity(false);
            });
        }

        if (blocksMined % 10 == 0) {
            player.sendSystemMessage(Component.literal("One Block: " + blocksMined + " blocks mined."));
            player.sendSystemMessage(Component.literal("Stage " + currentPhase.getId() + ": " + currentPhase.getName()));
        }
    }
}
