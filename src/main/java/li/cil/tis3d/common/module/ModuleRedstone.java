package li.cil.tis3d.common.module;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModuleRedstone extends AbstractModuleRotatable implements Redstone {
    // --------------------------------------------------------------------- //
    // Persisted data

    private int output = 0;
    private int input = 0;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_OUTPUT = "output";
    private static final String TAG_INPUT = "input";

    // Rendering info.
    private static final ResourceLocation LOCATION_OVERLAY = new ResourceLocation(API.MOD_ID, "textures/blocks/overlay/moduleRedstone.png");
    private static final float LEFT_U0 = 9 / 32f;
    private static final float LEFT_U1 = 12 / 32f;
    private static final float RIGHT_U0 = 20 / 32f;
    private static final float RIGHT_U1 = 23 / 32f;
    private static final float SHARED_V0 = 42 / 64f;
    private static final float SHARED_V1 = 57 / 64f;
    private static final float SHARED_W = 3 / 32f;
    private static final float SHARED_H = SHARED_V1 - SHARED_V0;

    /**
     * The last tick we updated. Used to avoid changing output and recomputing
     * input multiple times a tick, which is pointless and bad for performance.
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
        if (getCasing().getCasingWorld().getTotalWorldTime() > lastStep) {
            setRedstoneInput(computeRedstoneInput());
        }

        for (final Port port : Port.VALUES) {
            stepOutput(port);
            stepInput(port);
        }

        lastStep = getCasing().getCasingWorld().getTotalWorldTime();

        if (scheduledNeighborUpdate) {
            notifyNeighbors();
        }
    }

    @Override
    public void onDisabled() {
        input = 0;
        output = 0;

        getCasing().markDirty();
        notifyNeighbors();

        if (!getCasing().getCasingWorld().isRemote) {
            sendData();
        }
    }

    @Override
    public void onEnabled() {
        if (!getCasing().getCasingWorld().isRemote) {
            sendData();
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Start writing again right away to write as fast as possible.
        stepOutput(port);
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        output = nbt.getInteger(TAG_OUTPUT);
        input = nbt.getInteger(TAG_INPUT);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        rotateForRendering();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        RenderUtil.bindTexture(LOCATION_OVERLAY);

        // Draw base overlay.
        RenderUtil.drawQuad(0, 0, 1, 0.5f);

        // Draw output bar.
        final float relativeOutput = output / 15f;
        final float heightOutput = relativeOutput * SHARED_H;
        final float v0Output = SHARED_V1 - heightOutput;
        RenderUtil.drawQuad(LEFT_U0, (v0Output - 0.5f) * 2f, SHARED_W, heightOutput * 2, LEFT_U0, v0Output, LEFT_U1, SHARED_V1);

        // Draw input bar.
        final float relativeInput = input / 15f;
        final float heightInput = relativeInput * SHARED_H;
        final float v0Input = SHARED_V1 - heightInput;
        RenderUtil.drawQuad(RIGHT_U0, (v0Input - 0.5f) * 2f, SHARED_W, heightInput * 2, RIGHT_U0, v0Input, RIGHT_U1, SHARED_V1);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        output = Math.max(0, Math.min(15, nbt.getInteger(TAG_OUTPUT)));
        input = Math.max(0, Math.min(15, nbt.getInteger(TAG_INPUT)));
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger(TAG_OUTPUT, output);
        nbt.setInteger(TAG_INPUT, input);
    }

    @Override
    public int getRedstoneOutput() {
        return output;
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the known redstone input signal.
     *
     * @param value the new input value.
     */
    private void setRedstoneInput(final int value) {
        if (value == input) {
            return;
        }

        // Clamp to valid redstone range.
        input = Math.max(0, Math.min(15, value));

        if (!getCasing().getCasingWorld().isRemote) {
            // If the value changed, cancel our output to make sure it's up-to-date.
            cancelWrite();

            sendData();
        }
    }

    /**
     * Update the redstone signal we're outputting.
     *
     * @param value the new output value.
     */
    private void setRedstoneOutput(final int value) {
        if (value == output) {
            return;
        }

        // Clamp to valid redstone range.
        output = Math.max(0, Math.min(15, value));

        if (!getCasing().getCasingWorld().isRemote) {
            // If the value changed, notify neighboring blocks and make sure we're saved.
            getCasing().markDirty();

            // Avoid multiple world updates per tick.
            if (getCasing().getCasingWorld().getTotalWorldTime() > lastStep) {
                notifyNeighbors();
            } else {
                scheduledNeighborUpdate = true;
            }

            sendData();
        }
    }

    /**
     * Notify all neighbors of a block update, to let them realize our output changed.
     */
    private void notifyNeighbors() {
        scheduledNeighborUpdate = false;
        final Block blockType = getCasing().getCasingWorld().getBlockState(getCasing().getPosition()).getBlock();
        getCasing().getCasingWorld().notifyNeighborsOfStateChange(getCasing().getPosition(), blockType);
    }

    /**
     * Compute the redstone signal we're currently getting from the block in
     * front of the module (i.e. in front of the case on the side of the
     * module's face).
     *
     * @return the current input value.
     */
    private int computeRedstoneInput() {
        final EnumFacing facing = Face.toEnumFacing(getFace());
        final BlockPos inputPos = getCasing().getPosition().offset(facing);
        final int input = getCasing().getCasingWorld().getRedstonePower(inputPos, facing);
        if (input >= 15) {
            return input;
        } else {
            final IBlockState state = getCasing().getCasingWorld().getBlockState(inputPos);
            return Math.max(input, state.getBlock() == Blocks.redstone_wire ? state.getValue(BlockRedstoneWire.POWER) : 0);
        }
    }

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
     * Send the current state of the module (to the client).
     */
    private void sendData() {
        final NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        getCasing().sendData(getFace(), nbt);
    }
}
