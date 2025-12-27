package com.example.simpleblocks;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleBlocksMod implements ModInitializer {
    public static final String MOD_ID = "immersive_campfire";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Immersive Campfire Mod!");
        
        // Register all mod content
        ModBlocks.registerBlocks();
        ModItems.registerItems();
        ModItemGroups.registerItemGroups();
        
        LOGGER.info("Immersive Campfire Mod initialized successfully!");
    }
}
