package li.cil.tis3d.common.block;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.mixin.ItemUsageContextAccessors;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ManualBookItem;
import li.cil.tis3d.util.InventoryUtils;
import li.cil.tis3d.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.container.Container;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Block for the module casings.
 */
public final class CasingBlock extends Block implements BlockEntityProvider {
    private static final BooleanProperty MODULE_X_NEG = BooleanProperty.of("xneg");
    private static final BooleanProperty MODULE_X_POS = BooleanProperty.of("xpos");
    private static final BooleanProperty MODULE_Y_NEG = BooleanProperty.of("yneg");
    private static final BooleanProperty MODULE_Y_POS = BooleanProperty.of("ypos");
    private static final BooleanProperty MODULE_Z_NEG = BooleanProperty.of("zneg");
    private static final BooleanProperty MODULE_Z_POS = BooleanProperty.of("zpos");

    // --------------------------------------------------------------------- //

    public CasingBlock(final Block.Settings builder) {
        super(builder);
        setDefaultState(getDefaultState().with(MODULE_X_NEG, false).with(MODULE_X_POS, false).with(MODULE_Y_NEG, false).with(MODULE_Y_POS, false).with(MODULE_Z_NEG, false).with(MODULE_Z_POS, false));
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        builder.add(
            MODULE_X_NEG,
            MODULE_X_POS,
            MODULE_Y_NEG,
            MODULE_Y_POS,
            MODULE_Z_NEG,
            MODULE_Z_POS
        );
    }

    public BlockState updateBlockState(final BlockState state, final World world, final BlockPos pos) {
        final BlockEntity blockEntity = WorldUtils.getBlockEntityThreadsafe(world, pos);
        if (!(blockEntity instanceof CasingBlockEntity)) {
            return state;
        }
        final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
        final BlockState newState = state.
            with(MODULE_X_NEG, casing.getModule(Face.X_NEG) != null).
            with(MODULE_X_POS, casing.getModule(Face.X_POS) != null).
            with(MODULE_Y_NEG, casing.getModule(Face.Y_NEG) != null).
            with(MODULE_Y_POS, casing.getModule(Face.Y_POS) != null).
            with(MODULE_Z_NEG, casing.getModule(Face.Z_NEG) != null).
            with(MODULE_Z_POS, casing.getModule(Face.Z_POS) != null);
        world.setBlockState(pos, newState, 2);
        return newState;
    }

    // --------------------------------------------------------------------- //
    // Client

    public ItemStack getPickStack(final BlockView view, final BlockPos pos, final Direction side, final BlockState state) {
        // Allow picking modules installed in the casing.
        final BlockEntity blockEntity = view.getBlockEntity(pos);
        if (blockEntity instanceof CasingBlockEntity) {
            final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
            final ItemStack stack = casing.getInvStack(side.ordinal());
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return super.getPickStack(view, pos, state);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public BlockEntity createBlockEntity(final BlockView view) {
        return new CasingBlockEntity();
    }

    public static boolean activate(final ItemUsageContext context) {
        if (!context.shouldCancelInteraction()) {
            return false;
        }

        final BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
        if (!(blockState.getBlock() instanceof CasingBlock)) {
            return false;
        }

        // TODO Ugly, but context does not pass on hand...
        final Hand hand = context.getPlayer() != null && context.getPlayer().getStackInHand(Hand.OFF_HAND) == context.getStack() ? Hand.OFF_HAND : Hand.MAIN_HAND;
        final ActionResult ar = ((CasingBlock)blockState.getBlock()).onUse(blockState, context.getWorld(), context.getBlockPos(), context.getPlayer(), hand, ((ItemUsageContextAccessors)context).getBlockHitResult());
        return ar == ActionResult.CONSUME;
    }

    @SuppressWarnings("deprecation")
    public ActionResult onUse(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult blockHitResult) {
        final Vec3d hit = blockHitResult.getPos().subtract(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ());
        if (WorldUtils.isBlockLoaded(world, pos)) {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CasingBlockEntity) {
                final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
                final ItemStack heldItem = player.getStackInHand(hand);

                // Locking or unlocking the casing or a port?
                if (Items.isKey(heldItem)) {
                    if (!world.isClient) {
                        if (casing.isLocked()) {
                            casing.unlock(heldItem);
                        } else {
                            if (!player.isSneaking()) {
                                casing.lock(heldItem);
                            } else {
                                final Face face = Face.fromDirection(blockHitResult.getSide());
                                final Vec3d uv = TransformUtil.hitToUV(face, hit);
                                final Port port = Port.fromUVQuadrant(uv);

                                casing.setReceivingPipeLocked(face, port, !casing.isReceivingPipeLocked(face, port));
                            }
                        }
                    }
                    return ActionResult.CONSUME; // XXX
                }

                // Trying to look something up in the manual?
                if (Items.isBookManual(heldItem)) {
                    final ItemStack moduleStack = casing.getInvStack(blockHitResult.getSide().ordinal());
                    if (ManualBookItem.tryOpenManual(world, player, ManualAPI.pathFor(moduleStack))) {
                        return ActionResult.CONSUME; // XXX
                    }
                }

                // Let the module handle the activation.
                final Module module = casing.getModule(Face.fromDirection(blockHitResult.getSide()));
                if (module != null && module.onActivate(player, hand, hit)) {
                    return ActionResult.CONSUME; // XXX
                }

                // Don't allow changing modules while casing is locked.
                if (casing.isLocked()) {
                    return super.onUse(state, world, pos, player, hand, blockHitResult);
                }

                // Remove old module or install new one.
                final ItemStack oldModule = casing.getInvStack(blockHitResult.getSide().ordinal());
                if (!oldModule.isEmpty()) {
                    // Removing a present module from the casing.
                    if (!world.isClient) {
                        final ItemEntity entity = InventoryUtils.drop(world, pos, casing, blockHitResult.getSide().ordinal(), 1, blockHitResult.getSide());
                        if (entity != null) {
                            entity.resetPickupDelay();
                            entity.onPlayerCollision(player);
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, 0.2f, 0.8f + world.random.nextFloat() * 0.1f);
                        }
                    }
                    return ActionResult.CONSUME; // XXX
                } else if (!heldItem.isEmpty()) {
                    // Installing a new module in the casing.
                    if (casing.canInsertInvStack(blockHitResult.getSide().ordinal(), heldItem, blockHitResult.getSide())) {
                        if (!world.isClient) {
                            final ItemStack insertedStack;
                            if (player.abilities.creativeMode) {
                                insertedStack = heldItem.copy().split(1);
                            } else {
                                insertedStack = heldItem.split(1);
                            }
                            if (blockHitResult.getSide().getAxis() == Direction.Axis.Y) {
                                final Port orientation = Port.fromDirection(player.getHorizontalFacing());
                                casing.setInventorySlotContents(blockHitResult.getSide().ordinal(), insertedStack, orientation);
                            } else {
                                casing.setInvStack(blockHitResult.getSide().ordinal(), insertedStack);
                            }
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.2f, 0.8f + world.random.nextFloat() * 0.1f);
                        }
                        return ActionResult.CONSUME; // XXX
                    }
                }
            }
        }

        return super.onUse(state, world, pos, player, hand, blockHitResult);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockRemoved(final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean isMoved) {
        if (state.getBlock() != newState.getBlock()) {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CasingBlockEntity) {
                ItemScatterer.spawn(world, pos, (CasingBlockEntity)blockEntity);
                world.updateHorizontalAdjacent(pos, this);
            }
            world.removeBlockEntity(pos);
        }
        super.onBlockRemoved(state, world, pos, newState, isMoved);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorOutput(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorOutput(final BlockState state, final World world, final BlockPos pos) {
        return Container.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakRedstonePower(final BlockState blockState, final BlockView world, final BlockPos pos, final Direction side) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CasingBlockEntity) {
            final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
            final Module module = casing.getModule(Face.fromDirection(side.getOpposite()));
            if (module instanceof Redstone) {
                return ((Redstone)module).getRedstoneOutput();
            }
        }
        return super.getWeakRedstonePower(blockState, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean emitsRedstonePower(final BlockState state) {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos, final boolean isMoved) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CasingBlockEntity) {
            final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
            casing.checkNeighbors();
            casing.notifyModulesOfBlockChange(neighborPos);
            casing.markRedstoneDirty();
        }
        super.neighborUpdate(state, world, pos, neighborBlock, neighborPos, isMoved);
    }
}
