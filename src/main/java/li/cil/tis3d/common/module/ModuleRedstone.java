package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.traits.Redstone;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public final class ModuleRedstone extends AbstractModuleRotatable implements Redstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    private short output = 0;
    private short input = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";

    // Data packet types.
    private static final byte DATA_TYPE_UPDATE = 0;

    // Rendering info.
    private static final float LEFT_U0 = 9 / 32f;
    private static final float LEFT_U1 = 12 / 32f;
    private static final float RIGHT_U0 = 20 / 32f;
    private static final float RIGHT_U1 = 23 / 32f;
    private static final float SHARED_V0 = 10 / 32f;
    private static final float SHARED_V1 = 25 / 32f;
    private static final float SHARED_W = 3 / 32f;
    private static final float SHARED_H = SHARED_V1 - SHARED_V0;

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

    public ModuleRedstone(final Casing casing, final Face face) {
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
        input = 0;
        output = 0;

        notifyNeighbors();

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
        input = data.readShort();
        output = data.readShort();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        rotateForRendering();
        RenderUtil.ignoreLighting();

        // Draw base overlay.
        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_REDSTONE_OVERLAY));

        if (!enabled) {
            return;
        }

        final TextureAtlasSprite barsSprite = RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_REDSTONE_BARS_OVERLAY);

        // Draw output bar.
        final float relativeOutput = output / 15f;
        final float heightOutput = relativeOutput * SHARED_H;
        final float v0Output = SHARED_V1 - heightOutput;
        RenderUtil.drawQuad(barsSprite, LEFT_U0, v0Output, SHARED_W, heightOutput, LEFT_U0, v0Output, LEFT_U1, SHARED_V1);

        // Draw input bar.
        final float relativeInput = input / 15f;
        final float heightInput = relativeInput * SHARED_H;
        final float v0Input = SHARED_V1 - heightInput;
        RenderUtil.drawQuad(barsSprite, RIGHT_U0, v0Input, SHARED_W, heightInput, RIGHT_U0, v0Input, RIGHT_U1, SHARED_V1);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        output = (short) Math.max(0, Math.min(15, nbt.getShort(TAG_OUTPUT)));
        input = (short) Math.max(0, Math.min(15, nbt.getShort(TAG_INPUT)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger(TAG_OUTPUT, output);
        nbt.setInteger(TAG_INPUT, input);
    }

    // --------------------------------------------------------------------- //
    // Redstone

    @Override
    public short getRedstoneOutput() {
        return output;
    }

    @Override
    public void setRedstoneInput(final short value) {
        // We never call this on the client side, but other might...
        final World world = getCasing().getCasingWorld();
        if (world.isRemote) {
            return;
        }

        // Clamp to valid redstone range.
        final short validatedValue = (short) Math.max(0, Math.min(15, value));
        if (validatedValue == input) {
            return;
        }

        input = validatedValue;

        // If the value changed, make sure we're saved.
        getCasing().markDirty();

        // The value changed, cancel our output to make sure it's up-to-date.
        cancelWrite();

        // Update client representation.
        sendData();
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the output of the module, pushing a value read from any pipe.
     */
    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            sendingPipe.beginWrite(input);
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
            setRedstoneOutput(receivingPipe.read());

            // Start reading again right away to read as fast as possible.
            receivingPipe.beginRead();
        }
    }

    /**
     * Update the redstone signal we're outputting.
     *
     * @param value the new output value.
     */
    private void setRedstoneOutput(final short value) {
        // Clamp to valid redstone range.
        final short validatedValue = (short) Math.max(0, Math.min(15, value));
        if (validatedValue == output) {
            return;
        }

        output = validatedValue;

        // If the value changed, make sure we're saved.
        getCasing().markDirty();

        // Notify neighbors, avoid multiple world updates per tick.
        scheduledNeighborUpdate = true;

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
        data.writeShort(input);
        data.writeShort(output);
        getCasing().sendData(getFace(), data, DATA_TYPE_UPDATE);
    }
}
