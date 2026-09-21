package com.infiniteoneblock.event;

import com.infiniteoneblock.InfiniteOneBlock;
import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;
import com.infiniteoneblock.oneblock.OneBlockManager;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class PlayerRespawnHandler {

    private final IslandManager islandManager;
    private final OneBlockManager oneBlockManager;

    public PlayerRespawnHandler(
            IslandManager islandManager,
            OneBlockManager oneBlockManager
    ) {
        this.islandManager = islandManager;
        this.oneBlockManager = oneBlockManager;
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

        /*
         * ─── ✅ FIXED 26.2 BED RESPAWN DETECTION ───
         * Using the exact method from the IDE dropdown: getRespawnConfig()
         * If the player has set a bed or anchor, this returns a configuration container.
         * If it's not null, we stop our code and let the vanilla bed spawn work!
         */
        if (newPlayer.getRespawnConfig() != null) {
            System.out.println("[InfiniteOneBlock] Player has a valid Bed/Anchor configuration. Skipping island force-spawn.");
            newPlayer.sendSystemMessage(Component.literal("You have respawned at your bed."));
            return;
        }

        Island island = islandManager.getIsland(newPlayer.getUUID());
        if (island == null) {
            System.out.println("[InfiniteOneBlock] No island found for respawned player.");
            return;
        }

        ServerLevel currentLevel = (ServerLevel) newPlayer.level();
        var server = currentLevel.getServer();
        if (server == null) return;

        ServerLevel voidWorld = server.getLevel(InfiniteOneBlock.ONEBLOCK_WORLD_KEY);
        if (voidWorld == null) {
            System.out.println("[InfiniteOneBlock] ERROR: Custom void level key could not be retrieved from engine.");
            return;
        }

        BlockPos oneBlockPosition = island.getOneBlockPosition();
        BlockState oneBlockState = voidWorld.getBlockState(oneBlockPosition);

        if (oneBlockState.isAir() || !oneBlockState.getFluidState().isEmpty()) {
            System.out.println("[InfiniteOneBlock] OneBlock was missing or was a fluid. Regenerating before respawn.");
            oneBlockManager.regenerate(island);
            oneBlockState = voidWorld.getBlockState(oneBlockPosition);
        }

        if (oneBlockState.isAir() || !oneBlockState.getFluidState().isEmpty()) {
            System.out.println("[InfiniteOneBlock] ERROR: Unable to restore a valid OneBlock before respawn.");
            newPlayer.sendSystemMessage(Component.literal("Your OneBlock could not be restored."));
            return;
        }

        BlockPos spawn = island.getSpawnPosition();
        Vec3 spawnVec = new Vec3(spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5);

        TeleportTransition respawnTransition = new TeleportTransition(
                voidWorld,
                spawnVec,
                Vec3.ZERO,
                newPlayer.getYRot(),
                newPlayer.getXRot(),
                Set.of(),
                TeleportTransition.DO_NOTHING
        );

        newPlayer.teleport(respawnTransition);
        newPlayer.sendSystemMessage(Component.literal("You have respawned on your island."));
    }
}
