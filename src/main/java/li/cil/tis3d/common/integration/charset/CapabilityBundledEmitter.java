package li.cil.tis3d.common.integration.charset;

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
import pl.asie.charset.api.wires.IBundledEmitter;

import javax.annotation.Nullable;

public final class CapabilityBundledEmitter {
    public static final ResourceLocation PROVIDER_BUNDLED_EMITTER = new ResourceLocation(API.MOD_ID, "charset_bundled_emitter");

    @CapabilityInject(IBundledEmitter.class)
    public static Capability<IBundledEmitter> INSTANCE = null;

    public static class Provider implements ICapabilityProvider {
        private final BundledEmitter[] emitters = new BundledEmitter[EnumFacing.VALUES.length];

        public Provider(final TileEntityCasing tileEntity) {
            for (final EnumFacing facing : EnumFacing.VALUES) {
                emitters[facing.ordinal()] = new BundledEmitter(tileEntity, facing);
            }
        }

        @Override
        public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
            if (capability != INSTANCE) {
                return false;
            }
            if (facing == null) {
                return false;
            }

            final Module module = emitters[facing.ordinal()].tileEntity.getModule(Face.fromEnumFacing(facing));
            return module instanceof BundledRedstone;
        }

        @SuppressWarnings("unchecked")
        @Override
        @Nullable
        public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
            if (hasCapability(capability, facing) && facing != null) {
                return (T) emitters[facing.ordinal()];
            }
            return null;
        }

        private static final class BundledEmitter implements IBundledEmitter {
            private final TileEntityCasing tileEntity;
            private final EnumFacing facing;

            private BundledEmitter(final TileEntityCasing tileEntity, final EnumFacing facing) {
                this.tileEntity = tileEntity;
                this.facing = facing;
            }

            @Nullable
            @Override
            public byte[] getBundledSignal() {
                final Module module = tileEntity.getModule(Face.fromEnumFacing(facing));
                if (module instanceof BundledRedstone) {
                    final BundledRedstone bundledRedstone = (BundledRedstone) module;

                    final byte[] signal = new byte[16];
                    for (int channel = 0; channel < signal.length; channel++) {
                        signal[channel] = (byte) bundledRedstone.getBundledRedstoneOutput(channel);
                    }
                    return signal;
                }
                return null;
            }
        }
    }
}