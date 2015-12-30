package li.cil.tis3d.common.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.network.message.MessageCasingData;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class MessageHandlerCasingData extends AbstractMessageHandlerWithLocation<MessageCasingData> {
    @Override
    protected void process(final MessageCasingData message, final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(message, context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final NBTTagCompound nbt = message.getNbt();

        for (final String key : nbt.getKeySet()) {
            final int i = Integer.parseInt(key);
            final Module module = casing.getModule(Face.VALUES[i]);
            if (module == null) {
                continue;
            }

            final NBTTagList datums = (NBTTagList) nbt.getTag(key);
            for (int j = datums.tagCount() - 1; j >= 0; j--) {
                final NBTBase data = datums.get(j);
                if (data instanceof NBTTagByteArray) {
                    final ByteBuf buf = Unpooled.wrappedBuffer(((NBTTagByteArray) datums.get(j)).getByteArray());
                    module.onData(buf);
                } else if (data instanceof NBTTagCompound) {
                    module.onData((NBTTagCompound) data);
                } else {
                    TIS3D.getLog().warn("Unexpected casing data type! (" + data.getId() + ")");
                }
            }
        }
    }
}
