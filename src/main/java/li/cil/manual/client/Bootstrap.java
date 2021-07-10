package li.cil.manual.client;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.ManualStyle;
import li.cil.manual.api.Tab;
import li.cil.manual.api.provider.ContentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.provider.RendererProvider;
import li.cil.manual.api.util.Constants;
import li.cil.manual.api.util.ShowManualScreenEvent;
import li.cil.manual.client.gui.ManualScreen;
import li.cil.manual.client.provider.BlockRendererProvider;
import li.cil.manual.client.provider.ItemRendererProvider;
import li.cil.manual.client.provider.TagRendererProvider;
import li.cil.manual.client.provider.TextureRendererProvider;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

@OnlyIn(Dist.CLIENT)
public final class Bootstrap {
    public static void initialize() {
        final DeferredRegister<Tab> tabs = DeferredRegister.create(Tab.class, Constants.MOD_ID);
        final DeferredRegister<PathProvider> pathProviders = DeferredRegister.create(PathProvider.class, Constants.MOD_ID);
        final DeferredRegister<ContentProvider> contentProviders = DeferredRegister.create(ContentProvider.class, Constants.MOD_ID);
        final DeferredRegister<RendererProvider> rendererProviders = DeferredRegister.create(RendererProvider.class, Constants.MOD_ID);
        final DeferredRegister<ManualModel> manuals = DeferredRegister.create(ManualModel.class, Constants.MOD_ID);

        pathProviders.makeRegistry(Constants.PATH_PROVIDERS.location().getPath(), () -> new RegistryBuilder<PathProvider>().disableSaving());
        contentProviders.makeRegistry(Constants.CONTENT_PROVIDERS.location().getPath(), () -> new RegistryBuilder<ContentProvider>().disableSaving());
        rendererProviders.makeRegistry(Constants.RENDERER_PROVIDERS.location().getPath(), () -> new RegistryBuilder<RendererProvider>().disableSaving());
        tabs.makeRegistry(Constants.TABS.location().getPath(), () -> new RegistryBuilder<Tab>().disableSaving());
        manuals.makeRegistry(Constants.MANUALS.location().getPath(), () -> new RegistryBuilder<ManualModel>().disableSaving());

        rendererProviders.register("texture", TextureRendererProvider::new);
        rendererProviders.register("item", ItemRendererProvider::new);
        rendererProviders.register("block", BlockRendererProvider::new);
        rendererProviders.register("tag", TagRendererProvider::new);

        pathProviders.register(FMLJavaModLoadingContext.get().getModEventBus());
        contentProviders.register(FMLJavaModLoadingContext.get().getModEventBus());
        rendererProviders.register(FMLJavaModLoadingContext.get().getModEventBus());
        tabs.register(FMLJavaModLoadingContext.get().getModEventBus());
        manuals.register(FMLJavaModLoadingContext.get().getModEventBus());

        MinecraftForge.EVENT_BUS.addListener(Bootstrap::handleShowManualScreen);
    }

    private static void handleShowManualScreen(final ShowManualScreenEvent event) {
        final ManualModel model = event.getManualModel();
        final ManualStyle manualStyle = event.getManualStyle().orElse(ManualStyle.DEFAULT);
        final ManualScreenStyle screenStyle = event.getScreenStyle().orElse(ManualScreenStyle.DEFAULT);

        final ManualScreen screen = new ManualScreen(model, manualStyle, screenStyle);
        Minecraft.getInstance().setScreen(screen);
    }
}
