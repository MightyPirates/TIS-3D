package li.cil.tis3d.common.block;

import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemBookManual;
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
import net.minecraft.sortme.ItemScatterer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Block for the module casings.
 */
public final class BlockCasing extends Block implements BlockEntityProvider {
    private static final BooleanProperty MODULE_X_NEG = BooleanProperty.create("xneg");
    private static final BooleanProperty MODULE_X_POS = BooleanProperty.create("xpos");
    private static final BooleanProperty MODULE_Y_NEG = BooleanProperty.create("yneg");
    private static final BooleanProperty MODULE_Y_POS = BooleanProperty.create("ypos");
    private static final BooleanProperty MODULE_Z_NEG = BooleanProperty.create("zneg");
    private static final BooleanProperty MODULE_Z_POS = BooleanProperty.create("zpos");

    // --------------------------------------------------------------------- //

    public BlockCasing(Block.Settings builder) {
        super(builder);
        setDefaultState(getDefaultState().with(MODULE_X_NEG, false).with(MODULE_X_POS, false).with(MODULE_Y_NEG, false).with(MODULE_Y_POS, false).with(MODULE_Z_NEG, false).with(MODULE_Z_POS, false));
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        builder.with(
            MODULE_X_NEG,
            MODULE_X_POS,
            MODULE_Y_NEG,
            MODULE_Y_POS,
            MODULE_Z_NEG,
            MODULE_Z_POS
        );
    }

    public void updateBlockState(final BlockState state, final World world, final BlockPos pos) {
        final BlockEntity tileEntity = WorldUtils.getTileEntityThreadsafe(world, pos);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }
        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        world.setBlockState(pos, state.
            with(MODULE_X_NEG, casing.getModule(Face.X_NEG) != null).
            with(MODULE_X_POS, casing.getModule(Face.X_POS) != null).
            with(MODULE_Y_NEG, casing.getModule(Face.Y_NEG) != null).
            with(MODULE_Y_POS, casing.getModule(Face.Y_POS) != null).
            with(MODULE_Z_NEG, casing.getModule(Face.Z_NEG) != null).
            with(MODULE_Z_POS, casing.getModule(Face.Z_POS) != null), 2);
    }

    // --------------------------------------------------------------------- //
    // Client

    public ItemStack getPickStack(BlockView view, BlockPos pos, Direction side, BlockState state) {
        // Allow picking modules installed in the casing.
        final BlockEntity tileEntity = view.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
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
    public BlockEntity createBlockEntity(BlockView view) {
        return new TileEntityCasing();
    }

    public static boolean activate(ItemUsageContext context) {
        if (!context.isPlayerSneaking()) {
            return false;
        }

        final BlockState blockState = context.getWorld().getBlockState(context.getPos());
        if (!(blockState.getBlock() instanceof BlockCasing)) {
            return false;
        }

        // TODO Ugly, but context does not pass on hand...
        final Hand hand = context.getPlayer() != null && context.getPlayer().getStackInHand(Hand.OFF) == context.getItemStack() ? Hand.OFF : Hand.MAIN;
        return ((BlockCasing) blockState.getBlock()).activate(blockState, context.getWorld(), context.getPos(), context.getPlayer(), hand, context.getFacing(), context.getHitX(), context.getHitY(), context.getHitZ());
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean activate(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final Direction side, final float hitX, final float hitY, final float hitZ) {
        if (world.isBlockLoaded(pos)) {
            final BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;
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
                                final Face face = Face.fromDirection(side);
                                final Vec3d uv = TransformUtil.hitToUV(face, new Vec3d(hitX, hitY, hitZ));
                                final Port port = Port.fromUVQuadrant(uv);

                                casing.setReceivingPipeLocked(face, port, !casing.isReceivingPipeLocked(face, port));
                            }
                        }
                    }
                    return true;
                }

                // Trying to look something up in the manual?
                if (Items.isBookManual(heldItem)) {
                    final ItemStack moduleStack = casing.getInvStack(side.ordinal());
                    if (ItemBookManual.tryOpenManual(world, player, ManualAPI.pathFor(moduleStack))) {
                        return true;
                    }
                }

                // Let the module handle the activation.
                final Module module = casing.getModule(Face.fromDirection(side));
                if (module != null && module.onActivate(player, hand, hitX, hitY, hitZ)) {
                    return true;
                }

                // Don't allow changing modules while casing is locked.
                if (casing.isLocked()) {
                    return super.activate(state, world, pos, player, hand, side, hitX, hitY, hitZ);
                }

                // Remove old module or install new one.
                final ItemStack oldModule = casing.getInvStack(side.ordinal());
                if (!oldModule.isEmpty()) {
                    // Removing a present module from the casing.
                    if (!world.isClient) {
                        final ItemEntity entity = InventoryUtils.drop(world, pos, casing, side.ordinal(), 1, side);
                        if (entity != null) {
                            entity.resetPickupDelay();
                            entity.onPlayerCollision(player);
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCK, 0.2f, 0.8f + world.random.nextFloat() * 0.1f);
                        }
                    }
                    return true;
                } else if (!heldItem.isEmpty()) {
                    // Installing a new module in the casing.
                    if (casing.canInsertInvStack(side.ordinal(), heldItem, side)) {
                        if (!world.isClient) {
                            final ItemStack insertedStack;
                            if (player.abilities.creativeMode) {
                                insertedStack = heldItem.copy().split(1);
                            } else {
                                insertedStack = heldItem.split(1);
                            }
                            if (side.getAxis() == Direction.Axis.Y) {
                                final Port orientation = Port.fromDirection(player.getHorizontalFacing());
                                casing.setInventorySlotContents(side.ordinal(), insertedStack, orientation);
                            } else {
                                casing.setInvStack(side.ordinal(), insertedStack);
                            }
                            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCK, 0.2f, 0.8f + world.random.nextFloat() * 0.1f);
                        }
                        return true;
                    }
                }
            }
        }

        return super.activate(state, world, pos, player, hand, side, hitX, hitY, hitZ);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean flag) {
        if (state.getBlock() != newState.getBlock()) {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityCasing) {
                ItemScatterer.spawn(world, pos, (TileEntityCasing) blockEntity);
                world.updateHorizontalAdjacent(pos, this);
            }
            world.removeBlockEntity(pos);
        }
        super.onBlockRemoved(state, world, pos, newState, flag);
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
        if (blockEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) blockEntity;
            final Module module = casing.getModule(Face.fromDirection(side.getOpposite()));
            if (module instanceof Redstone) {
                return ((Redstone) module).getRedstoneOutput();
            }
        }
        return super.getWeakRedstonePower(blockState, world, pos, side);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityCasing) {
            final TileEntityCasing casing = (TileEntityCasing) tileEntity;
            casing.checkNeighbors();
            casing.notifyModulesOfBlockChange(neighborPos);
            casing.markRedstoneDirty();
        }
        super.neighborUpdate(state, world, pos, neighborBlock, neighborPos);
    }
}
