package li.cil.tis3d.util.neoforge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.neoforge.ModEventBus;
import li.cil.tis3d.util.ConfigManager;
import li.cil.tis3d.util.config.ConfigType;
import li.cil.tis3d.util.config.Type;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@EventBusSubscriber(modid = API.MOD_ID)
public final class ConfigManagerImpl extends ConfigManager {
    private static final Map<IConfigSpec, ConfigDefinition> CONFIGS = new HashMap<>();

    // --------------------------------------------------------------------- //

    public static <T> void add(final Supplier<T> factory) {
        final ArrayList<ConfigFieldPair<?>> values = new ArrayList<>();
        final var config = new ModConfigSpec.Builder().configure(builder -> {
            final T instance = factory.get();
            fillSpec(instance, new BuilderImpl(builder), values);
            return instance;
        });
        CONFIGS.put(config.getValue(), createDefinition(config.getKey(), values));
    }

    public static void initialize() {
        CONFIGS.forEach((spec, config) -> {
            final Type typeAnnotation = config.instance().getClass().getAnnotation(Type.class);
            final ConfigType configType = typeAnnotation != null ? typeAnnotation.value() : ConfigType.COMMON;
            final ModConfig.Type platformType = switch (configType) {
                case COMMON -> ModConfig.Type.COMMON;
                case CLIENT -> ModConfig.Type.CLIENT;
                case SERVER -> ModConfig.Type.SERVER;
            };
            ModEventBus.MOD_CONTAINER.registerConfig(platformType, spec);
        });
    }

    // --------------------------------------------------------------------- //

    @SubscribeEvent
    public static void handleModConfigEvent(final ModConfigEvent event) {
        final ConfigDefinition config = CONFIGS.get(event.getConfig().getSpec());
        if (config != null) {
            config.apply();
        }
    }

    // --------------------------------------------------------------------- //

    private record BuilderImpl(ModConfigSpec.Builder builder) implements Builder {
        @Override
        public <T> ConfigValue<T> define(final String path, final T defaultValue) {
            return new ConfigValueImpl<>(builder.define(path, defaultValue));
        }

        @Override
        public <T extends Comparable<? super T>> ConfigValue<T> defineInRange(final String path, final T defaultValue, final T min, final T max, final Class<T> type) {
            return new ConfigValueImpl<>(builder.defineInRange(path, defaultValue, min, max, type));
        }

        @Override
        public Builder comment(final String... comment) {
            builder.comment(comment);
            return this;
        }

        @Override
        public Builder translation(@Nullable final String translationKey) {
            builder.translation(translationKey);
            return this;
        }

        @Override
        public Builder worldRestart() {
            builder.worldRestart();
            return this;
        }
    }

    private record ConfigValueImpl<T>(ModConfigSpec.ConfigValue<T> value) implements ConfigValue<T> {
        @Override
        public T get() {
            return value().get();
        }
    }
}
