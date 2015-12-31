package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

/**
 * The RAm module can be used to store up to 256 values by address. It runs
 * as a basic state machine with the following states:
 * <ul>
 * <li>ADDRESS: await address input, no ports writing, all ports reading.</li>
 * <li>READ_WRITE: await either read to retrieve value or write to set value, all ports writing, all ports reading.</li>
 * </ul>
 */
public final class ModuleRandomAccessMemory extends AbstractModuleRotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    private byte[] memory = new byte[MEMORY_SIZE];
    private byte address;
    private State state = State.ADDRESS;

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * The size of the memory, in bytes.
     */
    private static final int MEMORY_SIZE = 256;

    private enum State {
        ADDRESS,
        READ_WRITE
    }

    // NBT data names.
    private static final String TAG_MEMORY = "memory";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_STATE = "state";

    // Data packet types.
    private static final byte DATA_TYPE_CLEAR = 0;

    // Rendering info.
    public static final float QUADS_U0 = 5 / 32f;
    public static final float QUADS_V0 = 5 / 32f;
    public static final float QUADS_SIZE_U = 4 / 32f;
    public static final float QUADS_SIZE_V = 4 / 32f;
    public static final float QUADS_STEP_U = 6 / 32f;
    public static final float QUADS_STEP_V = 6 / 32f;

    // --------------------------------------------------------------------- //

    public ModuleRandomAccessMemory(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        assert (!getCasing().getCasingWorld().isRemote);

        stepInput();
    }

    @Override
    public void onDisabled() {
        assert (!getCasing().getCasingWorld().isRemote);

        // Wipe memory on shutdown.
        Arrays.fill(memory, (byte) 0);
        address = 0;
        state = State.ADDRESS;

        sendClear();
    }

    @Override
    public void onWriteComplete(final Port port) {
        assert (!getCasing().getCasingWorld().isRemote);

        // Memory access was completed with a read operation.
        state = State.ADDRESS;

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onData(final ByteBuf data) {
        if (data.readBoolean()) {
            Arrays.fill(memory, (byte) 0);
        } else {
            address = data.readByte();
            set(data.readByte());
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled || !isVisible()) {
            return;
        }

        rotateForRendering();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);
        GlStateManager.disableTexture2D();

        final int step = 4;
        for (int y = 0; y < step; y++) {
            for (int x = 0; x < step; x++) {
                final int offset = (y * step + x) * step;
                final float brightness = 0.25f + sectorSum(offset) * 0.75f;
                GlStateManager.color(1, 1, 1, brightness);

                final float u0 = QUADS_U0 + x * QUADS_STEP_U;
                final float v0 = QUADS_V0 + y * QUADS_STEP_V;
                RenderUtil.drawUntexturedQuad(u0, v0, QUADS_SIZE_U, QUADS_SIZE_V);
            }
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        memory = nbt.getByteArray(TAG_MEMORY);
        address = nbt.getByte(TAG_ADDRESS);
        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setByteArray(TAG_MEMORY, memory);
        nbt.setByte(TAG_ADDRESS, address);
        EnumUtils.writeToNBT(state, TAG_STATE, nbt);
    }

    // --------------------------------------------------------------------- //

    private int get() {
        return memory[address & 0xFF] & 0xFF;
    }

    private void set(final int value) {
        memory[address & 0xFF] = (byte) value;
    }

    /**
     * Update the inputs of the RAM, start reading if we're not already.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                // Handle the input.
                process(receivingPipe.read());

                // Start reading again right away to read as fast as possible.
                receivingPipe.beginRead();
            }
        }
    }

    private void process(final short value) {
        switch (state) {
            case ADDRESS:
                beginReadWrite((byte) value);
                break;
            case READ_WRITE:
                finishReading((byte) value);
                break;
        }
    }

    /**
     * Setting address for next read or write operation.
     *
     * @param address the address to operate on.
     */
    private void beginReadWrite(final byte address) {
        // Set the address the next operation will operate on.
        this.address = address;

        // Change to the read/write state.
        state = State.READ_WRITE;

        // Begin writing the value at that address to all ports.
        final short value = (short) get();
        for (final Port port : Port.VALUES) {
            getCasing().getSendingPipe(getFace(), port).beginWrite(value);
        }
    }

    /**
     * Memory access was completed with a write operation.
     *
     * @param value the value that is to be written to memory.
     */
    private void finishReading(final byte value) {
        // Store the value written to the RAM.
        set(value);

        // Restart the cycle waiting for the next address to operate on.
        state = State.ADDRESS;

        // If reading completes, cancel all writes to avoid read/write in one.
        cancelWrite();

        // Update client representation.
        sendData();
    }

    private void sendClear() {
        final ByteBuf data = Unpooled.buffer();
        data.writeBoolean(true);
        getCasing().sendData(getFace(), data, DATA_TYPE_CLEAR);
    }

    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        data.writeBoolean(false);
        data.writeByte(address);
        data.writeByte(memory[address & 0xFF]);
        getCasing().sendData(getFace(), data);
    }

    private float sectorSum(final int offset) {
        final int sum = (memory[offset] & 0xFF) + (memory[offset + 1] & 0xFF) + (memory[offset + 2] & 0xFF) + (memory[offset + 3] & 0xFF);
        return sum / (4f * 0xFF);
    }
}
