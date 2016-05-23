package li.cil.tis3d.common.integration.charsetwires;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pl.asie.charset.api.wires.IRedstoneReceiver;

public final class CapabilityRedstoneReceiver {
    public static final ResourceLocation PROVIDER_REDSTONE_RECEIVER = new ResourceLocation(API.MOD_ID, "charset_redstone_receiver");

    @CapabilityInject(IRedstoneReceiver.class)
    public static Capability<IRedstoneReceiver> INSTANCE = null;

    public static class Provider implements ICapabilityProvider, IRedstoneReceiver {
        private final TileEntityCasing tileEntity;

        public Provider(final TileEntityCasing tileEntity) {
            this.tileEntity = tileEntity;
        }

        @Override
        public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
            if (capability != INSTANCE) {
                return false;
            }

            final Module module = tileEntity.getModule(Face.fromEnumFacing(facing));
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

        @Override
        public void onRedstoneInputChange() {
            tileEntity.markRedstoneDirty();
        }
    }
}