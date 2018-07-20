package li.cil.tis3d.api.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Creates a module instance for a specified item stack.
 */
public interface ModuleProvider {
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
