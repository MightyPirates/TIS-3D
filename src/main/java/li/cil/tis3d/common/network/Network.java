package li.cil.tis3d.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.charset.NetworkContext;
import li.cil.tis3d.charset.Packet;
import li.cil.tis3d.charset.PacketRegistry;
import li.cil.tis3d.charset.PacketServerHelper;
import li.cil.tis3d.client.network.handler.*;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.network.handler.AbstractMessageHandler;
import li.cil.tis3d.common.network.handler.MessageHandlerBookCodeData;
import li.cil.tis3d.common.network.handler.MessageHandlerCasingData;
import li.cil.tis3d.common.network.handler.MessageHandlerModuleReadOnlyMemoryDataServer;
import li.cil.tis3d.common.network.message.*;
import li.cil.tis3d.util.Side;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.particle.DustParticleParameters;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Central networking hub for TIS-3D.
 * <p>
 * Aside from managing the mod's channel this also has facilities for throttling
 * package throughput to avoid overloading the network when a large number of
 * casings are active and nearby players. Throttling is applied to particle
 * effect emission and module packets where possible.
 */
public final class Network {
    public static final Network INSTANCE = new Network();
    public static Map<Class<?>, BiConsumer<Packet, NetworkContext>> HANDLER_MAP_CLIENT = new HashMap<>();
    public static Map<Class<?>, BiConsumer<Packet, NetworkContext>> HANDLER_MAP_SERVER = new HashMap<>();

    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private enum Messages {
        CasingDataClient,
        CasingDataServer,
        ParticleEffects,
        CasingEnabledState,
        BookCodeData,
        HaltAndCatchFire,
        CasingLockedState,
        ReceivingPipeLockedState,
        CasingInventory,
        ReadOnlyMemoryData,
    }

    // --------------------------------------------------------------------- //

    private void registerMessage(PacketRegistry registry, Class<? extends AbstractMessageHandler> hcl, Class<? extends AbstractMessage> cl, int id, Side side) {
        try {
            AbstractMessageHandler mh = hcl.newInstance();
            if (side == Side.CLIENT) {
                HANDLER_MAP_CLIENT.put(cl, mh::onMessage);
            } else {
                HANDLER_MAP_SERVER.put(cl, mh::onMessage);
            }
            registry.register(new Identifier("tis3d", cl.getSimpleName().toLowerCase(Locale.ROOT)), cl, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerClientPackets(PacketRegistry packetRegistry) {
        registerMessage(packetRegistry, MessageHandlerCasingData.class, MessageCasingData.class, Messages.CasingDataClient.ordinal(), Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerCasingEnabledState.class, MessageCasingEnabledState.class, Messages.CasingEnabledState.ordinal(), Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerCasingLockedState.class, MessageCasingLockedState.class, Messages.CasingLockedState.ordinal(), Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerCasingInventory.class, MessageCasingInventory.class, Messages.CasingInventory.ordinal(), Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerHaltAndCatchFire.class, MessageHaltAndCatchFire.class, Messages.HaltAndCatchFire.ordinal(), Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerOpenGUI.class, MessageOpenGUI.class, -1, Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerReceivingPipeLockedState.class, MessageReceivingPipeLockedState.class, Messages.ReceivingPipeLockedState.ordinal(), Side.CLIENT);
        registerMessage(packetRegistry, MessageHandlerModuleReadOnlyMemoryDataClient.class, MessageModuleReadOnlyMemoryData.class, Messages.ReadOnlyMemoryData.ordinal(), Side.CLIENT);
    }

    public void registerServerPackets(PacketRegistry packetRegistry) {
        registerMessage(packetRegistry, MessageHandlerBookCodeData.class, MessageBookCodeData.class, Messages.BookCodeData.ordinal(), Side.SERVER);
        registerMessage(packetRegistry, MessageHandlerCasingData.class, MessageCasingData.class, Messages.CasingDataServer.ordinal(), Side.SERVER);
        registerMessage(packetRegistry, MessageHandlerModuleReadOnlyMemoryDataServer.class, MessageModuleReadOnlyMemoryData.class, Messages.ReadOnlyMemoryData.ordinal(), Side.SERVER);
    }

    public void init() {
    }

    // --------------------------------------------------------------------- //

    public static void sendModuleData(final Casing casing, final Face face, final CompoundTag data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendModuleData(final Casing casing, final Face face, final ByteBuf data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendPipeEffect(final World world, final double x, final double y, final double z) {
        final BlockPos position = new BlockPos(x, y, z);
        if (!world.isBlockLoaded(position)) {
            final BlockState state = world.getBlockState(position);
            if (state.isFullBoundsCubeForCulling()) {
                // Skip particle emission when inside a block where they aren't visible anyway.
                return;
            }
        }

        queueParticleEffect(world, (float) x, (float) y, (float) z);
    }

    // --------------------------------------------------------------------- //
    // Message flushing

    public void serverTick(MinecraftServer server) {
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
            final int throttle = (int) Math.ceil(particlesSent / (float) Settings.maxParticlesPerTick);
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
            if (((ServerWorld) world).method_14199(
                new DustParticleParameters(1f, 0.2f, 0, 1f),
                x, y, z, 1, 0, 0, 0, 0
            ) > 0) {
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

            final Position that = (Position) obj;
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
            final int throttle = (int) Math.min(40, Math.ceil(sent / (float) Settings.maxPacketsPerTick));
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
                final MessageCasingData message = new MessageCasingData(casing, data);
                final boolean[] didSend = new boolean[1];
                if (side == Side.CLIENT) {
                    MinecraftClient.getInstance().getNetworkHandler().sendPacket(PacketRegistry.CLIENT.wrap(message));
                    didSend[0] = true;
                } else {
                    PacketServerHelper.forEachWatching(casing.getCasingWorld(), casing.getPosition(), (player) -> {
                        player.networkHandler.sendPacket(PacketRegistry.SERVER.wrap(message));
                        didSend[0] = true;
                    });
                }
                if (didSend[0]) {
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

    /**
     * Check if there are any players nearby the specified target point.
     * <p>
     * Used to determine whether a packet will actually be sent to any
     * clients.
     *
     * @return <tt>true</tt> if there are nearby players; <tt>false</tt> otherwise.
     */
    private static boolean areAnyPlayersNear(final MinecraftServer server, final DimensionType dimension, final double x, final double y, final double z, final double range) {
        double sqRange = range * range;

        for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.dimension == dimension && player.networkHandler != null && player.squaredDistanceTo(x, y, z) < sqRange) {
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------------- //
}
