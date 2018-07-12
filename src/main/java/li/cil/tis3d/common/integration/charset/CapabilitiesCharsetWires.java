package li.cil.tis3d.common.integration.charset;

import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public enum CapabilitiesCharsetWires {
    INSTANCE;

    // --------------------------------------------------------------------- //

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CapabilitiesCharsetWires.INSTANCE);
    }

    @SubscribeEvent
    public void onAttachTileEntityCapabilities(final AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileEntityCasing) {
            final TileEntityCasing tileEntity = (TileEntityCasing) event.getObject();
            event.addCapability(CapabilityRedstoneEmitter.PROVIDER_REDSTONE_EMITTER, new CapabilityRedstoneEmitter.Provider(tileEntity));
            event.addCapability(CapabilityRedstoneReceiver.PROVIDER_REDSTONE_RECEIVER, new CapabilityRedstoneReceiver.Provider(tileEntity));
            event.addCapability(CapabilityBundledEmitter.PROVIDER_BUNDLED_EMITTER, new CapabilityBundledEmitter.Provider(tileEntity));
            event.addCapability(CapabilityBundledReceiver.PROVIDER_BUNDLED_RECEIVER, new CapabilityBundledReceiver.Provider(tileEntity));
        }
    }
}
