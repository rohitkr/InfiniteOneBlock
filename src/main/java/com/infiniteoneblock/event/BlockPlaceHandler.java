package com.infiniteoneblock.event;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public class BlockPlaceHandler {

    private final IslandManager islandManager;

    public BlockPlaceHandler(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            Island island = islandManager.getIsland(serverPlayer.getUUID());
            if (island == null) {
                return InteractionResult.PASS;
            }

            BlockPos supportPos = island.getSupportPosition();
            if (!world.getBlockState(supportPos).is(Blocks.BARRIER)) {
                return InteractionResult.PASS;
            }

            boolean targetingSupport = hitResult.getBlockPos().equals(supportPos)
                    || (hitResult.getBlockPos().equals(island.getOneBlockPosition())
                    && hitResult.getDirection() == Direction.DOWN);
            if (!targetingSupport) {
                return InteractionResult.PASS;
            }

            ItemStack stack = serverPlayer.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem blockItem)) {
                return InteractionResult.PASS;
            }

            BlockPlaceContext context = new BlockPlaceContext(
                    serverPlayer,
                    hand,
                    stack,
                    new BlockHitResult(
                            hitResult.getLocation(),
                            Direction.DOWN,
                            island.getOneBlockPosition(),
                            hitResult.isInside()
                    )
            );
            var toPlace = blockItem.getBlock().getStateForPlacement(context);
            if (toPlace == null) {
                return InteractionResult.PASS;
            }

            world.setBlockAndUpdate(supportPos, toPlace);
            if (serverPlayer.gameMode.getGameModeForPlayer().isSurvival()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        });
    }
}
