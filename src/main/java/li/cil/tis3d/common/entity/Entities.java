package li.cil.tis3d.common.entity;

import li.cil.tis3d.util.RegistryUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public final class Entities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = RegistryUtils.create(ForgeRegistries.ENTITIES);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<EntityType<InfraredPacketEntity>> INFRARED_PACKET = register("robot", InfraredPacketEntity::new, EntityClassification.MISC, b -> b
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

    private static <T extends Entity> RegistryObject<EntityType<T>> register(final String name, final EntityType.IFactory<T> factory, final EntityClassification classification, final Function<EntityType.Builder<T>, EntityType.Builder<T>> customizer) {
        return ENTITIES.register(name, () -> customizer.apply(EntityType.Builder.of(factory, classification)).build(name));
    }
}
