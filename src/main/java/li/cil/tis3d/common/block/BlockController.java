package li.cil.tis3d.common.block;

import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.block.entity.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.world.BlockView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import javax.annotation.Nullable;

/**
 * Block for the controller driving the casings.
 */
public final class BlockController extends Block implements BlockEntityProvider {
    public BlockController(Block.Settings builder) {
        super(builder);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean activate(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final Direction side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack heldItem = player.getStackInHand(hand);
        if (!heldItem.isEmpty()) {
            final Item item = heldItem.getItem();
            if (item == net.minecraft.item.Items.field_8744) {
                if (!world.isClient) {
                    if (!player.abilities.creativeMode) {
                        heldItem.split(1);
                    }
                    final ItemStack bookManual = new ItemStack(Items.bookManual);
                    if (player.inventory.insertStack(bookManual)) {
                        player.containerPlayer.sendContentUpdates();
                    }
                    if (bookManual.getAmount() > 0) {
                        player.dropItem(bookManual, false, false);
                    }
                }
                return true;
            }
        }

        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;

            if (!world.isClient) {
                controller.forceStep();
            }

            return true;
        }
        return super.activate(state, world, pos, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean flag) {
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
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            return controller.getState() == TileEntityController.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(final BlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            controller.checkNeighbors();
        }
        super.neighborUpdate(state, world, pos, neighborBlock, neighborPos);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new TileEntityController();
    }
}
