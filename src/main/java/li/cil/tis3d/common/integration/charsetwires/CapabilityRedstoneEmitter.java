package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.api.wires.IRedstoneEmitter;

public final class CapabilityRedstoneEmitter {
    public static final ResourceLocation PROVIDER_REDSTONE_EMITTER = new ResourceLocation(API.MOD_ID, "charset_redstone_emitter");

    @CapabilityInject(IRedstoneEmitter.class)
    public static Capability<IRedstoneEmitter> INSTANCE = null;

    public static class Provider implements ICapabilityProvider {
        private final RedstoneEmitter[] emitters = new RedstoneEmitter[EnumFacing.VALUES.length];

        public Provider(final TileEntityCasing tileEntity) {
            for (final EnumFacing facing : EnumFacing.VALUES) {
                emitters[facing.ordinal()] = new RedstoneEmitter(tileEntity, facing);
            }
        }

        @Override
        public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
            if (capability != INSTANCE) {
                return false;
            }

            final Module module = emitters[facing.ordinal()].tileEntity.getModule(Face.fromEnumFacing(facing));
            return module instanceof Redstone;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
            if (hasCapability(capability, facing)) {
                return (T) this;
            }
            return null;
        }

        private static final class RedstoneEmitter implements IRedstoneEmitter {
            private final TileEntityCasing tileEntity;
            private final EnumFacing facing;

            private RedstoneEmitter(final TileEntityCasing tileEntity, final EnumFacing facing) {
                this.tileEntity = tileEntity;
                this.facing = facing;
            }

            @Override
            public int getRedstoneSignal() {
                final Module module = tileEntity.getModule(Face.fromEnumFacing(facing));
                if (module instanceof Redstone) {
                    final Redstone redstone = (Redstone) module;

                    return redstone.getRedstoneOutput();
                }
                return 0;
            }
        }
    }
}