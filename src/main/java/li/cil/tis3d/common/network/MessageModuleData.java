package li.cil.tis3d.common.network;

import io.netty.buffer.ByteBuf;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public final class MessageModuleData implements IMessage {
    private int dimension;
    private BlockPos position;
    private Face face;
    private NBTTagCompound nbt;

    public MessageModuleData(final Casing casing, final Face face, final NBTTagCompound nbt) {
        this.dimension = casing.getWorld().provider.getDimensionId();
        this.position = casing.getPosition();
        this.face = face;
        this.nbt = nbt;
    }

    public MessageModuleData() {
    }

    // --------------------------------------------------------------------- //

    public void process(final MessageContext context) {
        final TileEntity tileEntity = getTileEntity(context);
        if (!(tileEntity instanceof TileEntityCasing)) {
            return;
        }

        final TileEntityCasing casing = (TileEntityCasing) tileEntity;
        final Module module = casing.getModule(face);
        if (module == null) return;

        module.onData(nbt);
    }

    // --------------------------------------------------------------------- //

    @Override
    public void fromBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        dimension = buffer.readInt();
        position = buffer.readBlockPos();
        face = buffer.readEnumValue(Face.class);
        try {
            nbt = buffer.readNBTTagCompoundFromBuffer();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        final PacketBuffer buffer = new PacketBuffer(buf);
        buf.writeInt(dimension);
        buffer.writeBlockPos(position);
        buffer.writeEnumValue(face);
        buffer.writeNBTTagCompoundToBuffer(nbt);
    }

    // --------------------------------------------------------------------- //

    private TileEntity getTileEntity(final MessageContext context) {
        switch (context.side) {
            case CLIENT:
                return getTileEntityClient();
            case SERVER:
                return getTileEntityServer();
        }
        return null;
    }

    private TileEntity getTileEntityClient() {
        final World world = FMLClientHandler.instance().getClient().theWorld;
        if (world == null) return null;
        if (world.provider.getDimensionId() != dimension) return null;
        if (!world.isBlockLoaded(position)) {
            return null;
        }
        return world.getTileEntity(position);
    }

    private TileEntity getTileEntityServer() {
        final World world = DimensionManager.getWorld(dimension);
        if (world == null) return null;
        if (!world.isBlockLoaded(position)) {
            return null;
        }
        return world.getTileEntity(position);
    }
}
