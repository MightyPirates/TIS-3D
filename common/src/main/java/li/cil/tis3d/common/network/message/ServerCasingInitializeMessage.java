/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import static java.util.Objects.requireNonNull;

public final class ServerCasingInitializeMessage extends AbstractMessageWithPosition {
    private static final String MODULES_TAG = "modules";

    private ListTag tag;

    public ServerCasingInitializeMessage(final Casing casing, final ListTag tag) {
        super(casing.getPosition());
        this.tag = tag;
    }

    public ServerCasingInitializeMessage(final RegistryFriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(NetworkManager.PacketContext context) {
        final var level = getClientLevel();
        if (level != null) {
            withBlockEntity(level, CasingBlockEntity.class, casing -> {
                for (int i = 0; i < Face.VALUES.length; i++) {
                    final var face = Face.VALUES[i];
                    final var moduleTag = tag.getCompoundOrEmpty(i);
                    final var module = casing.getModule(face);
                    if (module != null) {
                        module.load(moduleTag);
                    }
                }
            });
        }
    }

    @Override
    public void fromBytes(RegistryFriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        final var wrapper = requireNonNull(buffer.readNbt());
        tag = wrapper.getListOrEmpty(MODULES_TAG);
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buffer) {
        super.toBytes(buffer);

        final var wrapper = new CompoundTag();
        wrapper.put(MODULES_TAG, tag);
        buffer.writeNbt(wrapper);
    }
}
