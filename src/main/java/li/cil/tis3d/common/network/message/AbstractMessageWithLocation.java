package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import li.cil.tis3d.charset.SendNetwork;

public abstract class AbstractMessageWithLocation extends AbstractMessageWithDimension {
    @SendNetwork public BlockPos position;

    AbstractMessageWithLocation(final World world, final BlockPos position) {
        super(world);
        this.position = position;
    }

    AbstractMessageWithLocation() {
    }

    // --------------------------------------------------------------------- //

    public BlockPos getPosition() {
        return position;
    }
}
