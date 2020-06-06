package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

/**
 * The RAM module can be used to store up to 256 values by address. It runs
 * as a basic state machine with the following states:
 * <ul>
 * <li>ADDRESS: await address input, no ports writing, all ports reading.</li>
 * <li>ACCESS: await either read to retrieve value or write to set value, all ports writing, all ports reading.</li>
 * </ul>
 */
public class ModuleRandomAccessMemory extends AbstractModuleRotatable {
    // --------------------------------------------------------------------- //
    // Persisted data

    protected final byte[] memory = new byte[MEMORY_SIZE];
    protected byte address;
    protected State state = State.ADDRESS;

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * The size of the memory, in bytes.
     */
    public static final int MEMORY_SIZE = 256;

    protected enum State {
        ADDRESS,
        ACCESS
    }

    // NBT data names.
    private static final String TAG_MEMORY = "memory";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_STATE = "state";

    // Data packet types.
    private static final byte DATA_TYPE_CLEAR = 0;

    // Message types.
    private static final byte PACKET_CLEAR = 0;
    private static final byte PACKET_SINGLE = 1;
    private static final byte PACKET_FULL = 2;

    // Rendering info.
    private static final float QUADS_U0 = 5 / 32f;
    private static final float QUADS_V0 = 5 / 32f;
    private static final float QUADS_SIZE_U = 4 / 32f;
    private static final float QUADS_SIZE_V = 4 / 32f;
    private static final float QUADS_STEP_U = 6 / 32f;
    private static final float QUADS_STEP_V = 6 / 32f;

    // --------------------------------------------------------------------- //

    public ModuleRandomAccessMemory(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepInput();
    }

    @Override
    public void onDisabled() {
        // Wipe memory on shutdown.
        clearOnDisabled();

        // Reset protocol state.
        address = 0;
        state = State.ADDRESS;
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure we're not writing while waiting for operation type input.
        cancelWrite();

        // Memory access was completed with a read operation.
        state = State.ADDRESS;

        // Start reading again right away to read as fast as possible.
        stepInput();
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final float hitX, final float hitY, final float hitZ) {
        final ItemStack heldItem = player.getHeldItem(hand);
        if (!Items.isModuleReadOnlyMemory(heldItem)) {
            return false;
        }

        final boolean isReading = player.isSneaking();
        if (!isReading && getCasing().isLocked()) {
            return false;
        }

        if (!getCasing().getCasingWorld().isRemote) {
            if (isReading) {
                ItemModuleReadOnlyMemory.saveToStack(heldItem, memory);
            } else {
                load(ItemModuleReadOnlyMemory.loadFromStack(heldItem));
                sendFull();
            }
        }

        return true;
    }

    @Override
    public void onData(final ByteBuf data) {
        switch (data.readByte()) {
            case PACKET_CLEAR:
                clear();
                break;
            case PACKET_SINGLE:
                address = data.readByte();
                set(data.readByte());
                break;
            case PACKET_FULL:
                data.readBytes(memory);
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled || !isVisible()) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        final int cells = 4;
        final int cellSize = MEMORY_SIZE / (cells * cells);
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                final int offset = (y * cells + x) * cellSize;
                final float brightness = 0.25f + sectorSum(offset, cellSize) * 0.75f;
                setCellColor(brightness);

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

        load(nbt.getByteArray(TAG_MEMORY));
        address = nbt.getByte(TAG_ADDRESS);
        state = EnumUtils.readFromNBT(State.class, TAG_STATE, nbt);
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setByteArray(TAG_MEMORY, memory.clone());
        nbt.setByte(TAG_ADDRESS, address);
        EnumUtils.writeToNBT(state, TAG_STATE, nbt);
    }

    // --------------------------------------------------------------------- //

    protected void clearOnDisabled() {
        clear();
        sendClear();
    }

    /**
     * Called whenever the module tries to start reading.
     *
     * @param pipe the pipe to start reading on.
     */
    protected void beginRead(final Pipe pipe) {
        pipe.beginRead();
    }

    /**
     * Set the color of the memory cell currently being drawn in the {@link GlStateManager}.
     *
     * @param brightness the brightness the cell is rendered at (the alpha).
     */
    @SideOnly(Side.CLIENT)
    protected void setCellColor(final float brightness) {
        GlStateManager.color(0.4f, 1f, 1f, brightness);
    }

    // --------------------------------------------------------------------- //

    private int get() {
        return memory[address & 0xFF] & 0xFF;
    }

    private void set(final int value) {
        memory[address & 0xFF] = (byte) value;
    }

    private void clear() {
        Arrays.fill(memory, (byte) 0);
    }

    /**
     * Update the inputs of the RAM, start reading if we're not already.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                beginRead(receivingPipe);
            }
            if (receivingPipe.canTransfer()) {
                // Handle the input.
                process(receivingPipe.read());
            }
        }
    }

    private void process(final short value) {
        switch (state) {
            case ADDRESS:
                beginReadWrite((byte) value);
                break;
            case ACCESS:
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
        state = State.ACCESS;

        // Begin writing the value at that address to all ports.
        final short value = (short) get();
        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);

            // Cancel previous writes since we're now potentially on a new address with a new value.
            if (sendingPipe.isWriting()) {
                sendingPipe.cancelWrite();
            }

            sendingPipe.beginWrite(value);
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
        sendSingle();
    }

    private void sendClear() {
        final ByteBuf data = Unpooled.buffer();
        data.writeByte(PACKET_CLEAR);
        getCasing().sendData(getFace(), data, DATA_TYPE_CLEAR);
    }

    private void sendSingle() {
        final ByteBuf data = Unpooled.buffer();
        data.writeByte(PACKET_SINGLE);
        data.writeByte(address);
        data.writeByte(memory[address & 0xFF]);
        getCasing().sendData(getFace(), data);
    }

    private void sendFull() {
        final ByteBuf data = Unpooled.buffer();
        data.writeByte(PACKET_FULL);
        data.writeBytes(memory);
        getCasing().sendData(getFace(), data);
    }

    private float sectorSum(final int offset, final int count) {
        int sum = 0;
        for (int i = offset, end = offset + count; i < end; i++) {
            sum += memory[i] & 0xFF;
        }
        return sum / (count * (float) 0xFF);
    }

    protected final void load(final byte[] data) {
        System.arraycopy(data, 0, memory, 0, Math.min(data.length, memory.length));
        if (data.length < memory.length) {
            Arrays.fill(memory, data.length, memory.length, (byte) 0);
        }
    }
}
