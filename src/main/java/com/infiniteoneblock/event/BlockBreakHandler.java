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
import net.minecraft.world.entity.EquipmentSlot; // Added for tool damage tracking

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

        /*
         * FIX BUG: TOOL DURABILITY LOSS
         * Since we cancel vanilla execution, manually inflict 1 point of block-break durability damage
         * onto the item held in the player's main hand, respecting unbreaking enchantments natively.
         */
        ItemStack tool = player.getMainHandItem();
        if (!tool.isEmpty() && player.gameMode.getGameModeForPlayer().isSurvival()) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
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

        int stage = oneBlockManager.getCurrentStage(island);

        // Evaluate the chest spawn sequence directly
        boolean spawnedChest = rewardManager.trySpawnSupplyChest(
                player,
                world,
                pos,
                stage
        );

        if (!spawnedChest) {
            // Sand/Gravel support floor handling
            BlockPos supportPos = pos.below();
            if (world.getBlockState(supportPos).isAir()) {
                world.setBlockAndUpdate(supportPos, Blocks.BARRIER.defaultBlockState());
            }

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
