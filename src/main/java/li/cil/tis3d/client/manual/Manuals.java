package li.cil.tis3d.client.manual;

import li.cil.manual.api.Manual;
import li.cil.manual.api.Tab;
import li.cil.manual.api.prefab.ItemStackTab;
import li.cil.manual.api.prefab.NamespaceContentProvider;
import li.cil.manual.api.prefab.TextureTab;
import li.cil.manual.api.provider.ContentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.manual.provider.ModPathProvider;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

@OnlyIn(Dist.CLIENT)
public final class Manuals {
    private static final DeferredRegister<Tab> TABS = DeferredRegister.create(Tab.class, API.MOD_ID);
    private static final DeferredRegister<PathProvider> PATH_PROVIDERS = DeferredRegister.create(PathProvider.class, API.MOD_ID);
    private static final DeferredRegister<ContentProvider> CONTENT_PROVIDERS = DeferredRegister.create(ContentProvider.class, API.MOD_ID);
    private static final DeferredRegister<Manual> MANUALS = DeferredRegister.create(Manual.class, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Manual> MANUAL = MANUALS.register("manual", Manual::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        PATH_PROVIDERS.register("game_registry", ModPathProvider::new);
        CONTENT_PROVIDERS.register("resources", () -> new NamespaceContentProvider(API.MOD_ID, "doc"));
        CONTENT_PROVIDERS.register("serial_protocols", SerialProtocolContentProvider::new);

        TABS.register("home", () -> new TextureTab(
            Manual.LANGUAGE_KEY + "/index.md",
            new TranslationTextComponent("tis3d.manual.home"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")));
        TABS.register("blocks", () -> new ItemStackTab(
            Manual.LANGUAGE_KEY + "/block/index.md",
            new TranslationTextComponent("tis3d.manual.blocks"),
            new ItemStack(Blocks.CONTROLLER.get())));
        TABS.register("modules", () -> new ItemStackTab(
            Manual.LANGUAGE_KEY + "/item/index.md",
            new TranslationTextComponent("tis3d.manual.items"),
            new ItemStack(Items.EXECUTION_MODULE.get())));
        TABS.register("serial_protocols", () -> new TextureTab(
            Manual.LANGUAGE_KEY + "/protocols/index.md",
            new TranslationTextComponent("tis3d.manual.serial_protocols"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")));

        TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PATH_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTENT_PROVIDERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MANUALS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
