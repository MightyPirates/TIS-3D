package li.cil.tis3d.client.forge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.ClientSetup;
import li.cil.tis3d.client.gui.TerminalModuleScreen;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.block.forge.ModuleModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = API.MOD_ID)
public final class ClientSetupForge {
    @SubscribeEvent
    public static void handleClientSetup(final FMLClientSetupEvent ignoredEvent) {
        ClientSetup.run();

        MinecraftForge.EVENT_BUS.addListener((RenderGuiOverlayEvent.Pre event) -> {
            if (Minecraft.getInstance().screen instanceof TerminalModuleScreen) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void handleModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        event.register("module", new ModuleModelLoader());
    }

    @SubscribeEvent
    public static void handleTextureStitchEvent(TextureStitchEvent.Pre event) {
        if (Objects.equals(event.getAtlas().location(), InventoryMenu.BLOCK_ATLAS)) {
            Textures.visitBlockAtlasTextures(event::addSprite);
        }
    }
}
