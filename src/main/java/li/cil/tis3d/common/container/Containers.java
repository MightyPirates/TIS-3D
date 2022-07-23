package li.cil.tis3d.common.container;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class Containers {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = RegistryUtils.getInitializerFor(ForgeRegistries.MENU_TYPES);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<MenuType<ReadOnlyMemoryModuleContainer>> READ_ONLY_MEMORY_MODULE = MENU_TYPES.register("read_only_memory_module", () -> IForgeMenuType.create(ReadOnlyMemoryModuleContainer::create));

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }
}
