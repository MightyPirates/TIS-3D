package li.cil.tis3d.common.provider.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.ModuleProvider;
import li.cil.tis3d.common.item.ModuleItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public final class SimpleModuleProvider<T extends Module> extends ForgeRegistryEntry<ModuleProvider> implements ModuleProvider {
    private final RegistryObject<? extends ModuleItem> module;
    private final BiFunction<Casing, Face, T> moduleConstructor;

    public SimpleModuleProvider(final RegistryObject<? extends ModuleItem> module, final BiFunction<Casing, Face, T> moduleConstructor) {
        this.module = module;
        this.moduleConstructor = moduleConstructor;
    }

    public boolean matches(final ItemStack stack, final Casing casing, final Face face) {
        return stack.getItem() == module.get();
    }

    @Nullable
    @Override
    public Module createModule(final ItemStack stack, final Casing casing, final Face face) {
        return moduleConstructor.apply(casing, face);
    }
}
