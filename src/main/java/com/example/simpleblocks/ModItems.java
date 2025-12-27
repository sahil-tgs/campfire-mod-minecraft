package com.example.simpleblocks;

import com.example.simpleblocks.item.FirewoodItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    
    // Firewood - Obtained by chopping logs on a stump
    public static final Item FIREWOOD = registerItem("firewood",
            new FirewoodItem(new FabricItemSettings()));
    
    /**
     * Registers an item
     */
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(SimpleBlocksMod.MOD_ID, name), item);
    }
    
    /**
     * Called to register all items
     */
    public static void registerItems() {
        SimpleBlocksMod.LOGGER.info("Registering items for " + SimpleBlocksMod.MOD_ID);
    }
}
