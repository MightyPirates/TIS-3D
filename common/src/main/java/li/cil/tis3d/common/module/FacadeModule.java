package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import li.cil.tis3d.api.module.traits.ModuleWithBlockChangeListener;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.util.BlockStateUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public final class FacadeModule extends AbstractModule implements ModuleWithBlockChangeListener, ModuleWithBakedModel {
    // --------------------------------------------------------------------- //
    // Persisted data

    private BlockState facadeState;

    // --------------------------------------------------------------------- //
    // Computed data

    // Error message when trying to configure with an incompatible block.
    public static final Component MESSAGE_FACADE_INVALID_TARGET = Component.translatable("tis3d.facade.invalid_target");

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
    public boolean use(final Player player, final InteractionHand hand, final Vec3 hit) {
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
    public void onData(final CompoundTag data) {
        load(data);

        // Force re-render to make change of facade configuration visible.
        final Level level = getCasing().getCasingLevel();
        final BlockPos position = getCasing().getPosition();
        final BlockState state = level.getBlockState(position);
        level.sendBlockUpdated(position, state, state, Block.UPDATE_ALL);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        facadeState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(TAG_STATE));
        if (facadeState == Blocks.AIR.defaultBlockState()) {
            facadeState = null;
        }
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);

        if (facadeState != null) {
            tag.put(TAG_STATE, NbtUtils.writeBlockState(facadeState));
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

    @Environment(EnvType.CLIENT)
    @Override
    public OptionalInt getTintColor(@Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex) {
        return OptionalInt.of(Minecraft.getInstance().getBlockColors().getColor(facadeState, level, pos, tintIndex));
    }

    // --------------------------------------------------------------------- //

    private boolean trySetFacadeState(final BlockState state) {
        if (state.getRenderShape() != RenderShape.MODEL ||
            !state.isSolidRender(getCasing().getCasingLevel(), getCasing().getPosition()) ||
            state.getBlock() instanceof EntityBlock) {
            return false;
        }

        if (!getCasing().getCasingLevel().isClientSide()) {
            facadeState = state;
            sendState();
            getCasing().setChanged();
        }

        return true;
    }

    private void sendState() {
        final CompoundTag tag = new CompoundTag();
        save(tag);
        getCasing().sendData(getFace(), tag, DATA_TYPE_FULL);
    }
}
