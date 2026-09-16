        package com.infiniteoneblock.event;

import com.infiniteoneblock.island.IslandManager;

public class IslandProtectionHandler {

    private final IslandManager islandManager;

    public IslandProtectionHandler(
            IslandManager islandManager
    ) {
        this.islandManager = islandManager;
    }

    public void register() {

        /*
         * Island break protection is handled directly
         * by BlockBreakHandler.
         *
         * This class is intentionally kept as a separate
         * system so we can add other island protections
         * later without having multiple block-break
         * listeners fighting each other.
         */
    }
}
