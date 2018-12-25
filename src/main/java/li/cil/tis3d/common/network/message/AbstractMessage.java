package li.cil.tis3d.common.network.message;

import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.charset.Packet;
import li.cil.tis3d.common.network.Network;

public abstract class AbstractMessage implements Packet {
    @Override
    public void apply(NetworkContext ctx) {
        (ctx.getPlayer().world.isClient ? Network.HANDLER_MAP_CLIENT : Network.HANDLER_MAP_SERVER).get(getClass()).accept(this, ctx);
    }
}
