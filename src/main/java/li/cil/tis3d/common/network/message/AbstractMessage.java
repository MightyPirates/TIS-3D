package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.network.Network;
import net.minecraft.network.play.INetHandlerPlayClient;
import pl.asie.protocharset.rift.network.NetworkContext;
import pl.asie.protocharset.rift.network.Packet;

public abstract class AbstractMessage implements Packet {
	@Override
	public void apply(NetworkContext ctx) {
		((ctx.getHandler() instanceof INetHandlerPlayClient) ? Network.HANDLER_MAP_CLIENT : Network.HANDLER_MAP_SERVER).get(getClass()).accept(this, ctx);
	}
}
