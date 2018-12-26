package li.cil.tis3d.common.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.network.message.MessageCasingData;
import net.fabricmc.fabric.networking.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;

public final class MessageHandlerCasingData extends AbstractMessageHandlerWithLocation<MessageCasingData> {
    @Override
    protected void onMessageSynchronized(final MessageCasingData message, final PacketContext context) {
        final BlockEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final ByteBuf data = message.getData();
        while (data.readableBytes() > 0) {
            final Module module = casing.getModule(Face.VALUES[data.readByte()]);
            final ByteBuf moduleData = data.readBytes(data.readShort());
            while (moduleData.readableBytes() > 0) {
                final boolean isNbt = moduleData.readBoolean();
                int size = moduleData.readShort();
                final ByteBuf packet = moduleData.readBytes(size);
                if (module != null) {
                    if (isNbt) {
                        try {
                            final ByteBufInputStream bis = new ByteBufInputStream(packet);
                            final CompoundTag nbt = NbtIo.readCompressed(bis);
                            module.onData(nbt);
                        } catch (final IOException e) {
                            TIS3D.getLog().warn("Invalid packet received.", e);
                        }
                    } else {
                        module.onData(packet);
                    }
                }
            }
        }
    }
}
