package com.infiniteoneblock.oneblock;

import com.infiniteoneblock.island.Island;
import com.infiniteoneblock.phase.Phase;
import com.infiniteoneblock.phase.PhaseManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
            EntityType<?> selectedMobType = pickDirectMobType(stage);

            if (selectedMobType != null) {
                BlockPos spawnPos = position.above();
                spawnPreparedMob(world, selectedMobType, spawnPos, island);
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

    public void onMonsterTntExplode(ServerLevel world, BlockPos spawnPos, int stage, Island island) {
        MobSpawnTable.SuperMob superMob = MobSpawnTable.rollTntSuper(stage, island, random);
        if (superMob != null) {
            spawnSuperMob(world, spawnPos, island, superMob);
            return;
        }

        spawnTntWave(world, spawnPos, island);
    }

    private void spawnSuperMob(ServerLevel world, BlockPos spawnPos, Island island, MobSpawnTable.SuperMob superMob) {
        EntityType<?> type = getMobType(superMob.entityPath());
        if (type == null) {
            spawnTntWave(world, spawnPos, island);
            return;
        }

        if (superMob.needsWater()) {
            placeGuardianWater(world, spawnPos, island);
        }

        spawnPreparedMob(world, type, spawnPos, island);
        superMob.markSpawned(island);
        announceSuperMob(island, superMob.announceMessage());
    }

    private void spawnTntWave(ServerLevel world, BlockPos spawnPos, Island island) {
        int count = MobSpawnTable.rollWaveCount(random);
        for (int i = 0; i < count; i++) {
            EntityType<?> type = getMobType(MobSpawnTable.pickTntWaveMob(random));
            if (type == null) {
                continue;
            }

            double offsetX = (random.nextDouble() - 0.5) * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 1.5;
            BlockPos scatteredSpawnPos = new BlockPos(
                    (int) (spawnPos.getX() + offsetX),
                    spawnPos.getY(),
                    (int) (spawnPos.getZ() + offsetZ)
            );
            spawnPreparedMob(world, type, scatteredSpawnPos, island);
        }
    }

    private void placeGuardianWater(ServerLevel world, BlockPos origin, Island island) {
        BlockPos oneBlock = island.getOneBlockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (pos.equals(oneBlock)) {
                        continue;
                    }
                    if (world.getBlockState(pos).isAir()) {
                        world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private EntityType<?> pickDirectMobType(int stage) {
        return getMobType(MobSpawnTable.pickDirectMob(stage, false, random));
    }

    private void spawnPreparedMob(ServerLevel world, EntityType<?> type, BlockPos spawnPos, Island island) {
        var entity = type.spawn(world, spawnPos, EntitySpawnReason.EVENT);
        if (!(entity instanceof Mob mob)) {
            return;
        }

        mob.setPersistenceRequired();
        ServerPlayer owner = getIslandOwner(island);
        if (owner != null) {
            mob.setTarget(owner);
        }
    }

    private ServerPlayer getIslandOwner(Island island) {
        if (island == null || island.getWorld() == null || island.getWorld().getServer() == null) {
            return null;
        }
        return island.getWorld().getServer().getPlayerList().getPlayer(island.getOwnerId());
    }

    /**
     * Unwraps the Optional Reference container natively required by Minecraft 26.2 registry maps.
     */
    public EntityType<?> getMobType(String path) {
        return BuiltInRegistries.ENTITY_TYPE.get(Identifier.fromNamespaceAndPath("minecraft", path))
                .map(Holder::value)
                .orElse(null);
    }

    /**
     * HIGH-PERFORMANCE BLOCK HEALING VALVE
     * Directly checks if the specific OneBlock coordinates have changed to Air.
     * If empty, it immediately generates the next progression tile.
     */
    public void validateAndRepairBlock(Island island) {
        ServerLevel world = island.getWorld();
        BlockPos position = island.getOneBlockPosition();

        if (world.getBlockState(position).isAir()) {
            System.out.println("[InfiniteOneBlock] Empty space detected at OneBlock coordinate! Regenerating instantly.");

            this.regenerate(island);

            com.infiniteoneblock.event.ModStateSaver.save(world.getServer());
        }
    }

    private void announceSuperMob(Island island, String message) {
        ServerPlayer player = getIslandOwner(island);
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
