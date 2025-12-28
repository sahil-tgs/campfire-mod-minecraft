package com.example.simpleblocks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
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

import java.util.HashMap;
import java.util.Map;

public class UnlitCampfireBlock extends Block {
    
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.4375, 1.0);
    
    // ========== DEV MODE ==========
    // Set to true for testing (10 clicks = guaranteed fire)
    // Set to false for production (chance-based)
    private static final boolean DEV_MODE = true;
    private static final int DEV_CLICKS_TO_IGNITE = 10;
    
    // Track attempt counts per block position (for dev mode)
    private static final Map<BlockPos, Integer> attemptCounts = new HashMap<>();
    
    // ========== IGNITION SUCCESS RATES (Production) ==========
    // These are used when DEV_MODE = false
    private static final float HAND_DRILL_SUCCESS_RATE = 0.15f;   // ~15%
    private static final float FLINT_SPARK_SUCCESS_RATE = 0.25f;  // ~25%
    private static final float BOW_DRILL_SUCCESS_RATE = 0.40f;    // ~40%
    
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
    
    /**
     * Returns a modifier for ignition success rates.
     * Currently returns 1.0 (no modification).
     * Future: Will check weather conditions (rain, etc.) to reduce success rates.
     */
    protected float getIgnitionChanceModifier(World world, BlockPos pos) {
        // TODO: Future weather integration
        // if (world.isRaining() && world.isSkyVisible(pos)) return 0.5f;
        // if (world.isThundering() && world.isSkyVisible(pos)) return 0.2f;
        return 1.0f;
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getStackInHand(hand);
        
        // ===== GUARANTEED IGNITION METHODS =====
        
        // Flint and Steel - 100% success, the "end goal" of fire starting
        if (heldItem.isOf(Items.FLINT_AND_STEEL)) {
            if (!world.isClient) {
                lightCampfire(world, pos, state);
                clearAttemptCount(pos); // Reset counter
                if (!player.getAbilities().creativeMode) {
                    heldItem.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
                }
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else {
                spawnSuccessParticles(world, pos);
            }
            player.swingHand(hand, true);
            return ActionResult.success(world.isClient);
        }
        
        // Fire Charge - 100% success, consumed on use
        if (heldItem.isOf(Items.FIRE_CHARGE)) {
            if (!world.isClient) {
                lightCampfire(world, pos, state);
                clearAttemptCount(pos); // Reset counter
                if (!player.getAbilities().creativeMode) {
                    heldItem.decrement(1);
                }
                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            } else {
                spawnSuccessParticles(world, pos);
            }
            player.swingHand(hand, true);
            return ActionResult.success(world.isClient);
        }
        
        // ===== PRIMITIVE IGNITION METHODS =====
        
        // Bow Drill - Uses bow
        if (heldItem.getItem() instanceof BowItem) {
            return attemptPrimitiveIgnition(world, pos, state, player, hand, heldItem, 
                    IgnitionMethod.BOW_DRILL);
        }
        
        // Flint Sparking - Uses flint
        if (heldItem.isOf(Items.FLINT)) {
            return attemptPrimitiveIgnition(world, pos, state, player, hand, heldItem, 
                    IgnitionMethod.FLINT_SPARK);
        }
        
        // Hand Drill (Sticks) - Requires at least 1 stick
        if (heldItem.isOf(Items.STICK)) {
            return attemptPrimitiveIgnition(world, pos, state, player, hand, heldItem, 
                    IgnitionMethod.HAND_DRILL);
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * Defines the different primitive ignition methods.
     */
    private enum IgnitionMethod {
        HAND_DRILL,
        FLINT_SPARK,
        BOW_DRILL
    }
    
    /**
     * Attempts to ignite the campfire using a primitive method.
     * In DEV_MODE: Guaranteed success after DEV_CLICKS_TO_IGNITE attempts.
     * In Production: Chance-based success.
     */
    private ActionResult attemptPrimitiveIgnition(World world, BlockPos pos, BlockState state, 
            PlayerEntity player, Hand hand, ItemStack heldItem, IgnitionMethod method) {
        
        boolean success;
        int currentAttempts = 0;
        
        if (DEV_MODE) {
            // Dev mode: count clicks, ignite after threshold
            currentAttempts = getAttemptCount(pos) + 1;
            setAttemptCount(pos, currentAttempts);
            success = currentAttempts >= DEV_CLICKS_TO_IGNITE;
        } else {
            // Production mode: chance-based
            float baseSuccessRate = switch (method) {
                case HAND_DRILL -> HAND_DRILL_SUCCESS_RATE;
                case FLINT_SPARK -> FLINT_SPARK_SUCCESS_RATE;
                case BOW_DRILL -> BOW_DRILL_SUCCESS_RATE;
            };
            float finalSuccessRate = baseSuccessRate * getIgnitionChanceModifier(world, pos);
            success = world.random.nextFloat() < finalSuccessRate;
        }
        
        if (!world.isClient) {
            // Consume/damage items on each attempt
            consumeIgnitionItem(player, hand, heldItem, method);
            
            // Play attempt sound
            playAttemptSound(world, pos, method);
            
            if (success) {
                lightCampfire(world, pos, state);
                clearAttemptCount(pos);
                // Play success sound
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0f, 0.9f);
            }
        } else {
            // Client-side: ALWAYS show attempt particles for feedback
            spawnAttemptParticles(world, pos, method, currentAttempts);
            
            if (success) {
                spawnSuccessParticles(world, pos);
            }
        }
        
        player.swingHand(hand, true);
        return ActionResult.success(world.isClient);
    }
    
    // ========== ATTEMPT COUNTER METHODS ==========
    
    private int getAttemptCount(BlockPos pos) {
        return attemptCounts.getOrDefault(pos.toImmutable(), 0);
    }
    
    private void setAttemptCount(BlockPos pos, int count) {
        attemptCounts.put(pos.toImmutable(), count);
    }
    
    private void clearAttemptCount(BlockPos pos) {
        attemptCounts.remove(pos.toImmutable());
    }
    
    // ========== ITEM CONSUMPTION ==========
    
    /**
     * Consumes or damages the item used for ignition.
     */
    private void consumeIgnitionItem(PlayerEntity player, Hand hand, ItemStack heldItem, IgnitionMethod method) {
        if (player.getAbilities().creativeMode) {
            return;
        }
        
        switch (method) {
            case HAND_DRILL -> {
                // Consumes 1 stick per attempt (DEV: was 2 in production)
                heldItem.decrement(1);
            }
            case FLINT_SPARK -> {
                // 30% chance to consume flint on each attempt
                if (player.getWorld().random.nextFloat() < 0.30f) {
                    heldItem.decrement(1);
                }
            }
            case BOW_DRILL -> {
                // Damages bow by 2 durability per attempt
                heldItem.damage(2, player, (p) -> p.sendToolBreakStatus(hand));
            }
        }
    }
    
    // ========== SOUND EFFECTS ==========
    
    /**
     * Plays sound effect for ignition attempt.
     * Placeholder sounds - will be replaced with custom sounds later.
     */
    private void playAttemptSound(World world, BlockPos pos, IgnitionMethod method) {
        switch (method) {
            case HAND_DRILL -> {
                // Wood rubbing sound
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 0.5f, 1.5f);
            }
            case FLINT_SPARK -> {
                // Flint striking sound
                world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 1.0f, 1.2f);
            }
            case BOW_DRILL -> {
                // Bow drill friction sound
                world.playSound(null, pos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 0.8f, 1.3f);
            }
        }
    }
    
    // ========== PARTICLE EFFECTS ==========
    
    /**
     * Spawns particles on EVERY attempt to show the player something is happening.
     * Particles intensify as attempts increase (in dev mode).
     */
    private void spawnAttemptParticles(World world, BlockPos pos, IgnitionMethod method, int attemptCount) {
        // Base particle count, increases with attempts in dev mode
        int intensity = DEV_MODE ? Math.min(attemptCount, DEV_CLICKS_TO_IGNITE) : 3;
        
        switch (method) {
            case HAND_DRILL -> {
                // Smoke particles - friction heating up
                for (int i = 0; i < 2 + intensity; i++) {
                    world.addParticle(ParticleTypes.SMOKE,
                            pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3,
                            0, 0.02, 0);
                }
                // Add small flames as we get closer to success
                if (intensity > 5) {
                    for (int i = 0; i < intensity - 5; i++) {
                        world.addParticle(ParticleTypes.SMALL_FLAME,
                                pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                                pos.getY() + 0.45,
                                pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                                0, 0.01, 0);
                    }
                }
            }
            case FLINT_SPARK -> {
                // Spark particles
                for (int i = 0; i < 3 + intensity; i++) {
                    world.addParticle(ParticleTypes.CRIT,
                            pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.4,
                            pos.getY() + 0.4,
                            pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.4,
                            (world.random.nextDouble() - 0.5) * 0.15,
                            0.1,
                            (world.random.nextDouble() - 0.5) * 0.15);
                }
                // Add embers as we get closer
                if (intensity > 6) {
                    for (int i = 0; i < intensity - 6; i++) {
                        world.addParticle(ParticleTypes.SMALL_FLAME,
                                pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3,
                                pos.getY() + 0.4,
                                pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3,
                                0, 0.02, 0);
                    }
                }
            }
            case BOW_DRILL -> {
                // Smoke with increasing intensity
                for (int i = 0; i < 3 + intensity; i++) {
                    world.addParticle(ParticleTypes.SMOKE,
                            pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.2,
                            0, 0.03, 0);
                }
                // Embers appear earlier with bow drill (more effective)
                if (intensity > 4) {
                    for (int i = 0; i < intensity - 4; i++) {
                        world.addParticle(ParticleTypes.SMALL_FLAME,
                                pos.getX() + 0.5,
                                pos.getY() + 0.45,
                                pos.getZ() + 0.5,
                                (world.random.nextDouble() - 0.5) * 0.02,
                                0.01,
                                (world.random.nextDouble() - 0.5) * 0.02);
                    }
                }
            }
        }
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
     * Spawns flame particles when the campfire is successfully ignited.
     */
    private void spawnSuccessParticles(World world, BlockPos pos) {
        for (int i = 0; i < 20; i++) {
            world.addParticle(ParticleTypes.FLAME,
                    pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.5,
                    0, 0.08, 0);
        }
        // Lava particles for dramatic effect
        for (int i = 0; i < 8; i++) {
            world.addParticle(ParticleTypes.LAVA,
                    pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.4,
                    pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0);
        }
    }
    
    /**
     * Called when the block is removed - clean up attempt counter.
     */
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            clearAttemptCount(pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
