package com.infiniteoneblock.event;

import com.infiniteoneblock.InfiniteOneBlock;
import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class PlayerJoinHandler {

    private final IslandManager islandManager;

    public PlayerJoinHandler(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    public void onPlayerJoin(ServerPlayer player) {

        System.out.println(
                "[InfiniteOneBlock] Player joined: "
                        + player.getGameProfile().name()
        );

        // Modern 26.2 Server Level Extraction
        ServerLevel currentLevel = (ServerLevel) player.level();
        MinecraftServer server = currentLevel.getServer();

        if (server == null) return;

        // Retrieves the custom dimension via the 26.2 ResourceKey layout
        ServerLevel voidWorld = server.getLevel(InfiniteOneBlock.ONEBLOCK_WORLD_KEY);
        if (voidWorld == null) {
            System.out.println("[InfiniteOneBlock] ERROR: OneBlock void dimension not found! Verify your dimension JSON files.");
            return;
        }

        /*
         * If the player already has an island, make sure they route back to it safely.
         */
        if (islandManager.hasIsland(player.getUUID())) {
            System.out.println("[InfiniteOneBlock] Player already has an island.");

            if (!player.level().dimension().equals(InfiniteOneBlock.ONEBLOCK_WORLD_KEY)) {
                Island existingIsland = islandManager.getIsland(player.getUUID());
                if (existingIsland != null) {
                    BlockPos existingSpawn = existingIsland.getSpawnPosition();

                    TeleportTransition transition = new TeleportTransition(
                            voidWorld,
                            new Vec3(existingSpawn.getX() + 0.5, existingSpawn.getY(), existingSpawn.getZ() + 0.5),
                            Vec3.ZERO,
                            player.getYRot(),
                            player.getXRot(),
                            Set.of(),
                            TeleportTransition.DO_NOTHING
                    );
                    player.teleport(transition);
                }
            }
            return;
        }

        /*
         * ========================================
         * CREATE ISLAND ON VOID WORLD
         * ========================================
         */
        Island island = islandManager.createIsland(
                player.getUUID(),
                voidWorld
        );

        /*
         * Load disk counters directly AFTER the new island object container maps
         * into server memory. This ensures it applies to the new instance correctly.
         */
        com.infiniteoneblock.event.ModStateSaver.load(server);

        /*
         * ========================================
         * CREATE THE ONE BLOCK
         * ========================================
         */
        voidWorld.setBlockAndUpdate(
                island.getOneBlockPosition(),
                Blocks.GRASS_BLOCK.defaultBlockState()
        );

        /*
         * ========================================
         * TELEPORT PLAYER VIA MODERN TRANSITION
         * ========================================
         */
        BlockPos spawn = island.getSpawnPosition();
        Vec3 spawnVec = new Vec3(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);

        TeleportTransition islandTransition = new TeleportTransition(
                voidWorld,
                spawnVec,
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                Set.of(),
                TeleportTransition.DO_NOTHING
        );

        player.teleport(islandTransition);

        /*
         * ========================================
         * SURVIVAL MODE & MESSAGES
         * ========================================
         */
        player.setGameMode(GameType.SURVIVAL);

        player.sendSystemMessage(Component.literal("Welcome to Infinite OneBlock!"));
        player.sendSystemMessage(Component.literal("Mine the block beneath you to begin."));

        System.out.println(
                "[InfiniteOneBlock] One Block created for "
                        + player.getGameProfile().name()
                        + " at "
                        + island.getOneBlockPosition()
        );
    }
}
