/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.block;

import com.mojang.serialization.MapCodec;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Block for the controller driving the casings.
 */
public final class ControllerBlock extends BaseEntityBlock {
    public static final MapCodec<ControllerBlock> CODEC = simpleCodec(ControllerBlock::new);

    // --------------------------------------------------------------------- //

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public ControllerBlock(Properties properties) {
        super(properties);
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return BlockEntities.CONTROLLER.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        } else {
            return createTickerHelper(type, BlockEntities.CONTROLLER.get(), ControllerBlockEntity::serverTick);
        }
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit) {
        final InteractionResult result = useController(level, pos, player, hand);
        return result == InteractionResult.PASS
            ? InteractionResult.TRY_WITH_EMPTY_HAND
            : result;
    }

    private InteractionResult useController(final Level level, final BlockPos pos, final Player player, final InteractionHand hand) {
        final ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            final Item item = heldItem.getItem();
            if (item == net.minecraft.world.item.Items.BOOK) {
                if (!level.isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        heldItem.split(1);
                    }
                    final ItemStack bookManual = new ItemStack(Items.BOOK_MANUAL.get());
                    if (player.getInventory().add(bookManual)) {
                        player.containerMenu.broadcastChanges();
                    }
                    if (bookManual.getCount() > 0) {
                        player.drop(bookManual, false, false);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            if (!level.isClientSide()) {
                controller.forceStep();
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos, final Direction side) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            return controller.getState() == ControllerBlockEntity.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, @Nullable final Orientation orientation, final boolean isMoving) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final ControllerBlockEntity controller) {
            controller.checkNeighbors();
        }
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
    }
}
