package li.cil.tis3d.common.network.message;

import dev.architectury.networking.NetworkManager;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.Network;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class ClientCasingLoadedMessage extends AbstractMessageWithPosition {
    public ClientCasingLoadedMessage(final Casing casing) {
        super(casing.getPosition());
    }

    public ClientCasingLoadedMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void handleMessage(NetworkManager.PacketContext context) {
        final var level = getServerLevel(context);
        if (level != null && context.getPlayer() instanceof ServerPlayer player) {
            withBlockEntity(level, CasingBlockEntity.class, casing -> {
                final var listTag = new ListTag();
                for (var face : Face.VALUES) {
                    final var module = casing.getModule(face);
                    final var moduleTag = new CompoundTag();
                    if (module != null) {
                        module.save(moduleTag);
                    }
                    listTag.add(moduleTag);
                }
                Network.sendToPlayer(player, new ServerCasingInitializeMessage(casing, listTag));
            });
        }
    }
}
