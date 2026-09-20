package com.infiniteoneblock.event;

import com.infiniteoneblock.oneblock.OneBlockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MonsterTntEntity extends PrimedTnt {
    private final int stage;
    private final OneBlockManager manager;

    @SuppressWarnings("unchecked")
    public MonsterTntEntity(Level level, double x, double y, double z, int stage, OneBlockManager manager) {
        super((EntityType<? extends PrimedTnt>) BuiltInRegistries.ENTITY_TYPE.get(
                                Identifier.fromNamespaceAndPath("minecraft", "tnt"))
                        .map(Holder::value)
                        .orElseThrow(() -> new IllegalStateException("Core TNT Entity Type Registry missing!")),
                level);

        this.setPos(x, y, z);
        this.setFuse(60); // 3-second countdown animation
        this.stage = stage;
        this.manager = manager;
    }

    /*
     * ─── ✅ COSMETIC ENTITY OVERRIDE ───
     * This intercepts the client-side renderer. Instead of drawing a vanilla red TNT block,
     * it forces the floating entity to look like a Crying Obsidian block!
     */
    @Override
    public BlockState getBlockState() {
        return Blocks.CRYING_OBSIDIAN.defaultBlockState();
    }

    @Override
    public void tick() {
        // Intercept right before the fuse finishes to process our damage-free explosion mechanics
        if (this.getFuse() <= 1 && !this.level().isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) this.level();
            BlockPos spawnPos = this.blockPosition();

            // Play safe cosmetic audio and visual smoke explosion instead of breaking blocks
            serverWorld.playSound(null, spawnPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            serverWorld.sendParticles(ParticleTypes.EXPLOSION_EMITTER, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 1, 0.0D, 0.0D, 0.0D, 0.0D);

            // Spawn the stage-appropriate monster/animal
            EntityType<?> selectedMobType = manager.getMobTypeForStage(this.stage);
            if (selectedMobType != null) {
                java.util.Random rand = new java.util.Random();
                int count = 0;
                int limit = 1;

                if (selectedMobType.equals(manager.getMobType("zombie")) ||
                        selectedMobType.equals(manager.getMobType("skeleton")) ||
                        selectedMobType.equals(manager.getMobType("piglin")) ||
                        selectedMobType.equals(manager.getMobType("cow")) ||
                        selectedMobType.equals(manager.getMobType("pig")) ||
                        selectedMobType.equals(manager.getMobType("sheep"))
                ) {
                    limit = 4 + rand.nextInt(3);
                }

                while (count < limit) {
                    double offsetX = (rand.nextDouble() - 0.5) * 1.5;
                    double offsetZ = (rand.nextDouble() - 0.5) * 1.5;
                    BlockPos scatteredSpawnPos = new BlockPos(
                            (int) (spawnPos.getX() + offsetX),
                            spawnPos.getY(),
                            (int) (spawnPos.getZ() + offsetZ)
                    );

                    var entity = selectedMobType.spawn(serverWorld, scatteredSpawnPos, EntitySpawnReason.EVENT);
                    if (entity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                    }
                    count++;
                }
            }

            this.discard();
            return;
        }

        super.tick();
    }
}
