package li.cil.tis3d.common.integration.bluepower;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.misc.MinecraftColor;
import com.bluepowermod.api.wire.redstone.IBundledDevice;
import li.cil.tis3d.api.module.BundledRedstone;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BluePowerBundledRedstoneDevice implements IBundledDevice {
    private final BundledRedstone module;
    private final IConnectionCache<IBundledDevice> cache;

    public BluePowerBundledRedstoneDevice(final BundledRedstone module) {
        this.module = module;
        this.cache = BPApi.getInstance().getRedstoneApi().createBundledConnectionCache(this);
    }

    // --------------------------------------------------------------------- //
    // IWorldLocation

    @Override
    public World getWorld() {
        return module.getCasing().getCasingWorld();
    }

    @Override
    public int getX() {
        return module.getCasing().getPositionX();
    }

    @Override
    public int getY() {
        return module.getCasing().getPositionY();
    }

    @Override
    public int getZ() {
        return module.getCasing().getPositionZ();
    }

    // --------------------------------------------------------------------- //
    // IBundledDevice

    @Override
    public boolean canConnect(final ForgeDirection face, final IBundledDevice device, final ConnectionType connectionType) {
        return true;
    }

    @Override
    public IConnectionCache<? extends IBundledDevice> getBundledConnectionCache() {
        return cache;
    }

    @Override
    public byte[] getBundledOutput(final ForgeDirection side) {
        final byte[] signal = new byte[16];
        for (int channel = 0; channel < signal.length; channel++) {
            signal[channel] = (byte) module.getBundledRedstoneOutput(channel);
        }
        return signal;
    }

    @Override
    public void setBundledPower(final ForgeDirection face, final byte[] bytes) {
        for (int channel = 0; channel < bytes.length; channel++) {
            module.setBundledRedstoneInput(channel, (short) (bytes[channel] & 0xFF));
        }
    }

    @Override
    public byte[] getBundledPower(final ForgeDirection face) {
        final byte[] signal = new byte[16];
        for (int channel = 0; channel < signal.length; channel++) {
            signal[channel] = (byte) module.getBundledRedstoneInput(channel);
        }
        return signal;
    }

    @Override
    public void onBundledUpdate() {
        module.getCasing().markDirty();
    }

    @Override
    public MinecraftColor getBundledColor(final ForgeDirection side) {
        return MinecraftColor.ANY;
    }

    @Override
    public boolean isNormalFace(final ForgeDirection side) {
        return true;
    }
}
