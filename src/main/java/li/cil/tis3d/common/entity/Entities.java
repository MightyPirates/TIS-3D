package li.cil.tis3d.common.entity;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public final class Entities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = RegistryUtils.getInitializerFor(ForgeRegistries.ENTITY_TYPES);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<EntityType<InfraredPacketEntity>> INFRARED_PACKET = register("robot", InfraredPacketEntity::new, MobCategory.MISC, b -> b
        .sized(0.25f, 0.25f)
        .setTrackingRange(16)
        .setUpdateInterval(1)
        .setShouldReceiveVelocityUpdates(true)
        .fireImmune()
        .noSummon());

    // --------------------------------------------------------------------- //

    public static void initialize() {
    }

    // --------------------------------------------------------------------- //

    private static <T extends Entity> RegistryObject<EntityType<T>> register(final String name, final EntityType.EntityFactory<T> factory, final MobCategory classification, final Function<EntityType.Builder<T>, EntityType.Builder<T>> customizer) {
        return ENTITY_TYPES.register(name, () -> customizer.apply(EntityType.Builder.of(factory, classification)).build(name));
    }
}
