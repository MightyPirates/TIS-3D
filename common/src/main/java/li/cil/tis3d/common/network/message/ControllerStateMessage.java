/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;

public final class ControllerStateMessage extends AbstractMessageWithPosition {
    private ControllerBlockEntity.ControllerState state;

    public ControllerStateMessage(final ControllerBlockEntity controller, final ControllerBlockEntity.ControllerState state) {
        super(controller.getBlockPos());
        this.state = state;
    }

    public ControllerStateMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(final NetworkManager.PacketContext context) {
        final var level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, ControllerBlockEntity.class, controller ->
                controller.setStateClient(state));
        }
    }

    @Override
    public void fromBytes(final RegistryFriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        state = buffer.readEnum(ControllerBlockEntity.ControllerState.class);
    }

    @Override
    public void toBytes(final RegistryFriendlyByteBuf buffer) {
        super.toBytes(buffer);

        buffer.writeEnum(state);
    }
}
