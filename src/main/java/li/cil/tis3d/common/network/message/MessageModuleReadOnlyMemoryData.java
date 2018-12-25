package li.cil.tis3d.common.network.message;

import li.cil.tis3d.charset.SendNetwork;

public final class MessageModuleReadOnlyMemoryData extends AbstractMessage {
    @SendNetwork
    public byte[] data;

    public MessageModuleReadOnlyMemoryData(final byte[] data) {
        this.data = data;
    }

    @SuppressWarnings("unused") // For deserialization.
    public MessageModuleReadOnlyMemoryData() {
    }

    // --------------------------------------------------------------------- //

    public byte[] getData() {
        return data;
    }
}
