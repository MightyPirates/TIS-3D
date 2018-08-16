package li.cil.tis3d.common.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import pl.asie.protocharset.rift.network.NetworkContext;
import pl.asie.protocharset.rift.network.Packet;
import pl.asie.protocharset.rift.network.SendNetwork;

public abstract class AbstractMessageWithDimension extends AbstractMessage {
    @SendNetwork public int dimension;

    AbstractMessageWithDimension(final World world) {
        this.dimension = world.provider.getDimensionType().getId();
    }

    AbstractMessageWithDimension() {
    }

    // --------------------------------------------------------------------- //

    public int getDimension() {
        return dimension;
    }
}
