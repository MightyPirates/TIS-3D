package li.cil.tis3d.util;

public final class IterableUtils {
    @FunctionalInterface
    public interface IntAndObjectConsumer<T> {
        void accept(final int i, final T t);
    }

    public static <T> void forEachWithIndex(final Iterable<T> iterable, final IntAndObjectConsumer<T> consumer) {
        int i = 0;
        for (final T t : iterable) {
            consumer.accept(i++, t);
        }
    }
}
