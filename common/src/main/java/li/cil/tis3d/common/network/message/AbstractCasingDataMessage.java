package li.cil.tis3d.common.network.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.io.IOException;

public abstract class AbstractCasingDataMessage extends AbstractMessageWithPosition {
    private ByteBuf data;

    public AbstractCasingDataMessage(final Casing casing, final ByteBuf data) {
        super(casing.getPosition());
        this.data = data;
    }

    public AbstractCasingDataMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    // --------------------------------------------------------------------- //

    protected void handleMessage(final Level level) {
        withBlockEntity(level, CasingBlockEntity.class, casing -> {
            while (data.readableBytes() > 0) {
                final Module module = casing.getModule(Face.VALUES[data.readByte()]);
                final ByteBuf moduleData = data.readBytes(data.readUnsignedShort());
                while (moduleData.readableBytes() > 0) {
                    final boolean isCompoundTag = moduleData.readBoolean();
                    final ByteBuf packet = moduleData.readBytes(moduleData.readUnsignedShort());
                    if (module != null) {
                        if (isCompoundTag) {
                            try {
                                final ByteBufInputStream bis = new ByteBufInputStream(packet);
                                final CompoundTag tag = NbtIo.readCompressed(bis);
                                module.onData(tag);
                            } catch (final IOException e) {
                                LOGGER.warn("Invalid packet received.", e);
                            }
                        } else {
                            module.onData(packet);
                        }
                    }
                }
            }
        });
    }

    // --------------------------------------------------------------------- //
    // AbstractMessage

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        super.fromBytes(buffer);

        final int count = buffer.readInt();
        data = buffer.readBytes(count);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        super.toBytes(buffer);

        final int oldReaderIndex = data.readerIndex();
        buffer.writeInt(data.readableBytes());
        buffer.writeBytes(data);
        data.readerIndex(oldReaderIndex);
    }
}
