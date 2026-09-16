package com.infiniteoneblock.event;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class PlayerJoinHandler {

    private final IslandManager islandManager;

    public PlayerJoinHandler(
            IslandManager islandManager
    ) {
        this.islandManager = islandManager;
    }

    public void onPlayerJoin(ServerPlayer player) {

        System.out.println(
                "[InfiniteOneBlock] Player joined: "
                        + player.getGameProfile().name()
        );

        /*
         * If the player already has an island,
         * do not create another one.
         */
        if (islandManager.hasIsland(
                player.getUUID()
        )) {

            System.out.println(
                    "[InfiniteOneBlock] Player already has an island."
            );

            return;
        }

        /*
         * Make sure we are on a server world.
         */
        if (!(player.level() instanceof ServerLevel world)) {

            System.out.println(
                    "[InfiniteOneBlock] Player is not in a server world."
            );

            return;
        }

        /*
         * ========================================
         * CREATE ISLAND
         * ========================================
         *
         * The island consists of ONE BLOCK.
         */
        Island island =
                islandManager.createIsland(
                        player.getUUID(),
                        world
                );

        /*
         * ========================================
         * CREATE THE ONE BLOCK
         * ========================================
         */
        world.setBlockAndUpdate(
                island.getOneBlockPosition(),
                Blocks.GRASS_BLOCK.defaultBlockState()
        );

        /*
         * ========================================
         * TELEPORT PLAYER
         * ========================================
         *
         * Spawn the player directly above the
         * One Block.
         */
        BlockPos spawn =
                island.getSpawnPosition();

        player.teleportTo(
                spawn.getX() + 0.5,
                spawn.getY(),
                spawn.getZ() + 0.5
        );

        /*
         * ========================================
         * SURVIVAL MODE
         * ========================================
         */
        player.setGameMode(
                GameType.SURVIVAL
        );

        /*
         * ========================================
         * WELCOME MESSAGE
         * ========================================
         */
        player.sendSystemMessage(
                Component.literal(
                        "Welcome to Infinite OneBlock!"
                )
        );

        player.sendSystemMessage(
                Component.literal(
                        "Mine the block beneath you to begin."
                )
        );

        /*
         * ========================================
         * LOG
         * ========================================
         */
        System.out.println(
                "[InfiniteOneBlock] One Block created for "
                        + player.getGameProfile().name()
                        + " at "
                        + island.getOneBlockPosition()
        );
    }
}
