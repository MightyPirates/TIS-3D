/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.neoforge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.ClientBootstrap;
import li.cil.tis3d.client.ClientSetup;
import li.cil.tis3d.client.gui.TerminalModuleScreen;
import li.cil.tis3d.client.renderer.block.neoforge.ModuleModelLoader;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(value = Dist.CLIENT, modid = API.MOD_ID)
public final class ClientSetupNeoForge {
    @SubscribeEvent
    public static void handleClientSetup(final FMLClientSetupEvent ignoredEvent) {
        ClientBootstrap.run();
        ClientSetup.run();

        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Pre event) -> {
            if (Minecraft.getInstance().screen instanceof TerminalModuleScreen) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void handleBlockStateModelRegistry(final RegisterBlockStateModels event) {
        ModuleModelLoader.register(event);
    }
}
