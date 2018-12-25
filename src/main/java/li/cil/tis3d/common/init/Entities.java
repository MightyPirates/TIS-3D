package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.entity.FabricEntityTypeBuilder;
import net.minecraft.util.registry.Registry;

@SuppressWarnings("unused")
public class Entities implements ModInitializer {
    @Override
    public void onInitialize() {
        EntityInfraredPacket.TYPE = Registry.register(Registry.ENTITY_TYPE, Constants.NAME_ENTITY_INFRARED_PACKET,
            FabricEntityTypeBuilder.create(EntityInfraredPacket.class, EntityInfraredPacket::new)
                .disableSummon()
                .build()
        );
    }
}
