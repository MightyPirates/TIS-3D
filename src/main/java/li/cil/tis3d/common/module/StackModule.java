package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.BiConsumer;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.client.render.font.AbstractFontRenderer;
import li.cil.tis3d.client.render.font.SmallFontRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * The stack module can be used to store a number of values to be retrieved
 * later on. It operates as LIFO queue, providing the top element to all ports
 * but a single value can only be read from one port.
 * <p>
 * While it is not full, it will receive data on all ports and push them back.
 */
public class StackModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    protected final short[] values;
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

    public StackModule(final Casing casing, final Face face) {
        super(casing, face);
        values = new short[getDataSize()];
    }

    // Class behavior
    protected String getValuesTag() {
        // Backwards compat...
        return TAG_STACK;
    }

    protected int getDataSize() {
        return STACK_SIZE;
    }

    protected Identifier getBaseTexture() {
        return Textures.LOCATION_OVERLAY_MODULE_STACK;
    }

    protected void forEachValue(BiConsumer<Integer,Short> op) {
        for (int i = 0; i <= top; i++) {
            op.accept(i, values[i]);
        }
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
    public void onBeforeWriteComplete(final Port port) {
        // Pop the top value (the one that was being written).
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
        top = data.readByte();
        for (int i = 0; i < values.length; i++) {
            values[i] = data.readShort();
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                       final MatrixStack matrices, final VertexConsumerProvider vcp,
                       final int light, final int overlay) {
        if (!getCasing().isEnabled()) {
            return;
        }

        matrices.push();
        rotateForRendering(matrices);

        final Sprite baseSprite = RenderUtil.getSprite(getBaseTexture());
        final VertexConsumer vc = vcp.getBuffer(RenderLayer.getCutoutMipped());

        RenderUtil.drawQuad(baseSprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);

        // Render detailed state when player is close.
        if (!isEmpty() && rendererDispatcher.camera.getBlockPos().getSquaredDistance(getCasing().getPosition()) < 64) {
            drawState(matrices, vcp, RenderUtil.maxLight, overlay);
        }

        matrices.pop();
    }

    protected void readFromNBTInternal(final CompoundTag nbt) {
        top = MathHelper.clamp(nbt.getInt(TAG_TOP), -1, STACK_SIZE - 1);
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        final int[] valueNbt = nbt.getIntArray(getValuesTag());
        final int count = Math.min(valueNbt.length, values.length);
        for (int i = 0; i < count; i++) {
            values[i] = (short)valueNbt[i];
        }

        readFromNBTInternal(nbt);
    }

    protected void writeToNBTInternal(final CompoundTag nbt) {
        nbt.putInt(TAG_TOP, top);
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        final int[] valueNbt = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            valueNbt[i] = values[i];
        }
        nbt.putIntArray(getValuesTag(), valueNbt);

        writeToNBTInternal(nbt);
    }

    // --------------------------------------------------------------------- //

    /**
     * Check whether the stack is currently empty, i.e. no more items can be retrieved.
     *
     * @return <tt>true</tt> if the stack is empty, <tt>false</tt> otherwise.
     */
    protected boolean isEmpty() {
        return top < 0;
    }

    /**
     * Check whether the stack is currently full, i.e. no more items can be stored.
     *
     * @return <tt>true</tt> if the stack is full, <tt>false</tt> otherwise.
     */
    protected boolean isFull() {
        return top >= STACK_SIZE - 1;
    }

    /**
     * Store the specified item on the stack.
     *
     * @param value the value to store on the stack.
     * @throws ArrayIndexOutOfBoundsException if the stack is full.
     */
    protected void push(final short value) {
        values[++top] = value;

        sendData();
    }

    /**
     * Retrieve the value that's currently on top of the stack, i.e. the value
     * that was last pushed to the stack.
     *
     * @return the value on top of the stack.
     * @throws ArrayIndexOutOfBoundsException if the stack is empty.
     */
    protected short peek() {
        return values[top];
    }

    /**
     * Reduces the stack size by one.
     */
    protected void pop() {
        top = Math.max(-1, top - 1);

        sendData();
    }

    /**
     * Update the outputs of the stack, pushing the top value.
     */
    protected void stepOutput() {
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
    protected void stepInput() {
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
                stepOutput();
            }
        }
    }

    protected void sendDataInternal(final ByteBuf data) {
        data.writeByte(top);
    }

    protected void sendData() {
        final ByteBuf data = Unpooled.buffer();
        sendDataInternal(data);

        for (final short value : values) {
            data.writeShort(value);
        }
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }

    @Environment(EnvType.CLIENT)
    private void drawState(final MatrixStack matrices, final VertexConsumerProvider vcp,
                           final int light, final int overlay) {
        // Offset to start drawing at top left of inner area, slightly inset.
        matrices.translate(3 / 16f, 5 / 16f, 0);
        matrices.scale(1 / 128f, 1 / 128f, 1);
        matrices.translate(4.5f, 14.5f, 0);

        final AbstractFontRenderer fontRenderer = (AbstractFontRenderer) SmallFontRenderer.INSTANCE;
        final VertexConsumer vcFont = fontRenderer.chooseVertexConsumer(vcp);
        final int charWidth = fontRenderer.getCharWidth();
        final int charHeight = fontRenderer.getCharHeight();

        forEachValue((i, value) -> {
            final String str = String.format("%4X", value);
            fontRenderer.drawString(matrices.peek(), vcFont, light, overlay, str);
            matrices.translate(0, charHeight + 1, 0);

            if ((i + 1) % 4 == 0) {
                matrices.translate((charWidth + 1) * 5, (charHeight + 1) * -4, 0);
            }
        });
    }
}
