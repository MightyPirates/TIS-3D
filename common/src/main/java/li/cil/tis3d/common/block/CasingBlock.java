/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.block;

import com.mojang.serialization.MapCodec;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.ModuleWithRedstone;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Optional;

/**
 * Block for the module casings.
 */
public class CasingBlock extends BaseEntityBlock {
    public static final MapCodec<CasingBlock> CODEC = simpleCodec(CasingBlock::new);

    public static final BooleanProperty MODULE_X_NEG = BooleanProperty.create("xneg");
    public static final BooleanProperty MODULE_X_POS = BooleanProperty.create("xpos");
    public static final BooleanProperty MODULE_Y_NEG = BooleanProperty.create("yneg");
    public static final BooleanProperty MODULE_Y_POS = BooleanProperty.create("ypos");
    public static final BooleanProperty MODULE_Z_NEG = BooleanProperty.create("zneg");
    public static final BooleanProperty MODULE_Z_POS = BooleanProperty.create("zpos");

    public static final EnumMap<Face, BooleanProperty> FACE_TO_PROPERTY = Util.make(() -> {
        final EnumMap<Face, BooleanProperty> map = new EnumMap<>(Face.class);
        map.put(Face.X_NEG, MODULE_X_NEG);
        map.put(Face.X_POS, MODULE_X_POS);
        map.put(Face.Y_NEG, MODULE_Y_NEG);
        map.put(Face.Y_POS, MODULE_Y_POS);
        map.put(Face.Z_NEG, MODULE_Z_NEG);
        map.put(Face.Z_POS, MODULE_Z_POS);
        return map;
    });

    // --------------------------------------------------------------------- //

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public CasingBlock(BlockBehaviour.Properties properties) {
        super(properties);

        BlockState defaultState = getStateDefinition().any();
        for (final BooleanProperty value : FACE_TO_PROPERTY.values()) {
            defaultState = defaultState.setValue(value, false);
        }
        registerDefaultState(defaultState);
    }

    // --------------------------------------------------------------------- //
    // State

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        for (final BooleanProperty value : FACE_TO_PROPERTY.values()) {
            builder.add(value);
        }
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.CASING.get().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // Common

    public static Optional<InteractionResult> useIfCasing(final UseOnContext context) {
        final var player = context.getPlayer();
        if (player != null) {
            final var state = context.getLevel().getBlockState(context.getClickedPos());
            if (state.getBlock() instanceof final CasingBlock casing) {
                final var hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                return Optional.of(casing.useCasing(context.getLevel(), context.getClickedPos(), player, context.getHand(), hit));
            }
        }
        return Optional.empty();
    }

    public static ItemStack getPickedModule(final BlockGetter level, final BlockPos pos, final Player player) {
        if (!(level.getBlockEntity(pos) instanceof final CasingBlockEntity casing)) {
            return ItemStack.EMPTY;
        }

        final Vec3 from = player.getEyePosition();
        final Vec3 to = from.add(player.getViewVector(1).scale(player.blockInteractionRange() + 1));
        final BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(pos)) {
            return ItemStack.EMPTY;
        }

        return casing.getItem(hit.getDirection().ordinal()).copy();
    }

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit) {
        final InteractionResult result = useCasing(level, pos, player, hand, hit);
        return result == InteractionResult.PASS
            ? InteractionResult.TRY_WITH_EMPTY_HAND
            : result;
    }

    private InteractionResult useCasing(final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof final CasingBlockEntity casing)) {
            return InteractionResult.PASS;
        }

        final BlockPos hitPos = hit.getBlockPos();
        final Vec3 localHitPos = hit.getLocation().subtract(hitPos.getX(), hitPos.getY(), hitPos.getZ());
        final Direction side = hit.getDirection();
        final ItemStack heldItem = player.getItemInHand(hand);

        // Locking or unlocking the casing or a port?
        if (Items.is(heldItem, Items.KEY) || Items.is(heldItem, Items.KEY_CREATIVE)) {
            if (!level.isClientSide()) {
                if (casing.isLocked()) {
                    casing.unlock(heldItem);
                } else {
                    if (!player.isShiftKeyDown()) {
                        casing.lock(heldItem);
                    } else {
                        final Face face = Face.fromDirection(side);
                        final Vec3 uv = TransformUtil.hitToUV(face, localHitPos);
                        final Port port = Port.fromUVQuadrant(uv);

                        casing.setReceivingPipeLocked(face, port, !casing.isReceivingPipeLocked(face, port));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Let the module handle the activation.
        final Module module = casing.getModule(Face.fromDirection(side));
        if (module != null && module.use(player, hand, localHitPos)) {
            return InteractionResult.SUCCESS;
        }

        // Don't allow changing modules while casing is locked.
        if (casing.isLocked()) {
            return InteractionResult.PASS;
        }

        // Remove old module or install new one.
        final ItemStack oldModule = casing.getItem(side.ordinal());
        if (!oldModule.isEmpty()) {
            // Removing a present module from the casing.
            if (!level.isClientSide()) {
                final ItemEntity entity = InventoryUtils.drop(level, pos, casing, side.ordinal(), 1, side);
                if (entity != null) {
                    entity.setNoPickUpDelay();
                    entity.playerTouch(player);
                    level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.2f, 0.8f + level.random.nextFloat() * 0.1f);
                }
            }
            return InteractionResult.SUCCESS;
        } else if (!heldItem.isEmpty()) {
            // Installing a new module in the casing.
            if (casing.canPlaceItemThroughFace(side.ordinal(), heldItem, side)) {
                if (!level.isClientSide()) {
                    final ItemStack insertedStack;
                    if (player.getAbilities().instabuild) {
                        insertedStack = heldItem.copy().split(1);
                    } else {
                        insertedStack = heldItem.split(1);
                    }
                    if (side.getAxis() == Direction.Axis.Y) {
                        final Port orientation = Port.fromDirection(player.getDirection());
                        casing.setInventorySlotContents(side.ordinal(), insertedStack, orientation);
                    } else {
                        casing.setItem(side.ordinal(), insertedStack);
                    }
                    level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.2f, 0.8f + level.random.nextFloat() * 0.1f);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(final BlockState state, final ServerLevel level, final BlockPos pos, final boolean isMoving) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing) {
            Containers.dropContents(level, pos, casing);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos, final Direction side) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public int getSignal(final BlockState blockState, final BlockGetter level, final BlockPos pos, final Direction side) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing) {
            final Module module = casing.getModule(Face.fromDirection(side.getOpposite()));
            if (module instanceof final ModuleWithRedstone redstoneModule) {
                return redstoneModule.getRedstoneOutput();
            }
        }
        return super.getSignal(blockState, level, pos, side);
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, @Nullable final Orientation orientation, final boolean isMoving) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing) {
            casing.checkNeighbors();
            casing.notifyModulesOfBlockChange();
            casing.markRedstoneDirty();
        }
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
    }
}
