package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

public abstract class AbstractMessageWithLocation extends AbstractMessageWithDimension {
    private int x, y, z;

    protected AbstractMessageWithLocation(final World world, final int x, final int y, final int z) {
        super(world);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected AbstractMessageWithLocation() {
    }

    // --------------------------------------------------------------------- //

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
        super.fromBytes(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

    }

    @Override
    public void toBytes(final ByteBuf buf) {
        super.toBytes(buf);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }
}
