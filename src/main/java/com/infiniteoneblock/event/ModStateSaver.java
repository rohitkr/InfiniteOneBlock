package com.infiniteoneblock.event;

import com.infiniteoneblock.InfiniteOneBlock;
import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.island.IslandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModStateSaver {

    private static final Set<UUID> knownPlayers = new HashSet<>();

    public static boolean hasSavedProgress(UUID playerId) {
        return knownPlayers.contains(playerId);
    }

    public static void rememberPlayer(UUID playerId) {
        knownPlayers.add(playerId);
    }

    public static void load(MinecraftServer server) {
        knownPlayers.clear();
        File file = progressFile(server);
        if (!file.exists()) {
            return;
        }

        IslandManager islandManager = InfiniteOneBlock.getIslandManager();
        ServerLevel voidWorld = server.getLevel(InfiniteOneBlock.ONEBLOCK_WORLD_KEY);
        if (islandManager == null || voidWorld == null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || !line.contains("=")) {
                    continue;
                }

                String[] parts = line.split("=");
                if (parts.length < 2) {
                    continue;
                }

                try {
                    UUID uuid = UUID.fromString(parts[0].trim());
                    knownPlayers.add(uuid);

                    String[] values = parts[1].trim().split(",");
                    Island island = islandManager.ensureIsland(uuid, voidWorld);

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
                    if (values.length > 5) {
                        island.setGrantedEssentialsMask(Integer.parseInt(values[5].trim()));
                    }
                    if (values.length > 6) {
                        island.setCobblestoneCollected(Integer.parseInt(values[6].trim()));
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            System.out.println("[InfiniteOneBlock] Error reading island progress: " + e.getMessage());
        }
    }

    public static void save(MinecraftServer server) {
        IslandManager islandManager = InfiniteOneBlock.getIslandManager();
        if (islandManager == null) {
            return;
        }

        File file = progressFile(server);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            Set<UUID> written = new HashSet<>();
            for (Island island : islandManager.getAllIslands()) {
                written.add(island.getOwnerId());
                knownPlayers.add(island.getOwnerId());
                writer.println(formatIsland(island));
            }
            writer.flush();
        } catch (IOException e) {
            System.out.println("[InfiniteOneBlock] Error writing island progress: " + e.getMessage());
        }
    }

    private static String formatIsland(Island island) {
        return island.getOwnerId()
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
                + ","
                + island.getGrantedEssentialsMask()
                + ","
                + island.getCobblestoneCollected();
    }

    private static File progressFile(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("oneblock_progress.txt").toFile();
    }

    private static boolean parseFlag(String value) {
        return "1".equals(value.trim()) || Boolean.parseBoolean(value.trim());
    }
}
