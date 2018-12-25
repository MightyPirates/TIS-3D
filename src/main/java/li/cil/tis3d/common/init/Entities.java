package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.fabricmc.fabric.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.registry.Registry;

public final class Entities {
    public static final EntityType<EntityInfraredPacket> infraredPacket = FabricEntityTypeBuilder.create(EntityInfraredPacket.class, EntityInfraredPacket::new).disableSummon().build();

    // --------------------------------------------------------------------- //

    static void registerEntities() {
        Registry.register(Registry.ENTITY_TYPE, Constants.NAME_ENTITY_INFRARED_PACKET, infraredPacket);
    }

    // --------------------------------------------------------------------- //

    private Entities() {
    }
}
