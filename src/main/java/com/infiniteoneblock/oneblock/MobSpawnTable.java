package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.island.Island;

import java.util.List;
import java.util.Random;

public class MobSpawnTable {

    public static final int WITCH_CHANCE = 5;
    public static final int STAGE_1_ANIMAL_CHANCE = 40;
    public static final int SUPER_MOB_CHANCE = 35;
    public static final int WAVE_MIN = 2;
    public static final int WAVE_MAX = 4;

    private static final List<String> DIRECT_HOSTILES = List.of(
            "zombie",
            "skeleton",
            "creeper",
            "enderman"
    );

    private static final List<String> TNT_WAVE_MOBS = List.of(
            "zombie",
            "skeleton"
    );

    private static final List<String> STAGE_1_ANIMALS = List.of(
            "chicken",
            "pig",
            "sheep",
            "cow"
    );

    private static final List<SuperMob> SUPER_MOBS = List.of(
            SuperMob.GUARDIAN,
            SuperMob.WITHER_SKELETON,
            SuperMob.WARDEN
    );

    public enum SuperMob {
        GUARDIAN(4, "guardian", "A Guardian has appeared! This super mob spawns only once."),
        WITHER_SKELETON(6, "wither_skeleton", "A Wither Skeleton has appeared! This super mob spawns only once."),
        WARDEN(7, "warden", "The Warden has broken out! This super mob spawns only once.");

        private final int minStage;
        private final String entityPath;
        private final String announceMessage;

        SuperMob(int minStage, String entityPath, String announceMessage) {
            this.minStage = minStage;
            this.entityPath = entityPath;
            this.announceMessage = announceMessage;
        }

        public int minStage() {
            return minStage;
        }

        public String entityPath() {
            return entityPath;
        }

        public String announceMessage() {
            return announceMessage;
        }

        public boolean needsWater() {
            return this == GUARDIAN;
        }

        public boolean hasSpawned(Island island) {
            if (island == null) {
                return true;
            }
            return switch (this) {
                case GUARDIAN -> island.hasSpawnedGuardian();
                case WITHER_SKELETON -> island.hasSpawnedWitherSkeleton();
                case WARDEN -> island.hasSpawnedWarden();
            };
        }

        public void markSpawned(Island island) {
            if (island == null) {
                return;
            }
            switch (this) {
                case GUARDIAN -> island.setSpawnedGuardian(true);
                case WITHER_SKELETON -> island.setSpawnedWitherSkeleton(true);
                case WARDEN -> island.setSpawnedWarden(true);
            }
        }
    }

    public static String pickDirectMob(int stage, boolean hostileOnly, Random random) {
        if (random.nextInt(100) < WITCH_CHANCE) {
            return "witch";
        }

        if (!hostileOnly && stage <= 1 && random.nextInt(100) < STAGE_1_ANIMAL_CHANCE) {
            return pick(STAGE_1_ANIMALS, random);
        }

        return pick(DIRECT_HOSTILES, random);
    }

    public static SuperMob nextEligibleSuper(int stage, Island island) {
        for (SuperMob superMob : SUPER_MOBS) {
            if (stage >= superMob.minStage() && !superMob.hasSpawned(island)) {
                return superMob;
            }
        }
        return null;
    }

    public static SuperMob rollTntSuper(int stage, Island island, Random random) {
        SuperMob eligible = nextEligibleSuper(stage, island);
        if (eligible == null) {
            return null;
        }
        if (random.nextInt(100) >= SUPER_MOB_CHANCE) {
            return null;
        }
        return eligible;
    }

    public static String pickTntWaveMob(Random random) {
        return pick(TNT_WAVE_MOBS, random);
    }

    public static int rollWaveCount(Random random) {
        return WAVE_MIN + random.nextInt((WAVE_MAX - WAVE_MIN) + 1);
    }

    private static String pick(List<String> options, Random random) {
        return options.get(random.nextInt(options.size()));
    }
}
