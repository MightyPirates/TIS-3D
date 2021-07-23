package li.cil.tis3d.common.container;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class Containers {
    private static final DeferredRegister<MenuType<?>> CONTAINERS = RegistryUtils.create(ForgeRegistries.CONTAINERS);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<MenuType<ReadOnlyMemoryModuleContainer>> READ_ONLY_MEMORY_MODULE = CONTAINERS.register("read_only_memory_module", () -> IForgeContainerType.create(ReadOnlyMemoryModuleContainer::create));

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }
}
