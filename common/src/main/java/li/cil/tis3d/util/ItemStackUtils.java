/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

public final class ItemStackUtils {
    public static CompoundTag getData(final ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void updateData(final ItemStack stack, final Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }

    // --------------------------------------------------------------------- //

    private ItemStackUtils() {
    }
}
