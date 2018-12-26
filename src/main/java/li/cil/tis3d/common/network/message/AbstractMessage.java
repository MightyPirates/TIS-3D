package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;

public abstract class AbstractMessage {
    public abstract void fromBytes(ByteBuf buf);

    public abstract void toBytes(ByteBuf buf);
}
