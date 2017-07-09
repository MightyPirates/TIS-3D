package li.cil.tis3d.common.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.network.message.MessageCasingData;
import li.cil.tis3d.common.tileentity.TileEntityCasing;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public final class MessageHandlerCasingData extends AbstractMessageHandlerWithLocation<MessageCasingData> {
    @Override
    protected void onMessageSynchronized(final MessageCasingData message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final ByteBuf data = message.getData();
        while (data.readableBytes() > 0) {
            final Module module = casing.getModule(Face.VALUES[data.readByte()]);
            final ByteBuf moduleData = data.readBytes(ByteBufUtils.readVarShort(data));
            while (moduleData.readableBytes() > 0) {
                final boolean isNbt = moduleData.readBoolean();
                final ByteBuf packet = moduleData.readBytes(ByteBufUtils.readVarShort(moduleData));
                if (module != null) {
                    if (isNbt) {
                        try {
                            final ByteBufInputStream bis = new ByteBufInputStream(packet);
                            final NBTTagCompound nbt = CompressedStreamTools.readCompressed(bis);
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
