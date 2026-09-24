package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.phase.PhaseManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.validation.PathAllowList;
import com.infiniteoneblock.event.MonsterTntEntity;

import java.util.Random;

public class OneBlockManager {

    private final PhaseManager phaseManager;
    private final BlockGenerator blockGenerator;
    private final Random random = new Random();

    public OneBlockManager() {
        phaseManager = new PhaseManager();
        blockGenerator = new BlockGenerator(phaseManager);
    }

    public void initialize() {
        phaseManager.initialize();
    }

    public Block getNextBlock(Island island) {
        int stage = getCurrentStage(island);
        Block block = blockGenerator.generateNextBlock(stage, island);

        // Prerequisite check for Stage 2 Iron Ore
        if (stage == 2 && block == Blocks.IRON_ORE) {
            if (island.getCobblestoneCollected() < 15) {
                // Fallback to Cobblestone if prerequisites aren't met
                return Blocks.COBBLESTONE;
            }
        }

        return block;
    }

    public void regenerate(Island island) {
        ServerLevel world = island.getWorld();
        BlockPos position = island.getOneBlockPosition();
        int stage = getCurrentStage(island);

        System.out.println("Current Stage: " + stage);
        if (stage > 2 && random.nextInt(100) < 3) {
            MonsterTntEntity tnt = new MonsterTntEntity(
                    world,
                    position.getX() + 0.5,
                    position.getY() + 1.0,
                    position.getZ() + 0.5,
                    stage,
                    this,
                    island
            );
            world.addFreshEntity(tnt);
            placeStableOneBlock(island, world, position, Blocks.GRASS_BLOCK.defaultBlockState());
            return;
        }

        if (random.nextInt(100) < (3 + stage)) {
                EntityType<?> selectedMobType = getMobTypeForStage(stage, island, false);

                if (selectedMobType != null) {
                    BlockPos spawnPos = position.above();

                    var entity = selectedMobType.spawn(world, spawnPos, EntitySpawnReason.EVENT);
                    if (entity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }

                    placeStableOneBlock(island, world, position, Blocks.GRASS_BLOCK.defaultBlockState());
                    return;
                }
        }

        Block nextBlock = getNextBlock(island);
        BlockState nextState = nextBlock.defaultBlockState();

        island.updateGravitySupport(nextState);
        if (nextState.getFluidState().isEmpty() && nextState.canSurvive(world, position)) {
            world.setBlock(position, nextState, 3);
            return;
        }

        System.out.println("[InfiniteOneBlock] Invalid OneBlock candidate: " + nextBlock);
        placeStableOneBlock(island, world, position, Blocks.STONE.defaultBlockState());
    }

    private void placeStableOneBlock(Island island, ServerLevel world, BlockPos position, BlockState state) {
        island.updateGravitySupport(state);
        world.setBlock(position, state, 3);
    }

    /**
     * TNT rendering logic to spawn the mobs on explosion
     * render the tnt
     */
    private Boolean getTNTRender (int stage, ServerLevel world, BlockPos position) {
        net.minecraft.world.entity.item.PrimedTnt tnt = new net.minecraft.world.entity.item.PrimedTnt(world, position.getX() + 0.5, position.getY(), position.getZ() + 0.5, null);
        // 2. Set the fuse timer in ticks (20 ticks = 1 second. 60 ticks = 3 seconds to run away!)
        tnt.setFuse(300);

        // 3. Attach a custom text tag to this specific TNT so our explosion system knows it contains mobs
        tnt.addTag("OneBlockMobTNT_Stage_" + stage);

        // 4. Force inject the primed animating TNT entity into the world map
        world.addFreshEntity(tnt);

        // Place a safe stone base block so the player has something to stand on while running
        world.setBlock(position, Blocks.COBBLESTONE.defaultBlockState(), 3);
        return true;
    }

    public EntityType<?> getMob(int stage) {
        return getMobType("creeper");
    }

    /**
     * ✅ HIGH-PERFORMANCE BLOCK HEALING VALVE
     * Directly checks if the specific OneBlock coordinates have changed to Air.
     * If empty, it immediately generates the next progression tile.
     */
    public void validateAndRepairBlock(Island island) {
        ServerLevel world = island.getWorld();
        BlockPos position = island.getOneBlockPosition();

        // Check if the block at the island coordinate was turned to air (by Creepers, TNT, or blocks breaking)
        if (world.getBlockState(position).isAir()) {
            System.out.println("[InfiniteOneBlock] Empty space detected at OneBlock coordinate! Regenerating instantly.");

            // Re-run your verified block regeneration system
            this.regenerate(island);

            // Force save to disk so data matches the new block placement metrics
            com.infiniteoneblock.event.ModStateSaver.save(world.getServer());
        }
    }

    /**
     * Unwraps the Optional Reference container natively required by Minecraft 26.2 registry maps.
     */
    public EntityType<?> getMobType(String path) {
        return BuiltInRegistries.ENTITY_TYPE.get(Identifier.fromNamespaceAndPath("minecraft", path))
                .map(Holder::value)
                .orElse(null);
    }

    // Updated signature to take the Island object so we can read boss tracking states
    public EntityType<?> getMobTypeForStage(int stage, Island island) {
        return getMobTypeForStage(stage, island, false);
    }

    public EntityType<?> getMobTypeForStage(int stage, Island island, boolean hostileOnly) {
        // --- 1. CRITICAL RULES: SUPER MOBS (Do Not Change) ---
        if (stage == 4 && island != null && !island.hasSpawnedGuardian()) {
            island.setSpawnedGuardian(true);
            announceSuperMob(island, "A Guardian has appeared! This super mob spawns only once.");
            return getMobType("elder_guardian");
        }
        if (stage == 6 && island != null && !island.hasSpawnedWitherSkeleton()) {
            island.setSpawnedWitherSkeleton(true);
            announceSuperMob(island, "A Wither Skeleton has appeared! This super mob spawns only once.");
            return getMobType("wither");
        }
        if (stage >= 7 && island != null && !island.hasSpawnedWarden()) {
            island.setSpawnedWarden(true);
            announceSuperMob(island, "The Warden has broken out! This super mob spawns only once.");
            return getMobType("warden");
        }

        // --- 2. DEFINE THE SCALED ANIMAL PROBABILITIES PER STAGE ---
        int animalChance = 0; // Out of 100%

        if (!hostileOnly) {
            animalChance = switch (stage) {
                case 1  -> 85; // Lower stage: Heavily weights animals (85% animal, 15% monster)
                case 2  -> 60; // 60% animal
                case 3  -> 50; // 50% animal
                case 4  -> 40; // 40% animal
                case 5  -> 35; // 35% animal
                case 6  -> 30; // 30% animal
                default -> 15; // Upper stage: Monster heavy (15% animal, 90% monster)
            };
        }

        // --- 3. EXECUTE THE ACCESSIBLE RANDOM ROLL ---
        int poolRoll = random.nextInt(100);
        int subRoll = random.nextInt(100);

        // Roll for an Animal (Guaranteed baseline scaling)
        if (poolRoll < animalChance) {
            if (subRoll < 25) return getMobType("chicken");
            if (subRoll < 50) return getMobType("pig");
            if (subRoll < 75) return getMobType("sheep");
            return getMobType("cow");
        }

        // Roll for a Basic Monster (Guaranteed availability across ALL stages)
        else {
            // Stage-based theme optimization (e.g., adding nether/end themes into upper stages)
            if (stage >= 6) {
                if (subRoll < 10) return getMobType("piglin");
                if (subRoll < 30) return getMobType("zombified_piglin");
                if (subRoll < 70) return getMobType("wither_skeleton");
                if (subRoll < 80) return getMobType("phantom");
                if (subRoll < 90) return getMobType("ghast");
                // if (subRoll < 85) return getMobType("blaze");
                return getMobType("enderman");
            }
            if (stage == 4) {
                if (subRoll < 30) return getMobType("zombie");
                if (subRoll < 70) return getMobType("skeleton");
                if (subRoll < 80) return getMobType("creeper");
                return getMobType("zombie");
            }
            if (stage == 3) {
                if (subRoll < 40) return getMobType("stray");
                if (subRoll < 75) return getMobType("skeleton");
                return getMobType("creeper");
            }

            // Universal basic monster fallback list for all stages
            if (subRoll < 25) return getMobType("zombie");
            if (subRoll < 45) return getMobType("skeleton");
            if (subRoll < 60) return getMobType("creeper");
            if (subRoll < 90) return getMobType("spider");
            return getMobType("enderman");
        }
    }

    private void announceSuperMob(Island island, String message) {
        var server = island.getWorld().getServer();
        if (server == null) {
            return;
        }
        var player = server.getPlayerList().getPlayer(island.getOwnerId());
        if (player != null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
        }
    }

    public Phase getCurrentPhase(Island island) {
        return blockGenerator.getCurrentPhase(getCurrentStage(island));
    }

    public int getCurrentStage(Island island) {
        int blocksMined = island.getBlocksMined();

        if (blocksMined < 80) return 1;
        if (blocksMined < 200) return 2;
        if (blocksMined < 400) return 3;
        if (blocksMined < 700) return 4;
        if (blocksMined < 1100) return 5;
        if (blocksMined < 1600) return 6;
        return 7;
    }
}
