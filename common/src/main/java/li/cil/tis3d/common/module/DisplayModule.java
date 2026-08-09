package li.cil.tis3d.common.module;

import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.DisplayModuleRenderer;
import li.cil.tis3d.util.Color;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;

public final class DisplayModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The uncompressed image as RGBA, for faster application of draw calls
     * and uploading to the GPU. Also kept on the server to allow sending
     * current state to newly connected/coming closer clients.
     */
    private final int[] image = new int[RESOLUTION * RESOLUTION];

    /**
     * Whether the image data changed and needs to be re-uploaded to the GPU.
     */
    private boolean imageDirty;

    /**
     * The current input state, i.e. what value we're currently reading.
     */
    private State state = State.COLOR;

    /**
     * The currently being-built draw call. Stored as byte-array for more
     * convenient saving and loading (and sending to clients).
     */
    private final byte[] drawCall = new byte[State.values().length];

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * Current state of the display module, decides what happens with the next
     * value read on any of the ports.
     */
    private enum State {
        COLOR, X, Y, W, H;

        public static final State[] VALUES = State.values();

        public State getNext() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }
    }

    // Resolution of the screen in pixels, width = height.
    public static final int RESOLUTION = 24;

    // Don't allow displaying stuff on the edge of the casing. I mean we could,
    // technically, but that'd usually look pretty weird. Also it's more
    // intuitive that the usable area start in the inner, black part.
    public static final int MARGIN = 4;

    // NBT tag names.
    private static final String TAG_IMAGE = "image";
    private static final String TAG_STATE = "state";
    private static final String TAG_DRAW_CALL = "drawCall";

    // Data packet types.
    private static final byte DATA_TYPE_CLEAR = 0;

    // --------------------------------------------------------------------- //

    public DisplayModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    public int[] getImage() {
        return image;
    }

    public boolean consumeImageDirty() {
        if (!imageDirty) {
            return false;
        }
        imageDirty = false;
        return true;
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        for (final Port port : Port.VALUES) {
            stepInput(port);
        }
    }

    @Override
    public void onDisabled() {
        Arrays.fill(image, 0);
        state = State.COLOR;
        imageDirty = true;

        sendClear();
    }

    @Override
    public void onDisposed() {
        super.onDisposed();
        if (getCasing().getCasingLevel().isClientSide()) {
            DisplayModuleRenderer.dispose(this);
        }
    }

    @Override
    public void onData(final ByteBuf data) {
        if (data.readBoolean()) {
            Arrays.fill(image, 0);
        } else {
            data.readBytes(drawCall);
            applyDrawCall(drawCall);
        }

        imageDirty = true;
    }

    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final PoseStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        DisplayModuleRenderer.render(this, context);

        matrixStack.popPose();
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        final int[] imageTag = tag.getIntArray(TAG_IMAGE).orElse(new int[0]);
        System.arraycopy(imageTag, 0, image, 0, Math.min(imageTag.length, image.length));
        imageDirty = true;

        state = EnumUtils.load(State.class, TAG_STATE, tag);

        final byte[] drawCallTag = tag.getByteArray(TAG_DRAW_CALL).orElse(new byte[0]);
        System.arraycopy(drawCallTag, 0, drawCall, 0, Math.min(drawCallTag.length, drawCall.length));
    }

    @Override
    public void save(final CompoundTag tag) {
        super.save(tag);

        tag.putIntArray(TAG_IMAGE, image.clone());
        EnumUtils.save(state, TAG_STATE, tag);
        tag.putByteArray(TAG_DRAW_CALL, drawCall.clone());
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the input of the module, adding any read value to our draw call.
     */
    private void stepInput(final Port port) {
        // Continuously read from all ports, set output to last received value.
        final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
        if (!receivingPipe.isReading()) {
            receivingPipe.beginRead();
        }
        if (receivingPipe.canTransfer()) {
            process(receivingPipe.read());
        }
    }

    /**
     * Process a value read from any port.
     *
     * @param value the value that was read.
     */
    private void process(final short value) {
        drawCall[state.ordinal()] = (byte) value;
        state = state.getNext();
        if (state == State.COLOR) {
            // Draw call completed, apply and send to client.
            applyDrawCall(drawCall);
            sendDrawCall();
        }
        getCasing().setChanged();
    }

    /**
     * Apply a draw call encoded in the specified byte array to our data array.
     *
     * @param drawCall the draw call to apply.
     */
    private void applyDrawCall(final byte[] drawCall) {
        final byte color = drawCall[State.COLOR.ordinal()];
        final byte xin = drawCall[State.X.ordinal()];
        final byte yin = drawCall[State.Y.ordinal()];
        final byte w = drawCall[State.W.ordinal()];
        final byte h = drawCall[State.H.ordinal()];

        final int argb = Color.getColorByIndex(color & 0xFF);
        final int x0 = Math.max(0, xin);
        final int x1 = Math.min(RESOLUTION, xin + w);
        final int y0 = Math.max(0, yin);
        final int y1 = Math.min(RESOLUTION, yin + h);

        for (int y = y0; y < y1; y++) {
            final int offset = y * RESOLUTION;
            for (int x = x0; x < x1; x++) {
                final int index = offset + x;
                image[index] = argb;
            }
        }
    }

    /**
     * Indicate to our client representation to clear the image data.
     */
    private void sendClear() {
        final ByteBuf data = Unpooled.buffer();
        data.writeBoolean(true);
        getCasing().sendData(getFace(), data, DATA_TYPE_CLEAR);
    }

    /**
     * Send a draw call to our client representation.
     */
    private void sendDrawCall() {
        final ByteBuf data = Unpooled.buffer();
        data.writeBoolean(false);
        data.writeBytes(drawCall);
        getCasing().sendData(getFace(), data);
    }
}
