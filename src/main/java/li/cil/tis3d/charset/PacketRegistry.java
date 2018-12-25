/*
 * Copyright (c) 2015, 2016, 2017, 2018 Adrian Siekierka
 *
 * This file is part of ProtoCharset.
 *
 * ProtoCharset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtoCharset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ProtoCharset.  If not, see <http://www.gnu.org/licenses/>.
 */

package li.cil.tis3d.charset;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class PacketRegistry {
    public static final PacketRegistry CLIENT = new PacketRegistry();
    public static final PacketRegistry SERVER = new PacketRegistry();

    private static final Map<Class, Identifier> classToType = Maps.newIdentityHashMap();
    private final Map<Identifier, BiConsumer<PacketByteBuf, Object>> packetSerializers = new HashMap<>();
    private final Map<Identifier, BiConsumer<NetworkContext, PacketByteBuf>> packetTypes = new HashMap<>();
    private BiFunction<Identifier, PacketByteBuf, net.minecraft.network.Packet> packetFunction;

    private PacketRegistry() {

    }

    public net.minecraft.network.Packet wrap(Object p) {
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        //noinspection unchecked
        ((BiConsumer<PacketByteBuf, Object>) PacketSerializers.getSerializer(p.getClass())).accept(buffer, p);
        return wrap(classToType.get(p.getClass()), buffer);
    }

    public net.minecraft.network.Packet wrap(Identifier location, PacketByteBuf buffer) {
        return packetFunction.apply(location, buffer);
    }

    private boolean initialized = false;

    public boolean markInitialized() {
        if (!initialized) {
            initialized = true;
            return true;
        } else {
            return false;
        }
    }

    public void setPacketFunction(BiFunction<Identifier, PacketByteBuf, net.minecraft.network.Packet> func) {
        if (packetFunction == null) {
            packetFunction = func;
        }
    }

    public void register(Identifier location, Class<? extends Packet> type, boolean asynchronous) {
        if (location.toString().length() > 32767) {
            throw new RuntimeException("Packet location '" + location + "' longer than maximum allowed! (" + location.toString().length() + " > 32767)");
        }

        register(location, (ctx, buffer) -> {
            Function<PacketByteBuf, ? extends Packet> function = PacketSerializers.getDeserializer(type);
            Packet packet = function.apply(buffer);
            if (!asynchronous) {
                ctx.getListener().execute(() -> packet.apply(ctx));
            } else {
                packet.apply(ctx);
            }
        });
        classToType.put(type, location);
    }

    public void register(Identifier location, BiConsumer<NetworkContext, PacketByteBuf> consumer) {
        packetTypes.put(location, consumer);
    }

    public boolean accepts(Identifier location) {
        return packetTypes.containsKey(location);
    }

    public void parse(Identifier location, NetworkContext ctx, PacketByteBuf buffer) {
        BiConsumer<NetworkContext, PacketByteBuf> entry = packetTypes.get(location);
        entry.accept(ctx, buffer);
    }
}
