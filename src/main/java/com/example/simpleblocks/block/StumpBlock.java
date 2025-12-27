package com.example.simpleblocks.block;

import com.example.simpleblocks.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class StumpBlock extends Block {
    
    // Property to track if a log is placed on the stump
    public static final BooleanProperty HAS_LOG = BooleanProperty.of("has_log");
    
    // Stump shape - shorter than a full block
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.625, 1.0);
    // Stump with log shape - taller
    private static final VoxelShape SHAPE_WITH_LOG = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    
    public StumpBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(HAS_LOG, false));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_LOG);
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(HAS_LOG) ? SHAPE_WITH_LOG : SHAPE;
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        boolean hasLog = state.get(HAS_LOG);
        
        if (!hasLog) {
            // No log on stump - check if player is holding a log to place it
            if (heldItem.isIn(ItemTags.LOGS)) {
                if (!world.isClient) {
                    // Place the log on the stump
                    world.setBlockState(pos, state.with(HAS_LOG, true));
                    
                    // Consume one log from player's hand
                    if (!player.getAbilities().creativeMode) {
                        heldItem.decrement(1);
                    }
                    
                    // Play wood placement sound
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
                return ActionResult.success(world.isClient);
            }
        } else {
            // Log is on stump - chop it into firewood!
            if (!world.isClient) {
                // Remove the log from stump
                world.setBlockState(pos, state.with(HAS_LOG, false));
                
                // Drop 4 firewood
                for (int i = 0; i < 4; i++) {
                    ItemStack firewood = new ItemStack(ModItems.FIREWOOD);
                    ItemEntity itemEntity = new ItemEntity(world, 
                            pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                            firewood);
                    itemEntity.setVelocity(
                            (world.random.nextDouble() - 0.5) * 0.1,
                            0.2,
                            (world.random.nextDouble() - 0.5) * 0.1
                    );
                    world.spawnEntity(itemEntity);
                }
                
                // Play chopping sound
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1.0f, 0.8f);
            }
            
            // Swing the player's arm
            player.swingHand(hand, true);
            return ActionResult.success(world.isClient);
        }
        
        return ActionResult.PASS;
    }
}

