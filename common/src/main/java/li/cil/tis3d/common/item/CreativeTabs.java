package li.cil.tis3d.common.item;

import dev.architectury.registry.CreativeTabRegistry;
import li.cil.tis3d.api.API;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class CreativeTabs {
    public static final CreativeModeTab COMMON = CreativeTabRegistry.create(
        new ResourceLocation(API.MOD_ID, "common"),
        () -> new ItemStack(Items.CONTROLLER.get()));
}
