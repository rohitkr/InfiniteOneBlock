package com.infiniteoneblock.event;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.commands.Commands; // Required for modern permission checks
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class IslandProtectionHandler {

    private final IslandManager islandManager;
    private static final int PROTECTION_RADIUS = 100;

    public IslandProtectionHandler(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    public void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            // Target block position a player is clicking/building on
            BlockPos targetPos = hitResult.getBlockPos();

            // Fetch the acting player's personal island space using your verified manager method
            Island playerIsland = islandManager.getIsland(serverPlayer.getUUID());

            if (playerIsland == null) {
                return InteractionResult.PASS;
            }

            BlockPos centerPos = playerIsland.getOneBlockPosition();

            // Calculate taxicab distance block radius grid boundaries safely
            int distanceX = Math.abs(targetPos.getX() - centerPos.getX());
            int distanceZ = Math.abs(targetPos.getZ() - centerPos.getZ());

            // If a player tries to interact or build outside their 100-block boundary grid line
            if (distanceX > PROTECTION_RADIUS || distanceZ > PROTECTION_RADIUS) {

                /*
                 * ─── VERIFIED 26.2 PERMISSION CHECK ───
                 * In 26.2, hasPermission requires a PermissionCheck constant instead of an integer.
                 * LEVEL_GAMEMASTERS represents standard operator level 2 clearance.
                 */
                boolean isOp = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS).test(serverPlayer.createCommandSourceStack());

                if (serverPlayer.isCreative() && isOp) {
                    return InteractionResult.PASS;
                }

                serverPlayer.sendSystemMessage(
                        Component.literal("§cYou cannot build or interact outside your island protection zone!")
                );

                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }
}
