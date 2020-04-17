package li.cil.tis3d.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.client.network.handler.*;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.handler.CasingDataMessageHandler;
import li.cil.tis3d.common.network.handler.CodeBookDataMessageHandler;
import li.cil.tis3d.common.network.handler.ReadOnlyMemoryModuleDataServerMessageHandler;
import li.cil.tis3d.common.network.message.*;
import li.cil.tis3d.util.Side;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.*;

/**
 * Central networking hub for TIS-3D.
 * <p>
 * Aside from providing basic message sending capabilities this also has facilities
 * for throttling package throughput to avoid overloading the network when a large
 * number of casings are active and nearby players. Throttling is applied to particle
 * effect emission and module packets where possible.
 */
public final class Network {
    public static final Network INSTANCE = new Network();

    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private final Map<Class<AbstractMessage>, Identifier> messageIdCache = new HashMap<>();

    // --------------------------------------------------------------------- //

    public void initClient() {
        registerMessage(new CasingDataMessageHandler(), CasingDataMessage.class, Side.CLIENT);
        registerMessage(new CasingEnabledStateMessageHandler(), CasingEnabledStateMessage.class, Side.CLIENT);
        registerMessage(new CasingLockedStateMessageHandler(), CasingLockedStateMessage.class, Side.CLIENT);
        registerMessage(new CasingInventoryMessageHandler(), CasingInventoryMessage.class, Side.CLIENT);
        registerMessage(new HaltAndCatchFireMessageHandler(), HaltAndCatchFireMessage.class, Side.CLIENT);
        registerMessage(new PipeLockedStateMessageHandler(), PipeLockedStateMessage.class, Side.CLIENT);
        registerMessage(new ReadOnlyMemoryModuleDataClientMessageHandler(), ReadOnlyMemoryModuleDataMessage.class, Side.CLIENT);
    }

    public void initServer() {
        registerMessage(new CodeBookDataMessageHandler(), CodeBookDataMessage.class, Side.SERVER);
        registerMessage(new CasingDataMessageHandler(), CasingDataMessage.class, Side.SERVER);
        registerMessage(new ReadOnlyMemoryModuleDataServerMessageHandler(), ReadOnlyMemoryModuleDataMessage.class, Side.SERVER);
    }

    // --------------------------------------------------------------------- //

    public void sendModuleData(final Casing casing, final Face face, final CompoundTag data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public void sendModuleData(final Casing casing, final Face face, final ByteBuf data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public void sendRedstoneEffect(final World world, final double x, final double y, final double z) {
        final BlockPos position = new BlockPos(x, y, z);
        if (!world.isBlockLoaded(position)) {
            final BlockState state = world.getBlockState(position);
            // Note: in 1.12 and earlier this was what is now
            //     Block.isShapeFullCube(state.getCollisionShape(world, position))
            // But this should work too, for the most part, and is more efficient, so let's leave it at that.
            if (state.isOpaque()) {
                // Skip particle emission when inside a block where they aren't visible anyway.
                return;
            }
        }

        queueParticleEffect(world, (float)x, (float)y, (float)z);
    }

    public int sendToClientsInDimension(final AbstractMessage message, final World world) {
        if (world.getPlayers().isEmpty()) {
            return 0;
        }

        final Identifier id = getMessageIdentifier(message.getClass());
        final PacketByteBuf buffer = serializeMessage(message);
        final CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(id, buffer);

        int sent = 0;
        for (final PlayerEntity player : world.getPlayers()) {
            if (player instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)player).networkHandler.sendPacket(packet);
                sent++;
            }
        }

        return sent;
    }

    public int sendToClientsNearLocation(final AbstractMessage message, final World world, final BlockPos pos, final int range) {
        if (world.getPlayers().isEmpty()) {
            return 0;
        }

        final Identifier id = getMessageIdentifier(message.getClass());
        final PacketByteBuf buffer = serializeMessage(message);
        final CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(id, buffer);

        return sendToClientsNearLocation(packet, world, pos, range);
    }

    public void sendToClient(final AbstractMessage message, final PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        final Identifier id = getMessageIdentifier(message.getClass());
        final PacketByteBuf buffer = serializeMessage(message);
        final CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(id, buffer);
        ((ServerPlayerEntity)player).networkHandler.sendPacket(packet);
    }

    public void sendToServer(final AbstractMessage message) {
        final Identifier id = getMessageIdentifier(message.getClass());
        final PacketByteBuf buffer = serializeMessage(message);
        final CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(id, buffer);
        final ClientPlayNetworkHandler networkHandler = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler());
        networkHandler.sendPacket(packet);
    }

    private int sendToClientsNearLocation(final Packet<?> packet, final World world, final BlockPos pos, final int range) {
        final int rangeSq = range * range;
        int sent = 0;
        for (final PlayerEntity player : world.getPlayers()) {
            if (player instanceof ServerPlayerEntity) {
                if (player.squaredDistanceTo(new Vec3d(pos)) < rangeSq) {
                    final ServerPlayerEntity networkedPlayer = (ServerPlayerEntity)player;
                    networkedPlayer.networkHandler.sendPacket(packet);
                    if (!networkedPlayer.networkHandler.connection.isLocal()) {
                        sent++;
                    }
                }
            }
        }

        return sent;
    }

    // --------------------------------------------------------------------- //
    // Message registration and packaging

    private <TMessage extends AbstractMessage> void registerMessage(final AbstractMessageHandler<TMessage> handler, final Class<TMessage> messageClass, final Side handlerSide) {
        final PacketRegistry registry;
        switch (handlerSide) {
            case CLIENT:
                registry = ClientSidePacketRegistry.INSTANCE;
                break;
            case SERVER:
                registry = ServerSidePacketRegistry.INSTANCE;
                break;
            default:
                throw new IndexOutOfBoundsException();
        }

        final Identifier id = getMessageIdentifier(messageClass);
        registry.register(id, (context, buffer) -> {
            try {
                final TMessage message = messageClass.newInstance();
                message.fromBytes(buffer);
                handler.onMessage(message, context);
            } catch (final Exception e) {
                TIS3D.getLog().error(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <TMessage extends AbstractMessage> Identifier getMessageIdentifier(final Class<TMessage> messageClass) {
        return messageIdCache.computeIfAbsent((Class<AbstractMessage>)messageClass, clazz -> new Identifier(API.MOD_ID, clazz.getSimpleName().toLowerCase(Locale.ROOT)));
    }

    private PacketByteBuf serializeMessage(final AbstractMessage message) {
        final PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        message.toBytes(buffer);
        return buffer;
    }

    // --------------------------------------------------------------------- //
    // Message flushing

    public void serverTick() {
        flushCasingQueues(Side.SERVER);
        flushParticleQueue();
    }

    public void clientTick() {
        flushCasingQueues(Side.CLIENT);
    }

    // --------------------------------------------------------------------- //
    // Particle message queueing

    private static final int TICK_TIME = 50;
    private static final Set<Position> particleQueue = new HashSet<>();
    private static long lastParticlesSent = 0;
    private static int particlesSent = 0;
    private static int particleSendInterval = TICK_TIME;

    private static void queueParticleEffect(final World world, final float x, final float y, final float z) {
        final Position position = new Position(world, x, y, z);
        particleQueue.add(position);
    }

    private static void flushParticleQueue() {
        final long now = System.currentTimeMillis();
        if (now - lastParticlesSent < particleSendInterval) {
            return;
        }
        lastParticlesSent = now;

        particlesSent = 0;
        particleQueue.forEach(Position::sendMessage);

        if (particlesSent > Settings.maxParticlesPerTick) {
            final int throttle = (int)Math.ceil(particlesSent / (float)Settings.maxParticlesPerTick);
            particleSendInterval = Math.min(2000, TICK_TIME * throttle);
        } else {
            particleSendInterval = TICK_TIME;
        }

        particleQueue.clear();
    }

    /**
     * Track dimensional position of particle emission for culling duplicates
     * when currently throttling.
     */
    private static final class Position {
        private final World world;
        private final float x;
        private final float y;
        private final float z;

        private Position(final World world, final float x, final float y, final float z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private void sendMessage() {
            final ParticleS2CPacket packet = new ParticleS2CPacket(DustParticleEffect.RED, false, x, y, z, 0, 0, 0, 0, 1);
            if (Network.INSTANCE.sendToClientsNearLocation(packet, world, new BlockPos(x, y, z), RANGE_LOW) > 0) {
                particlesSent++;
            }
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            final Position that = (Position)obj;
            return world.dimension.getType() == that.world.dimension.getType() &&
                Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0 &&
                Float.compare(that.z, z) == 0;

        }

        @Override
        public int hashCode() {
            int result = world.dimension.getType().getRawId();
            result = 31 * result + (x != +0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
            result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
            return result;
        }
    }

    // --------------------------------------------------------------------- //
    // Module data metering

    private static int packetsSentServer = 0;
    private static int packetsSentClient = 0;
    private static int throttleServer = 0;
    private static int throttleClient = 0;

    private static int getPacketsSent(final Side side) {
        return side == Side.CLIENT ? packetsSentClient : packetsSentServer;
    }

    private static void resetPacketsSent(final Side side) {
        if (side == Side.CLIENT) {
            packetsSentClient = 0;
        } else {
            packetsSentServer = 0;
        }
    }

    private static void incrementPacketsSent(final Side side) {
        if (side == Side.CLIENT) {
            packetsSentClient++;
        } else {
            packetsSentServer++;
        }
    }

    private static int getThrottle(final Side side) {
        return side == Side.CLIENT ? throttleClient : throttleServer;
    }

    private static void setThrottle(final Side side, final int value) {
        if (side == Side.CLIENT) {
            throttleClient = value;
        } else {
            throttleServer = value;
        }
    }

    private static void decrementThrottle(final Side side) {
        if (side == Side.CLIENT) {
            throttleClient--;
        } else {
            throttleServer--;
        }
    }

    // --------------------------------------------------------------------- //
    // Module data queueing

    private static final Stack<CasingSendQueue> queuePool = new Stack<>();
    private static final Map<Casing, CasingSendQueue> clientQueues = new HashMap<>();
    private static final Map<Casing, CasingSendQueue> serverQueues = new HashMap<>();

    private static Map<Casing, CasingSendQueue> getQueues(final Side side) {
        if (side == Side.CLIENT) {
            return clientQueues;
        } else {
            return serverQueues;
        }
    }

    private static CasingSendQueue getQueueFor(final Casing casing) {
        final World world = casing.getCasingWorld();
        final Side side = world.isClient ? Side.CLIENT : Side.SERVER;
        final Map<Casing, CasingSendQueue> queues = getQueues(side);
        CasingSendQueue queue = queues.get(casing);
        if (queue == null) {
            synchronized (queuePool) {
                if (queuePool.size() > 0) {
                    queue = queuePool.pop();
                } else {
                    queue = new CasingSendQueue();
                }
            }
            queues.put(casing, queue);
        }
        return queue;
    }

    private static void flushCasingQueues(final Side side) {
        if (getThrottle(side) > 0) {
            decrementThrottle(side);
            return;
        }

        resetPacketsSent(side);

        final Map<Casing, CasingSendQueue> queues = getQueues(side);
        queues.forEach(Network::flushCasingQueue);
        clearQueues(queues);

        final int sent = getPacketsSent(side);
        if (sent > Settings.maxPacketsPerTick) {
            final int throttle = (int)Math.min(40, Math.ceil(sent / (float)Settings.maxPacketsPerTick));
            setThrottle(side, throttle);
        }
    }

    private static void flushCasingQueue(final Casing casing, final CasingSendQueue queue) {
        queue.flush(casing);
    }

    private static void clearQueues(final Map<Casing, CasingSendQueue> queues) {
        synchronized (queuePool) {
            queuePool.addAll(queues.values());
        }
        queues.clear();
    }

    /**
     * Collects messages for a single casing.
     */
    private static final class CasingSendQueue {
        private final ModuleSendQueue[] moduleQueues = new ModuleSendQueue[Face.VALUES.length];

        private CasingSendQueue() {
            for (int i = 0; i < moduleQueues.length; i++) {
                moduleQueues[i] = new ModuleSendQueue();
            }
        }

        private void queueData(final Face face, final CompoundTag data, final byte type) {
            moduleQueues[face.ordinal()].queueData(data, type);
        }

        private void queueData(final Face face, final ByteBuf data, final byte type) {
            moduleQueues[face.ordinal()].queueData(data, type);
        }

        /**
         * Flush the casing's queue, sending all queued packets to clients.
         *
         * @param casing the casing this queue belongs to.
         */
        private void flush(final Casing casing) {
            final World world = casing.getCasingWorld();
            final Side side = world.isClient ? Side.CLIENT : Side.SERVER;
            final ByteBuf data = Unpooled.buffer();
            collectData(data);
            if (data.readableBytes() > 0) {
                final CasingDataMessage message = new CasingDataMessage(casing, data);
                final boolean didSend;
                if (side == Side.CLIENT) {
                    Network.INSTANCE.sendToServer(message);
                    didSend = true;
                } else {
                    didSend = Network.INSTANCE.sendToClientsNearLocation(message, casing.getCasingWorld(), casing.getPosition(), Network.RANGE_HIGH) > 0;
                }
                if (didSend) {
                    incrementPacketsSent(side);
                }
            }
        }

        private void collectData(final ByteBuf data) {
            for (int i = 0; i < moduleQueues.length; i++) {
                final ByteBuf moduleData = moduleQueues[i].collectData();
                if (moduleData.readableBytes() > 0) {
                    data.writeByte(i);
                    data.writeShort(moduleData.readableBytes());
                    data.writeBytes(moduleData);
                }
            }
        }
    }

    /**
     * Collects messages for a single module.
     */
    private static final class ModuleSendQueue {
        private final List<QueueEntry> sendQueue = new ArrayList<>();
        private final BitSet sentTypes = new BitSet(0xFF);

        /**
         * Enqueue the specified data packet.
         *
         * @param data the data to enqueue.
         * @param type the type of the data.
         */
        private void queueData(final CompoundTag data, final byte type) {
            sendQueue.add(new QueueEntryNBT(type, data));
        }

        /**
         * Enqueue the specified data packet.
         *
         * @param data the data to enqueue.
         * @param type the type of the data.
         */
        private void queueData(final ByteBuf data, final byte type) {
            sendQueue.add(new QueueEntryByteBuf(type, data));
        }

        /**
         * Collect all data in a tag list and clear the queue.
         *
         * @return the collected data for the module.
         */
        private ByteBuf collectData() {
            // Building the list backwards to easily use the last data of
            // any type without having to remove from the queue. However,
            // that could lead to sending different types in the reverse
            // they were queued in, so we first collect all packets to
            // actually send (by appending to the queue), and then sending
            // those selected packets -- in reverse again, to restore the
            // original order they were queued in.
            final ByteBuf data = Unpooled.buffer();
            final int firstToWrite = sendQueue.size();
            for (int i = sendQueue.size() - 1; i >= 0; i--) {
                final byte type = sendQueue.get(i).type;
                if (type >= 0) {
                    if (sentTypes.get(type)) {
                        continue;
                    }
                    sentTypes.set(type);
                }

                sendQueue.add(sendQueue.get(i));
            }
            for (int i = sendQueue.size() - 1; i >= firstToWrite; i--) {
                sendQueue.get(i).write(data);
            }

            sendQueue.clear();
            sentTypes.clear();

            return data;
        }

        /**
         * Base class for collected data packets.
         */
        private static abstract class QueueEntry {
            public final byte type;

            private QueueEntry(final byte type) {
                this.type = type;
            }

            /**
             * Serialize the queue entry into the specified byte buffer.
             *
             * @param buffer the buffer to write into.
             */
            public abstract void write(final ByteBuf buffer);
        }

        /**
         * Queue entry for pending NBT data.
         */
        private static final class QueueEntryNBT extends QueueEntry {
            public final CompoundTag data;

            private QueueEntryNBT(final byte type, final CompoundTag data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                final ByteBuf data = Unpooled.buffer();
                final ByteBufOutputStream bos = new ByteBufOutputStream(data);
                try {
                    NbtIo.writeCompressed(this.data, bos);
                    if (data.readableBytes() > 0) {
                        buffer.writeBoolean(true);
                        buffer.writeShort(data.readableBytes());
                        buffer.writeBytes(data);
                    }
                } catch (final IOException e) {
                    TIS3D.getLog().warn("Failed sending packet.", e);
                }
            }
        }

        /**
         * Queue entry for pending raw data.
         */
        private static final class QueueEntryByteBuf extends QueueEntry {
            public final ByteBuf data;

            private QueueEntryByteBuf(final byte type, final ByteBuf data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                if (data.readableBytes() > 0) {
                    buffer.writeBoolean(false);
                    buffer.writeShort(data.readableBytes());
                    buffer.writeBytes(data);
                }
            }
        }
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
