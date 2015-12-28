package li.cil.tis3d.common.integration.bluepower;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import com.bluepowermod.api.wire.redstone.IRedstoneProvider;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;
import java.util.WeakHashMap;

public final class BluePowerProvider implements IRedstoneProvider {
    public static final BluePowerProvider INSTANCE = new BluePowerProvider();

    private final Map<Redstone, IRedstoneDevice> redstoneDevices = new WeakHashMap<>();

    private final Map<BundledRedstone, IBundledDevice> bundledRedstoneDevices = new WeakHashMap<>();

    public void register() {
        BPApi.getInstance().getRedstoneApi().registerRedstoneProvider(this);
    }

    @Override
    public IRedstoneDevice getRedstoneDeviceAt(final World world, final int x, final int y, final int z, final ForgeDirection side, final ForgeDirection face) {
        if (world != null && world.blockExists(x, y, z)) {
            final TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;
                final Module module = casing.getModule(Face.fromForgeDirection(side));
                if (module instanceof Redstone) {
                    return redstoneDevices.computeIfAbsent((Redstone) module, BluePowerRedstoneDevice::new);
                }
            }
        }
        return null;
    }

    @Override
    public IBundledDevice getBundledDeviceAt(final World world, final int x, final int y, final int z, final ForgeDirection side, final ForgeDirection face) {
        if (world != null && world.blockExists(x, y, z)) {
            final TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing casing = (TileEntityCasing) tileEntity;
                final Module module = casing.getModule(Face.fromForgeDirection(side));
                if (module instanceof BundledRedstone) {
                    return bundledRedstoneDevices.computeIfAbsent((BundledRedstone) module, BluePowerBundledRedstoneDevice::new);
                }
            }
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    private BluePowerProvider() {
    }
}
