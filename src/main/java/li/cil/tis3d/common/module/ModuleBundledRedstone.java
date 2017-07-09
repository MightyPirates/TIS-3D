package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.BundledRedstone;
import li.cil.tis3d.api.module.traits.BundledRedstoneOutputChangedEvent;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.util.ColorUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public final class ModuleBundledRedstone extends AbstractModuleRotatable implements BundledRedstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final short[] output = new short[16];
    private final short[] input = new short[16];
    private short channel = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";
    private static final String TAG_CHANNEL = "channel";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    // Rendering info.
    private static final float LEFT_U0 = 8 / 32f;
    private static final float RIGHT_U0 = 20 / 32f;
    private static final float SHARED_V0 = 10 / 32f;
    private static final float V_STEP = 1 / 16f;

    /**
     * The last tick we updated. Used to avoid changing output multiple times a
     * tick, which is usually pointless and really bad for performance.
     */
    private long lastStep = 0L;

    /**
     * Something changed last tick after the first neighbor block update, so
     * we need to update again in the next tick (if we don't anyway).
     */
    private boolean scheduledNeighborUpdate = false;

    // --------------------------------------------------------------------- //

    public ModuleBundledRedstone(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final World world = getCasing().getCasingWorld();

        for (final Port port : Port.VALUES) {
            stepOutput(port);
            stepInput(port);
        }

        if (scheduledNeighborUpdate && world.getTotalWorldTime() > lastStep) {
            notifyNeighbors();
        }

        lastStep = world.getTotalWorldTime();
    }

    @Override
    public void onDisabled() {
        Arrays.fill(input, (short) 0);
        Arrays.fill(output, (short) 0);
        channel = 0;

        final BundledRedstoneOutputChangedEvent event = new BundledRedstoneOutputChangedEvent(this, -1);
        MinecraftForge.EVENT_BUS.post(event);

        sendData();
    }

    @Override
    public void onEnabled() {
        sendData();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void onData(final ByteBuf data) {
        for (int i = 0; i < input.length; i++) {
            input[i] = data.readShort();
        }
        for (int i = 0; i < output.length; i++) {
            output[i] = data.readShort();
        }
        channel = data.readShort();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!isVisible()) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();

        // Draw base overlay.
        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_BUNDLED_REDSTONE_OVERLAY));

        if (!enabled) {
            return;
        }

        // Draw output bar.
        renderBar(output, LEFT_U0);

        // Draw input bar.
        renderBar(input, RIGHT_U0);

        // Draw active channel indicator.
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        final int color = ColorUtils.getColorByIndex(channel);
        GL11.glColor4f(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color), 1f);
        RenderUtil.drawUntexturedQuad(7 / 16f, 7 / 16f, 2 / 16f, 2 / 16f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final int[] outputNbt = nbt.getIntArray(TAG_OUTPUT);
        for (int i = 0; i < outputNbt.length; i++) {
            output[i] = (short) outputNbt[i];
        }

        final int[] inputNbt = nbt.getIntArray(TAG_INPUT);
        for (int i = 0; i < inputNbt.length; i++) {
            input[i] = (short) inputNbt[i];
        }

        channel = (short) Math.max(0, Math.min(input.length - 1, nbt.getShort(TAG_CHANNEL)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final int[] outputNbt = new int[output.length];
        for (int i = 0; i < output.length; i++) {
            outputNbt[i] = output[i];
        }
        nbt.setIntArray(TAG_OUTPUT, outputNbt);

        final int[] inputNbt = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            inputNbt[i] = input[i];
        }
        nbt.setIntArray(TAG_INPUT, inputNbt);

        nbt.setShort(TAG_CHANNEL, channel);
    }

    // --------------------------------------------------------------------- //
    // BundledRedstone

    @Override
    public int getBundledRedstoneOutput(final int channel) {
        return output[channel];
    }

    @Override
    public void setBundledRedstoneInput(final int channel, final short value) {
        // We never call this on the client side, but other might...
        final World world = getCasing().getCasingWorld();
        if (world.isRemote) {
            return;
        }

        if (value == input[channel]) {
            return;
        }

        input[channel] = value;

        // If the value changed, make sure we're saved.
        getCasing().markDirty();

        // The value changed, cancel our output to make sure it's up-to-date.
        cancelWrite();

        // Update client representation.
        sendData();
    }

    @Override
    public short getBundledRedstoneInput(final int channel) {
        return input[channel];
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the output of the module, pushing a value read from any pipe.
     */
    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            sendingPipe.beginWrite(input[channel]);
        }
    }

    /**
     * Update the input of the module, pushing the current input to any pipe.
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
     * Handle a read value, perform appropriate action.
     *
     * @param value the value that was read.
     */
    private void process(final int value) {
        final short hi = (short) ((value & 0xFF00) >>> 8);
        final short lo = (short) (value & 0xFF);
        if (hi == 0xFF) {
            // lo = new output channel
            if (lo < 0 || lo >= input.length) {
                return;
            }

            if (lo != channel) {
                channel = lo;

                // We changed channel, update what we pass on.
                cancelWrite();

                // Tell our client representation the channel changed.
                sendData();
            }
        } else {
            // hi = channel, lo = value
            setRedstoneOutput(hi, lo);
        }
    }

    /**
     * Update the redstone signal we're outputting.
     *
     * @param channel the channel to change the output of.
     * @param value   the new output value.
     */
    private void setRedstoneOutput(final int channel, final short value) {
        // Ignore invalid channels.
        if (channel < 0 || channel >= output.length) {
            return;
        }

        if (value == output[channel]) {
            return;
        }

        output[channel] = value;

        // If the value changed, make sure we're saved.
        getCasing().markDirty();

        // Notify neighbors, avoid multiple world updates per tick.
        scheduledNeighborUpdate = true;

        // Notify bundled redstone APIs.
        final BundledRedstoneOutputChangedEvent event = new BundledRedstoneOutputChangedEvent(this, channel);
        MinecraftForge.EVENT_BUS.post(event);

        sendData();
    }

    /**
     * Notify all neighbors of a block update, to let them realize our output changed.
     */
    private void notifyNeighbors() {
        final World world = getCasing().getCasingWorld();

        scheduledNeighborUpdate = false;
        final Block blockType = world.getBlock(getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ());
        world.notifyBlocksOfNeighborChange(getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ(), blockType);
    }

    /**
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final ByteBuf data = Unpooled.buffer();
        for (final short value : input) {
            data.writeShort(value);
        }
        for (final short value : output) {
            data.writeShort(value);
        }
        data.writeShort(channel);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }

    @SideOnly(Side.CLIENT)
    private void renderBar(final short[] values, final float u) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        for (int channel = 0; channel < values.length; channel++) {
            if (values[channel] > 0) {
                final int color = ColorUtils.getColorByIndex(channel);
                GL11.glColor3f(ColorUtils.getRed(color), ColorUtils.getGreen(color), ColorUtils.getBlue(color));

                final float u0 = u + (channel & 1) * V_STEP;
                final float v0 = SHARED_V0 + (channel >> 1) * V_STEP;
                RenderUtil.drawUntexturedQuad(u0, v0, V_STEP, V_STEP);
            }
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
