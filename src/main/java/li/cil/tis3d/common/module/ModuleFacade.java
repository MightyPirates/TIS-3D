package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.BlockChangeAware;
import li.cil.tis3d.api.module.traits.CasingFaceQuadOverride;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.util.BlockStateUtils;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class ModuleFacade extends AbstractModule implements BlockChangeAware, CasingFaceQuadOverride {
    // --------------------------------------------------------------------- //
    // Persisted data

    private BlockState facadeState;

    // --------------------------------------------------------------------- //
    // Computed data

    // Error message when trying to configure with an incompatible block.
    public static final TranslationTextComponent MESSAGE_FACADE_INVALID_TARGET = new TranslationTextComponent("tis3d.facade.invalid_target");

    // Data packet types.
    private static final byte DATA_TYPE_FULL = 0;

    // NBT tag names.
    private static final String TAG_STATE = "state";

    // --------------------------------------------------------------------- //

    public ModuleFacade(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vector3d hit) {
        if (getCasing().isLocked()) {
            return false;
        }

        final ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return false;
        }

        final BlockState state = BlockStateUtils.getBlockStateFromItemStack(stack);
        if (state == null) {
            return false;
        }

        if (!trySetFacadeState(state)) {
            if (!getCasing().getCasingWorld().isRemote()) {
                player.sendStatusMessage(MESSAGE_FACADE_INVALID_TARGET, true);
            }

            // No return false here to avoid popping out module due to invalid block.
        }

        return true;
    }

    @Override
    public void onData(final CompoundNBT nbt) {
        readFromNBT(nbt);

        // Force re-render to make change of facade configuration visible.
        final World world = getCasing().getCasingWorld();
        final BlockPos position = getCasing().getPosition();
        final BlockState state = world.getBlockState(position);
        world.notifyBlockUpdate(position, state, state, 1);
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
        super.readFromNBT(nbt);

        facadeState = NBTUtil.readBlockState(nbt.getCompound(TAG_STATE));
        if (facadeState == Blocks.AIR.getDefaultState()) {
            facadeState = null;
        }
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
        super.writeToNBT(nbt);

        if (facadeState != null) {
            nbt.put(TAG_STATE, NBTUtil.writeBlockState(facadeState));
        }
    }

    // --------------------------------------------------------------------- //
    // BlockChangeAware

    @Override
    public void onNeighborBlockChange(final BlockPos neighborPos, final boolean isModuleNeighbor) {
        if (!isModuleNeighbor) {
            return;
        }

        if (getCasing().isLocked()) {
            return;
        }

        trySetFacadeState(getCasing().getCasingWorld().getBlockState(neighborPos));
    }

    // --------------------------------------------------------------------- //
    // CasingFaceQuadOverride

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> getCasingFaceQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random random) {
        if (facadeState == null) {
            return Collections.emptyList();
        }

        final BlockModelShapes shapes = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes();
        final IBakedModel model = shapes.getModel(facadeState);
        final World world = getCasing().getCasingWorld();
        final BlockPos position = getCasing().getPosition();
        final IModelData modelData = model.getModelData(world, position, facadeState, EmptyModelData.INSTANCE);
        return model.getQuads(facadeState, side, random, modelData);
    }

    // --------------------------------------------------------------------- //

    private boolean trySetFacadeState(final BlockState state) {
        if (state.getRenderType() != BlockRenderType.MODEL ||
            !state.isOpaqueCube(getCasing().getCasingWorld(), getCasing().getPosition()) ||
            state.getBlock().hasTileEntity(state)) {
            return false;
        }

        if (!getCasing().getCasingWorld().isRemote()) {
            facadeState = state;
            sendState();
        }

        return true;
    }

    private void sendState() {
        final CompoundNBT nbt = new CompoundNBT();
        writeToNBT(nbt);
        getCasing().sendData(getFace(), nbt, DATA_TYPE_FULL);
    }
}
