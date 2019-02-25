package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public final class Entities {
    public static final EntityType<InfraredPacketEntity> INFRARED_PACKET = FabricEntityTypeBuilder.create(EntityCategory.MISC, InfraredPacketEntity::new).disableSummon().size(0.25f, 0.25f).build();

    // --------------------------------------------------------------------- //

    static void registerEntities() {
        Registry.register(Registry.ENTITY_TYPE, Constants.NAME_ENTITY_INFRARED_PACKET, INFRARED_PACKET);
    }

    // --------------------------------------------------------------------- //

    private Entities() {
    }
}
