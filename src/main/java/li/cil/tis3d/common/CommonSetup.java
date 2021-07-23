package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.api.InfraredAPIImpl;
import li.cil.tis3d.common.capabilities.Capabilities;
import li.cil.tis3d.common.event.InfraredPacketTickHandler;
import li.cil.tis3d.common.event.LevelUnloadHandler;
import li.cil.tis3d.common.item.ItemGroups;
import li.cil.tis3d.common.network.Network;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CommonSetup {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLCommonSetupEvent event) {
        API.itemGroup = ItemGroups.COMMON;
        API.infraredAPI = new InfraredAPIImpl();

        Capabilities.initialize();

        Network.initialize();
        InfraredPacketTickHandler.initialize();
        LevelUnloadHandler.initialize();
    }
}
