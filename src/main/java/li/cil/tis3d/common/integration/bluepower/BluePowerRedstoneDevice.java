package li.cil.tis3d.common.integration.bluepower;

import com.bluepowermod.api.BPApi;
import com.bluepowermod.api.connect.ConnectionType;
import com.bluepowermod.api.connect.IConnectionCache;
import com.bluepowermod.api.wire.redstone.IRedstoneDevice;
import li.cil.tis3d.api.module.traits.Redstone;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BluePowerRedstoneDevice implements IRedstoneDevice {
    private final Redstone module;
    private final IConnectionCache<IRedstoneDevice> cache;

    public BluePowerRedstoneDevice(final Redstone module) {
        this.module = module;
        this.cache = BPApi.getInstance().getRedstoneApi().createRedstoneConnectionCache(this);
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
    // IRedstoneDevice

    @Override
    public boolean canConnect(final ForgeDirection face, final IRedstoneDevice device, final ConnectionType connectionType) {
        return true;
    }

    @Override
    public IConnectionCache<? extends IRedstoneDevice> getRedstoneConnectionCache() {
        return cache;
    }

    @Override
    public byte getRedstonePower(final ForgeDirection face) {
        return (byte) module.getRedstoneOutput();
    }

    @Override
    public void setRedstonePower(final ForgeDirection face, final byte value) {
        module.setRedstoneInput((short) (value & 0xFF));
    }

    @Override
    public void onRedstoneUpdate() {
        module.getCasing().markDirty();
    }

    @Override
    public boolean isNormalFace(final ForgeDirection face) {
        return true;
    }
}
