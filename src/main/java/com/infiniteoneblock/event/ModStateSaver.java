package com.infiniteoneblock.event;

import com.infiniteoneblock.InfiniteOneBlock;
import com.infiniteoneblock.island.Island;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.util.UUID;

public class ModStateSaver {

    public static void load(MinecraftServer server) {
        File file = server.getWorldPath(LevelResource.ROOT).resolve("oneblock_progress.txt").toFile();
        if (!file.exists()) return;

        var islandManager = InfiniteOneBlock.getIslandManager();
        if (islandManager == null) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || !line.contains("=")) continue;

                String[] parts = line.split("=");
                if (parts.length < 2) continue;

                try {
                    UUID uuid = UUID.fromString(parts[0].trim());
                    int minedCount = Integer.parseInt(parts[1].trim());

                    Island island = islandManager.getIsland(uuid);
                    if (island != null) {
                        // Force synchronization parameters back into active variables
                        while (island.getBlocksMined() < minedCount) {
                            island.incrementBlocksMined();
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.out.println("[InfiniteOneBlock] Error reading file rules: " + e.getMessage());
        }
    }

    public static void save(MinecraftServer server) {
        var islandManager = InfiniteOneBlock.getIslandManager();
        if (islandManager == null) return;

        File file = server.getWorldPath(LevelResource.ROOT).resolve("oneblock_progress.txt").toFile();

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                Island island = islandManager.getIsland(player.getUUID());
                if (island != null) {
                    writer.println(player.getUUID().toString() + "=" + island.getBlocksMined());
                }
            }
            writer.flush(); // Forces writing down remaining byte blocks before context breaks
        } catch (IOException e) {
            System.out.println("[InfiniteOneBlock] Error writing file rules: " + e.getMessage());
        }
    }
}
