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
        return blockGenerator.generateNextBlock(stage);
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
        int roll = random.nextInt(100);

        if (stage == 1) {
            if (!hostileOnly && roll < 3) return getMobType("villager");
            if (roll < 8 || hostileOnly) return getMobType("zombie");
            if (roll < 45) return getMobType("chicken");
            if (roll < 75) return getMobType("pig");
            return getMobType("sheep");
        }

        if (stage == 2) {
            if (roll < 40) return getMobType("zombie");
            if (roll < 75) return getMobType("skeleton");
            if (roll < 95 || hostileOnly) return getMobType("creeper");
            return hostileOnly ? getMobType("zombie") : getMobType("cow");
        }

        if (stage == 3) {
            if (roll < 20) return getMobType("stray");
            if (roll < 60 || hostileOnly) return getMobType("skeleton");
            return hostileOnly ? getMobType("stray") : getMobType("sheep");
        }

        if (stage == 4) {
            if (island != null && !island.hasSpawnedGuardian()) {
                island.setSpawnedGuardian(true);
                announceSuperMob(island, "A Guardian has appeared! This super mob spawns only once.");
                return getMobType("guardian");
            }
            if (roll < 70 || hostileOnly) return getMobType("drowned");
            return getMobType("cod");
        }

        if (stage == 5) {
            if (roll < 15) return getMobType("witch");
            if (roll < 60 || hostileOnly) return getMobType("slime");
            return hostileOnly ? getMobType("witch") : getMobType("cow");
        }

        if (stage == 6) {
            if (island != null && !island.hasSpawnedWitherSkeleton()) {
                island.setSpawnedWitherSkeleton(true);
                announceSuperMob(island, "A Wither Skeleton has appeared! This super mob spawns only once.");
                return getMobType("wither_skeleton");
            }
            if (roll < 40) return getMobType("piglin");
            if (roll < 75) return getMobType("zombified_piglin");
            return getMobType("blaze");
        }

        if (island != null && !island.hasSpawnedWarden()) {
            island.setSpawnedWarden(true);
            announceSuperMob(island, "The Warden has broken out! This super mob spawns only once.");
            return getMobType("warden");
        }
        if (roll < 70) return getMobType("enderman");
        return getMobType("silverfish");
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
