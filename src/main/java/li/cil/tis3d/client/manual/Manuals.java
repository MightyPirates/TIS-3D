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
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

@OnlyIn(Dist.CLIENT)
public final class Manuals {
    private static final DeferredRegister<Manual> MANUALS = RegistryUtils.create(Manual.class);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<Manual> MANUAL = MANUALS.register("manual", Manual::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        final DeferredRegister<PathProvider> pathProviders = RegistryUtils.create(PathProvider.class);
        final DeferredRegister<ContentProvider> contentProviders = RegistryUtils.create(ContentProvider.class);
        final DeferredRegister<Tab> tabs = RegistryUtils.create(Tab.class);

        pathProviders.register("path_provider", ModPathProvider::new);
        contentProviders.register("content_provider", () -> new NamespaceContentProvider(API.MOD_ID, "doc"));
        contentProviders.register("serial_protocols", SerialProtocolContentProvider::new);

        tabs.register("home", () -> new TextureTab(
            Manual.LANGUAGE_KEY + "/index.md",
            new TranslationTextComponent("tis3d.manual.home"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")));
        tabs.register("blocks", () -> new ItemStackTab(
            Manual.LANGUAGE_KEY + "/block/index.md",
            new TranslationTextComponent("tis3d.manual.blocks"),
            new ItemStack(Blocks.CONTROLLER.get())));
        tabs.register("modules", () -> new ItemStackTab(
            Manual.LANGUAGE_KEY + "/item/index.md",
            new TranslationTextComponent("tis3d.manual.items"),
            new ItemStack(Items.EXECUTION_MODULE.get())));
        tabs.register("serial_protocols", () -> new TextureTab(
            Manual.LANGUAGE_KEY + "/protocols/index.md",
            new TranslationTextComponent("tis3d.manual.serial_protocols"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")));
    }
}
