package com.example.simpleblocks;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

public class ModItemGroups {
    
    public static void registerItemGroups() {
        // Add our items to the Building Blocks creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
            content.add(ModBlocks.STUMP);
            content.add(ModBlocks.FIREWOOD_STACK);
            content.add(ModBlocks.UNLIT_CAMPFIRE);
        });
        
        // Add firewood item to the Ingredients tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
            content.add(ModItems.FIREWOOD);
        });
        
        SimpleBlocksMod.LOGGER.info("Registered item groups for " + SimpleBlocksMod.MOD_ID);
    }
}

