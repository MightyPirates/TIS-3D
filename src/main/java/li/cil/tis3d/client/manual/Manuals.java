package li.cil.tis3d.client.manual;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.Tab;
import li.cil.manual.api.prefab.Manual;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.manual.api.prefab.tab.ItemStackTab;
import li.cil.manual.api.prefab.tab.TextureTab;
import li.cil.manual.api.provider.DocumentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.manual.provider.ModPathProvider;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

@OnlyIn(Dist.CLIENT)
public final class Manuals {
    private static final DeferredRegister<ManualModel> MANUALS = RegistryUtils.create(ManualModel.class);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<ManualModel> MANUAL = MANUALS.register("manual", Manual::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        final DeferredRegister<PathProvider> pathProviders = RegistryUtils.create(PathProvider.class);
        final DeferredRegister<DocumentProvider> documentProviders = RegistryUtils.create(DocumentProvider.class);
        final DeferredRegister<Tab> tabs = RegistryUtils.create(Tab.class);

        pathProviders.register("path_provider", ModPathProvider::new);
        documentProviders.register("content_provider", () -> new NamespaceDocumentProvider(API.MOD_ID, "doc"));
        documentProviders.register("serial_protocols", SerialProtocolContentProvider::new);

        tabs.register("home", () -> new TextureTab(
                ManualModel.LANGUAGE_KEY + "/index.md",
            new TranslatableComponent("tis3d.manual.home"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")));
        tabs.register("blocks", () -> new ItemStackTab(
                ManualModel.LANGUAGE_KEY + "/block/index.md",
            new TranslatableComponent("tis3d.manual.blocks"),
            new ItemStack(Blocks.CONTROLLER.get())));
        tabs.register("modules", () -> new ItemStackTab(
                ManualModel.LANGUAGE_KEY + "/item/index.md",
            new TranslatableComponent("tis3d.manual.items"),
            new ItemStack(Items.EXECUTION_MODULE.get())));
        tabs.register("serial_protocols", () -> new TextureTab(
                ManualModel.LANGUAGE_KEY + "/protocols/index.md",
            new TranslatableComponent("tis3d.manual.serial_protocols"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")));
    }
}
