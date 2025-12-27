package com.example.simpleblocks.block;

import com.example.simpleblocks.ModBlocks;
import com.example.simpleblocks.ModItems;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
    
    public static final IntProperty COUNT = IntProperty.of("count", 1, 4);
    
    private static final VoxelShape SHAPE_1 = VoxelShapes.cuboid(0.375, 0.0, 0.0, 0.625, 0.25, 1.0);
    private static final VoxelShape SHAPE_2 = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.3125, 0.25, 1.0),
            VoxelShapes.cuboid(0.6875, 0.0, 0.0, 0.9375, 0.25, 1.0)
    );
    private static final VoxelShape SHAPE_3 = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.3125, 0.25, 1.0),
            VoxelShapes.cuboid(0.6875, 0.0, 0.0, 0.9375, 0.25, 1.0),
            VoxelShapes.cuboid(0.0, 0.1875, 0.6875, 1.0, 0.4375, 0.9375)
    );
    private static final VoxelShape SHAPE_4 = VoxelShapes.union(
            VoxelShapes.cuboid(0.0625, 0.0, 0.0, 0.3125, 0.25, 1.0),
            VoxelShapes.cuboid(0.6875, 0.0, 0.0, 0.9375, 0.25, 1.0),
            VoxelShapes.cuboid(0.0, 0.1875, 0.6875, 1.0, 0.4375, 0.9375),
            VoxelShapes.cuboid(0.0, 0.1875, 0.0625, 1.0, 0.4375, 0.3125)
    );
    
    public FirewoodBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(COUNT, 1));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(COUNT);
    }
    
    /**
     * Checks if an item is a valid fuel source.
     * Accepts all furnace fuels except lava bucket.
     */
    private boolean isValidFuel(Item item) {
        // Exclude lava bucket
        if (item == Items.LAVA_BUCKET) {
            return false;
        }
        // Check if item is registered as fuel in Fabric's FuelRegistry
        Integer fuelTime = FuelRegistry.INSTANCE.get(item);
        return fuelTime != null && fuelTime > 0;
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
        
        // When firewood stack is full (4), accept any fuel source to create unlit campfire
        if (count == 4) {
            if (isValidFuel(heldItem.getItem())) {
                if (!world.isClient) {
                    world.setBlockState(pos, ModBlocks.UNLIT_CAMPFIRE.getDefaultState());
                    if (!player.getAbilities().creativeMode) {
                        heldItem.decrement(1);
                    }
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.2f);
                }
                return ActionResult.success(world.isClient);
            }
        }
        
        if (count < 4 && heldItem.isOf(ModItems.FIREWOOD)) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(COUNT, count + 1));
                if (!player.getAbilities().creativeMode) {
                    heldItem.decrement(1);
                }
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
