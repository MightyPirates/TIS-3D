package li.cil.tis3d.common.module;

import com.mojang.blaze3d.platform.GlStateManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import net.minecraft.util.math.MathHelper;

/**
 * The queue module can be used to store a number of values to be retrieved
 * later on. It operates as FIFO queue, providing the bottom element to all
 * ports but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class QueueModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final short[] queue = new short[QUEUE_SIZE];
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

    // --------------------------------------------------------------------- //

    public QueueModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepOutput();
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Clear queue on shutdown.
        head = tail = 0;

        sendData();
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Pop the bottom value (the one that was being written).
        pop();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure all our writes are in sync.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void onData(final ByteBuf data) {
        head = data.readByte();
        tail = data.readByte();
        for (int i = 0; i < queue.length; i++) {
            queue[i] = data.readShort();
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks) {
        if (!getCasing().isEnabled()) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();

        RenderUtil.drawQuad(RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_QUEUE));

        // Render detailed state when player is close.
        if (!isEmpty() && rendererDispatcher.camera.getBlockPos().getSquaredDistance(getCasing().getPosition()) < 64) {
            drawState();
        }
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        final int[] queueNbt = nbt.getIntArray(TAG_QUEUE);
        final int count = Math.min(queueNbt.length, queue.length);
        for (int i = 0; i < count; i++) {
            queue[i] = (short)queueNbt[i];
        }

        head = MathHelper.clamp(nbt.getInt(TAG_HEAD), 0, QUEUE_SIZE - 1);
        tail = MathHelper.clamp(nbt.getInt(TAG_TAIL), 0, QUEUE_SIZE - 1);
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        final int[] queueNbt = new int[queue.length];
        for (int i = 0; i < queue.length; i++) {
            queueNbt[i] = queue[i];
        }
        nbt.putIntArray(TAG_QUEUE, queueNbt);

        nbt.putInt(TAG_HEAD, head);
        nbt.putInt(TAG_TAIL, tail);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check whether the queue is currently empty, i.e. no more items can be retrieved.
     *
     * @return <tt>true</tt> if the queue is empty, <tt>false</tt> otherwise.
     */
    private boolean isEmpty() {
        return head == tail;
    }

    /**
     * Check whether the queue is currently full, i.e. no more items can be stored.
     *
     * @return <tt>true</tt> if the queue is full, <tt>false</tt> otherwise.
     */
    private boolean isFull() {
        return (head + 1) % QUEUE_SIZE == tail;
    }

    /**
     * Store the specified item on the queue.
     *
     * @param value the value to store on the queue.
     * @throws ArrayIndexOutOfBoundsException if the queue is full.
     */
    private void push(final short value) {
        queue[head] = value;
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
    private short peek() {
        return queue[tail];
    }

    /**
     * Reduces the queue size by one.
     */
    private void pop() {
        tail = (tail + 1) % QUEUE_SIZE;

        sendData();
    }

    /**
     * Update the outputs of the queue, pushing the top value.
     */
    private void stepOutput() {
        // Don't try to write if the queue is empty.
        if (isEmpty()) {
            return;
        }

        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(peek());
            }
        }
    }

    /**
     * Update the inputs of the queue, pulling values onto the queue.
     */
    private void stepInput() {
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

    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeByte(head);
        data.writeByte(tail);
        for (final short value : queue) {
            data.writeShort(value);
        }
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }

    @Environment(EnvType.CLIENT)
    private void drawState() {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translatef(3 / 16f, 5 / 16f, 0);
        GlStateManager.scalef(1 / 128f, 1 / 128f, 1);
        GlStateManager.translatef(4.5f, 14.5f, 0);
        GlStateManager.color4f(1f, 1f, 1f, 1f);

        for (int i = tail, j = 0; i != head; i = (i + 1) % QUEUE_SIZE, j++) {
            FontRendererAPI.drawString(String.format("%4X", queue[i]));
            GlStateManager.translatef(0, FontRendererAPI.getCharHeight() + 1, 0);
            if ((j + 1) % 4 == 0) {
                GlStateManager.translatef((FontRendererAPI.getCharWidth() + 1) * 5, (FontRendererAPI.getCharHeight() + 1) * -4, 0);
            }
        }
    }
}
