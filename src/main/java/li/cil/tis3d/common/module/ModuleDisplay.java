package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.util.ColorUtils;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

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
        final World world = getCasing().getCasingWorld();
        if (world.isRemote) {
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
        TextureUtil.uploadTexture(getGlTextureId(), image, RESOLUTION, RESOLUTION);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, getGlTextureId());

        RenderUtil.drawQuad();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] imageNbt = nbt.getIntArray(TAG_IMAGE);
        System.arraycopy(imageNbt, 0, image, 0, Math.min(imageNbt.length, image.length));

        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);

        final byte[] drawCallNbt = nbt.getByteArray(TAG_DRAW_CALL);
        System.arraycopy(drawCallNbt, 0, drawCall, 0, Math.min(drawCallNbt.length, drawCall.length));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setIntArray(TAG_IMAGE, image);
        EnumUtils.writeToNBT(state, TAG_STATE, nbt);
        nbt.setByteArray(TAG_DRAW_CALL, drawCall.clone());
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

        final int x0 = MARGIN + Math.max(0, xin);
        final int x1 = MARGIN + Math.min(RESOLUTION - 2 * MARGIN, xin + w);
        final int y0 = MARGIN + Math.max(0, yin);
        final int y1 = MARGIN + Math.min(RESOLUTION - 2 * MARGIN, yin + h);

        for (int y = y0; y < y1; y++) {
            final int offset = y * RESOLUTION;
            for (int x = x0; x < x1; x++) {
                final int index = offset + x;
                image[index] = ColorUtils.getColorByIndex(Math.max(0, color));
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
            glTextureId = TextureUtil.glGenTextures();
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
