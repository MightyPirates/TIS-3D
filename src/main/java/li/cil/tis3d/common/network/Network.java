package li.cil.tis3d.common.network;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.client.network.handler.MessageHandlerCasingState;
import li.cil.tis3d.client.network.handler.MessageHandlerHaltAndCatchFire;
import li.cil.tis3d.client.network.handler.MessageHandlerParticleEffects;
import li.cil.tis3d.common.network.handler.MessageHandlerBookCodeData;
import li.cil.tis3d.common.network.handler.MessageHandlerCasingData;
import li.cil.tis3d.common.network.message.MessageBookCodeData;
import li.cil.tis3d.common.network.message.MessageCasingData;
import li.cil.tis3d.common.network.message.MessageCasingState;
import li.cil.tis3d.common.network.message.MessageHaltAndCatchFire;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public final class Network {
    public static final Network INSTANCE = new Network();

    public static final int RANGE_HIGH = 64;
    public static final int RANGE_MEDIUM = 32;
    public static final int RANGE_LOW = 16;

    private static SimpleNetworkWrapper wrapper;

    // --------------------------------------------------------------------- //

    public void init() {
        wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(API.MOD_ID);

        int discriminator = 1;

        wrapper.registerMessage(MessageHandlerCasingData.class, MessageCasingData.class, discriminator++, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingData.class, MessageCasingData.class, discriminator++, Side.SERVER);
        wrapper.registerMessage(MessageHandlerParticleEffects.class, MessageParticleEffect.class, discriminator++, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerCasingState.class, MessageCasingState.class, discriminator++, Side.CLIENT);
        wrapper.registerMessage(MessageHandlerBookCodeData.class, MessageBookCodeData.class, discriminator++, Side.SERVER);
        wrapper.registerMessage(MessageHandlerHaltAndCatchFire.class, MessageHaltAndCatchFire.class, discriminator++, Side.CLIENT);
    }

    public SimpleNetworkWrapper getWrapper() {
        return wrapper;
    }

    // --------------------------------------------------------------------- //

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final double x, final double y, final double z, final int range) {
        return new NetworkRegistry.TargetPoint(world.provider.getDimensionId(), x, y, z, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final World world, final BlockPos position, final int range) {
        return getTargetPoint(world, position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, range);
    }

    public static NetworkRegistry.TargetPoint getTargetPoint(final TileEntity tileEntity, final int range) {
        return getTargetPoint(tileEntity.getWorld(), tileEntity.getPos(), range);
    }

    // --------------------------------------------------------------------- //

    public static void sendModuleData(final Casing casing, final Face face, final NBTTagCompound data, final byte type) {
        getQueueFor(casing).queueData(face, data, type);
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.type == TickEvent.Type.SERVER && event.getPhase() == EventPriority.NORMAL) {
            flushQueues(Side.SERVER);
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.type == TickEvent.Type.CLIENT && event.getPhase() == EventPriority.NORMAL) {
            flushQueues(Side.CLIENT);
        }
    }

    // --------------------------------------------------------------------- //

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
        final Side side = casing.getCasingWorld().isRemote ? Side.CLIENT : Side.SERVER;
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

    private static void flushQueues(final Side side) {
        final Map<Casing, CasingSendQueue> queues = getQueues(side);
        queues.forEach(Network::flushQueue);
        clearQueues(queues);
    }

    private static void flushQueue(final Casing casing, final CasingSendQueue queue) {
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

        public void flush(final Casing casing) {
            final NBTTagCompound nbt = new NBTTagCompound();
            collectData(nbt);
            if (!nbt.hasNoTags()) {
                final MessageCasingData message = new MessageCasingData(casing, nbt);
                if (casing.getCasingWorld().isRemote) {
                    Network.INSTANCE.getWrapper().sendToServer(message);
                } else {
                    final NetworkRegistry.TargetPoint point = Network.getTargetPoint(casing.getCasingWorld(), casing.getPosition(), Network.RANGE_HIGH);
                    Network.INSTANCE.getWrapper().sendToAllAround(message, point);
                }
            }
        }

        private void collectData(final NBTTagCompound nbt) {
            for (int i = 0; i < moduleQueues.length; i++) {
                final NBTTagList data = moduleQueues[i].collectData();
                if (data.tagCount() > 0) {
                    nbt.setTag(String.valueOf(i), data);
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
            sendQueue.add(new QueueEntry(type, data));
        }

        /**
         * Collect all data in a tag list and clear the queue.
         *
         * @return the collected data for the module.
         */
        public NBTTagList collectData() {
            // Building the list backwards to easily use the last data of
            // any type without having to remove from the queue.
            final NBTTagList nbt = new NBTTagList();
            for (int i = sendQueue.size() - 1; i >= 0; i--) {
                final byte type = sendQueue.get(i).type;
                if (type >= 0) {
                    if (sentTypes.get(type)) {
                        continue;
                    }
                    sentTypes.set(type);
                }
                final NBTTagCompound data = sendQueue.get(i).data;
                nbt.appendTag(data);
            }

            sendQueue.clear();
            sentTypes.clear();

            return nbt;
        }

        private static final class QueueEntry {
            public final byte type;
            public final NBTTagCompound data;

            private QueueEntry(final byte type, final NBTTagCompound data) {
                this.type = type;
                this.data = data;
            }
        }
    }

    // --------------------------------------------------------------------- //

    private Network() {
    }
}
