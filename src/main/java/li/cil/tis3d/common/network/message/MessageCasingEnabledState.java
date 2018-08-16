package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.machine.Casing;
import pl.asie.protocharset.rift.network.SendNetwork;

public final class MessageCasingEnabledState extends AbstractMessageWithLocation {
    @SendNetwork public boolean isEnabled;

    public MessageCasingEnabledState(final Casing casing, final boolean isEnabled) {
        super(casing.getCasingWorld(), casing.getPosition());
        this.isEnabled = isEnabled;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageCasingEnabledState() {
    }

    // --------------------------------------------------------------------- //

    public boolean isEnabled() {
        return isEnabled;
    }
}
