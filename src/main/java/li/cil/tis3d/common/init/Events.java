package li.cil.tis3d.common.init;

import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.TickEvent;

@SuppressWarnings("unused")
public class Events implements ModInitializer {
    @Override
    public void onInitialize() {
        TickEvent.SERVER.register(TickHandlerInfraredPacket.INSTANCE::serverTick);
        TickEvent.SERVER.register(Network.INSTANCE::serverTick);
    }
}
