package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

public final class ModuleDisplay extends AbstractModuleRotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The uncompressed image as RGBA, for faster application of draw calls
     * and uploading to the GPU. Also kept on the server to allow sending
     * current state to newly connected/coming closer clients.
     */
    private final int[] image = new int[RESOLUTION * RESOLUTION];

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

    /**
     * Mapping of indices to colors as ARGB.
     */
    private static final int[] COLORS = new int[]{
            0xFFFFFFFF, // 0: White
            0xFFFFCC33, // 1: Orange
            0xFFCC66CC, // 2: Magenta
            0xFF6699FF, // 3: Light Blue
            0xFFFFFF33, // 4: Yellow
            0xFF33CC33, // 5: Lime
            0xFFFF6699, // 6: Pink
            0xFF333333, // 7: Gray
            0xFFCCCCCC, // 8: Silver
            0xFF336699, // 9: Cyan
            0xFF9933CC, // 10: Purple
            0xFF333399, // 11: Blue
            0xFF663300, // 12: Brown
            0xFF336600, // 13: Green
            0xFFFF3333, // 14: Red
            0xFF000000  // 15: Black
    };

    // Resolution of the screen in pixels, width = height.
    private static final int RESOLUTION = 32;

    // Don't allow displaying stuff on the edge of the casing. I mean we could,
    // technically, but that'd usually look pretty weird. Also it's more
    // intuitive that the usable area start in the inner, black part.
    private static final int MARGIN = 2;

    // NBT tag names.
    private static final String TAG_IMAGE = "image";
    private static final String TAG_STATE = "state";
    private static final String TAG_CLEAR = "clear";
    private static final String TAG_DRAW_CALL = "drawCall";

    /**
     * The ID of the uploaded texture on the GPU (client only).
     */
    private int glTextureId;

    // --------------------------------------------------------------------- //

    public ModuleDisplay(final Casing casing, final Face face) {
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

        sendClear();
    }

    @Override
    public void onDisposed() {
        if (getCasing().getCasingWorld().isRemote) {
            deleteTexture();
        }
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        if (nbt.getBoolean(TAG_CLEAR)) {
            Arrays.fill(image, 0);
        } else {
            applyDrawCall(nbt.getByteArray(TAG_DRAW_CALL));
        }
        TextureUtil.uploadTexture(getGlTextureId(), image, RESOLUTION, RESOLUTION);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 0f);

        GlStateManager.bindTexture(getGlTextureId());

        RenderUtil.drawQuad();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] imageNbt = nbt.getIntArray(TAG_IMAGE);
        System.arraycopy(imageNbt, 0, image, 0, Math.min(imageNbt.length, image.length));

        state = State.valueOf(nbt.getString(TAG_STATE));

        final byte[] drawCallNbt = nbt.getByteArray(TAG_DRAW_CALL);
        System.arraycopy(drawCallNbt, 0, drawCall, 0, Math.min(drawCallNbt.length, drawCall.length));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setIntArray(TAG_IMAGE, image);
        nbt.setString(TAG_STATE, state.name());
        nbt.setByteArray(TAG_DRAW_CALL, drawCall);
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

            // Start reading again right away to read as fast as possible.
            receivingPipe.beginRead();
        }
    }

    /**
     * Process a value read from any port.
     *
     * @param value the value that was read.
     */
    private void process(final int value) {
        drawCall[state.ordinal()] = (byte) (value & 0xFF);
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
                image[index] = COLORS[color % COLORS.length];
            }
        }
    }

    /**
     * Getter for the ID of the texture on the GPU we're using, creates one if necessary.
     *
     * @return the texture ID we're currently using.
     */
    private int getGlTextureId() {
        if (glTextureId == 0) {
            glTextureId = GlStateManager.generateTexture();
            TextureUtil.allocateTexture(glTextureId, RESOLUTION, RESOLUTION);
            TextureUtil.uploadTexture(glTextureId, image, RESOLUTION, RESOLUTION);
        }
        return glTextureId;
    }

    /**
     * Deletes our texture from the GPU, if we have one.
     */
    private void deleteTexture() {
        if (glTextureId != 0) {
            TextureUtil.deleteTexture(glTextureId);
            glTextureId = 0;
        }
    }

    /**
     * Indicate to our client representation to clear the image data.
     */
    private void sendClear() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean(TAG_CLEAR, true);
        getCasing().sendData(getFace(), nbt);
    }

    /**
     * Send a draw call to our client representation.
     */
    private void sendDrawCall() {
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByteArray(TAG_DRAW_CALL, drawCall);
        getCasing().sendData(getFace(), nbt);
    }
}
