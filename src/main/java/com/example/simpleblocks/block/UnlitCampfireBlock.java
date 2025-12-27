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
        
        // Only flint and steel can light the campfire
        if (heldItem.isOf(Items.FLINT_AND_STEEL)) {
            if (!world.isClient) {
                Direction facing = state.get(FACING);
                BlockState campfireState = Blocks.CAMPFIRE.getDefaultState()
                        .with(CampfireBlock.LIT, true)
                        .with(CampfireBlock.FACING, facing);
                world.setBlockState(pos, campfireState);
                if (!player.getAbilities().creativeMode) {
                    heldItem.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
                }
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else {
                spawnIgniteParticles(world, pos);
            }
            player.swingHand(hand, true);
            return ActionResult.success(world.isClient);
        }
        
        // Fire charge also works as an alternative to flint and steel
        if (heldItem.isOf(Items.FIRE_CHARGE)) {
            if (!world.isClient) {
                lightCampfire(world, pos, state);
                if (!player.getAbilities().creativeMode) {
                    heldItem.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else {
                spawnIgniteParticles(world, pos);
            }
            player.swingHand(hand, true);
            return ActionResult.success(world.isClient);
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * Lights the campfire by replacing this block with a lit vanilla campfire.
     */
    private void lightCampfire(World world, BlockPos pos, BlockState state) {
        Direction facing = state.get(FACING);
        BlockState campfireState = Blocks.CAMPFIRE.getDefaultState()
                .with(CampfireBlock.LIT, true)
                .with(CampfireBlock.FACING, facing);
        world.setBlockState(pos, campfireState);
    }
    
    /**
     * Spawns flame particles when the campfire is ignited.
     */
    private void spawnIgniteParticles(World world, BlockPos pos) {
        for (int i = 0; i < 10; i++) {
            world.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    0, 0.05, 0);
        }
    }
}
