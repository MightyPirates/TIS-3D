package li.cil.tis3d.common.item;

import dev.architectury.registry.CreativeTabRegistry;
import li.cil.tis3d.api.API;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Supplier;

public final class ModCreativeTabs {
    public static final Supplier<CreativeModeTab> COMMON = CreativeTabRegistry.create(
        new ResourceLocation(API.MOD_ID, "common"),
        builder -> builder
            .icon(() -> new ItemStack(Items.CONTROLLER.get()))
            .displayItems((features, output, hasPermissions) -> {
                BuiltInRegistries.ITEM.entrySet().stream()
                    .filter(entry -> entry.getKey().location().getNamespace().equals(API.MOD_ID))
                    .map(Map.Entry::getValue)
                    .forEach(item -> output.accept(new ItemStack(item)));
            }));

    public static void initialize() {
    }
}
