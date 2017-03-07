package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The queue module can be used to store a number of values to be retrieved
 * later on. It operates as FIFO queue, providing the bottom element to all ports
 * but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class ModuleQueue extends AbstractModuleRotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final short[] queue = new short[QUEUE_SIZE];
    private int next = -1;
    private int bottom = -1;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_QUEUE = "queue";
    private static final String TAG_NEXT = "next";
    private static final String TAG_BOTTOM = "bottom";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    /**
     * The number of elements the stack may store.
     */
    private static final int QUEUE_SIZE = 16;

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
        bottom = next = -1;

        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Pop the bottom value (the one that was being written).
        pop();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void onData(final ByteBuf data) {
        next = data.readByte();
        bottom = data.readByte();
        for (int i = 0; i < queue.length; i++) {
            queue[i] = data.readShort();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_QUEUE_OVERLAY));

        // Render detailed state when player is close.
        if (!isEmpty() && Minecraft.getMinecraft().player.getDistanceSqToCenter(getCasing().getPosition()) < 64) {
            drawState();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] stackNbt = nbt.getIntArray(TAG_QUEUE);
        final int count = Math.min(stackNbt.length, queue.length);
        for (int i = 0; i < count; i++) {
            queue[i] = (short) stackNbt[i];
        }

        next = Math.max(-1, Math.min(QUEUE_SIZE - 1, nbt.getInteger(TAG_NEXT)));
        bottom = Math.max(-1, Math.min(QUEUE_SIZE - 1, nbt.getInteger(TAG_BOTTOM)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final int[] stackNbt = new int[queue.length];
        for (int i = 0; i < queue.length; i++) {
            stackNbt[i] = queue[i];
        }
        nbt.setIntArray(TAG_QUEUE, stackNbt);

        nbt.setInteger(TAG_NEXT, next);
        nbt.setInteger(TAG_BOTTOM, bottom);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check whether the queue is currently empty, i.e. no more items can be retrieved.
     *
     * @return <tt>true</tt> if the queue is empty, <tt>false</tt> otherwise.
     */
    private boolean isEmpty() {
        return bottom < 0;
    }

    /**
     * Check whether the queue is currently full, i.e. no more items can be stored.
     *
     * @return <tt>true</tt> if the queue is full, <tt>false</tt> otherwise.
     */
    private boolean isFull() {
        return next == bottom && next != -1;
    }

    /**
     * Store the specified item on the queue.
     *
     * @param value the value to store on the queue.
     * @throws ArrayIndexOutOfBoundsException if the queue is full.
     */
    private void push(final short value) {
        if(next == -1) next = bottom = 0; // empty, so start at 0
        queue[next++] = value;
        next = next % QUEUE_SIZE;

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
        return queue[bottom];
    }

    /**
     * Reduces the queue size by one.
     */
    private void pop() {
        bottom = (bottom + 1) % QUEUE_SIZE;
        if(bottom == next) // we've overflowed into unset data
            bottom = next = -1;

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
                // Store the value.
                push(receivingPipe.read());

                // Restart all writes to ensure we're outputting the top-most value.
                cancelWrite();

                // Start reading again right away to read as fast as possible.
                if (!isFull()) {
                    receivingPipe.beginRead();
                }
            }
        }
    }

    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeByte(next);
        data.writeByte(bottom);
        for (final short value : queue) {
            data.writeShort(value);
        }
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }

    @SideOnly(Side.CLIENT)
    private void drawState() {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translate(3 / 16f, 5 / 16f, 0);
        GlStateManager.scale(1 / 128f, 1 / 128f, 1);
        GlStateManager.translate(4.5f, 14.5f, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);

        if(next != -1) {
            // because if the queue is full, bottom == top. If we don't ignore a collision on the first iteration it
            // won't render anything.
            boolean first = true;
            int relativeI = 0;
            for (int i = bottom; first || i != next; i = (i + 1) % QUEUE_SIZE, relativeI++) {
                first = false;
                FontRendererAPI.drawString(String.format("%4X", queue[i]));
                GlStateManager.translate(0, FontRendererAPI.getCharHeight() + 1, 0);
                if ((relativeI + 1) % 4 == 0) {
                    GlStateManager.translate((FontRendererAPI.getCharWidth() + 1) * 5, (FontRendererAPI.getCharHeight() + 1) * -4, 0);
                }
            }
        }
    }
}
