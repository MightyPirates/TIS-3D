package li.cil.tis3d.api;

import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * API entry point for registering {@link li.cil.tis3d.api.serial.SerialInterfaceProvider}s and other
 * module related tasks.
 * <p>
 * When a serial port module tries to establish a serial connection with the
 * block in front of it, the registered providers supporting the position will
 * be used to create the {@link li.cil.tis3d.api.serial.SerialInterface} used
 * to communicate with the block.
 * <p>
 * This is made available in the init phase, so you'll either have to (soft)
 * depend on TIS-3D or you must not make calls to this before the init phase.
 */
public final class SerialAPI {
    /**
     * Register the specified provider.
     *
     * @param provider the provider to register.
     */
    public static void addProvider(final SerialInterfaceProvider provider) {
        if (API.serialAPI != null)
            API.serialAPI.addProvider(provider);
    }

    /**
     * Find the first provider supporting the specified block position.
     *
     * @param world    the world the block to get the provider for lives in.
     * @param position the position the block to get the provider for lives at.
     * @param side     the side of the block the provider should work for.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    public static SerialInterfaceProvider getProviderFor(final World world, final BlockPos position, final EnumFacing side) {
        if (API.serialAPI != null)
            return API.serialAPI.getProviderFor(world, position, side);
        return null;
    }

    // --------------------------------------------------------------------- //

    private SerialAPI() {
    }
}
