package li.cil.tis3d.client.manual;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.Manual;
import li.cil.manual.api.prefab.provider.NamespaceDocumentProvider;
import li.cil.manual.api.prefab.tab.ItemStackTab;
import li.cil.manual.api.prefab.tab.TextureTab;
import li.cil.manual.api.util.Constants;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.manual.provider.ModPathProvider;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class Manuals {
    private static final DeferredRegister<ManualModel> MANUALS = RegistryUtils.get(Constants.MANUAL_REGISTRY);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<ManualModel> MANUAL = MANUALS.register("manual", Manual::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        final var pathProviders = RegistryUtils.get(Constants.PATH_PROVIDER_REGISTRY);
        final var documentProviders = RegistryUtils.get(Constants.DOCUMENT_PROVIDER_REGISTRY);
        final var tabs = RegistryUtils.get(Constants.TAB_REGISTRY);

        pathProviders.register("path_provider", ModPathProvider::new);
        documentProviders.register("content_provider", () -> new NamespaceDocumentProvider(API.MOD_ID, "doc"));
        documentProviders.register("serial_protocols", SerialProtocolContentProvider::new);

        tabs.register("home", () -> new TextureTab(
            ManualModel.LANGUAGE_KEY + "/index.md",
            Component.translatable("tis3d.manual.home"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_home.png")));
        tabs.register("blocks", () -> new ItemStackTab(
            ManualModel.LANGUAGE_KEY + "/block/index.md",
            Component.translatable("tis3d.manual.blocks"),
            new ItemStack(Blocks.CONTROLLER.get())));
        tabs.register("modules", () -> new ItemStackTab(
            ManualModel.LANGUAGE_KEY + "/item/index.md",
            Component.translatable("tis3d.manual.items"),
            new ItemStack(Items.EXECUTION_MODULE.get())));
        tabs.register("serial_protocols", () -> new TextureTab(
            ManualModel.LANGUAGE_KEY + "/protocols/index.md",
            Component.translatable("tis3d.manual.serial_protocols"),
            new ResourceLocation(API.MOD_ID, "textures/gui/manual_serial_protocols.png")));
    }
}
