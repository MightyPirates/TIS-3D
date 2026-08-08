package li.cil.tis3d.common.capabilities;

import li.cil.tis3d.api.API;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;

public final class Capabilities {
    public static class InfraredReceiver {
        public static final BlockCapability<li.cil.tis3d.api.infrared.InfraredReceiver, Direction> BLOCK = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "infrared_receiver"), li.cil.tis3d.api.infrared.InfraredReceiver.class);
        public static final EntityCapability<li.cil.tis3d.api.infrared.InfraredReceiver, Void> ENTITY = EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "infrared_receiver"), li.cil.tis3d.api.infrared.InfraredReceiver.class);
    }
}
