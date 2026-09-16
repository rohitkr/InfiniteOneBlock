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

public class InfiniteOneBlock implements ModInitializer {

	public static final String MOD_ID =
			"infiniteoneblock";

	private static IslandManager islandManager;
	private static OneBlockManager oneBlockManager;

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
				}
		);

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
