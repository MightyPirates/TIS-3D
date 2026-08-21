/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.api.module.RedstoneInputProvider;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import li.cil.tis3d.common.item.ModuleItem;
import li.cil.tis3d.common.provider.module.SimpleModuleProvider;
import net.minecraft.core.registries.Registries;

import static li.cil.tis3d.gametest.TestSupport.MOD_ID;

public final class TestRegistry {
    public static final String TEST_MODULE_NAME = "test_module";

    private static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
        DeferredRegister.create(MOD_ID, Registries.ITEM);
    private static final DeferredRegister<ModuleProvider> MODULE_PROVIDERS =
        DeferredRegister.create(MOD_ID, ModuleProvider.REGISTRY);
    private static final DeferredRegister<RedstoneInputProvider> REDSTONE_INPUT_PROVIDERS =
        DeferredRegister.create(MOD_ID, RedstoneInputProvider.REGISTRY);
    private static final DeferredRegister<SerialInterfaceProvider> SERIAL_INTERFACE_PROVIDERS =
        DeferredRegister.create(MOD_ID, SerialInterfaceProvider.REGISTRY);

    public static final RegistrySupplier<ModuleItem> TEST_MODULE_ITEM =
        ITEMS.register(TEST_MODULE_NAME, ModuleItem::new);

    // --------------------------------------------------------------------- //

    public static void initialize() {
        MODULE_PROVIDERS.register(TEST_MODULE_NAME, () -> new SimpleModuleProvider<>(TEST_MODULE_ITEM, TestModule::new));
        REDSTONE_INPUT_PROVIDERS.register(TEST_MODULE_NAME, TestRedstoneInputProvider::new);
        SERIAL_INTERFACE_PROVIDERS.register(TEST_MODULE_NAME, TestSerialInterfaceProvider::new);

        ITEMS.register();
        MODULE_PROVIDERS.register();
        REDSTONE_INPUT_PROVIDERS.register();
        SERIAL_INTERFACE_PROVIDERS.register();
    }

    // --------------------------------------------------------------------- //

    private TestRegistry() {
    }
}
