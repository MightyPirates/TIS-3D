package li.cil.tis3d.common.network.message;

import net.minecraft.world.World;

public final class MessageHaltAndCatchFire extends AbstractMessageWithLocation {
    public MessageHaltAndCatchFire(final World world, final int x, final int y, final int z) {
        super(world, x, y, z);
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageHaltAndCatchFire() {
    }
}
