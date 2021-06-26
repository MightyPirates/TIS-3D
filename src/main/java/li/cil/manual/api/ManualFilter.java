package li.cil.manual.api;

import li.cil.manual.api.provider.ContentProvider;
import li.cil.manual.api.provider.PathProvider;
import li.cil.manual.api.provider.RendererProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Objects;

/**
 * Interface implemented by manual-specific types.
 * <p>
 * This is used when associating various registry objects, such as {@link ContentProvider}s,
 * {@link PathProvider}s and {@link RendererProvider}s with manuals.
 * <p>
 * By default, the filter will only match manuals registered in the same namespace, usually
 * meaning the manual owned by the same mod as the providers.
 * <p>
 * To add global logic, override the {@link #matches(Manual)} method to always return {@code true}.
 * <p>
 * This filtering is necessary since there can only be one registry for any particular type,
 * so we have to collect all manual content in shared registries.
 *
 * @param <T> the generic type of the manual specific type.
 */
public interface ManualFilter<T> extends IForgeRegistryEntry<T>, Comparable<ManualFilter<T>> {
    /**
     * Checks if this instance applies to the specified manual and should be used
     * in the internal logic of the manual, such as looking up paths and content.
     *
     * @param manual the manual to check.
     * @return {@code true} if this instance applies to the manual, {@code false} otherwise.
     */
    default boolean matches(final Manual manual) {
        final ResourceLocation manualId = manual.getRegistryName();
        final ResourceLocation contentId = this.getRegistryName();
        return manualId != null && contentId != null &&
               Objects.equals(manualId.getNamespace(), contentId.getNamespace());
    }

    /**
     * The sort order of this instance.
     * <p>
     * Registry objects are sorted using this order before being queried. For example,
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
    default int compareTo(final ManualFilter<T> other) {
        return Integer.compare(sortOrder(), other.sortOrder());
    }
}
