package com.example.simpleblocks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;

public class UnlitCampfireBlock extends Block {
    
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    
    // Shape for the unlit campfire (firewood pile with coal on top)
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.4375, 1.0);
    
    public UnlitCampfireBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        
        // Check if player is using flint and steel to light it
        if (heldItem.isOf(Items.FLINT_AND_STEEL)) {
            if (!world.isClient) {
                // Get the facing direction to preserve it
                Direction facing = state.get(FACING);
                
                // Replace with vanilla campfire (lit)
                BlockState campfireState = Blocks.CAMPFIRE.getDefaultState()
                        .with(CampfireBlock.LIT, true)
                        .with(CampfireBlock.FACING, facing);
                world.setBlockState(pos, campfireState);
                
                // Damage the flint and steel
                if (!player.getAbilities().creativeMode) {
                    heldItem.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
                }
                
                // Play fire ignite sound
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else {
                // Spawn fire particles on client
                for (int i = 0; i < 10; i++) {
                    world.addParticle(ParticleTypes.FLAME,
                            pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                            0, 0.05, 0);
                }
            }
            
            player.swingHand(hand, true);
            return ActionResult.success(world.isClient);
        }
        
        // Also allow fire charge to light it
        if (heldItem.isOf(Items.FIRE_CHARGE)) {
            if (!world.isClient) {
                Direction facing = state.get(FACING);
                
                BlockState campfireState = Blocks.CAMPFIRE.getDefaultState()
                        .with(CampfireBlock.LIT, true)
                        .with(CampfireBlock.FACING, facing);
                world.setBlockState(pos, campfireState);
                
                // Consume fire charge
                if (!player.getAbilities().creativeMode) {
                    heldItem.decrement(1);
                }
                
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }
        
        return ActionResult.PASS;
    }
}

