package li.cil.tis3d.common.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Function;

public final class Entities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = RegistryUtils.get(Registries.ENTITY_TYPE);

    // --------------------------------------------------------------------- //

    public static final RegistrySupplier<EntityType<InfraredPacketEntity>> INFRARED_PACKET = register("infrared_packet", InfraredPacketEntity::new, MobCategory.MISC, b -> b
        .sized(0.25f, 0.25f)
        .clientTrackingRange(16)
        .updateInterval(1)
        .canSpawnFarFromPlayer()
        .fireImmune()
        .noSummon());

    // --------------------------------------------------------------------- //

    public static void initialize() {
        ENTITY_TYPES.register();
    }

    // --------------------------------------------------------------------- //

    private static <T extends Entity> RegistrySupplier<EntityType<T>> register(final String name, final EntityType.EntityFactory<T> factory, final MobCategory classification, final Function<EntityType.Builder<T>, EntityType.Builder<T>> customizer) {
        return ENTITY_TYPES.register(name, () -> customizer.apply(EntityType.Builder.of(factory, classification)).build(name));
    }
}
