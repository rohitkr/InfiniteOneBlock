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
        if (stage > 2 && random.nextInt(100) < 5) {
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
            // Keeps floor solid under the ticking fuse so infinite block doesn't vanish
            world.setBlock(position, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            return;
        }

        if (random.nextInt(100) < 20) {
            EntityType<?> selectedMobType = getMobTypeForStage(stage, island);

            if (selectedMobType != null) {
                BlockPos spawnPos = position.above();

                var entity = selectedMobType.spawn(world, spawnPos, EntitySpawnReason.EVENT);
                if (entity instanceof Mob mob) {
                    mob.setPersistenceRequired();
                }

                world.setBlock(position, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                return;
            }
        }

        Block nextBlock = getNextBlock(island);
        BlockState nextState = nextBlock.defaultBlockState();

        if (nextState.getFluidState().isEmpty() && nextState.canSurvive(world, position)) {
            world.setBlock(position, nextState, 3);
            return;
        }

        System.out.println("[InfiniteOneBlock] Invalid OneBlock candidate: " + nextBlock);
        world.setBlock(position, Blocks.STONE.defaultBlockState(), 3);
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
        int roll = random.nextInt(100);

        // ============================================================
        // 🟥 STAGE 1: Plains
        // ============================================================
        if (stage == 1) {
            if (roll < 3) return getMobType("villager");
            if (roll < 8) return getMobType("zombie"); // Baby zombie handled in TNT class
            if (roll < 45) return getMobType("chicken");
            if (roll < 75) return getMobType("pig");
            return getMobType("sheep");
        }

        // ============================================================
        // 🪨 STAGE 2: Underground (SAFE - Warden Completely Removed!)
        // ============================================================
        else if (stage == 2) {
            if (roll < 40) return getMobType("zombie");
            if (roll < 75) return getMobType("skeleton");
            if (roll < 95) return getMobType("creeper");
            return getMobType("cow");
        }

        // ============================================================
        // ❄️ STAGE 3: Winter
        // ============================================================
        else if (stage == 3) {
            if (roll < 20) return getMobType("stray");
            if (roll < 60) return getMobType("skeleton");
            return getMobType("sheep");
        }

        // ============================================================
        // 🌊 STAGE 4: Ocean (Elder Guardian SPAWNS ONLY ONCE)
        // ============================================================
        else if (stage == 4) {
            // If the roll lands on the boss slot AND this island has NEVER spawned it yet
            if (roll < 5 && island != null && !island.hasSpawnedStage4Boss()) {
                island.setSpawnedStage4Boss(true); // Lock it forever!
                System.out.println("[InfiniteOneBlock] BOSS WARNING: An Elder Guardian has risen from the deep!");
                return getMobType("elder_guardian");
            }
            // Fallback if boss already spawned or roll missed
            if (roll < 50) return getMobType("guardian");
            if (roll < 85) return getMobType("drowned");
            return getMobType("chicken");
        }

        // ============================================================
        // 🌴 STAGE 5: Jungle / Swamp
        // ============================================================
        else if (stage == 5) {
            if (roll < 15) return getMobType("witch");
            if (roll < 60) return getMobType("slime");
            return getMobType("cow");
        }

        // ============================================================
        // 🌋 STAGE 6: Nether (Wither Skeleton Grinding)
        // ============================================================
        else if (stage == 6) {
            if (roll < 25) return getMobType("wither_skeleton");
            if (roll < 60) return getMobType("piglin");
            if (roll < 90) return getMobType("zombified_piglin");
            return getMobType("blaze");
        }

        // ============================================================
        // 👁️ STAGE 7: Stronghold & End (Ultimate Warden Boss Spawns ONLY ONCE)
        // ============================================================
        else {
            // Warden shifted here as an ultimate endgame boss threat! Spawns ONLY ONCE.
            if (roll < 4 && island != null && !island.hasSpawnedStage7Boss()) {
                island.setSpawnedStage7Boss(true); // Lock it forever!
                System.out.println("[InfiniteOneBlock] BOSS WARNING: The Warden has broken out of the ancient portal!");
                return getMobType("warden");
            }
            if (roll < 60) return getMobType("enderman");
            return getMobType("shulker");
        }
    }

    public Phase getCurrentPhase(Island island) {
        return blockGenerator.getCurrentPhase(getCurrentStage(island));
    }

    public int getCurrentStage(Island island) {
        int blocksMined = island.getBlocksMined();
        int stage = 7;

        if (blocksMined < 50) stage = 1;
        else if (blocksMined < 100) stage = 2;
        else if (blocksMined < 300) stage = 3;
        else if (blocksMined < 600) stage = 4;
        else if (blocksMined < 1000) stage = 5;
        else if (blocksMined < 1500) stage = 6;

        return stage;
    }
}
