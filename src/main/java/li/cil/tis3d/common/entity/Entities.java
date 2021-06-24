package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.API;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public final class Entities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, API.MOD_ID);

    // --------------------------------------------------------------------- //

    public static final RegistryObject<EntityType<EntityInfraredPacket>> INFRARED_PACKET = register("robot", EntityInfraredPacket::new, EntityClassification.MISC, b -> b
        .size(0.25f, 0.25f)
        .trackingRange(16)
        .setUpdateInterval(1)
        .setShouldReceiveVelocityUpdates(true)
        .immuneToFire()
        .disableSummoning());

    // --------------------------------------------------------------------- //

    public static void initialize() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    // --------------------------------------------------------------------- //

    private static <T extends Entity> RegistryObject<EntityType<T>> register(final String name, final EntityType.IFactory<T> factory, final EntityClassification classification, final Function<EntityType.Builder<T>, EntityType.Builder<T>> customizer) {
        return ENTITIES.register(name, () -> customizer.apply(EntityType.Builder.create(factory, classification)).build(name));
    }
}
