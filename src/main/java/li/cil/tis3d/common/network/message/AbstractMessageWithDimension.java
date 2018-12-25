package li.cil.tis3d.common.network.message;

import li.cil.tis3d.charset.SendNetwork;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public abstract class AbstractMessageWithDimension extends AbstractMessage {
    @SendNetwork
    public DimensionType dimension;

    AbstractMessageWithDimension(final World world) {
        this.dimension = world.dimension.getType();
    }

    AbstractMessageWithDimension() {
    }

    // --------------------------------------------------------------------- //

    public DimensionType getDimension() {
        return dimension;
    }
}
