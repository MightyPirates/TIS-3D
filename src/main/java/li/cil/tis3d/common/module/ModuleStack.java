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
 * The stack module can be used to store a number of values to be retrieved
 * later on. It operates as LIFO queue, providing the top element to all ports
 * but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public final class ModuleStack extends AbstractModuleRotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final short[] stack = new short[STACK_SIZE];
    private int top = -1;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_STACK = "stack";
    private static final String TAG_TOP = "top";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    /**
     * The number of elements the stack may store.
     */
    private static final int STACK_SIZE = 16;

    // --------------------------------------------------------------------- //

    public ModuleStack(final Casing casing, final Face face) {
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
        // Clear stack on shutdown.
        top = -1;

        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Pop the top value (the one that was being written).
        pop();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @Override
    public void onData(final ByteBuf data) {
        top = data.readByte();
        for (int i = 0; i < stack.length; i++) {
            stack[i] = data.readShort();
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

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_STACK_OVERLAY));

        // Render detailed state when player is close.
        if (!isEmpty() && OneEightCompat.getDistanceSqToCenter(Minecraft.getMinecraft().thePlayer, getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ()) < 64) {
            drawState();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] stackNbt = nbt.getIntArray(TAG_STACK);
        final int count = Math.min(stackNbt.length, stack.length);
        for (int i = 0; i < count; i++) {
            stack[i] = (short) stackNbt[i];
        }

        top = MathHelper.clamp_int(nbt.getInteger(TAG_TOP), -1, STACK_SIZE - 1);
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final int[] stackNbt = new int[stack.length];
        for (int i = 0; i < stack.length; i++) {
            stackNbt[i] = stack[i];
        }
        nbt.setIntArray(TAG_STACK, stackNbt);

        nbt.setInteger(TAG_TOP, top);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check whether the stack is currently empty, i.e. no more items can be retrieved.
     *
     * @return <tt>true</tt> if the stack is empty, <tt>false</tt> otherwise.
     */
    private boolean isEmpty() {
        return top < 0;
    }

    /**
     * Check whether the stack is currently full, i.e. no more items can be stored.
     *
     * @return <tt>true</tt> if the stack is full, <tt>false</tt> otherwise.
     */
    private boolean isFull() {
        return top >= STACK_SIZE - 1;
    }

    /**
     * Store the specified item on the stack.
     *
     * @param value the value to store on the stack.
     * @throws ArrayIndexOutOfBoundsException if the stack is full.
     */
    private void push(final short value) {
        stack[++top] = value;

        sendData();
    }

    /**
     * Retrieve the value that's currently on top of the stack, i.e. the value
     * that was last pushed to the stack.
     *
     * @return the value on top of the stack.
     * @throws ArrayIndexOutOfBoundsException if the stack is empty.
     */
    private short peek() {
        return stack[top];
    }

    /**
     * Reduces the stack size by one.
     */
    private void pop() {
        top = Math.max(-1, top - 1);

        sendData();
    }

    /**
     * Update the outputs of the stack, pushing the top value.
     */
    private void stepOutput() {
        // Don't try to write if the stack is empty.
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
     * Update the inputs of the stack, pulling values onto the stack.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Stop reading if the stack is full.
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
        data.writeByte(top);
        for (final short value : stack) {
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

        for (int i = 0; i <= top; i++) {
            FontRendererAPI.drawString(String.format("%4X", stack[i]));
            GL11.glTranslatef(0, FontRendererAPI.getCharHeight() + 1, 0);
            if ((i + 1) % 4 == 0) {
                GL11.glTranslatef((FontRendererAPI.getCharWidth() + 1) * 5, (FontRendererAPI.getCharHeight() + 1) * -4, 0);
            }
        }
    }
}
