package li.cil.tis3d.api.detail;

import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

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
    void addProvider(final SerialInterfaceProvider provider);

    /**
     * Find the first provider supporting the specified block position.
     *
     * @param world    the world the block to get the provider for lives in.
     * @param position the position the block to get the provider for lives at.
     * @param side     the side of the block the provider should work for.
     * @return the first provider supporting the item stack, or <tt>null</tt>.
     */
    @Nullable
    SerialInterfaceProvider getProviderFor(final World world, final BlockPos position, final EnumFacing side);
}
