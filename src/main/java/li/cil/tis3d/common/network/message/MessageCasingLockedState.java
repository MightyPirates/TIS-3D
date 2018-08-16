package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import pl.asie.protocharset.rift.network.SendNetwork;

public final class MessageCasingLockedState extends AbstractMessageWithLocation {
    @SendNetwork public boolean isLocked;

    public MessageCasingLockedState(final Casing casing, final boolean isLocked) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.isLocked = isLocked;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingLockedState() {
    }

    // --------------------------------------------------------------------- //

    public boolean isLocked() {
        return isLocked;
    }
}
