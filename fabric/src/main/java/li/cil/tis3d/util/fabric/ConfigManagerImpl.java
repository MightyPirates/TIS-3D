/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.util.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import li.cil.tis3d.api.API;
import li.cil.tis3d.util.ConfigManager;
import li.cil.tis3d.util.config.ConfigType;
import li.cil.tis3d.util.config.Type;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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
        ModConfigEvents.loading(API.MOD_ID).register(config -> handleModConfigEvent(config, false));
        ModConfigEvents.reloading(API.MOD_ID).register(config -> handleModConfigEvent(config, false));
        ModConfigEvents.unloading(API.MOD_ID).register(config -> handleModConfigEvent(config, true));

        CONFIGS.forEach((spec, config) -> {
            final Type typeAnnotation = config.instance().getClass().getAnnotation(Type.class);
            final ConfigType configType = typeAnnotation != null ? typeAnnotation.value() : ConfigType.COMMON;
            final ModConfig.Type platformType = switch (configType) {
                case COMMON -> ModConfig.Type.COMMON;
                case CLIENT -> ModConfig.Type.CLIENT;
                case SERVER -> ModConfig.Type.SERVER;
            };
            ConfigRegistry.INSTANCE.register(API.MOD_ID, platformType, spec);
        });
    }

    // --------------------------------------------------------------------- //

    private static void handleModConfigEvent(final ModConfig eventConfig, final boolean isUnloading) {
        if (!eventConfig.getModId().equals(API.MOD_ID))
            return;
        final ConfigDefinition config = CONFIGS.get(eventConfig.getSpec());
        if (config == null) {
            return;
        }

        if (isUnloading) {
            config.applyDefaults();
        } else {
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

        @Override
        public T getDefault() {
            return value().getDefault();
        }
    }
}
