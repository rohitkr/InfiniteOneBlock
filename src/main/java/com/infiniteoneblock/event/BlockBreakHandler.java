package com.infiniteoneblock.event;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;
import com.infiniteoneblock.oneblock.OneBlockManager;
import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.reward.OneBlockRewardManager;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBreakHandler {

    private final IslandManager islandManager;
    private final OneBlockManager oneBlockManager;
    private final OneBlockRewardManager rewardManager;

    public BlockBreakHandler(
            IslandManager islandManager,
            OneBlockManager oneBlockManager
    ) {
        this.islandManager =
                islandManager;

        this.oneBlockManager =
                oneBlockManager;

        this.rewardManager =
                new OneBlockRewardManager();
    }

    public void register() {

        PlayerBlockBreakEvents.AFTER.register(
                (world,
                 player,
                 pos,
                 state,
                 blockEntity) -> {

                    if (!(player instanceof ServerPlayer serverPlayer)) {
                        return;
                    }

                    Island island =
                            islandManager.getIsland(
                                    serverPlayer.getUUID()
                            );

                    if (island == null) {
                        return;
                    }

                    /*
                     * Only our special OneBlock triggers
                     * progression and rewards.
                     */
                    if (!pos.equals(
                            island.getOneBlockPosition()
                    )) {
                        return;
                    }

                    handleOneBlockBreak(
                            serverPlayer,
                            island,
                            state
                    );
                }
        );
    }

    private void handleOneBlockBreak(
            ServerPlayer player,
            Island island,
            BlockState brokenState
    ) {

        /*
         * Count successful OneBlock breaks.
         */
        int blocksMined =
                island.incrementBlocksMined();

        /*
         * Oak Log progression.
         */
        if (brokenState.is(Blocks.OAK_LOG)) {

            int oakLogs =
                    island.incrementOakLogsCollected();

            player.sendSystemMessage(
                    Component.literal(
                            "Oak Logs: "
                                    + oakLogs
                                    + "/6"
                    )
            );

            if (oakLogs == 6) {

                player.sendSystemMessage(
                        Component.literal(
                                "Stage 2 unlocked!"
                        )
                );

                player.sendSystemMessage(
                        Component.literal(
                                "Underground blocks are now available."
                        )
                );
            }
        }

        /*
         * Determine current phase.
         */
        Phase currentPhase =
                oneBlockManager.getCurrentPhase(
                        island
                );

        /*
         * Special supply chest.
         *
         * This is evaluated after the successful
         * OneBlock break.
         */
        rewardManager.trySpawnSupplyChest(
                player,
                island.getWorld(),
                island.getOneBlockPosition()
        );

        /*
         * Regenerate the OneBlock after Minecraft
         * has finished processing the broken block.
         *
         * ServerPlayer does not expose getServer()
         * in Minecraft 26.2 Mojang mappings.
         *
         * The island already knows its ServerLevel,
         * so obtain the MinecraftServer from there.
         */
        if (island.getWorld().getServer() != null) {

            island.getWorld().getServer().execute(
                    () -> {

                        Island currentIsland =
                                islandManager.getIsland(
                                        player.getUUID()
                                );

                        if (currentIsland == null) {
                            return;
                        }

                        oneBlockManager.regenerate(
                                currentIsland
                        );
                    }
            );
        }

        /*
         * Progress message every 10 blocks.
         */
        if (blocksMined % 10 == 0) {

            player.sendSystemMessage(
                    Component.literal(
                            "One Block: "
                                    + blocksMined
                                    + " blocks mined."
                    )
            );

            player.sendSystemMessage(
                    Component.literal(
                            "Stage "
                                    + currentPhase.getId()
                                    + ": "
                                    + currentPhase.getName()
                    )
            );
        }
    }
}