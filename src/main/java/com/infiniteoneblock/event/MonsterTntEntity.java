package com.infiniteoneblock.event;

import com.infiniteoneblock.island.Island;
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
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MonsterTntEntity extends PrimedTnt {
    private final int stage;
    private final OneBlockManager manager;
    private final Island island;

    @SuppressWarnings("unchecked")
    public MonsterTntEntity(Level level, double x, double y, double z, int stage, OneBlockManager manager, Island island) {
        super((EntityType<? extends PrimedTnt>) BuiltInRegistries.ENTITY_TYPE.get(
                                Identifier.fromNamespaceAndPath("minecraft", "tnt"))
                        .map(Holder::value)
                        .orElseThrow(() -> new IllegalStateException("Core TNT Entity Type Registry missing!")),
                level);

        this.setPos(x, y, z);
        this.setFuse(60);
        this.stage = stage;
        this.manager = manager;
        this.island = island;
    }

    @Override
    public BlockState getBlockState() {
        return Blocks.CRYING_OBSIDIAN.defaultBlockState();
    }

    @Override
    public void tick() {
        if (this.getFuse() <= 1 && !this.level().isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) this.level();
            BlockPos spawnPos = this.blockPosition();

            serverWorld.playSound(null, spawnPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            serverWorld.sendParticles(ParticleTypes.EXPLOSION_EMITTER, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 1, 0.0D, 0.0D, 0.0D, 0.0D);

            manager.onMonsterTntExplode(serverWorld, spawnPos, this.stage, this.island);

            this.discard();
            return;
        }

        super.tick();
    }
}
