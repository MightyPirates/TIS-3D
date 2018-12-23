package li.cil.tis3d.client.init;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.charset.PacketRegistry;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.networking.CustomPayloadPacketRegistry;
import net.minecraft.util.PacketByteBuf;

public class Packets implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CustomPayloadPacketRegistry.CLIENT.register(Constants.PACKET, (ctx, buf) -> {
            PacketRegistry.CLIENT.parse(buf.readIdentifier(), new NetworkContext(ctx), buf);
        });
        Network.INSTANCE.registerClientPackets(PacketRegistry.CLIENT);
    }
}
