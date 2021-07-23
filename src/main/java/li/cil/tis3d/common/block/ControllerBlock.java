package li.cil.tis3d.common.block;

import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.tileentity.ControllerTileEntity;
import li.cil.tis3d.common.tileentity.TileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Block for the controller driving the casings.
 */
public final class ControllerBlock extends BaseEntityBlock {
    public ControllerBlock() {
        super(Properties
            .of(Material.METAL)
            .sound(SoundType.METAL)
            .strength(1.5f, 6f));
    }

    // --------------------------------------------------------------------- //
    // BaseEntityBlock

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return TileEntities.CONTROLLER.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, TileEntities.CONTROLLER.get(), ControllerTileEntity::clientTick);
        } else {
            return createTickerHelper(type, TileEntities.CONTROLLER.get(), ControllerTileEntity::serverTick);
        }
    }

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return RenderShape.MODEL;
    }

    // --------------------------------------------------------------------- //
    // Common

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit) {
        final ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            final Item item = heldItem.getItem();
            if (item == net.minecraft.world.item.Items.BOOK) {
                if (!world.isClientSide()) {
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

                return InteractionResult.sidedSuccess(world.isClientSide());
            }
        }

        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof final ControllerTileEntity controller) {
            if (!world.isClientSide()) {
                controller.forceStep();
            }

            return InteractionResult.sidedSuccess(world.isClientSide());
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level world, final BlockPos pos) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof final ControllerTileEntity controller) {
            return controller.getState() == ControllerTileEntity.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos, final Block block, final BlockPos fromPos, final boolean isMoving) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof final ControllerTileEntity controller) {
            controller.checkNeighbors();
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }
}
