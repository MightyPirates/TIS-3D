package li.cil.tis3d.common.network.handler;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.network.message.MessageCasingData;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
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

            final NBTTagList datums = nbt.getTagList(key, Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < datums.tagCount(); j++) {
                final NBTTagCompound data = datums.getCompoundTagAt(j);
                module.onData(data);
            }
        }
    }
}
