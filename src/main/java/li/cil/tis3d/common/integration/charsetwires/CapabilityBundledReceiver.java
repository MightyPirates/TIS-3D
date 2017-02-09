package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.api.wires.IBundledReceiver;

import javax.annotation.Nullable;

public final class CapabilityBundledReceiver {
    public static final ResourceLocation PROVIDER_BUNDLED_RECEIVER = new ResourceLocation(API.MOD_ID, "charset_bundled_receiver");

    @CapabilityInject(IBundledReceiver.class)
    public static Capability<IBundledReceiver> INSTANCE = null;

    public static class Provider implements ICapabilityProvider, IBundledReceiver {
        private final TileEntityCasing tileEntity;

        public Provider(final TileEntityCasing tileEntity) {
            this.tileEntity = tileEntity;
        }

        @Override
        public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
            if (capability != INSTANCE) {
                return false;
            }
            if (facing == null) {
                return false;
            }

            final Module module = tileEntity.getModule(Face.fromEnumFacing(facing));
            return module instanceof BundledRedstone;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
            if (hasCapability(capability, facing)) {
                return (T) this;
            }
            return null;
        }

        @Override
        public void onBundledInputChange() {
            tileEntity.markRedstoneDirty();
        }
    }
}
