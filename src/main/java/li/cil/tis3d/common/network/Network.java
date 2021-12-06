package li.cil.tis3d.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.CommonConfig;
import li.cil.tis3d.common.network.message.*;
import li.cil.tis3d.common.tileentity.ComputerTileEntity;
import li.cil.tis3d.util.LevelUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Central networking hub for TIS-3D.
 * <p>
 * Aside from managing the mod's channel this also has facilities for throttling
 * package throughput to avoid overloading the network when a large number of
 * casings are active and nearby players. Throttling is applied to particle
 * effect emission and module packets where possible.
 */
public final class Network {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String PROTOCOL_VERSION = "1";

    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    // --------------------------------------------------------------------- //

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(API.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    // --------------------------------------------------------------------- //

    private static int nextPacketId = 1;

    // --------------------------------------------------------------------- //

    public static void initialize() {
        registerMessage(CodeBookDataMessage.class, CodeBookDataMessage::new, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(ServerCasingDataMessage.class, ServerCasingDataMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ClientCasingDataMessage.class, ClientCasingDataMessage::new, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(CasingEnabledStateMessage.class, CasingEnabledStateMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(CasingLockedStateMessage.class, CasingLockedStateMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(CasingInventoryMessage.class, CasingInventoryMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(HaltAndCatchFireMessage.class, HaltAndCatchFireMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(RedstoneParticleEffectMessage.class, RedstoneParticleEffectMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ReceivingPipeLockedStateMessage.class, ReceivingPipeLockedStateMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ServerReadOnlyMemoryModuleDataMessage.class, ServerReadOnlyMemoryModuleDataMessage::new, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ClientReadOnlyMemoryModuleDataMessage.class, ClientReadOnlyMemoryModuleDataMessage::new, NetworkDirection.PLAY_TO_SERVER);

        MinecraftForge.EVENT_BUS.addListener(Network::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(Network::onServerTick);
    }

    private static <T extends AbstractMessage> void registerMessage(final Class<T> type, final Function<FriendlyByteBuf, T> decoder, final NetworkDirection direction) {
        INSTANCE.messageBuilder(type, getNextPacketId(), direction)
            .encoder(AbstractMessage::toBytes)
            .decoder(decoder)
            .consumer(AbstractMessage::handleMessage)
            .add();
    }

    // --------------------------------------------------------------------- //

    public static PacketDistributor.PacketTarget getTargetPoint(final Level level, final double x, final double y, final double z, final int range) {
        final PacketDistributor.TargetPoint target = new PacketDistributor.TargetPoint(x, y, z, range, level.dimension());
        return PacketDistributor.NEAR.with(() -> target);
    }

    public static PacketDistributor.PacketTarget getTargetPoint(final Level level, final BlockPos position, final int range) {
        return getTargetPoint(level, position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, range);
    }

    public static PacketDistributor.PacketTarget getTargetPoint(final ComputerTileEntity tileEntity, final int range) {
        return getTargetPoint(Objects.requireNonNull(tileEntity.getBlockEntityLevel()), tileEntity.getBlockPos(), range);
    }

    public static PacketDistributor.PacketTarget getTracking(final Casing casing) {
        final LevelChunk chunk = casing.getCasingLevel().getChunkAt(casing.getPosition());
        return PacketDistributor.TRACKING_CHUNK.with(() -> chunk);
    }

    // --------------------------------------------------------------------- //

    public static void sendModuleData(final Casing casing, final Face face, final CompoundTag data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendModuleData(final Casing casing, final Face face, final ByteBuf data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendPipeEffect(final Level level, final double x, final double y, final double z) {
        final BlockPos position = new BlockPos(x, y, z);
        if (LevelUtils.isLoaded(level, position)) {
            final BlockState state = level.getBlockState(position);
            if (state.isSolidRender(level, position)) {
                // Skip particle emission when inside a block where they aren't visible anyway.
                return;
            }
        }

        queueParticleEffect(level, (float) x, (float) y, (float) z);
    }

    // --------------------------------------------------------------------- //

    private static int getNextPacketId() {
        return nextPacketId++;
    }

    // --------------------------------------------------------------------- //
    // Message flushing

    private static void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.type == TickEvent.Type.SERVER && event.getPhase() == EventPriority.NORMAL) {
            flushCasingQueues(Dist.DEDICATED_SERVER);
            flushParticleQueue();
        }
    }

    private static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.type == TickEvent.Type.CLIENT && event.getPhase() == EventPriority.NORMAL) {
            flushCasingQueues(Dist.CLIENT);
        }
    }

    // --------------------------------------------------------------------- //
    // Particle message queueing

    private static final int TICK_TIME = 50;
    private static final Set<Position> particleQueue = new HashSet<>();
    private static long lastParticlesSent = 0;
    private static int particlesSent = 0;
    private static int particleSendInterval = TICK_TIME;

    private static void queueParticleEffect(final Level level, final float x, final float y, final float z) {
        final Position position = new Position(level, x, y, z);
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

        if (particlesSent > CommonConfig.maxParticlesPerTick) {
            final int throttle = (int) Math.ceil(particlesSent / (float) CommonConfig.maxParticlesPerTick);
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
        private final Level level;
        private final float x;
        private final float y;
        private final float z;

        private Position(final Level level, final float x, final float y, final float z) {
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private void sendMessage() {
            final RedstoneParticleEffectMessage message = new RedstoneParticleEffectMessage(x, y, z);
            if (areAnyPlayersNear(level, new Vec3(x, y, z), RANGE_LOW)) {
                Network.INSTANCE.send(getTargetPoint(level, x, y, z, RANGE_LOW), message);
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
            return Objects.equals(level.dimension(), that.level.dimension()) &&
                   Float.compare(that.x, x) == 0 &&
                   Float.compare(that.y, y) == 0 &&
                   Float.compare(that.z, z) == 0;

        }

        @Override
        public int hashCode() {
            int result = level.dimension().hashCode();
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

    private static int getPacketsSent(final Dist side) {
        return side == Dist.CLIENT ? packetsSentClient : packetsSentServer;
    }

    private static void resetPacketsSent(final Dist side) {
        if (side == Dist.CLIENT) {
            packetsSentClient = 0;
        } else {
            packetsSentServer = 0;
        }
    }

    private static void incrementPacketsSent(final Dist side) {
        if (side == Dist.CLIENT) {
            packetsSentClient++;
        } else {
            packetsSentServer++;
        }
    }

    private static int getThrottle(final Dist side) {
        return side == Dist.CLIENT ? throttleClient : throttleServer;
    }

    private static void setThrottle(final Dist side, final int value) {
        if (side == Dist.CLIENT) {
            throttleClient = value;
        } else {
            throttleServer = value;
        }
    }

    private static void decrementThrottle(final Dist side) {
        if (side == Dist.CLIENT) {
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

    private static Map<Casing, CasingSendQueue> getQueues(final Dist side) {
        if (side == Dist.CLIENT) {
            return clientQueues;
        } else {
            return serverQueues;
        }
    }

    private static CasingSendQueue getQueueFor(final Casing casing) {
        final Level level = casing.getCasingLevel();
        final Dist side = level.isClientSide() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
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

    private static void flushCasingQueues(final Dist side) {
        if (getThrottle(side) > 0) {
            decrementThrottle(side);
            return;
        }

        resetPacketsSent(side);

        final Map<Casing, CasingSendQueue> queues = getQueues(side);
        queues.forEach(Network::flushCasingQueue);
        clearQueues(queues);

        final int sent = getPacketsSent(side);
        if (sent > CommonConfig.maxPacketsPerTick) {
            final int throttle = (int) Math.min(40, Math.ceil(sent / (float) CommonConfig.maxPacketsPerTick));
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
            final Level level = casing.getCasingLevel();
            final Dist side = level.isClientSide() ? Dist.CLIENT : Dist.DEDICATED_SERVER;
            final ByteBuf data = Unpooled.buffer();
            collectData(data);
            if (data.readableBytes() > 0) {
                final boolean didSend;
                if (side == Dist.CLIENT) {
                    final ClientCasingDataMessage message = new ClientCasingDataMessage(casing, data);
                    Network.INSTANCE.sendToServer(message);
                    didSend = true;
                } else {
                    final ServerCasingDataMessage message = new ServerCasingDataMessage(casing, data);
                    Network.INSTANCE.send(getTracking(casing), message);
                    didSend = areAnyPlayersNear(casing.getCasingLevel(), casing.getPosition(), RANGE_HIGH);
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
            sendQueue.add(new QueueEntryCompoundTag(type, data));
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
         * Queue entry for pending tag data.
         */
        private static final class QueueEntryCompoundTag extends QueueEntry {
            public final CompoundTag data;

            private QueueEntryCompoundTag(final byte type, final CompoundTag data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                final ByteBuf data = Unpooled.buffer();
                try (final ByteBufOutputStream bos = new ByteBufOutputStream(data)) {
                    NbtIo.writeCompressed(this.data, bos);

                    if (data.readableBytes() > 0) {
                        buffer.writeBoolean(true);
                        buffer.writeShort(data.readableBytes());
                        buffer.writeBytes(data);
                    }
                } catch (final IOException e) {
                    LOGGER.warn("Failed sending packet.", e);
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
     * @param level    the level to check in.
     * @param position the position to check for.
     * @param range    the radius around the position to check for.
     * @return <tt>true</tt> if there are nearby players; <tt>false</tt> otherwise.
     */
    private static boolean areAnyPlayersNear(final Level level, final Vec3 position, final int range) {
        for (final Player player : level.players()) {
            if (player instanceof ServerPlayer) {
                if (position.closerThan(player.position(), range)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean areAnyPlayersNear(final Level level, final BlockPos position, final int range) {
        return areAnyPlayersNear(level, Vec3.atCenterOf(position), range);
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
