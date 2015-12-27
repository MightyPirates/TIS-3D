package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Allows registering {@link li.cil.tis3d.api.serial.SerialInterfaceProvider}s.
 * <p>
 * When a serial port module tries to establish a serial connection with the
 * block in front of it, the registered providers supporting the position will
 * be used to create the {@link li.cil.tis3d.api.serial.SerialInterface} used
 * to communicate with the block.
 */
public interface SerialAPI {
    /**
     * Register the specified provider.
     *
     * @param provider the provider to register.
     */
    void addProvider(SerialInterfaceProvider provider);

    /**
     * Find the first provider supporting the specified block position.
     *
     * @param world    the world the block to get the provider for lives in.
     * @param position the position the block to get the provider for lives at.
     * @param side     the side of the block the provider should work for.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    SerialInterfaceProvider getProviderFor(World world, BlockPos position, EnumFacing side);
}
