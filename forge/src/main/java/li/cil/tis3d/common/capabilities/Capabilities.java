package li.cil.tis3d.common.capabilities;

import li.cil.tis3d.api.infrared.InfraredReceiver;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class Capabilities {
    public static Capability<InfraredReceiver> INFRARED_RECEIVER = CapabilityManager.get(new CapabilityToken<>() { });

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public static void initialize(final RegisterCapabilitiesEvent event) {
        event.register(InfraredReceiver.class);
    }
}
