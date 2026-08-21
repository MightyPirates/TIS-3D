/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.api.fabric;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * The Fabric lookups TIS-3D provides.
 */
public final class Lookups {
    /**
     * Infrared packet receiving blocks, {@code tis3d:infrared_receiver}.
     * <p>
     * Register a block with this to receive {@link li.cil.tis3d.api.infrared.InfraredPacket}s.
     */
    public static final BlockApiLookup<InfraredReceiver, Direction> INFRARED_RECEIVER_BLOCK = BlockApiLookup.get(
        ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "infrared_receiver"), InfraredReceiver.class, Direction.class);

    /**
     * Infrared packet receiving entities, {@code tis3d:infrared_receiver}.
     * <p>
     * Register an entity with this to receive {@link li.cil.tis3d.api.infrared.InfraredPacket}s.
     */
    public static final EntityApiLookup<InfraredReceiver, Void> INFRARED_RECEIVER_ENTITY = EntityApiLookup.get(
        ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "infrared_receiver"), InfraredReceiver.class, Void.class);

    // --------------------------------------------------------------------- //

    private Lookups() {
    }
}
