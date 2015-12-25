package li.cil.tis3d.common.block;

import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.tile.TileEntityCasing;
import li.cil.tis3d.common.tile.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Block for the controller driving the casings.
 */
public final class BlockController extends Block {
    public BlockController() {
        super(Material.iron);
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean isSideSolid(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side) {
        // Allow levers to be placed on us (wouldn't work because of isFullCube = false otherwise).
        return true;
    }

    @Override
    public boolean renderAsNormalBlock() {
        // Prevent fences from visually connecting.
        return false;
    }

    @Override
    public int getRenderType() {
        return TIS3D.proxy.getControllerRenderId();
    }

    @Override
    public boolean hasTileEntity(final int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final World world, final int metadata) {
        return new TileEntityController();
    }

    @Override
    public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ) {
        final ItemStack stack = player.getHeldItem();
        if (stack != null) {
            final Item item = stack.getItem();
            if (item == net.minecraft.init.Items.book) {
                if (!world.isRemote) {
                    if (!player.capabilities.isCreativeMode) {
                        stack.splitStack(1);
                    }
                    final ItemStack bookManual = new ItemStack(Items.bookManual);
                    if (player.inventory.addItemStackToInventory(bookManual)) {
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    if (bookManual.stackSize > 0) {
                        player.func_146097_a(bookManual, false, false);
                    }
                }
                return true;
            }
        }
        return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(final World world, final int x, final int y, final int z, final int side) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            return controller.getState() == TileEntityController.ControllerState.READY ? 15 : 0;
        }
        return 0;
    }

    // --------------------------------------------------------------------- //
    // Networking

    @Override
    public void onNeighborBlockChange(final World world, final int x, final int y, final int z, final Block neighborBlock) {
        final TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            for (final EnumFacing facing : EnumFacing.values()) {
                checkNeighbor(controller, facing);
            }
        }
        super.onNeighborBlockChange(world, x, y, z, neighborBlock);
    }

    private static void checkNeighbor(final TileEntityController controller, final EnumFacing facing) {
        final int neighborX = controller.xCoord + facing.getFrontOffsetX();
        final int neighborY = controller.yCoord + facing.getFrontOffsetY();
        final int neighborZ = controller.zCoord + facing.getFrontOffsetZ();
        if (controller.getWorldObj().blockExists(neighborX, neighborY, neighborZ)) {
            final TileEntity neighborTileEntity = controller.getWorldObj().getTileEntity(neighborX, neighborY, neighborZ);
            if (neighborTileEntity instanceof TileEntityController) {
                // If we have a controller that means we have more than one in
                // our multi-block. Rescan to enter appropriate error state.
                controller.scheduleScan();
            } else if (neighborTileEntity instanceof TileEntityCasing) {
                // Rescan if we don't know that casing (yet).
                final TileEntityCasing casing = (TileEntityCasing) neighborTileEntity;
                if (casing.getController() != controller) {
                    controller.scheduleScan();
                }
            }
        } else {
            // Make sure we notice we're on the border of the loaded area.
            controller.scheduleScan();
        }
    }
}
