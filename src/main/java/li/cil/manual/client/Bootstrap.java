package li.cil.manual.client;

import li.cil.manual.api.util.Constants;
import li.cil.manual.api.Manual;
import li.cil.manual.api.Tab;
import li.cil.manual.api.provider.ContentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.provider.RendererProvider;
import li.cil.manual.client.provider.BlockRendererProvider;
import li.cil.manual.client.provider.ItemRendererProvider;
import li.cil.manual.client.provider.TagRendererProvider;
import li.cil.manual.client.provider.TextureRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        final DeferredRegister<Manual> manuals = DeferredRegister.create(Manual.class, Constants.MOD_ID);

        pathProviders.makeRegistry(Constants.PATH_PROVIDERS.location().getPath(), RegistryBuilder::new);
        contentProviders.makeRegistry(Constants.CONTENT_PROVIDERS.location().getPath(), RegistryBuilder::new);
        rendererProviders.makeRegistry(Constants.RENDERER_PROVIDERS.location().getPath(), RegistryBuilder::new);
        tabs.makeRegistry(Constants.TABS.location().getPath(), RegistryBuilder::new);
        manuals.makeRegistry(Constants.MANUALS.location().getPath(), RegistryBuilder::new);

        rendererProviders.register("texture", TextureRendererProvider::new);
        rendererProviders.register("item", ItemRendererProvider::new);
        rendererProviders.register("block", BlockRendererProvider::new);
        rendererProviders.register("tag", TagRendererProvider::new);

        pathProviders.register(FMLJavaModLoadingContext.get().getModEventBus());
        contentProviders.register(FMLJavaModLoadingContext.get().getModEventBus());
        rendererProviders.register(FMLJavaModLoadingContext.get().getModEventBus());
        tabs.register(FMLJavaModLoadingContext.get().getModEventBus());
        manuals.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
