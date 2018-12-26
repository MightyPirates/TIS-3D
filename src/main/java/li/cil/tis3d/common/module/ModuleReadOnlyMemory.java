package li.cil.tis3d.common.module;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

/**
 * The RAM module can be used to store up to 256 values by address. It runs
 * as a basic state machine with the following states:
 * <ul>
 * <li>ADDRESS: await address input, no ports writing, all ports reading.</li>
 * <li>ACCESS: await either read to retrieve value or write to set value, all ports writing, all ports reading.</li>
 * </ul>
 */
public final class ModuleReadOnlyMemory extends ModuleRandomAccessMemory {
    public ModuleReadOnlyMemory(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void onInstalled(final ItemStack stack) {
        super.onInstalled(stack);

        load(ReadOnlyMemoryModuleItem.loadFromStack(stack));
    }

    @Override
    public void onUninstalled(final ItemStack stack) {
        super.onUninstalled(stack);

        // Write data back to the stack, our data may have changed in
        // the meantime (other ROM programmed into this one).
        ReadOnlyMemoryModuleItem.saveToStack(stack, memory);
    }

    // --------------------------------------------------------------------- //
    // ModuleRandomAccessMemory

    @Override
    protected void clearOnDisabled() {
    }

    @Override
    protected void beginRead(final Pipe pipe) {
        if (state == State.ADDRESS) {
            pipe.beginRead();
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void setCellColor(final float brightness) {
        GlStateManager.color4f(0.4f, 1, 0.4f, brightness);
    }
}
