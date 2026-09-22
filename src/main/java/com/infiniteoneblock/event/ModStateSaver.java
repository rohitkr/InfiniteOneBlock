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
                    String[] values = parts[1].trim().split(",");

                    Island island = islandManager.getIsland(uuid);
                    if (island == null) {
                        continue;
                    }

                    island.setBlocksMined(Integer.parseInt(values[0].trim()));
                    if (values.length > 1) {
                        island.setOakLogsCollected(Integer.parseInt(values[1].trim()));
                    }
                    if (values.length > 2) {
                        island.setSpawnedGuardian(parseFlag(values[2]));
                    }
                    if (values.length > 3) {
                        island.setSpawnedWitherSkeleton(parseFlag(values[3]));
                    }
                    if (values.length > 4) {
                        island.setSpawnedWarden(parseFlag(values[4]));
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
                    writer.println(
                            player.getUUID()
                                    + "="
                                    + island.getBlocksMined()
                                    + ","
                                    + island.getOakLogsCollected()
                                    + ","
                                    + (island.hasSpawnedGuardian() ? 1 : 0)
                                    + ","
                                    + (island.hasSpawnedWitherSkeleton() ? 1 : 0)
                                    + ","
                                    + (island.hasSpawnedWarden() ? 1 : 0)
                    );
                }
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("[InfiniteOneBlock] Error writing file rules: " + e.getMessage());
        }
    }

    private static boolean parseFlag(String value) {
        return "1".equals(value.trim()) || Boolean.parseBoolean(value.trim());
    }
}
