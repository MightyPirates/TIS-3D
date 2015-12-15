package li.cil.tis3d.common.module;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.FontRendererAPI;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    private final int[] stack = new int[STACK_SIZE];
    private int top = -1;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT data names.
    private static final String TAG_STACK = "stack";
    private static final String TAG_TOP = "top";

    /**
     * The number of elements the stack may store.
     */
    public static final int STACK_SIZE = 16;

    private static final ResourceLocation LOCATION_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleStack.png");

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
    public void onData(final NBTTagCompound nbt) {
        readFromNBT(nbt);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        RenderUtil.bindTexture(LOCATION_OVERLAY);

        // Draw base overlay.
        RenderUtil.drawQuad();

        // Render detailed state when player is close.
        if (!isEmpty() && Minecraft.getMinecraft().thePlayer.getDistanceSqToCenter(getCasing().getPosition()) < 64) {
            drawState();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] stackNbt = nbt.getIntArray(TAG_STACK);
        System.arraycopy(stackNbt, 0, stack, 0, Math.min(stackNbt.length, stack.length));
        top = Math.max(-1, Math.min(STACK_SIZE - 1, nbt.getInteger(TAG_TOP)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setIntArray(TAG_STACK, stack);
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
    private void push(final int value) {
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
    private int peek() {
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
                receivingPipe.beginRead();
            }
        }
    }

    private void sendData() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        getCasing().sendData(getFace(), nbt);
    }

    @SideOnly(Side.CLIENT)
    private void drawState() {
        // Offset to start drawing at top left of inner area, slightly inset.
        GlStateManager.translate(3 / 16f, 5 / 16f, 0);
        GlStateManager.scale(1 / 128f, 1 / 128f, 1);
        GlStateManager.translate(4.5f, 14.5f, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);

        for (int i = 0; i <= top; i++) {
            FontRendererAPI.drawString(String.format("%4X", (short) stack[i]));
            GlStateManager.translate(0, FontRendererAPI.getCharHeight() + 1, 0);
            if ((i + 1) % 4 == 0) {
                GlStateManager.translate((FontRendererAPI.getCharWidth() + 1) * 5, (FontRendererAPI.getCharHeight() + 1) * -4, 0);
            }
        }
    }
}
