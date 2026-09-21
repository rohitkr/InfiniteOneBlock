		package com.infiniteoneblock;

import com.infiniteoneblock.event.BlockBreakHandler;
import com.infiniteoneblock.event.BlockPlaceHandler;
import com.infiniteoneblock.event.IslandProtectionHandler;
import com.infiniteoneblock.event.PlayerJoinHandler;
import com.infiniteoneblock.event.PlayerRespawnHandler;
import com.infiniteoneblock.island.IslandManager;
import com.infiniteoneblock.oneblock.OneBlockManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier; // Use Identifier instead of ResourceLocation
import net.minecraft.world.level.Level;


public class InfiniteOneBlock implements ModInitializer {

	public static final String MOD_ID =
			"infiniteoneblock";

	private static IslandManager islandManager;
	private static OneBlockManager oneBlockManager;

	// ─── UPDATED FOR 26.2 ───
	public static final ResourceKey<Level> ONEBLOCK_WORLD_KEY = ResourceKey.create(
			Registries.DIMENSION,
			Identifier.fromNamespaceAndPath(MOD_ID, "oneblock_world")
	);

	@Override
	public void onInitialize() {

		islandManager =
				new IslandManager();

		oneBlockManager =
				new OneBlockManager();

		/*
		 * Minecraft's tags are not bound during normal
		 * mod initialization.
		 *
		 * Therefore the block tier system is initialized
		 * when the server is actually ready.
		 */
		ServerLifecycleEvents.SERVER_STARTED.register(
				server -> {

					System.out.println(
							"[InfiniteOneBlock] Server started."
					);

					oneBlockManager.initialize();
					// ─── ADD THIS LINE HERE ───
					// This reads the stored NBT stats from your HDD immediately on server boot
//					com.infiniteoneblock.event.ModStateSaver.load(server);
				}
		);

//		ServerLifecycleEvents.SERVER_STARTED.register(
//				server -> {
//
//					System.out.println(
//							"[InfiniteOneBlock] Server started."
//					);
//
//					oneBlockManager.initialize();
//
//					// ✅ ADD THIS HERE (inside lambda)
//					server.getAllLevels().forEach(level -> {
//						System.out.println(
//								"[InfiniteOneBlock] Found level: " + level
//						);
//					});
//				}
//		);

		PlayerJoinHandler playerJoinHandler =
				new PlayerJoinHandler(
						islandManager
				);

		ServerPlayerEvents.JOIN.register(
				playerJoinHandler::onPlayerJoin
		);

		PlayerRespawnHandler playerRespawnHandler =
				new PlayerRespawnHandler(
						islandManager,
						oneBlockManager
				);

		playerRespawnHandler.register();

		BlockBreakHandler blockBreakHandler =
				new BlockBreakHandler(
						islandManager,
						oneBlockManager
				);

		blockBreakHandler.register();

		IslandProtectionHandler islandProtectionHandler =
				new IslandProtectionHandler(
						islandManager
				);

		islandProtectionHandler.register();

		BlockPlaceHandler blockPlaceHandler =
				new BlockPlaceHandler(
						islandManager
				);

		blockPlaceHandler.register();

		net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			// When a primed Creeper or TNT entity vanishes/explodes from the world layer
			if (entity instanceof net.minecraft.world.entity.monster.Creeper || entity instanceof net.minecraft.world.entity.item.PrimedTnt) {
				var islandManager = InfiniteOneBlock.getIslandManager();
				if (islandManager != null) {
					// Query online players to see if their specific OneBlock was caught in the blast radius
					for (net.minecraft.server.level.ServerPlayer player : world.getServer().getPlayerList().getPlayers()) {
						com.infiniteoneblock.island.Island island = islandManager.getIsland(player.getUUID());
						if (island != null) {
							// Instantly runs our self-healing logic to replace the block if it went missing
							this.oneBlockManager.validateAndRepairBlock(island);
						}
					}
				}
			}
		});


		System.out.println(
				"========================================"
		);

		System.out.println(
				"       Infinite OneBlock loaded!"
		);

		System.out.println(
				"       Mod ID: " + MOD_ID
		);

		System.out.println(
				"       Island manager initialized!"
		);

		System.out.println(
				"       One Block manager initialized!"
		);

		System.out.println(
				"       Player join handler registered!"
		);

		System.out.println(
				"       Player respawn handler registered!"
		);

		System.out.println(
				"       One Block break handler registered!"
		);

		System.out.println(
				"       Island protection initialized!"
		);

		System.out.println(
				"       Normal block placement enabled!"
		);

		System.out.println(
				"========================================"
		);
	}

	public static IslandManager getIslandManager() {
		return islandManager;
	}

	public static OneBlockManager getOneBlockManager() {
		return oneBlockManager;
	}
}
