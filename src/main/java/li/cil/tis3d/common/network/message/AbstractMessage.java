package li.cil.tis3d.common.network.message;

import li.cil.tis3d.common.network.Network;
import pl.asie.protocharset.rift.network.NetworkContext;
import pl.asie.protocharset.rift.network.Packet;

public abstract class AbstractMessage implements Packet {
	@Override
	public void apply(NetworkContext ctx) {
		Network.HANDLER_MAP.get(getClass()).accept(this, ctx);
	}
}
