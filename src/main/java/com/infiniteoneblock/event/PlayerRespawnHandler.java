        package com.infiniteoneblock.event;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;
import com.infiniteoneblock.oneblock.OneBlockManager;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerRespawnHandler {

    private final IslandManager islandManager;
    private final OneBlockManager oneBlockManager;

    public PlayerRespawnHandler(
            IslandManager islandManager,
            OneBlockManager oneBlockManager
    ) {

        this.islandManager =
                islandManager;

        this.oneBlockManager =
                oneBlockManager;
    }

    public void register() {

        ServerPlayerEvents.AFTER_RESPAWN.register(
                this::onPlayerRespawn
        );
    }

    private void onPlayerRespawn(
            ServerPlayer oldPlayer,
            ServerPlayer newPlayer,
            boolean alive
    ) {

        System.out.println(
                "[InfiniteOneBlock] Player respawned: "
                        + newPlayer.getGameProfile().name()
        );

        Island island =
                islandManager.getIsland(
                        newPlayer.getUUID()
                );

        if (island == null) {

            System.out.println(
                    "[InfiniteOneBlock] No island found for respawned player."
            );

            return;
        }

        BlockPos oneBlockPosition =
                island.getOneBlockPosition();

        BlockState oneBlockState =
                island.getWorld().getBlockState(
                        oneBlockPosition
                );

        /*
         * The player must never respawn above
         * an empty or fluid OneBlock position.
         *
         * If the OneBlock is missing, regenerate it
         * before teleporting the player.
         */
        if (oneBlockState.isAir()
                || !oneBlockState.getFluidState().isEmpty()) {

            System.out.println(
                    "[InfiniteOneBlock] OneBlock was missing "
                            + "or was a fluid. Regenerating before respawn."
            );

            oneBlockManager.regenerate(
                    island
            );

            oneBlockState =
                    island.getWorld().getBlockState(
                            oneBlockPosition
                    );
        }

        /*
         * If something unexpected still happened and
         * the generated block is not usable, do not
         * teleport the player into the void.
         */
        if (oneBlockState.isAir()
                || !oneBlockState.getFluidState().isEmpty()) {

            System.out.println(
                    "[InfiniteOneBlock] ERROR: Unable to restore "
                            + "a valid OneBlock before respawn."
            );

            newPlayer.sendSystemMessage(
                    Component.literal(
                            "Your OneBlock could not be restored."
                    )
            );

            return;
        }

        BlockPos spawn =
                island.getSpawnPosition();

        newPlayer.teleportTo(
                spawn.getX() + 0.5,
                spawn.getY() + 0.5,
                spawn.getZ() + 0.5
        );

        newPlayer.sendSystemMessage(
                Component.literal(
                        "You have respawned on your island."
                )
        );

        System.out.println(
                "[InfiniteOneBlock] Player returned to island spawn at "
                        + spawn
        );
    }
}
