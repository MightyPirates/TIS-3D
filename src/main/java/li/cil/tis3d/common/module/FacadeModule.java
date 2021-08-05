package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import li.cil.tis3d.api.module.traits.ModuleWithBlockChangeListener;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.util.BlockStateUtils;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Constants.BlockFlags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

public final class FacadeModule extends AbstractModule implements ModuleWithBlockChangeListener, ModuleWithBakedModel {
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

    public FacadeModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public boolean use(final PlayerEntity player, final Hand hand, final Vector3d hit) {
        if (getCasing().isLocked()) {
            return false;
        }

        final BlockState state = BlockStateUtils.getBlockStateFromItemStack(player.getItemInHand(hand));
        if (state == null) {
            return false;
        }

        if (!trySetFacadeState(state)) {
            if (!getCasing().getCasingLevel().isClientSide()) {
                player.displayClientMessage(MESSAGE_FACADE_INVALID_TARGET, true);
            }

            // No return false here to avoid popping out module due to invalid block.
        }

        return true;
    }

    @Override
    public void onData(final CompoundNBT nbt) {
        load(nbt);

        // Force re-render to make change of facade configuration visible.
        final World world = getCasing().getCasingLevel();
        final BlockPos position = getCasing().getPosition();
        final BlockState state = world.getBlockState(position);
        world.sendBlockUpdated(position, state, state, BlockFlags.DEFAULT);
    }

    @Override
    public void load(final CompoundNBT tag) {
        super.load(tag);

        facadeState = NBTUtil.readBlockState(tag.getCompound(TAG_STATE));
        if (facadeState == Blocks.AIR.defaultBlockState()) {
            facadeState = null;
        }
    }

    @Override
    public void save(final CompoundNBT tag) {
        super.save(tag);

        if (facadeState != null) {
            tag.put(TAG_STATE, NBTUtil.writeBlockState(facadeState));
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

        trySetFacadeState(getCasing().getCasingLevel().getBlockState(neighborPos));
    }

    // --------------------------------------------------------------------- //
    // CasingFaceQuadOverride

    @Override
    public boolean hasModel() {
        return facadeState != null;
    }

    @Override
    public IModelData getModelData(final IBlockDisplayReader world, final BlockPos pos, final BlockState state, final IModelData data) {
        final BlockModelShapes shapes = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        final IBakedModel model = shapes.getBlockModel(facadeState);
        return model.getModelData(world, pos, facadeState, data);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random random, final IModelData data) {
        final BlockModelShapes shapes = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        final IBakedModel model = shapes.getBlockModel(facadeState);
        final World world = getCasing().getCasingLevel();
        final BlockPos position = getCasing().getPosition();
        final IModelData modelData = model.getModelData(world, position, facadeState, data);
        return model.getQuads(facadeState, side, random, modelData);
    }

    @Override
    public boolean canRenderInLayer(@Nullable final RenderType layer) {
        return layer == null || RenderTypeLookup.canRenderInLayer(facadeState, layer);
    }

    @Override
    public OptionalInt getTintColor(@Nullable final IBlockDisplayReader level, @Nullable final BlockPos pos, final int tintIndex) {
        return OptionalInt.of(Minecraft.getInstance().getBlockColors().getColor(facadeState, level, pos, tintIndex));
    }

    // --------------------------------------------------------------------- //

    private boolean trySetFacadeState(final BlockState state) {
        if (state.getRenderShape() != BlockRenderType.MODEL ||
            !state.isSolidRender(getCasing().getCasingLevel(), getCasing().getPosition()) ||
            state.getBlock().hasTileEntity(state)) {
            return false;
        }

        if (!getCasing().getCasingLevel().isClientSide()) {
            facadeState = state;
            sendState();
        }

        return true;
    }

    private void sendState() {
        final CompoundNBT nbt = new CompoundNBT();
        save(nbt);
        getCasing().sendData(getFace(), nbt, DATA_TYPE_FULL);
    }
}
