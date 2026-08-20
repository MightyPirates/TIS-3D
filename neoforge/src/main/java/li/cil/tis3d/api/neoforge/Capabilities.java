package li.cil.tis3d.api.neoforge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;

/**
 * The NeoForge capabilities TIS-3D provides.
 */
public final class Capabilities {
    /**
     * Infrared packet receiving blocks, {@code tis3d:infrared_receiver}.
     * <p>
     * Register a block with this to receive {@link li.cil.tis3d.api.infrared.InfraredPacket}s.
     */
    public static final BlockCapability<InfraredReceiver, Direction> INFRARED_RECEIVER_BLOCK = BlockCapability.createSided(
        ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "infrared_receiver"), InfraredReceiver.class);

    /**
     * Infrared packet receiving entities, {@code tis3d:infrared_receiver}.
     * <p>
     * Register an entity with this to receive {@link li.cil.tis3d.api.infrared.InfraredPacket}s.
     */
    public static final EntityCapability<InfraredReceiver, Void> INFRARED_RECEIVER_ENTITY = EntityCapability.createVoid(
        ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "infrared_receiver"), InfraredReceiver.class);

    // --------------------------------------------------------------------- //

    private Capabilities() {
    }
}
