package li.cil.tis3d.common.module;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.util.Color;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

/**
 * The RAM module can be used to store up to 256 values by address. It runs
 * as a basic state machine with the following states:
 * <ul>
 * <li>ADDRESS: await address input, no ports writing, all ports reading.</li>
 * <li>ACCESS: await either read to retrieve value or write to set value, all ports writing, all ports reading.</li>
 * </ul>
 */
public class RandomAccessMemoryModule extends AbstractModuleWithRotation {
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

    public RandomAccessMemoryModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepInput();

        if (state == State.ACCESS) {
            stepOutput();
        }
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
    public boolean use(final PlayerEntity player, final Hand hand, final Vector3d hit) {
        final ItemStack heldItem = player.getItemInHand(hand);
        if (!Items.is(heldItem, Items.READ_ONLY_MEMORY_MODULE)) {
            return false;
        }

        final boolean isReading = player.isShiftKeyDown();
        if (!isReading && getCasing().isLocked()) {
            return false;
        }

        if (!getCasing().getCasingLevel().isClientSide()) {
            if (isReading) {
                ReadOnlyMemoryModuleItem.saveToStack(heldItem, memory);
            } else {
                load(ReadOnlyMemoryModuleItem.loadFromStack(heldItem));
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled() || !isVisible()) {
            return;
        }

        final MatrixStack matrixStack = context.getMatrixStack();
        matrixStack.pushPose();
        rotateForRendering(matrixStack);

        final int cells = 4;
        final int cellSize = MEMORY_SIZE / (cells * cells);
        final int cellWidth = (int) Math.sqrt(cellSize);
        final int cellColor = getCellColor();
        for (int y = 0; y < cells; y++) {
            for (int x = 0; x < cells; x++) {
                final float brightness = 0.25f + sectorSum(x * cellWidth, y * cellWidth, cellWidth) * 0.75f;
                final int color = Color.withAlpha(cellColor, brightness);

                final float u0 = QUADS_U0 + x * QUADS_STEP_U;
                final float v0 = QUADS_V0 + y * QUADS_STEP_V;
                context.drawQuadUnlit(u0, v0, QUADS_SIZE_U, QUADS_SIZE_V, color);
            }
        }

        matrixStack.popPose();
    }

    @Override
    public void load(final CompoundNBT tag) {
        super.load(tag);

        load(tag.getByteArray(TAG_MEMORY));
        address = tag.getByte(TAG_ADDRESS);
        state = EnumUtils.readFromNBT(State.class, TAG_STATE, tag);
    }

    @Override
    public void save(final CompoundNBT tag) {
        super.save(tag);

        tag.putByteArray(TAG_MEMORY, memory.clone());
        tag.putByte(TAG_ADDRESS, address);
        EnumUtils.writeToNBT(state, TAG_STATE, tag);
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
     * Get the color of the memory cells for this module.
     */
    @OnlyIn(Dist.CLIENT)
    protected int getCellColor() {
        return 0xFFBBDDFF;
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
     * Update the inputs of the module, start reading if we're not already.
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

    /**
     * Update the output of the module, pushing the currently addressed value.
     * <p/>
     * This will be called once when we switch into address mode, but also every tick after that.
     * Trying to start writing in the following tick is necessary because we may have canceled the
     * write on some ports due to a value being read (consumed), and the pipe thus being in a
     * completed state which will only flip into ready again after a step. So we'd either have to
     * introduce extra steps (don't want) or can do what we do here: just don't start writing right
     * away and wait for the next update to start writing then.
     */
    private void stepOutput() {
        final short value = (short) get();
        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(value);
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
        stepOutput();
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

    private float sectorSum(final int x0, final int y0, final int sectorWidth) {
        int sum = 0;
        final int sectorSize = sectorWidth * sectorWidth;
        final int rowWidth = MEMORY_SIZE / sectorSize;
        for (int y = y0; y < y0 + sectorWidth; y++) {
            for (int x = x0; x < x0 + sectorWidth; x++) {
                final int i = y * rowWidth + x;
                sum += memory[i] & 0xFF;
            }
        }
        return sum / (sectorWidth * sectorWidth * (float) 0xFF);
    }

    protected final void load(final byte[] data) {
        System.arraycopy(data, 0, memory, 0, Math.min(data.length, memory.length));
        if (data.length < memory.length) {
            Arrays.fill(memory, data.length, memory.length, (byte) 0);
        }
    }
}
