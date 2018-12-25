package li.cil.tis3d.common.network.message;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.charset.SendNetwork;

public final class MessageCasingEnabledState extends AbstractMessageWithLocation {
    @SendNetwork
    public boolean isEnabled;

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
