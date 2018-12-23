package li.cil.tis3d.common.init;

import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.entity.FabricEntityTypeBuilder;
import net.minecraft.util.registry.Registry;

public class Entities implements ModInitializer {
    @Override
    public void onInitialize() {
        EntityInfraredPacket.TYPE = Registry.register(Registry.ENTITY_TYPE, "tis3d:infrared_packet",
            FabricEntityTypeBuilder.create(EntityInfraredPacket.class, EntityInfraredPacket::new)
                .disableSummon()
                .build()
        );
    }
}
