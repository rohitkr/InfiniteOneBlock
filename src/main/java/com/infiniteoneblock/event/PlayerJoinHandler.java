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
        ServerLevel currentLevel = (ServerLevel) player.level();
        MinecraftServer server = currentLevel.getServer();
        if (server == null) {
            return;
        }

        ServerLevel voidWorld = server.getLevel(InfiniteOneBlock.ONEBLOCK_WORLD_KEY);
        if (voidWorld == null) {
            System.out.println("[InfiniteOneBlock] ERROR: OneBlock void dimension not found! Verify your dimension JSON files.");
            return;
        }

        boolean returningPlayer = ModStateSaver.hasSavedProgress(player.getUUID())
                || player.level().dimension().equals(InfiniteOneBlock.ONEBLOCK_WORLD_KEY);

        Island island = islandManager.ensureIsland(player.getUUID(), voidWorld);
        island.updateGravitySupport(voidWorld.getBlockState(island.getOneBlockPosition()));

        if (voidWorld.getBlockState(island.getOneBlockPosition()).isAir()) {
            if (returningPlayer) {
                InfiniteOneBlock.getOneBlockManager().validateAndRepairBlock(island);
            } else {
                voidWorld.setBlockAndUpdate(
                        island.getOneBlockPosition(),
                        Blocks.GRASS_BLOCK.defaultBlockState()
                );
            }
        }

        if (returningPlayer) {
            ModStateSaver.rememberPlayer(player.getUUID());
            return;
        }

        BlockPos spawn = island.getSpawnPosition();
        TeleportTransition islandTransition = new TeleportTransition(
                voidWorld,
                new Vec3(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                Set.of(),
                TeleportTransition.DO_NOTHING
        );
        player.teleport(islandTransition);
        player.setGameMode(GameType.SURVIVAL);

        ModStateSaver.rememberPlayer(player.getUUID());
        ModStateSaver.save(server);

        player.sendSystemMessage(Component.literal("Welcome to Infinite OneBlock!"));
        player.sendSystemMessage(Component.literal("Mine the block beneath you to begin."));
    }
}
