package li.cil.tis3d.common.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.ModRenderType;
import li.cil.tis3d.util.Color;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;

public final class ModuleDisplay extends AbstractModuleWithRotation {
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
    private boolean imageDirty = false;

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
    private static final int RESOLUTION = 24;

    // Don't allow displaying stuff on the edge of the casing. I mean we could,
    // technically, but that'd usually look pretty weird. Also it's more
    // intuitive that the usable area start in the inner, black part.
    private static final int MARGIN = 4;

    // NBT tag names.
    private static final String TAG_IMAGE = "image";
    private static final String TAG_STATE = "state";
    private static final String TAG_DRAW_CALL = "drawCall";

    // Data packet types.
    private static final byte DATA_TYPE_CLEAR = 0;

    // Running counter for unique dynamic texture ids.
    @OnlyIn(Dist.CLIENT)
    private static int nextTextureId;

    // Backing texture used to render the module data.
    @OnlyIn(Dist.CLIENT)
    private DynamicTexture texture;

    // Id of the backing texture, required by MC dynamic texture system.
    @OnlyIn(Dist.CLIENT)
    private ResourceLocation textureId;

    // Render layer we render our texture in.
    @OnlyIn(Dist.CLIENT)
    private RenderType renderLayer;

    // --------------------------------------------------------------------- //

    public ModuleDisplay(final Casing casing, final Face face) {
        super(casing, face);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void finalize() {
        TextureDisposer.add(texture);
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
        if (getCasing().getCasingLevel().isClientSide()) {
            deleteTexture();
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        validateTexture();

        final IVertexBuilder buffer = context.getBuffer(getOrCreateRenderLayer());
        context.drawQuad(buffer, MARGIN / 32f, MARGIN / 32f, RESOLUTION / 32f, RESOLUTION / 32f);

        matrixStack.popPose();
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt) {
        super.readFromNBT(nbt);

        final int[] imageNbt = nbt.getIntArray(TAG_IMAGE);
        System.arraycopy(imageNbt, 0, image, 0, Math.min(imageNbt.length, image.length));
        imageDirty = true;

        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);

        final byte[] drawCallNbt = nbt.getByteArray(TAG_DRAW_CALL);
        System.arraycopy(drawCallNbt, 0, drawCall, 0, Math.min(drawCallNbt.length, drawCall.length));
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt) {
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
        drawCall[state.ordinal()] = (byte) value;
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
     * Gets the texture used for uploading module data to the GPU, creates one if necessary.
     *
     * @return the texture used to upload data to the GPU.
     */
    @OnlyIn(Dist.CLIENT)
    private DynamicTexture getOrCreateTexture() {
        if (texture == null) {
            texture = new DynamicTexture(RESOLUTION, RESOLUTION, false);
        }

        return texture;
    }

    @OnlyIn(Dist.CLIENT)
    private RenderType getOrCreateRenderLayer() {
        if (renderLayer == null) {
            final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            final DynamicTexture texture = getOrCreateTexture();
            textureId = new ResourceLocation(API.MOD_ID, "dynamic/display_module_" + (++nextTextureId));
            textureManager.register(textureId, texture);
            renderLayer = ModRenderType.unlitTexture(textureId);
        }

        return renderLayer;
    }

    /**
     * Deletes our texture from the GPU, if we have one.
     */
    @OnlyIn(Dist.CLIENT)
    private void deleteTexture() {
        if (textureId != null) {
            Minecraft.getInstance().getTextureManager().release(textureId);
        }

        if (texture != null) {
            texture.close();
            texture = null;
        }
    }

    /**
     * Uploads new image data if it changed, creates texture if necessary.
     */
    @OnlyIn(Dist.CLIENT)
    private void validateTexture() {
        if (!imageDirty) {
            return;
        }

        imageDirty = false;

        final DynamicTexture texture = getOrCreateTexture();
        final NativeImage nativeImage = texture.getPixels();
        if (nativeImage == null) {
            return;
        }

        int ip = 0;
        for (int iy = 0; iy < RESOLUTION; iy++) {
            for (int ix = 0; ix < RESOLUTION; ix++, ip++) {
                final int argb = image[ip];
                final int a = Color.getAlphaU8(argb);
                final int b = Color.getBlueU8(argb);
                final int g = Color.getGreenU8(argb);
                final int r = Color.getRedU8(argb);
                nativeImage.setPixelRGBA(ix, iy, NativeImage.combine(a, b, g, r));
            }
        }

        texture.upload();
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

    // --------------------------------------------------------------------- //

    /**
     * Used to make absolutely sure we don't leak GPU memory. We should explicitly
     * free all texture memory in destroy/unload handlers, but can't play it too
     * safe here.
     */
    @OnlyIn(Dist.CLIENT)
    public static final class TextureDisposer {
        private static final LinkedList<DynamicTexture> leakedTextures = new LinkedList<>();

        public static void add(@Nullable final DynamicTexture texture) {
            if (texture != null) {
                leakedTextures.add(texture);
            }
        }

        public static void tick(@SuppressWarnings("unused") final TickEvent.ClientTickEvent event) {
            while (!leakedTextures.isEmpty()) {
                leakedTextures.remove().close();
            }
        }
    }
}
