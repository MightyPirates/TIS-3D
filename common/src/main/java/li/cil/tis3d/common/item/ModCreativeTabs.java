package li.cil.tis3d.common.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.api.API;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(API.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> COMMON = TABS.register("common", () ->
        CreativeTabRegistry.create(builder -> {
            builder.icon(() -> new ItemStack(Items.CONTROLLER.get()));
            builder.title(Component.translatable("itemGroup.tis3d.common"));
            builder.displayItems((parameters, output) -> {
                BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(entry -> entry.getKey().location().getNamespace().equals(API.MOD_ID))
                    .map(Map.Entry::getValue)
                    .filter(ModCreativeTabs::isItemEnabled)
                    .forEach(item -> output.accept(new ItemStack(item)));
            });
        }));

    public static void initialize() {
        TABS.register();
    }

    @ExpectPlatform
    private static boolean isItemEnabled(final Item item) {
        throw new AssertionError();
    }
}
