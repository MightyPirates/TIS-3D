package li.cil.tis3d.common.network;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.client.network.handler.MessageHandlerCasingEnabledState;
import li.cil.tis3d.client.network.handler.MessageHandlerCasingInventory;
import li.cil.tis3d.client.network.handler.MessageHandlerCasingLockedState;
import li.cil.tis3d.client.network.handler.MessageHandlerHaltAndCatchFire;
import li.cil.tis3d.client.network.handler.MessageHandlerParticleEffects;
import li.cil.tis3d.client.network.handler.MessageHandlerReceivingPipeLockedState;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.common.network.handler.MessageHandlerBookCodeData;
import li.cil.tis3d.common.network.handler.MessageHandlerCasingData;
import li.cil.tis3d.common.network.message.MessageBookCodeData;
import li.cil.tis3d.common.network.message.MessageCasingData;
import li.cil.tis3d.common.network.message.MessageCasingEnabledState;
import li.cil.tis3d.common.network.message.MessageCasingInventory;
import li.cil.tis3d.common.network.message.MessageCasingLockedState;
import li.cil.tis3d.common.network.message.MessageHaltAndCatchFire;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import li.cil.tis3d.common.network.message.MessageReceivingPipeLockedState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

    public static final int RANGE_HIGH = 48;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private static SimpleNetworkWrapper wrapper;

    private enum Messages {
        CasingDataClient,
        CasingDataServer,
        ParticleEffects,
        CasingEnabledState,
        BookCodeData,
        HaltAndCatchFire,
        CasingLockedState,
        ReceivingPipeLockedState,
        CasingInventory
    }

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(API.MOD_ID);

        wrapper.registerMessage(MessageHandlerBookCodeData.class, MessageBookCodeData.class, Messages.BookCodeData.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerCasingData.class, MessageCasingData.class, Messages.CasingDataClient.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingData.class, MessageCasingData.class, Messages.CasingDataServer.ordinal(), Side.SERVER);
        wrapper.registerMessage(MessageHandlerCasingEnabledState.class, MessageCasingEnabledState.class, Messages.CasingEnabledState.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingLockedState.class, MessageCasingLockedState.class, Messages.CasingLockedState.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingInventory.class, MessageCasingInventory.class, Messages.CasingInventory.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerHaltAndCatchFire.class, MessageHaltAndCatchFire.class, Messages.HaltAndCatchFire.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerParticleEffects.class, MessageParticleEffect.class, Messages.ParticleEffects.ordinal(), Side.CLIENT);
        wrapper.registerMessage(MessageHandlerReceivingPipeLockedState.class, MessageReceivingPipeLockedState.class, Messages.ReceivingPipeLockedState.ordinal(), Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    // --------------------------------------------------------------------- //

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final double x, final double y, final double z, final int range) {
        return new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final TileEntity tileEntity, final int range) {
        return getTargetPoint(tileEntity.getWorldObj(), tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 0.5, range);
    }

    // --------------------------------------------------------------------- //

    public static void sendModuleData(final Casing casing, final Face face, final NBTTagCompound data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendModuleData(final Casing casing, final Face face, final ByteBuf data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    public static void sendPipeEffect(final World world, final double x, final double y, final double z) {
        final int positionX = (int) Math.floor(x);
        final int positionY = (int) Math.floor(y);
        final int positionZ = (int) Math.floor(z);
        if (!world.blockExists(positionX, positionY, positionZ) || world.getBlock(positionX, positionY, positionZ).isOpaqueCube()) {
            // Skip particle emission when inside a block where they aren't visible anyway.
            return;
        }

        queueParticleEffect(world, (float) x, (float) y, (float) z);
    }

    // --------------------------------------------------------------------- //
    // Message flushing

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.type == TickEvent.Type.SERVER && event.getPhase() == EventPriority.NORMAL) {
            flushCasingQueues(Side.SERVER);
            flushParticleQueue();
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.type == TickEvent.Type.CLIENT && event.getPhase() == EventPriority.NORMAL) {
            flushCasingQueues(Side.CLIENT);
        }
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

        public void sendMessage() {
            final MessageParticleEffect message = new MessageParticleEffect(world, "reddust", x, y, z);
            final NetworkRegistry.TargetPoint target = new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, RANGE_LOW);
            Network.INSTANCE.getWrapper().sendToAllAround(message, target);
            if (areAnyPlayersNear(target)) {
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
            return world.provider.dimensionId == that.world.provider.dimensionId &&
                   Float.compare(that.x, x) == 0 &&
                   Float.compare(that.y, y) == 0 &&
                   Float.compare(that.z, z) == 0;

        }

        @Override
        public int hashCode() {
            int result = world.provider.dimensionId;
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
        final Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
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
        public final ModuleSendQueue[] moduleQueues = new ModuleSendQueue[Face.VALUES.length];

        public CasingSendQueue() {
            for (int i = 0; i < moduleQueues.length; i++) {
                moduleQueues[i] = new ModuleSendQueue();
            }
        }

        public void queueData(final Face face, final NBTTagCompound data, final byte type) {
            moduleQueues[face.ordinal()].queueData(data, type);
        }

        public void queueData(final Face face, final ByteBuf data, final byte type) {
            moduleQueues[face.ordinal()].queueData(data, type);
        }

        /**
         * Flush the casing's queue, sending all queued packets to clients.
         *
         * @param casing the casing this queue belongs to.
         */
        public void flush(final Casing casing) {
            final World world = casing.getCasingWorld();
            final Side side = world.isRemote ? Side.CLIENT : Side.SERVER;
            final ByteBuf data = Unpooled.buffer();
            collectData(data);
            if (data.readableBytes() > 0) {
                final MessageCasingData message = new MessageCasingData(casing, data);
                final boolean didSend;
                if (side == Side.CLIENT) {
                    Network.INSTANCE.getWrapper().sendToServer(message);
                    didSend = true;
                } else {
                    final NetworkRegistry.TargetPoint point = Network.getTargetPoint(casing.getCasingWorld(), casing.getPositionX(), casing.getPositionY(), casing.getPositionZ(), Network.RANGE_HIGH);
                    Network.INSTANCE.getWrapper().sendToAllAround(message, point);
                    didSend = areAnyPlayersNear(point);
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
                    ByteBufUtils.writeVarShort(data, moduleData.readableBytes());
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
        public void queueData(final NBTTagCompound data, final byte type) {
            sendQueue.add(new QueueEntryNBT(type, data));
        }

        /**
         * Enqueue the specified data packet.
         *
         * @param data the data to enqueue.
         * @param type the type of the data.
         */
        public void queueData(final ByteBuf data, final byte type) {
            sendQueue.add(new QueueEntryByteBuf(type, data));
        }

        /**
         * Collect all data in a tag list and clear the queue.
         *
         * @return the collected data for the module.
         */
        public ByteBuf collectData() {
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

            protected QueueEntry(final byte type) {
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
            public final NBTTagCompound data;

            public QueueEntryNBT(final byte type, final NBTTagCompound data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                final ByteBuf data = Unpooled.buffer();
                final ByteBufOutputStream bos = new ByteBufOutputStream(data);
                try {
                    CompressedStreamTools.writeCompressed(this.data, bos);
                    if (data.readableBytes() > 0) {
                        buffer.writeBoolean(true);
                        ByteBufUtils.writeVarShort(buffer, data.readableBytes());
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

            public QueueEntryByteBuf(final byte type, final ByteBuf data) {
                super(type);
                this.data = data;
            }

            @Override
            public void write(final ByteBuf buffer) {
                if (data.readableBytes() > 0) {
                    buffer.writeBoolean(false);
                    ByteBufUtils.writeVarShort(buffer, data.readableBytes());
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
     * @param target the target point to check for.
     * @return <tt>true</tt> if there are nearby players; <tt>false</tt> otherwise.
     */
    @SuppressWarnings("unchecked")
    private static boolean areAnyPlayersNear(final NetworkRegistry.TargetPoint target) {
        for (final EntityPlayerMP player : (List<EntityPlayerMP>) FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList) {
            if (player.dimension == target.dimension) {
                final double dx = target.x - player.posX;
                final double dy = target.y - player.posY;
                final double dz = target.z - player.posZ;

                if (dx * dx + dy * dy + dz * dz < target.range * target.range) {
                    final NetworkDispatcher dispatcher = player.playerNetServerHandler.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
                    if (dispatcher != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
