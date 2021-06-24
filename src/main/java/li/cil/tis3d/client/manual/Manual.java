package li.cil.tis3d.client.manual;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.manual.ContentProvider;
import li.cil.tis3d.api.manual.ImageProvider;
import li.cil.tis3d.api.manual.PathProvider;
import li.cil.tis3d.api.manual.Tab;
import li.cil.tis3d.api.prefab.manual.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.api.prefab.manual.TextureTabIconRenderer;
import li.cil.tis3d.client.manual.provider.*;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class Manual {
    private static final DeferredRegister<Tab> TABS = DeferredRegister.create(Tab.class, API.MOD_ID);
    private static final DeferredRegister<PathProvider> PATH_PROVIDERS = DeferredRegister.create(PathProvider.class, API.MOD_ID);
    private static final DeferredRegister<ContentProvider> CONTENT_PROVIDERS = DeferredRegister.create(ContentProvider.class, API.MOD_ID);
    private static final DeferredRegister<ImageProvider> IMAGE_PROVIDERS = DeferredRegister.create(ImageProvider.class, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final Supplier<IForgeRegistry<Tab>> TAB_REGISTRY = TABS.makeRegistry("manual_tabs", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<PathProvider>> PATH_PROVIDER_REGISTRY = PATH_PROVIDERS.makeRegistry("manual_path_providers", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<ContentProvider>> CONTENT_PROVIDER_REGISTRY = CONTENT_PROVIDERS.makeRegistry("manual_content_providers", RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<ImageProvider>> IMAGE_PROVIDER_REGISTRY = IMAGE_PROVIDERS.makeRegistry("manual_image_providers", RegistryBuilder::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        PATH_PROVIDERS.register("game_registry", ModPathProvider::new);
        CONTENT_PROVIDERS.register("resources", () -> new ResourceContentProvider(API.MOD_ID, "doc/"));
        CONTENT_PROVIDERS.register("serial_protocols", SerialProtocolContentProvider::new);

        IMAGE_PROVIDERS.register("texture", TextureImageProvider::new);
        IMAGE_PROVIDERS.register("item", ItemImageProvider::new);
        IMAGE_PROVIDERS.register("block", BlockImageProvider::new);
        IMAGE_PROVIDERS.register("tag", TagImageProvider::new);

        TABS.register("home", () -> new TextureTabIconRenderer(
            "%LANGUAGE%/index.md",
            new TranslationTextComponent("tis3d.manual.home"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")));
        TABS.register("blocks", () -> new ItemStackTabIconRenderer(
            "%LANGUAGE%/block/index.md",
            new TranslationTextComponent("tis3d.manual.blocks"),
            new ItemStack(Blocks.CONTROLLER.get())));
        TABS.register("modules", () -> new ItemStackTabIconRenderer(
            "%LANGUAGE%/item/index.md",
            new TranslationTextComponent("tis3d.manual.items"),
            new ItemStack(Items.EXECUTION_MODULE.get())));
        TABS.register("serial_protocols", () -> new TextureTabIconRenderer(
            "%LANGUAGE%/serial_protocols.md",
            new TranslationTextComponent("tis3d.manual.serial_protocols"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")));

        TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PATH_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTENT_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        IMAGE_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
