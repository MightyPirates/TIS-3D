package li.cil.tis3d.common.block;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.init.Items;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Block for the controller driving the casings.
 */
public final class ControllerBlock extends Block implements BlockEntityProvider {
    public ControllerBlock(final Block.Settings builder) {
        super(builder);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public BlockEntity createBlockEntity(final BlockView view) {
        return new ControllerBlockEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onUse(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult blockHitResult) {
        final ItemStack heldItem = player.getStackInHand(hand);
        if (!heldItem.isEmpty()) {
            final Item item = heldItem.getItem();
            if (item == net.minecraft.item.Items.BOOK) {
                if (!world.isClient) {
                    if (!player.abilities.creativeMode) {
                        heldItem.split(1);
                    }
                    final ItemStack bookManual = new ItemStack(Items.BOOK_MANUAL);
                    if (player.inventory.insertStack(bookManual)) {
                        player.playerContainer.sendContentUpdates();
                    }
                    if (bookManual.getCount() > 0) {
                        player.dropItem(bookManual, false, false);
                    }
                }
                return true;
            }
        }

        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ControllerBlockEntity) {
            final ControllerBlockEntity controller = (ControllerBlockEntity)blockEntity;

            if (!world.isClient) {
                controller.forceStep();
            }

            return true;
        }

        return super.onUse(state, world, pos, player, hand, blockHitResult);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockRemoved(final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean flag) {
        if (state.getBlock() != newState.getBlock()) {
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
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ControllerBlockEntity) {
            final ControllerBlockEntity controller = (ControllerBlockEntity)blockEntity;
            return controller.getState() == ControllerBlockEntity.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos, final boolean isMoved) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ControllerBlockEntity) {
            final ControllerBlockEntity controller = (ControllerBlockEntity)blockEntity;
            controller.checkNeighbors();
        }
        super.neighborUpdate(state, world, pos, neighborBlock, neighborPos, isMoved);
    }
}
