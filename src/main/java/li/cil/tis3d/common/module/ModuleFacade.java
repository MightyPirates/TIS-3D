package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.BlockChangeAware;
import li.cil.tis3d.api.module.traits.CasingFaceQuadOverride;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.util.BlockStateUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public final class ModuleFacade extends AbstractModule implements BlockChangeAware, CasingFaceQuadOverride {
    // --------------------------------------------------------------------- //
    // Persisted data

    private IBlockState facadeState;

    // --------------------------------------------------------------------- //
    // Computed data

    // Data packet types.
    private static final byte DATA_TYPE_FULL = 0;

    // --------------------------------------------------------------------- //

    public ModuleFacade(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, @Nullable final ItemStack heldItem, final float hitX, final float hitY, final float hitZ) {
        if (getCasing().isLocked()) {
            return false;
        }

        if (heldItem == null || heldItem.stackSize < 1) {
            return false;
        }

        final IBlockState state = BlockStateUtils.getBlockStateFromItemStack(heldItem);
        if (state == null) {
            return false;
        }

        if (!trySetFacadeState(state)) {
            if (!getCasing().getCasingWorld().isRemote) {
                player.sendMessage(new TextComponentTranslation(Constants.MESSAGE_FACADE_INVALID_TARGET));
            }

            // No return false here to avoid popping out module due to invalid block.
        }

        return true;
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        readFromNBT(nbt);

        // Force re-render to make change of facade configuration visible.
        final World world = getCasing().getCasingWorld();
        final BlockPos position = getCasing().getPosition();
        final IBlockState state = world.getBlockState(position);
        world.notifyBlockUpdate(position, state, state, 1);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        facadeState = NBTUtil.readBlockState(nbt);
        if (facadeState == Blocks.AIR.getDefaultState()) {
            facadeState = null;
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (facadeState != null) {
            NBTUtil.writeBlockState(nbt, facadeState);
        }
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware


    @Override
    public void onNeighborBlockChange(final Block neighborBlock) {
        if (getCasing().isLocked()) {
            return;
        }

        final BlockPos neighborPos = getCasing().getPosition().offset(Face.toEnumFacing(getFace()));

        trySetFacadeState(getCasing().getCasingWorld().getBlockState(neighborPos));
    }

    // --------------------------------------------------------------------- //
    // CasingFaceQuadOverride

    @SideOnly(Side.CLIENT)
    @Override
    public List<BakedQuad> getCasingFaceQuads(@Nullable final IBlockState state, final EnumFacing side, final long randomSeed) {
        if (facadeState == null) {
            return null;
        }

        final BlockModelShapes shapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        return shapes.getModelForState(facadeState).getQuads(facadeState, side, randomSeed);
    }

    // --------------------------------------------------------------------- //

    private boolean trySetFacadeState(final IBlockState state) {
        if (state.getRenderType() != EnumBlockRenderType.MODEL || !state.isOpaqueCube() || state.getBlock().hasTileEntity(state)) {
            return false;
        }

        if (!getCasing().getCasingWorld().isRemote) {
            facadeState = state;
            sendState();
        }

        return true;
    }

    private void sendState() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        getCasing().sendData(getFace(), nbt, DATA_TYPE_FULL);
    }
}
