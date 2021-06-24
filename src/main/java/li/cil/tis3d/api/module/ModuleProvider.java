package li.cil.tis3d.api.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * Creates a module instance for a specified item stack.
 * <p>
 * Providers should be as specific as possible in the implementation of their
 * {@link #worksWith(ItemStack, Casing, Face)} method. The first provider claiming to support
 * the specified parameters will be used to create a {@link Module} instance via its
 * {@link #createModule(ItemStack, Casing, Face)} method and the order in which providers are
 * queried is not guaranteed to be deterministic. As such, there should be no two providers
 * that can support the same {@link ItemStack}.
 * <p>
 * Additional providers may be registered with the {@link Registry} <tt>tis3d:modules</tt>.
 */
public interface ModuleProvider extends IForgeRegistryEntry<ModuleProvider> {
    /**
     * Checks whether the provider supports the specified stack.
     *
     * @param stack  the stack to check for.
     * @param casing the casing the module would be installed in.
     * @param face   the face the module would be installed on.
     * @return <tt>true</tt> if the stack is supported, <tt>false</tt> otherwise.
     */
    boolean worksWith(final ItemStack stack, final Casing casing, final Face face);

    /**
     * Creates a new module instance for the specified item stack.
     * <p>
     * Returns <tt>null</tt> if the specified item type is not supported.
     *
     * @param stack  the stack to get the module instance for.
     * @param casing the casing the module will be installed in.
     * @param face   the face the module will be installed on.
     * @return a new module instance, or <tt>null</tt>.
     */
    @Nullable
    Module createModule(final ItemStack stack, final Casing casing, final Face face);
}
