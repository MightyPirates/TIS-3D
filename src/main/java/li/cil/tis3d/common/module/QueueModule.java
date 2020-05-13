package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.BiConsumer;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * The queue module can be used to store a number of values to be retrieved
 * later on. It operates as FIFO queue, providing the bottom element to all
 * ports but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class QueueModule extends StackModule {
    // --------------------------------------------------------------------- //
    // Persisted data

    private int head = 0; // Highest element index, exclusive.
    private int tail = 0; // Lowest element index, inclusive.

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_QUEUE = "queue";
    private static final String TAG_HEAD = "head";
    private static final String TAG_TAIL = "tail";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    /**
     * The number of elements the queue may store, plus one never used slot
     * to allow easily differentiating empty and full queue states.
     */
    private static final int QUEUE_SIZE = 17;

    @Override
    protected String getValuesTag() {
        return TAG_QUEUE;
    }

    @Override
    protected int getDataSize() {
        return QUEUE_SIZE;
    }

    @Override
    protected Identifier getBaseTexture() {
        return Textures.LOCATION_OVERLAY_MODULE_QUEUE;
    }

    @Override
    protected void forEachValue(BiConsumer<Integer,Short> op) {
        for (int i = tail, j = 0; i != head; i = (i + 1) % QUEUE_SIZE, j++) {
            op.accept(j, values[i]);
        }
    }

    // --------------------------------------------------------------------- //

    public QueueModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void onDisabled() {
        // Clear queue on shutdown.
        head = tail = 0;

        sendData();
    }

    @Override
    public void onData(final ByteBuf data) {
        head = data.readByte();
        tail = data.readByte();
        for (int i = 0; i < values.length; i++) {
            values[i] = data.readShort();
        }
    }

    @Override
    protected void readFromNBTInternal(final CompoundTag nbt) {
        head = MathHelper.clamp(nbt.getInt(TAG_HEAD), 0, QUEUE_SIZE - 1);
        tail = MathHelper.clamp(nbt.getInt(TAG_TAIL), 0, QUEUE_SIZE - 1);
    }

    @Override
    protected void writeToNBTInternal(final CompoundTag nbt) {
        nbt.putInt(TAG_HEAD, head);
        nbt.putInt(TAG_TAIL, tail);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check whether the queue is currently empty, i.e. no more items can be retrieved.
     *
     * @return <tt>true</tt> if the queue is empty, <tt>false</tt> otherwise.
     */
    @Override
    protected boolean isEmpty() {
        return head == tail;
    }

    /**
     * Check whether the queue is currently full, i.e. no more items can be stored.
     *
     * @return <tt>true</tt> if the queue is full, <tt>false</tt> otherwise.
     */
    @Override
    protected boolean isFull() {
        return (head + 1) % QUEUE_SIZE == tail;
    }

    /**
     * Store the specified item on the queue.
     *
     * @param value the value to store on the queue.
     * @throws ArrayIndexOutOfBoundsException if the queue is full.
     */
    @Override
    protected void push(final short value) {
        values[head] = value;
        head = (head + 1) % QUEUE_SIZE;

        sendData();
    }

    /**
     * Retrieve the value that's currently on top of the queue, i.e. the value
     * that was last pushed to the queue.
     *
     * @return the value on top of the queue.
     * @throws ArrayIndexOutOfBoundsException if the queue is empty.
     */
    @Override
    protected short peek() {
        return values[tail];
    }

    /**
     * Reduces the queue size by one.
     */
    @Override
    protected void pop() {
        tail = (tail + 1) % QUEUE_SIZE;

        sendData();
    }

    /**
     * Update the inputs of the queue, pulling values onto the queue.
     */
    @Override
    protected void stepInput() {
        for (final Port port : Port.VALUES) {
            // Stop reading if the queue is full.
            if (isFull()) {
                return;
            }

            // Continuously read from all ports, push back last received value.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                final boolean wasEmpty = isEmpty();

                // Store the value.
                push(receivingPipe.read());

                if (wasEmpty) {
                    stepOutput();
                }
            }
        }
    }

    @Override
    protected void sendDataInternal(final ByteBuf data) {
        data.writeByte(head);
        data.writeByte(tail);
    }
}
