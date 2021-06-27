package li.cil.manual.api.util;

import li.cil.manual.api.Manual;
import li.cil.manual.api.provider.ContentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.provider.RendererProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Objects;

/**
 * This is used when associating various registry objects, such as {@link ContentProvider}s,
 * {@link PathProvider}s and {@link RendererProvider}s with manuals that may need some particular
 * order in which they are queried, e.g. to overwrite some other existing provider.
 *
 * @param <T> the generic type of the entry.
 */
public interface ComparableRegistryEntry<T> extends IForgeRegistryEntry<T>, Comparable<ComparableRegistryEntry<T>> {
    /**
     * Checks if this instance applies to the specified manual and should be used
     * in the internal logic of the manual, such as looking up paths and content.
     *
     * @param manual the manual to check.
     * @return {@code true} if this instance applies to the manual, {@code false} otherwise.
     */
    default boolean matches(final Manual manual) {
        final ResourceLocation entryId = this.getRegistryName();
        final ResourceLocation manualId = manual.getRegistryName();
        return entryId != null && manualId != null &&
               Objects.equals(entryId.getNamespace(), manualId.getNamespace());
    }

    /**
     * The sort order of this instance.
     * <p>
     * Registry entries are sorted using this order before being queried. For example,
     * this may be used by {@link ContentProvider}s to replace a more generic
     * default provider.
     *
     * @return the sort order of this instance.
     */
    default int sortOrder() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default int compareTo(final ComparableRegistryEntry<T> other) {
        return Integer.compare(sortOrder(), other.sortOrder());
    }
}
