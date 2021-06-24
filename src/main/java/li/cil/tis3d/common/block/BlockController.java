package li.cil.tis3d.common.block;

import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.tileentity.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Block for the controller driving the casings.
 */
public final class BlockController extends Block {
    public BlockController() {
        super(Properties
            .create(Material.IRON)
            .sound(SoundType.METAL)
            .hardnessAndResistance(1.5f, 6f));
    }

    // --------------------------------------------------------------------- //
    // Common

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new TileEntityController();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit) {
        final ItemStack heldItem = player.getHeldItem(hand);
        if (!heldItem.isEmpty()) {
            final Item item = heldItem.getItem();
            if (item == net.minecraft.item.Items.BOOK) {
                if (!world.isRemote()) {
                    if (!player.abilities.isCreativeMode) {
                        heldItem.split(1);
                    }
                    final ItemStack bookManual = new ItemStack(Items.BOOK_MANUAL.get());
                    if (player.inventory.addItemStackToInventory(bookManual)) {
                        player.container.detectAndSendChanges();
                    }
                    if (bookManual.getCount() > 0) {
                        player.dropItem(bookManual, false, false);
                    }
                }

                return ActionResultType.func_233537_a_(world.isRemote());
            }
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;

            if (!world.isRemote()) {
                controller.forceStep();
            }

            return ActionResultType.func_233537_a_(world.isRemote());
        }

        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(final BlockState state, final World world, final BlockPos pos) {
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
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos, final Block block, final BlockPos fromPos, final boolean isMoving) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityController) {
            final TileEntityController controller = (TileEntityController) tileEntity;
            controller.checkNeighbors();
        }
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }
}
