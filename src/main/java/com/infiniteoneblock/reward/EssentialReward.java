package com.infiniteoneblock.reward;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EssentialReward {
    WATER_BUCKET(0, 1, "Water Bucket"),
    OAK_SAPLING(1, 1, "Oak Sapling"),
    LAVA_BUCKET(2, 2, "Lava Bucket"),
    FLINT_AND_STEEL(3, 2, "Flint and Steel");

    private final int bit;
    private final int minStage;
    private final String displayName;

    EssentialReward(int bit, int minStage, String displayName) {
        this.bit = bit;
        this.minStage = minStage;
        this.displayName = displayName;
    }

    public int bitMask() {
        return 1 << bit;
    }

    public int minStage() {
        return minStage;
    }

    public String displayName() {
        return displayName;
    }

    public ItemStack createStack() {
        return switch (this) {
            case WATER_BUCKET -> new ItemStack(Items.WATER_BUCKET);
            case OAK_SAPLING -> new ItemStack(Items.OAK_SAPLING, 2);
            case LAVA_BUCKET -> new ItemStack(Items.LAVA_BUCKET);
            case FLINT_AND_STEEL -> new ItemStack(Items.FLINT_AND_STEEL);
        };
    }
}
