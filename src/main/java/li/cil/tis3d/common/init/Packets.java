package li.cil.tis3d.common.init;

import io.netty.buffer.Unpooled;
import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.charset.PacketRegistry;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.minecraft.client.network.packet.CustomPayloadClientPacket;
import net.minecraft.server.network.packet.CustomPayloadServerPacket;
import net.minecraft.util.PacketByteBuf;

public class Packets implements ModInitializer {
    @Override
    public void onInitialize() {
        CustomPayloadPacketRegistry.SERVER.register(Constants.PACKET, (ctx, buf) -> {
            PacketRegistry.SERVER.parse(buf.readIdentifier(), new NetworkContext(ctx), buf);
        });

        PacketRegistry.SERVER.setPacketFunction((id, buf) -> {
            PacketByteBuf augmentedBuf = new PacketByteBuf(Unpooled.buffer());
            augmentedBuf.writeIdentifier(id);
            augmentedBuf.writeBytes(buf);
            return new CustomPayloadClientPacket(Constants.PACKET, augmentedBuf);
        });
        PacketRegistry.CLIENT.setPacketFunction((id, buf) -> {
            PacketByteBuf augmentedBuf = new PacketByteBuf(Unpooled.buffer());
            augmentedBuf.writeIdentifier(id);
            augmentedBuf.writeBytes(buf);
            return new CustomPayloadServerPacket(Constants.PACKET, augmentedBuf);
        });

        Network.INSTANCE.registerServerPackets(PacketRegistry.SERVER);
    }
}
