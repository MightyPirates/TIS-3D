package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderLayerAccess;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.ext.TextureManagerExt;
import li.cil.tis3d.util.ColorUtils;
import li.cil.tis3d.util.EnumUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;

public final class DisplayModule extends AbstractModuleWithRotation {
    @Environment(EnvType.CLIENT)
    public static final class LeakDetector {
        private static final LinkedList<NativeImageBackedTexture> leakedTextures = new LinkedList<>();

        private LeakDetector() {
        }

        public static void add(@Nullable final NativeImageBackedTexture texture) {
            if (texture != null) {
                leakedTextures.add(texture);
            }
        }

        public static void tick() {
            while (!leakedTextures.isEmpty()) {
                final NativeImageBackedTexture texture = leakedTextures.remove();
                texture.close();
            }
        }
    }

    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The uncompressed image as RGBA, for faster application of draw calls
     * and uploading to the GPU. Also kept on the server to allow sending
     * current state to newly connected/coming closer clients.
     */
    private final int[] image = new int[RESOLUTION * RESOLUTION];
    private boolean imageDirty = false;

    @Environment(EnvType.CLIENT)
    private NativeImageBackedTexture backingTexture;

    @Environment(EnvType.CLIENT)
    private Identifier backingTextureId;

    @Environment(EnvType.CLIENT)
    private RenderLayer renderLayer;

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
    private static final int RESOLUTION = 32;

    // Don't allow displaying stuff on the edge of the casing. I mean we could,
    // technically, but that'd usually look pretty weird. Also it's more
    // intuitive that the usable area start in the inner, black part.
    private static final int MARGIN = 2;

    // NBT tag names.
    private static final String TAG_IMAGE = "image";
    private static final String TAG_STATE = "state";
    private static final String TAG_DRAW_CALL = "drawCall";

    // Data packet types.
    private static final byte DATA_TYPE_CLEAR = 0;

    // Runtime client-side bookkeeping
    @Environment(EnvType.CLIENT)
    private static int textureIdCounter = 1;

    // --------------------------------------------------------------------- //

    public DisplayModule(final Casing casing, final Face face) {
        super(casing, face);
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
        if (getCasing().getCasingWorld().isClient) {
            //noinspection MethodCallSideOnly Guarded by isClient check.
            deleteTexture();
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void finalize() {
        LeakDetector.add(backingTexture);
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

        final RenderLayer rl = getRenderLayer();
        updateBackingTexture();

        final VertexConsumer vc = vcp.getBuffer(rl);
        RenderUtil.drawQuad(matrices.peek(), vc, RenderUtil.maxLight, overlay);

        matrices.pop();
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        final int[] imageNbt = nbt.getIntArray(TAG_IMAGE);
        System.arraycopy(imageNbt, 0, image, 0, Math.min(imageNbt.length, image.length));
        imageDirty = true;

        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);

        final byte[] drawCallNbt = nbt.getByteArray(TAG_DRAW_CALL);
        System.arraycopy(drawCallNbt, 0, drawCall, 0, Math.min(drawCallNbt.length, drawCall.length));
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        nbt.putIntArray(TAG_IMAGE, image);
        EnumUtils.writeToNBT(state, TAG_STATE, nbt);
        nbt.putByteArray(TAG_DRAW_CALL, drawCall.clone());
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
        drawCall[state.ordinal()] = (byte)value;
        state = state.getNext();
        if (state == State.COLOR) {
            // Draw call completed, apply and send to client.
            applyDrawCall(drawCall);
            sendDrawCall();
        }
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

        final int x0 = MARGIN + Math.max(0, xin);
        final int x1 = MARGIN + Math.min(RESOLUTION - 2 * MARGIN, xin + w);
        final int y0 = MARGIN + Math.max(0, yin);
        final int y1 = MARGIN + Math.min(RESOLUTION - 2 * MARGIN, yin + h);

        for (int y = y0; y < y1; y++) {
            final int offset = y * RESOLUTION;
            for (int x = x0; x < x1; x++) {
                final int index = offset + x;
                int v = ColorUtils.getColorByIndex(Math.max(0, color));
                v = (v & 0xFF00FF00) | ((v & 0xFF0000) >> 16) | ((v & 0xFF) << 16);
                image[index] = v;
            }
        }
    }

    /**
     * Gets the texture backed by our NativeImage, creating it if required
     */
    @Environment(EnvType.CLIENT)
    private NativeImageBackedTexture getBackingTexture() {
        if (backingTexture == null) {
            backingTexture = new NativeImageBackedTexture(RESOLUTION, RESOLUTION, false);
        }

        return backingTexture;
    }

    /**
     * Blit the raw image RGBA to @img
     *
     * @param img the NativeImage to blit to
     */
    @Environment(EnvType.CLIENT)
    private void blitToNativeImage(final NativeImage img) {
        int ip = 0;
        for (int iy = 0; iy < RESOLUTION; iy++) {
            for (int ix = 0; ix < RESOLUTION; ix++, ip++) {
                img.setPixelColor(ix, iy, image[ip]);
            }
        }
    }

    /**
     * Check if any image updates happened, and update the
     * GPU-side texture apporpriately
     */
    @Environment(EnvType.CLIENT)
    private void updateBackingTexture() {
        if (!imageDirty) {
            return;
        }

        final NativeImageBackedTexture texture = getBackingTexture();
        blitToNativeImage(texture.getImage());
        texture.upload();

        imageDirty = false;
    }

    @Environment(EnvType.CLIENT)
    static private Identifier generateDynTextureId() {
        final int id = textureIdCounter++;
        return new Identifier("tis3d", "dynamic/dispmod_" + id);
    }

    /**
     * Gets the render layer associated with our texture, creating it if required.
     */
    @Environment(EnvType.CLIENT)
    private RenderLayer getRenderLayer() {
        if (renderLayer == null) {
            final TextureManager texMan = MinecraftClient.getInstance().getTextureManager();
            final NativeImageBackedTexture tex = getBackingTexture();
            backingTextureId = generateDynTextureId();

            texMan.registerTexture(backingTextureId, tex);

            renderLayer = RenderLayerAccess.getCutoutNoDiffLight(backingTextureId);
        }

        return renderLayer;
    }

    /**
     * Deletes our texture from the GPU, if we have one.
     */
    @Environment(EnvType.CLIENT)
    private void deleteTexture() {
        if (backingTextureId != null) {
            final TextureManager texMan = MinecraftClient.getInstance().getTextureManager();
            TextureManagerExt.from(texMan).unregisterTexture(backingTextureId);
        }

        if (backingTexture != null) {
            // Also closes the associated image
            backingTexture.close();
            backingTexture = null;
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
