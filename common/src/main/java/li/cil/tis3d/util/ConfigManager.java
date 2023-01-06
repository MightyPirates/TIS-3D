package li.cil.tis3d.util;

import com.google.common.base.Strings;
import dev.architectury.injectables.annotations.ExpectPlatform;
import li.cil.tis3d.util.config.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger();

    // --------------------------------------------------------------------- //

    private static final Map<Class<?>, ConfigFieldParser> PARSERS = new HashMap<>();
    private static final Map<Class<?>, Pair<Function<Object, String>, Function<String, Object>>> STRING_CONVERTERS = new HashMap<>();

    static {
        PARSERS.put(boolean.class, ConfigManager::parseBooleanField);
        PARSERS.put(int.class, ConfigManager::parseIntField);
        PARSERS.put(long.class, ConfigManager::parseLongField);
        PARSERS.put(double.class, ConfigManager::parseDoubleField);

        STRING_CONVERTERS.put(boolean.class, Pair.of(o -> String.valueOf((boolean) o), Boolean::parseBoolean));
        STRING_CONVERTERS.put(int.class, Pair.of(o -> String.valueOf((int) o), Integer::decode));
        STRING_CONVERTERS.put(long.class, Pair.of(o -> String.valueOf((long) o), Long::decode));
        STRING_CONVERTERS.put(double.class, Pair.of(o -> String.valueOf((double) o), Double::parseDouble));
        STRING_CONVERTERS.put(String.class, Pair.of(s -> (String) s, s -> s));
        STRING_CONVERTERS.put(UUID.class, Pair.of(Object::toString, UUID::fromString));
        STRING_CONVERTERS.put(ResourceLocation.class, Pair.of(Object::toString, ResourceLocation::new));
    }

    // --------------------------------------------------------------------- //

    @ExpectPlatform
    public static <T> void add(Supplier<T> factory) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void initialize() {
        throw new AssertionError();
    }

    // --------------------------------------------------------------------- //

    protected static <T> void fillSpec(final T instance, final Builder builder, final ArrayList<ConfigFieldPair<?>> values) {
        for (final Field field : instance.getClass().getFields()) {
            parseField(instance, builder, values, field);
        }
    }

    protected static ConfigDefinition createDefinition(Object instance, ArrayList<ConfigFieldPair<?>> values) {
        return new ConfigDefinition(instance, values);
    }

    private static <T> void parseField(final T instance, final Builder builder, final ArrayList<ConfigFieldPair<?>> values, final Field field) {
        try {
            if (Collection.class.isAssignableFrom(field.getType())) {
                final var annotation = field.getAnnotation(ItemType.class);
                if (annotation == null) {
                    LOGGER.error("Config field with collection type does not have an ItemType annotation.");
                    return;
                }

                parseCollectionField(instance, builder, values, field, annotation);
            } else if (Map.class.isAssignableFrom(field.getType())) {
                final var annotation = field.getAnnotation(KeyValueTypes.class);
                if (annotation == null) {
                    LOGGER.error("Config field with collection type does not have an ItemType annotation.");
                    return;
                }

                parseMapField(instance, builder, values, field, annotation);
            } else {
                parseRegularField(instance, builder, values, field);
            }
        } catch (final IllegalAccessException e) {
            LOGGER.error("Failed accessing field [{}.{}], ignoring.", field.getDeclaringClass().getName(), field.getName());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> void parseCollectionField(final T instance, final Builder builder, final ArrayList<ConfigFieldPair<?>> values, final Field field, final ItemType annotation) throws IllegalAccessException {
        final var serializers = getSerializerPair(instance, annotation.valueSerializer(), STRING_CONVERTERS.get(annotation.value()));
        if (serializers.getLeft() == null || serializers.getRight() == null) {
            LOGGER.error("Collection item type [{}] is not supported (in field [{}]).", annotation.value(), field);
            return;
        }

        final Collection collection = (Collection) field.get(instance);

        final List<String> serializedValues = new ArrayList<>();
        for (final Object value : collection) {
            final String serializedValue = serializers.getLeft().apply(value);
            if (value != null) {
                serializedValues.add(serializedValue);
            }
        }

        final var configValue = withCommonAttributes(field, builder)
            .define(getPath(field), serializedValues);

        values.add(new ApplyFieldConfigItem<>(field, configValue, list -> {
            collection.clear();
            for (final String value : list) {
                final Object deserializedValue = serializers.getRight().apply(value);
                if (deserializedValue != null) {
                    collection.add(deserializedValue);
                }
            }
        }));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> void parseMapField(final T instance, final Builder builder, final ArrayList<ConfigFieldPair<?>> values, final Field field, final KeyValueTypes annotation) throws IllegalAccessException {
        final var keySerializers = getSerializerPair(instance, annotation.keySerializer(), STRING_CONVERTERS.get(annotation.keyType()));
        if (keySerializers.getLeft() == null || keySerializers.getRight() == null) {
            LOGGER.error("Map key type [{}] is not supported (in field [{}]).", annotation.keyType(), field);
            return;
        }

        final var valueSerializers = getSerializerPair(instance, annotation.valueSerializer(), STRING_CONVERTERS.get(annotation.valueType()));
        if (valueSerializers.getLeft() == null || valueSerializers.getRight() == null) {
            LOGGER.error("Map value type [{}] is not supported (in field [{}]).", annotation.valueType(), field);
            return;
        }

        final Map map = (Map) field.get(instance);

        final List<String> serializedValues = new ArrayList<>();
        for (final Object rawEntry : map.entrySet()) {
            final Map.Entry entry = (Map.Entry) rawEntry;
            final String serializedKey = keySerializers.getLeft().apply(entry.getKey());
            final String serializedValue = valueSerializers.getLeft().apply(entry.getValue());
            if (serializedKey != null && serializedValue != null) {
                serializedValues.add(serializedKey + "=" + serializedValue);
            }
        }

        final var configValue = withCommonAttributes(field, builder)
            .define(getPath(field), serializedValues);

        values.add(new ApplyFieldConfigItem<>(field, configValue, list -> {
            map.clear();
            for (final String value : list) {
                final String[] parts = value.split("=", 2);
                if (parts.length != 2) {
                    LOGGER.error("Failed parsing setting value [{}].", value);
                    continue;
                }

                final Object deserializedKey = keySerializers.getRight().apply(parts[0]);
                final Object deserializedValue = valueSerializers.getRight().apply(parts[1]);
                if (deserializedKey != null && deserializedValue != null) {
                    map.put(deserializedKey, deserializedValue);
                }
            }
        }));
    }

    private static <T> void parseRegularField(final T instance, final Builder builder, final ArrayList<ConfigFieldPair<?>> values, final Field field) throws IllegalAccessException {
        final ConfigFieldParser parser = PARSERS.get(field.getType());
        if (parser != null) {
            values.add(parser.apply(instance, field, builder));
            return;
        }

        final var serializers = STRING_CONVERTERS.get(field.getType());
        if (serializers != null) {
            values.add(parseStringLikeField(instance, field, builder, serializers));
            return;
        }

        throw new IllegalStateException("Field of type [" + field.getType() + "] is not supported.");
    }

    private static ConfigFieldPair<?> parseBooleanField(final Object instance, final Field field, final Builder builder) throws IllegalAccessException {
        final boolean defaultValue = field.getBoolean(instance);

        final var configValue = withCommonAttributes(field, builder)
            .define(getPath(field), defaultValue);

        return new SetFieldConfigItem<>(field, configValue);
    }

    private static ConfigFieldPair<?> parseIntField(final Object instance, final Field field, final Builder builder) throws IllegalAccessException {
        final int defaultValue = field.getInt(instance);
        final int minValue = (int) Math.max(getMin(field), Integer.MIN_VALUE);
        final int maxValue = (int) Math.min(getMax(field), Integer.MAX_VALUE);

        final var configValue = withCommonAttributes(field, builder)
            .defineInRange(getPath(field), defaultValue, minValue, maxValue, Integer.class);

        return new SetFieldConfigItem<>(field, configValue);
    }

    private static ConfigFieldPair<?> parseLongField(final Object instance, final Field field, final Builder builder) throws IllegalAccessException {
        final long defaultValue = field.getLong(instance);
        final long minValue = (long) Math.max(getMin(field), Long.MIN_VALUE);
        final long maxValue = (long) Math.min(getMax(field), Long.MAX_VALUE);

        final var configValue = withCommonAttributes(field, builder)
            .defineInRange(getPath(field), defaultValue, minValue, maxValue, Long.class);

        return new SetFieldConfigItem<>(field, configValue);
    }

    private static ConfigFieldPair<?> parseDoubleField(final Object instance, final Field field, final Builder builder) throws IllegalAccessException {
        final double defaultValue = field.getDouble(instance);
        final double minValue = getMin(field);
        final double maxValue = getMax(field);

        final var configValue = withCommonAttributes(field, builder)
            .defineInRange(getPath(field), defaultValue, minValue, maxValue, Double.class);

        return new SetFieldConfigItem<>(field, configValue);
    }

    private static ConfigFieldPair<?> parseStringLikeField(final Object instance, final Field field, final Builder builder, final Pair<Function<Object, String>, Function<String, Object>> defaultSerializers) throws IllegalAccessException {
        final var serializers = getSerializerPair(instance, field, defaultSerializers);

        final String defaultValue = serializers.getLeft().apply(field.get(instance));

        final var configValue = withCommonAttributes(field, builder)
            .define(getPath(field), defaultValue);

        return new SetFieldConfigItem<>(field, configValue, serializers.getRight());
    }

    private static Builder withCommonAttributes(final Field field, final Builder builder) {
        if (getWorldRestart(field)) {
            builder.worldRestart();
        }

        return builder
            .comment(getComment(field))
            .translation(getTranslation(field));
    }

    private static String getPath(final Field field) {
        final Path pathAnnotation = field.getAnnotation(Path.class);
        return (pathAnnotation != null ? pathAnnotation.value() + "." : "") + field.getName();
    }

    private static double getMin(final Field field) {
        final Min annotation = field.getAnnotation(Min.class);
        return annotation != null ? annotation.value() : 0;
    }

    private static double getMax(final Field field) {
        final Max annotation = field.getAnnotation(Max.class);
        return annotation != null ? annotation.value() : Double.POSITIVE_INFINITY;
    }

    private static String[] getComment(final Field field) {
        final Comment annotation = field.getAnnotation(Comment.class);
        return annotation != null ? annotation.value() : new String[0];
    }

    @Nullable
    private static String getTranslation(final Field field) {
        final Translation annotation = field.getAnnotation(Translation.class);
        return annotation != null ? annotation.value() : null;
    }

    private static boolean getWorldRestart(final Field field) {
        final WorldRestart annotation = field.getAnnotation(WorldRestart.class);
        return annotation != null;
    }

    private static Pair<Function<Object, String>, Function<String, Object>> getSerializerPair(final Object instance, final Field field, final Pair<Function<Object, String>, Function<String, Object>> defaultSerializers) {
        return getSerializerPair(instance, field.getAnnotation(CustomSerializer.class), defaultSerializers);
    }

    private static Pair<Function<Object, String>, Function<String, Object>> getSerializerPair(final Object instance, @Nullable final CustomSerializer annotation, final Pair<Function<Object, String>, Function<String, Object>> defaultSerializers) {
        final Function<Object, String> serializer;
        final Function<String, Object> deserializer;
        if (annotation == null) {
            serializer = defaultSerializers.getLeft();
            deserializer = defaultSerializers.getRight();
        } else {
            serializer = getSerializerMethod(instance, annotation.serializer(), defaultSerializers.getLeft());
            deserializer = getDeserializerMethod(instance, annotation.deserializer(), defaultSerializers.getRight());
        }
        return Pair.of(serializer, deserializer);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Function<Object, String> getSerializerMethod(final Object instance, final String methodName, @Nullable final Function<Object, String> defaultSerializer) {
        if (Strings.isNullOrEmpty(methodName)) {
            return defaultSerializer;
        }

        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final MethodType methodType = MethodType.methodType(String.class, Object.class);
            final MethodHandle methodHandle = lookup.findStatic(instance.getClass(), methodName, methodType);
            return (Function<Object, String>) LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(Function.class), methodType.generic(), methodHandle, methodType).getTarget().invokeExact();
        } catch (final Throwable e) {
            LOGGER.error("Serializer [{}] not found on config [{}] or could not be accessed. Error was: {}", methodName, instance.getClass().getName(), e);
            for (final Method method : instance.getClass().getDeclaredMethods()) {
                if (Objects.equals(method.getName(), methodName)) {
                    LOGGER.error("A method with this name exists but has an incompatible signature. Signature should be [static String {}(Object) {...}].", methodName);
                    break;
                }
            }
            return defaultSerializer;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Function<String, Object> getDeserializerMethod(final Object instance, final String methodName, @Nullable final Function<String, Object> defaultDeserializer) {
        if (Strings.isNullOrEmpty(methodName)) {
            return defaultDeserializer;
        }

        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final MethodType methodType = MethodType.methodType(Object.class, String.class);
            final MethodHandle methodHandle = lookup.findStatic(instance.getClass(), methodName, methodType);
            return (Function<String, Object>) LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(Function.class), methodType.generic(), methodHandle, methodType).getTarget().invokeExact();
        } catch (final Throwable e) {
            LOGGER.error("Deserializer [{}] not found on config [{}] or could not be accessed. Error was: {}", methodName, instance.getClass().getName(), e);
            for (final Method method : instance.getClass().getDeclaredMethods()) {
                if (Objects.equals(method.getName(), methodName)) {
                    LOGGER.error("A method with this name exists but has an incompatible signature. Signature should be [static Object {}(String) {...}].", methodName);
                    break;
                }
            }
            return defaultDeserializer;
        }
    }

    // --------------------------------------------------------------------- //

    @FunctionalInterface
    private interface ConfigFieldParser {
        ConfigFieldPair<?> apply(final Object instance, final Field field, final Builder builder) throws IllegalAccessException;
    }

    protected record ConfigDefinition(Object instance, ArrayList<ConfigFieldPair<?>> values) {
        public void apply() {
            for (final ConfigFieldPair<?> pair : values) {
                pair.apply(instance);
            }
        }
    }

    protected static abstract class ConfigFieldPair<T> {
        public final Field field;
        public final ConfigValue<T> value;

        public ConfigFieldPair(final Field field, final ConfigValue<T> value) {
            this.field = field;
            this.value = value;
        }

        public abstract void apply(Object instance);
    }

    private static final class SetFieldConfigItem<T> extends ConfigFieldPair<T> {
        private final Function<T, Object> converter;

        public SetFieldConfigItem(final Field field, final ConfigValue<T> value, final Function<T, Object> converter) {
            super(field, value);
            this.converter = converter;
        }

        public SetFieldConfigItem(final Field field, final ConfigValue<T> value) {
            this(field, value, x -> x);
        }

        @Override
        public void apply(final Object instance) {
            try {
                field.set(instance, converter.apply(value.get()));
            } catch (final IllegalAccessException ignored) {
            }
        }
    }

    private static final class ApplyFieldConfigItem<T> extends ConfigFieldPair<T> {
        private final Consumer<T> applier;

        public ApplyFieldConfigItem(final Field field, final ConfigValue<T> value, final Consumer<T> applier) {
            super(field, value);
            this.applier = applier;
        }

        @Override
        public void apply(final Object instance) {
            applier.accept(value.get());
        }
    }

    protected interface Builder {
        <T> ConfigValue<T> define(String path, T defaultValue);

        <T extends Comparable<? super T>> ConfigValue<T> defineInRange(String path, T defaultValue, T min, T max, Class<T> type);

        Builder comment(String... comment);

        Builder translation(@Nullable String translationKey);

        Builder worldRestart();
    }

    protected interface ConfigValue<T> {
        T get();
    }
}
