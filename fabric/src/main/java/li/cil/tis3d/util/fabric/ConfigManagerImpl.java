package li.cil.tis3d.util.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import li.cil.tis3d.api.API;
import li.cil.tis3d.util.ConfigManager;
import li.cil.tis3d.util.config.ConfigType;
import li.cil.tis3d.util.config.Type;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ConfigManagerImpl extends ConfigManager {
    private static final Map<IConfigSpec<ForgeConfigSpec>, ConfigDefinition> CONFIGS = new HashMap<>();

    // --------------------------------------------------------------------- //

    public static <T> void add(final Supplier<T> factory) {
        final ArrayList<ConfigFieldPair<?>> values = new ArrayList<>();
        final var config = new ForgeConfigSpec.Builder().configure(builder -> {
            final T instance = factory.get();
            fillSpec(instance, new BuilderImpl(builder), values);
            return instance;
        });
        CONFIGS.put(config.getValue(), createDefinition(config.getKey(), values));
    }

    public static void initialize() {
        ModConfigEvents.loading(API.MOD_ID).register(ConfigManagerImpl::handleModConfigEvent);
        ModConfigEvents.reloading(API.MOD_ID).register(ConfigManagerImpl::handleModConfigEvent);

        CONFIGS.forEach((spec, config) -> {
            final Type typeAnnotation = config.instance().getClass().getAnnotation(Type.class);
            final ConfigType configType = typeAnnotation != null ? typeAnnotation.value() : ConfigType.COMMON;
            final ModConfig.Type platformType = switch (configType) {
                case COMMON -> ModConfig.Type.COMMON;
                case CLIENT -> ModConfig.Type.CLIENT;
                case SERVER -> ModConfig.Type.SERVER;
            };
            ForgeConfigRegistry.INSTANCE.register(API.MOD_ID, platformType, spec);
        });
    }

    // --------------------------------------------------------------------- //

    private static void handleModConfigEvent(final ModConfig eventConfig) {
        if (!eventConfig.getModId().equals(API.MOD_ID))
            return;
        final ConfigDefinition config = CONFIGS.get(eventConfig.getSpec());
        if (config != null) {
            config.apply();
        }
    }

    // --------------------------------------------------------------------- //

    private record BuilderImpl(ForgeConfigSpec.Builder builder) implements Builder {
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

    private record ConfigValueImpl<T>(ForgeConfigSpec.ConfigValue<T> value) implements ConfigValue<T> {
        @Override
        public T get() {
            return value().get();
        }
    }
}
