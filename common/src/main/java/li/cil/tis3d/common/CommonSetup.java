package li.cil.tis3d.common;

import dev.architectury.event.events.common.TickEvent;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.common.api.InfraredAPIImpl;
import li.cil.tis3d.common.event.InfraredPacketTickHandler;
import li.cil.tis3d.common.item.ModCreativeTabs;
import li.cil.tis3d.common.network.Network;

public final class CommonSetup {
    public static void run() {
        API.itemGroup = ModCreativeTabs.COMMON;
        API.infraredAPI = new InfraredAPIImpl();

        Network.initialize();
        InfraredPacketTickHandler.initialize();

        TickEvent.SERVER_POST.register(level -> AbstractModule.MainThreadDisposer.disposeModules());
    }
}
