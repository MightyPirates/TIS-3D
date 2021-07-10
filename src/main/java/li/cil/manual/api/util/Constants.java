package li.cil.manual.api.util;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.Tab;
import li.cil.manual.api.provider.ContentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.provider.RendererProvider;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public final class Constants {
    public static final String MOD_ID = "markdown_manual";

    // ----------------------------------------------------------------------- //

    public static final RegistryKey<Registry<ContentProvider>> CONTENT_PROVIDERS = key("content_providers");
    public static final RegistryKey<Registry<PathProvider>> PATH_PROVIDERS = key("path_providers");
    public static final RegistryKey<Registry<RendererProvider>> RENDERER_PROVIDERS = key("renderer_providers");
    public static final RegistryKey<Registry<Tab>> TABS = key("tabs");
    public static final RegistryKey<Registry<ManualModel>> MANUALS = key("manuals");

    // ----------------------------------------------------------------------- //

    private static <T> RegistryKey<Registry<T>> key(final String name) {
        return RegistryKey.createRegistryKey(new ResourceLocation(MOD_ID, name));
    }

    // ----------------------------------------------------------------------- //

    private Constants() {
    }
}
