package li.cil.tis3d.common.container;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.MenuType;

public final class Containers {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = RegistryUtils.get(Registry.MENU_REGISTRY);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<MenuType<ReadOnlyMemoryModuleContainer>> READ_ONLY_MEMORY_MODULE = MENU_TYPES.register("read_only_memory_module", () -> MenuRegistry.ofExtended(ReadOnlyMemoryModuleContainer::create));

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MENU_TYPES.register();
    }
}
