package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.charset.SendNetwork;

public final class MessageReceivingPipeLockedState extends AbstractMessageWithLocation {
    @SendNetwork public Face face;
    @SendNetwork public Port port;
    @SendNetwork public boolean isLocked;

    public MessageReceivingPipeLockedState(final TileEntityCasing casing, final Face face, final Port port, final boolean isLocked) {
        super(casing.getWorld(), casing.getPos());
        this.face = face;
        this.port = port;
        this.isLocked = isLocked;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageReceivingPipeLockedState() {
    }

    // --------------------------------------------------------------------- //

    public Face getFace() {
        return face;
    }

    public Port getPort() {
        return port;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
