package li.cil.tis3d.common.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.api.util.FontRenderer;
import li.cil.tis3d.util.Color;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The queue module can be used to store a number of values to be retrieved
 * later on. It operates as FIFO queue, providing the bottom element to all
 * ports but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class ModuleQueue extends AbstractModuleWithRotation {
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

    public ModuleQueue(final Casing casing, final Face face) {
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        context.drawAtlasQuadUnlit(Textures.LOCATION_OVERLAY_MODULE_QUEUE);

        // Render detailed state when player is close.
        if (!isEmpty() && context.closeEnoughForDetails(getCasing().getPosition())) {
            drawState(context);
        }

        matrixStack.popPose();
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
        super.readFromNBT(nbt);

        final int[] queueNbt = nbt.getIntArray(TAG_QUEUE);
        final int count = Math.min(queueNbt.length, queue.length);
        for (int i = 0; i < count; i++) {
            queue[i] = (short) queueNbt[i];
        }

        head = MathHelper.clamp(nbt.getInt(TAG_HEAD), 0, QUEUE_SIZE - 1);
        tail = MathHelper.clamp(nbt.getInt(TAG_TAIL), 0, QUEUE_SIZE - 1);
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
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

    @OnlyIn(Dist.CLIENT)
    private void drawState(final RenderContext context) {
        final MatrixStack matrixStack = context.getMatrixStack();

        // Offset to start drawing at top left of inner area, slightly inset.
        matrixStack.translate(3 / 16f, 5 / 16f, 0);
        matrixStack.scale(1 / 128f, 1 / 128f, 1);
        matrixStack.translate(4.5f, 14.5f, 0);

        final FontRenderer fontRenderer = API.smallFontRenderer;
        for (int i = tail, j = 0; i != head; i = (i + 1) % QUEUE_SIZE, j++) {
            context.drawString(fontRenderer, String.format("%4X", queue[i]), Color.WHITE);
            matrixStack.translate(0, fontRenderer.getCharHeight() + 1, 0);
            if ((j + 1) % 4 == 0) {
                matrixStack.translate((fontRenderer.getCharWidth() + 1) * 5, (fontRenderer.getCharHeight() + 1) * -4, 0);
            }
        }
    }
}
