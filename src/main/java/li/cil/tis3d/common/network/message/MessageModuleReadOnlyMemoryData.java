package li.cil.tis3d.common.network.message;

import li.cil.tis3d.charset.SendNetwork;
import net.minecraft.util.Hand;

public final class MessageModuleReadOnlyMemoryData extends AbstractMessage {
    @SendNetwork
    public byte[] data;
    @SendNetwork
    public Hand hand;

    public MessageModuleReadOnlyMemoryData(final byte[] data, final Hand hand) {
        this.data = data;
        this.hand = hand;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageModuleReadOnlyMemoryData() {
    }

    // --------------------------------------------------------------------- //

    public byte[] getData() {
        return data;
    }

    public Hand getHand() {
        return hand;
    }
}
