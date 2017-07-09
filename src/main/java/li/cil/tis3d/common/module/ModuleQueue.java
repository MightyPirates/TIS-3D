package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import li.cil.tis3d.util.OneEightCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * The queue module can be used to store a number of values to be retrieved
 * later on. It operates as FIFO queue, providing the bottom element to all
 * ports but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class ModuleQueue extends AbstractModuleRotatable {
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
        head = data.readByte();
        tail = data.readByte();
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
        if (!isEmpty() && OneEightCompat.getDistanceSqToCenter(Minecraft.getMinecraft().thePlayer, getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ()) < 64) {
            drawState();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] queueNbt = nbt.getIntArray(TAG_QUEUE);
        final int count = Math.min(queueNbt.length, queue.length);
        for (int i = 0; i < count; i++) {
            queue[i] = (short) queueNbt[i];
        }

        head = MathHelper.clamp_int(nbt.getInteger(TAG_HEAD), 0, QUEUE_SIZE - 1);
        tail = MathHelper.clamp_int(nbt.getInteger(TAG_TAIL), 0, QUEUE_SIZE - 1);
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final int[] queueNbt = new int[queue.length];
        for (int i = 0; i < queue.length; i++) {
            queueNbt[i] = queue[i];
        }
        nbt.setIntArray(TAG_QUEUE, queueNbt);

        nbt.setInteger(TAG_HEAD, head);
        nbt.setInteger(TAG_TAIL, tail);
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
                // Store the value.
                push(receivingPipe.read());

                // Start reading again right away to read as fast as possible.
                if (!isFull()) {
                    receivingPipe.beginRead();
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

    @SideOnly(Side.CLIENT)
    private void drawState() {
        // Offset to start drawing at top left of inner area, slightly inset.
        GL11.glTranslatef(3 / 16f, 5 / 16f, 0);
        GL11.glScalef(1 / 128f, 1 / 128f, 1);
        GL11.glTranslatef(4.5f, 14.5f, 0);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        for (int i = tail, j = 0; i != head; i = (i + 1) % QUEUE_SIZE, j++) {
            FontRendererAPI.drawString(String.format("%4X", queue[i]));
            GL11.glTranslatef(0, FontRendererAPI.getCharHeight() + 1, 0);
            if ((j + 1) % 4 == 0) {
                GL11.glTranslatef((FontRendererAPI.getCharWidth() + 1) * 5, (FontRendererAPI.getCharHeight() + 1) * -4, 0);
            }
        }
    }
}
