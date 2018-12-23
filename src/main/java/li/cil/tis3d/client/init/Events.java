package li.cil.tis3d.client.init;

import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.TickEvent;
import net.fabricmc.fabric.events.client.ClientTickEvent;

public class Events implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvent.CLIENT.register((c) -> Network.INSTANCE.clientTick());
    }
}
