package com.infiniteoneblock.event;

import com.infiniteoneblock.island.IslandManager;

public class BlockPlaceHandler {

    private final IslandManager islandManager;

    public BlockPlaceHandler(
            IslandManager islandManager
    ) {
        this.islandManager = islandManager;
    }

    public void register() {

        /*
         * Normal Minecraft block placement is allowed.
         *
         * There is currently no artificial island boundary.
         *
         * Players are expected to build their island naturally
         * using blocks obtained from the One Block.
         *
         * A proper island boundary/level system will be added
         * later without preventing normal island building.
         */
    }
}
