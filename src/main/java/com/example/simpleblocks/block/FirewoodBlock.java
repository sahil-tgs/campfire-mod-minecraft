package com.example.simpleblocks.block;

import com.example.simpleblocks.ModBlocks;
import com.example.simpleblocks.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class FirewoodBlock extends Block {
    
    // Number of firewood pieces stacked (1-4)
    public static final IntProperty COUNT = IntProperty.of("count", 1, 4);
    
    // Shapes matching exact campfire log dimensions (4 pixels tall, 4 wide, 16 long)
    // 1 log: single horizontal log in center
    private static final VoxelShape SHAPE_1 = VoxelShapes.cuboid(0.375, 0.0, 0.0, 0.625, 0.25, 1.0);
    
    // 2 logs: two parallel horizontal logs
    private static final VoxelShape SHAPE_2 = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.3125, 0.25, 1.0),  // Left log
            VoxelShapes.cuboid(0.6875, 0.0, 0.0, 0.9375, 0.25, 1.0)   // Right log
    );
    
    // 3 logs: two bottom + one on top perpendicular
    private static final VoxelShape SHAPE_3 = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.3125, 0.25, 1.0),  // Bottom left
            VoxelShapes.cuboid(0.6875, 0.0, 0.0, 0.9375, 0.25, 1.0),  // Bottom right
            VoxelShapes.cuboid(0.0, 0.1875, 0.6875, 1.0, 0.4375, 0.9375) // Top back
    );
    
    // 4 logs: exactly like campfire - two bottom + two top crossing
    private static final VoxelShape SHAPE_4 = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.3125, 0.25, 1.0),  // Bottom left
            VoxelShapes.cuboid(0.6875, 0.0, 0.0, 0.9375, 0.25, 1.0),  // Bottom right
            VoxelShapes.cuboid(0.0, 0.1875, 0.6875, 1.0, 0.4375, 0.9375), // Top back
            VoxelShapes.cuboid(0.0, 0.1875, 0.0625, 1.0, 0.4375, 0.3125)  // Top front
    );
    
    public FirewoodBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(COUNT, 1));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COUNT);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(COUNT)) {
            case 1 -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            case 4 -> SHAPE_4;
            default -> SHAPE_1;
        };
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        int count = state.get(COUNT);
        
        // Check if stack is full (4) and player has coal/charcoal
        if (count == 4) {
            if (heldItem.isOf(Items.COAL) || heldItem.isOf(Items.CHARCOAL)) {
                if (!world.isClient) {
                    // Transform into unlit campfire
                    world.setBlockState(pos, ModBlocks.UNLIT_CAMPFIRE.getDefaultState());
                    
                    // Consume one coal
                    if (!player.getAbilities().creativeMode) {
                        heldItem.decrement(1);
                    }
                    
                    // Play placement sound
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                }
                return ActionResult.success(world.isClient);
            }
        }
        
        // Check if player is adding more firewood
        if (count < 4 && heldItem.isOf(ModItems.FIREWOOD)) {
            if (!world.isClient) {
                // Increase stack count
                world.setBlockState(pos, state.with(COUNT, count + 1));
                
                // Consume one firewood
                if (!player.getAbilities().creativeMode) {
                    heldItem.decrement(1);
                }
                
                // Play wood placement sound
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }
        
        return ActionResult.PASS;
    }
    
    @Override
    public boolean canReplace(BlockState state, net.minecraft.item.ItemPlacementContext context) {
        if (context.getStack().isOf(ModItems.FIREWOOD) && state.get(COUNT) < 4) {
            return false;
        }
        return super.canReplace(state, context);
    }
}
