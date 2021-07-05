package li.cil.tis3d.common.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.network.message.CasingDataMessage;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;

public final class CasingDataMessageHandler extends AbstractMessageHandlerWithLocation<CasingDataMessage> {
    @Override
    protected void onMessageSynchronized(final CasingDataMessage message, final PacketContext context) {
        final BlockEntity blockEntity = getBlockEntity(message, context);
        if (!(blockEntity instanceof CasingBlockEntity)) {
            return;
        }

        final CasingBlockEntity casing = (CasingBlockEntity)blockEntity;
        final ByteBuf data = message.getData();
        while (data.readableBytes() > 0) {
            final Module module = casing.getModule(Face.VALUES[data.readByte()]);
            final ByteBuf moduleData = data.readBytes(data.readShort());
            while (moduleData.readableBytes() > 0) {
                final boolean isNbt = moduleData.readBoolean();
                final int size = moduleData.readShort();
                final ByteBuf packet = moduleData.readBytes(size);
                if (module != null) {
                    if (isNbt) {
                        try {
                            final ByteBufInputStream bis = new ByteBufInputStream(packet);
                            final NbtCompound nbt = NbtIo.readCompressed(bis);
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
