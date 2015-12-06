package li.cil.tis3d.common.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

public abstract class AbstractMessageWithLocation implements IMessage {
    private int dimension;
    private int x, y, z;

    protected AbstractMessageWithLocation(final World world, final int x, final int y, final int z) {
        this.dimension = world.provider.dimensionId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected AbstractMessageWithLocation() {
    }

    // --------------------------------------------------------------------- //

    public int getDimension() {
        return dimension;
    }

    public int getPositionX() {
        return x;
    }

    public int getPositionY() {
        return y;
    }

    public int getPositionZ() {
        return z;
    }

    // --------------------------------------------------------------------- //
    // IMessage

    @Override
    public void fromBytes(final ByteBuf buf) {
        dimension = buf.readInt();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(dimension);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }
}
