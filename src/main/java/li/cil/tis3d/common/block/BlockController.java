package li.cil.tis3d.common.block;

import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Block for the controller driving the casings.
 */
public final class BlockController extends Block {
    public BlockController() {
        super(Material.IRON);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean hasTileEntity(final IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final IBlockState state) {
        return new TileEntityController();
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack heldItem = player.getHeldItem(hand);
        if (!heldItem.isEmpty()) {
            final Item item = heldItem.getItem();
            if (item == net.minecraft.init.Items.BOOK) {
                if (!world.isRemote) {
                    if (!player.capabilities.isCreativeMode) {
                        heldItem.splitStack(1);
                    }
                    final ItemStack bookManual = new ItemStack(Items.BOOK_MANUAL);
                    if (player.inventory.addItemStackToInventory(bookManual)) {
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    if (bookManual.getCount() > 0) {
                        player.dropItem(bookManual, false, false);
                    }
                }
                return true;
            }
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;

            if (!world.isRemote) {
                controller.forceStep();
            }

            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(final IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntity tileEntity = world.getTileEntity(pos);
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
    public void neighborChanged(final IBlockState state, final World world, final BlockPos pos, final Block neighborBlock, final BlockPos neighborPos) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            controller.checkNeighbors();
        }
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos);
    }
}
