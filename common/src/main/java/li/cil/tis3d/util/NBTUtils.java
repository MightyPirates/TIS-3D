/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.util;

import li.cil.tis3d.api.API;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public final class NBTUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static CompoundTag saveToTag(final ProblemReporter.PathElement path, final HolderLookup.Provider registries, final Consumer<ValueOutput> saver) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(path, LOGGER)) {
            final TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saver.accept(output);
            return output.buildResult();
        }
    }

    public static void loadFromTag(final ProblemReporter.PathElement path, final HolderLookup.Provider registries, final CompoundTag tag, final Consumer<ValueInput> loader) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(path, LOGGER)) {
            loader.accept(TagValueInput.create(reporter, registries, tag));
        }
    }

    // --------------------------------------------------------------------- //

    private NBTUtils() {
    }
}
