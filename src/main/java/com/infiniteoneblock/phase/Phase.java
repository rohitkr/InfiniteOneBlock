        package com.infiniteoneblock.phase;

import com.infiniteoneblock.oneblock.BlockPool;

public class Phase {

    private final int id;
    private final String name;
    private final BlockPool blockPool;

    public Phase(
            int id,
            String name,
            BlockPool blockPool
    ) {
        this.id = id;
        this.name = name;
        this.blockPool = blockPool;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BlockPool getBlockPool() {
        return blockPool;
    }
}
