package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.SerialAPI;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for serial interface providers.
 */
public final class SerialAPIImpl implements SerialAPI {
    private final List<SerialInterfaceProvider> providers = new ArrayList<>();

    @Override
    public void addProvider(final SerialInterfaceProvider provider) {
        if (!providers.contains(provider)) {
            providers.add(provider);
        }
    }

    @Override
    public SerialInterfaceProvider getProviderFor(final World world, final BlockPos position, final EnumFacing side) {
        if (world != null && position != null) {
            for (final SerialInterfaceProvider provider : providers) {
                if (provider.worksWith(world, position, side)) {
                    return provider;
                }
            }
        }
        return null;
    }
}
