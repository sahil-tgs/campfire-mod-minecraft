package com.example.simpleblocks.item;

import com.example.simpleblocks.ModBlocks;
import com.example.simpleblocks.block.FirewoodBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FirewoodItem extends Item {
    
    public FirewoodItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        
        // Check if clicking on existing firewood stack to add more
        if (state.isOf(ModBlocks.FIREWOOD_STACK)) {
            int count = state.get(FirewoodBlock.COUNT);
            if (count < 4) {
                if (!world.isClient) {
                    world.setBlockState(pos, state.with(FirewoodBlock.COUNT, count + 1));
                    
                    if (!context.getPlayer().getAbilities().creativeMode) {
                        context.getStack().decrement(1);
                    }
                    
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResult.success(world.isClient);
            }
            return ActionResult.PASS;
        }
        
        // Otherwise, try to place new firewood stack
        BlockPos placePos = pos.offset(context.getSide());
        BlockState placeState = world.getBlockState(placePos);
        
        if (placeState.isAir() || placeState.isReplaceable()) {
            if (!world.isClient) {
                world.setBlockState(placePos, ModBlocks.FIREWOOD_STACK.getDefaultState().with(FirewoodBlock.COUNT, 1));
                
                if (!context.getPlayer().getAbilities().creativeMode) {
                    context.getStack().decrement(1);
                }
                
                world.playSound(null, placePos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }
        
        return ActionResult.PASS;
    }
}

